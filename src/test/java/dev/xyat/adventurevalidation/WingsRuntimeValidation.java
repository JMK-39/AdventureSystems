package dev.xyat.adventurevalidation;

import dev.xyat.adventuresystems.curios.config.CuriosConfig;
import dev.xyat.adventuresystems.curios.init.Items;
import dev.xyat.adventuresystems.curios.windthunderwings.WingsMode;
import dev.xyat.adventuresystems.curios.windthunderwings.WingsNetwork;
import dev.xyat.adventuresystems.curios.windthunderwings.client.WingsClient;
import dev.xyat.kineticcore.api.client.event.KineticClientEvents;
import dev.xyat.kineticcore.api.flight.KineticFlightClient;
import dev.xyat.kineticcore.api.flight.KineticSuperFlight;
import dev.xyat.kineticcore.api.runtime.KineticClientRuntime;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * Wears the Wind-Thunder Wings in a real game and checks them on the server and the client: they grant super flight,
 * the wings fold on the back on the ground and spread in flight, holding forward flies in the flight pose at twice,
 * five and ten times creative speed in normal flight, light escape and blood burn (measured from the player's actual
 * movement), blood burn is refused at low health, costs its health share, lasts its time and cools down, landing ends
 * the light, and taking the wings off takes super flight away. The player really holds forward and steers by turning,
 * so super flight does the flying; each mode is captured from behind, in front and in first person, by day and by
 * night. Then KineticCore's super flight is switched on in this client and the player is flown faster and faster
 * round a wide circle to capture its effects: the takeoff blast, the vapour cone near the sound barrier, the sonic boom
 * when crossing it and the plasma at twice its speed. The player, the back slot, the world time and the settings are
 * put back afterwards.
 */
public final class WingsRuntimeValidation {
    private static final Logger LOG = LoggerFactory.getLogger(WingsRuntimeValidation.class);
    private static final String ROOT = System.getProperty("adventuresystemswings.guiValidation.output", "wings-validation");
    /** Super flight's creative-flight speed in blocks per tick; the wings' multipliers scale it. */
    private static final double CREATIVE_SPEED = 0.6D;
    private static final double SUPER_RADIUS = 100.0D;
    private static final int BURN_SECONDS = 4;
    // Long enough that screenshots slowing the client never let it run out before the refusal is checked.
    private static final int BURN_COOLDOWN = 6;
    private static final List<Step> STEPS = new ArrayList<>();
    private static boolean installed, finished;
    private static int index, wait = 100, checks, captures, failures;
    private static Vec3 center;
    // While steering the player holds forward and turns this many degrees a tick; recent positions measure the speed.
    private static boolean steering;
    private static float turn;
    private static final ArrayDeque<Vec3> TRACK = new ArrayDeque<>();
    // Super flight: blocks per tick round its own wide circle, placed by position (it stops any velocity of its own).
    private static double superSpeed, superAngle;
    private static Vec3 superCenter;
    private static float healthMark;
    private static boolean restored;
    // Times the wearer's model was drawn while counting: it must be drawn in normal flight and hidden in the light.
    // Player model replacements (as in the modpack) draw the player themselves and never report a render, so the
    // hiding is checked only where the normal model was seen.
    private static boolean countRenders, modelReported;
    private static int renders;
    // What the run changes, kept to put back.
    private static GameType originalMode;
    private static float originalHealth;
    private static boolean originalMayfly, originalFlying, originalSuperAvailable, originalSuperActive;
    private static ItemStack originalBack = ItemStack.EMPTY;
    private static Vec3 originalPosition;
    private static long originalTime;
    private static int originalBurnSeconds, originalBurnCooldown;
    private static CameraType originalCamera;

    private WingsRuntimeValidation() {
    }

    private record Step(String name, int waitAfter, Action action) {
    }

    private interface Action {
        void run() throws Exception;
    }

    public static void install() {
        if (installed) return;
        installed = true;
        steps();
        KineticClientEvents.onTick(KineticClientEvents.TickPhase.END, WingsRuntimeValidation::tick);
        KineticClientEvents.onPlayerRenderAfter(context -> {
            if (countRenders && context.player() == Minecraft.getInstance().player) renders++;
        });
    }

    private static void steps() {
        step("setup", 12, () -> {
            var mc = Minecraft.getInstance();
            mc.setScreen(null);
            originalCamera = mc.options.getCameraType();
            originalBurnSeconds = CuriosConfig.wtwBurnSeconds;
            originalBurnCooldown = CuriosConfig.wtwBurnCooldown;
            CuriosConfig.wtwBurnSeconds = BURN_SECONDS;
            CuriosConfig.wtwBurnCooldown = BURN_COOLDOWN;
            camera(CameraType.THIRD_PERSON_BACK);
            server(player -> {
                originalMode = player.gameMode.getGameModeForPlayer();
                originalHealth = player.getHealth();
                originalPosition = player.position();
                originalTime = dayTime(player);
                originalFlying = player.getAbilities().flying;
                player.setGameMode(GameType.SURVIVAL);
                originalMayfly = player.getAbilities().mayfly;
                originalSuperActive = KineticSuperFlight.active(player);
                KineticSuperFlight.setActive(player, false);
                // The player may already wear the wings: start from an empty back slot, put the original back at the end.
                ICurioStacksHandler back = back(player);
                require(back.getSlots() > 0, "the player has a back slot");
                originalBack = back.getStacks().getStackInSlot(0).copy();
                back.getStacks().setStackInSlot(0, ItemStack.EMPTY);
                player.setHealth(player.getMaxHealth());
                setDayTime(player, 6000L);
                center = new Vec3(originalPosition.x, originalPosition.y + 6.0D, originalPosition.z);
            });
        });
        step("without the wings", 10, () -> server(player -> {
            // Super flight may come from elsewhere too (a command grant, gear); the wings must leave that as it was.
            originalSuperAvailable = KineticSuperFlight.available(player);
            back(player).getStacks().setStackInSlot(0, new ItemStack(Items.WIND_THUNDER_WINGS.get()));
        }));
        step("folded on the ground", 3, () -> {
            server(player -> {
                require(KineticSuperFlight.available(player), "the wings grant super flight");
                require(!KineticSuperFlight.active(player), "wearing them alone does not take off");
            });
            Object wearer = wearer();
            require(wearer != null && boolField(wearer, "worn") && !boolField(wearer, "flying"), "the client sees the wings worn on the ground");
            require(KineticFlightClient.superFlightOverride() == 2.0D, "the wings drive super flight at normal speed (" + KineticFlightClient.superFlightOverride() + ")");
            capture("folded-back");
            server(player -> {
                player.teleportTo(center.x, center.y, center.z);
                require(KineticSuperFlight.setActive(player, true), "super flight switches on with the wings");
            });
        });
        step("unfolding", 21, () -> capture("unfold-back"));
        step("hover", 24, () -> {
            require(KineticFlightClient.superFlightActive(), "the client flies in super flight");
            mode(WingsMode.NORMAL, "the client sees the wings flying in normal flight");
        });
        step("hover behind", 7, () -> {
            require(!Minecraft.getInstance().player.isFallFlying(), "hovering keeps the upright pose");
            require(Minecraft.getInstance().player.walkAnimation.speed() < 0.05F,
                    "the legs hang still while hovering (walk speed " + Minecraft.getInstance().player.walkAnimation.speed() + ")");
            capture("hover-back");
        });
        // A beat takes 28 ticks hovering: four views a quarter beat apart show the whole stroke.
        step("hover beat 2", 7, () -> capture("hover-back-2"));
        step("hover beat 3", 7, () -> capture("hover-back-3"));
        step("hover beat 4", 7, () -> {
            capture("hover-back-4");
            camera(CameraType.THIRD_PERSON_FRONT);
        });
        step("hover front", 26, () -> {
            capture("hover-front");
            camera(CameraType.THIRD_PERSON_BACK);
            steer(3.0F);
            renders = 0;
            countRenders = true;
        });
        step("normal flight", 4, () -> {
            require(Minecraft.getInstance().player.isFallFlying() && KineticFlightClient.superFlightManeuvering(),
                    "moving forward takes the flight pose");
            measured(2.0D, "normal flight is twice creative speed");
            modelReported = renders > 0;
            if (!modelReported) LOG.info("WINGS_RUNTIME_NOTE the player model reports no renders here; the hiding check is skipped");
            countRenders = false;
            require(trail() > 3, "normal flight leaves a trail");
        });
        step("normal behind", 11, () -> {
            capture("normal-back");
        });
        step("normal behind half beat", 8, () -> {
            capture("normal-back-2");
            camera(CameraType.THIRD_PERSON_FRONT);
        });
        step("normal front", 8, () -> {
            capture("normal-front");
            camera(CameraType.THIRD_PERSON_BACK);
            server(player -> {
                player.setHealth(player.getMaxHealth() * 0.2F);
                healthMark = player.getHealth();
            });
            WingsNetwork.requestBurn();
        });
        step("low health refused", 4, () -> {
            server(player -> {
                require(player.getHealth() >= healthMark && player.getHealth() <= healthMark + 1.5F, "no health is taken below the minimum");
                player.setHealth(player.getMaxHealth());
            });
            mode(WingsMode.NORMAL, "blood burn is refused below the minimum health");
            WingsNetwork.requestBoost();
        });
        step("escape launch", 18, () -> {
            require((Boolean) field(wearer(), "lightOn"), "moving in light escape turns the wearer into light at once");
            capture("boost-launch");
        });
        step("light escape", 22, () -> {
            mode(WingsMode.BOOST, "the client sees the light escape");
            require(KineticFlightClient.superFlightOverride() == 6.0D, "light escape drives super flight at six times");
            steer(5.0F);
            renders = 0;
            countRenders = true;
        });
        step("light escape speed", 8, () -> {
            measured(6.0D, "light escape is six times creative speed");
            require((Float) field(wearer(), "spread") < 0.05F, "the wings are folded away in the light");
            if (modelReported) require(renders == 0, "the wearer is hidden in the light (drawn " + renders + " times)");
            countRenders = false;
            require(trail() > 3, "the light escape leaves a trail");
            capture("boost-back");
            camera(CameraType.THIRD_PERSON_FRONT);
        });
        step("light escape front", 16, () -> {
            capture("boost-front");
            camera(CameraType.THIRD_PERSON_BACK);
            // Letting go: the light glides on, gathers back into the body and the wearer hovers, wings out again.
            steering = false;
        });
        step("light gathering", 30, () -> capture("boost-stop"));
        step("light escape hover", 25, () -> {
            Object wearer = wearer();
            mode(WingsMode.BOOST, "light escape stays on while hovering");
            require(!(Boolean) field(wearer, "lightOn") && (Float) field(wearer, "light") < 0.05F,
                    "standing still in light escape shows the body, not the light (lightOn " + field(wearer, "lightOn") + ", light "
                            + field(wearer, "light") + ", speed " + field(wearer, "speed") + ", moving " + Minecraft.getInstance().player.getDeltaMovement()
                            + ", fallFlying " + Minecraft.getInstance().player.isFallFlying() + ", key " + Minecraft.getInstance().options.keyUp.isDown() + ")");
            require((Float) field(wearer, "spread") > 0.6F, "the wings come back out when the light gathers");
            capture("boost-hover");
            server(player -> {
                player.setHealth(player.getMaxHealth());
                healthMark = player.getHealth();
            });
            WingsNetwork.requestBurn();
        });
        step("blood burn armed", 10, () -> {
            mode(WingsMode.BURN, "blood burn is on");
            require(!(Boolean) field(wearer(), "lightOn"), "blood burn shows no light while standing still");
            steer(8.0F);
        });
        step("burn launch", 10, () -> capture("burn-launch"));
        step("blood burn", 24, () -> {
            server(player -> {
                float expected = healthMark - player.getMaxHealth() * 0.3F;
                require(player.getHealth() >= expected - 0.01F && player.getHealth() <= expected + 1.5F,
                        "blood burn takes 30% of max health (" + player.getHealth() + " of " + player.getMaxHealth() + ")");
            });
            mode(WingsMode.BURN, "the client sees the blood burn");
            require(intField(wearer(), "burnTicks") > 0, "the client counts the burn down");
            require((Boolean) field(wearer(), "lightOn"), "moving in blood burn turns the wearer into the blood shadow");
        });
        step("blood burn speed", 6, () -> {
            measured(20.0D, "blood burn is twenty times creative speed");
            capture("burn-back");
            camera(CameraType.THIRD_PERSON_FRONT);
        });
        step("blood burn front", 8, () -> {
            capture("burn-front");
            camera(CameraType.FIRST_PERSON);
        });
        step("blood burn first person", 46, () -> {
            capture("burn-first-person");
            camera(CameraType.THIRD_PERSON_BACK);
        });
        step("burn ended", 5, () -> {
            server(player -> healthMark = player.getHealth());
            mode(WingsMode.BOOST, "after the burn the wings return to light escape");
            require(KineticFlightClient.superFlightOverride() == 6.0D, "after the burn super flight is back at six times");
            steer(5.0F);
            WingsNetwork.requestBurn();
        });
        step("cooldown refused", 20, () -> {
            server(player -> {
                // Natural regeneration may add some; the burn would take 30%.
                require(player.getHealth() >= healthMark - 0.01F, "no health is taken during the cooldown (health "
                        + player.getHealth() + ", before " + healthMark + ", food " + player.getFoodData().getFoodLevel() + ")");
                setDayTime(player, 18000L);
            });
            mode(WingsMode.BOOST, "blood burn is refused during the cooldown");
        });
        step("night light escape", 110, () -> capture("boost-night"));
        step("second burn", 20, () -> {
            server(player -> player.setHealth(player.getMaxHealth()));
            WingsNetwork.requestBurn();
            steer(8.0F);
        });
        step("night blood burn", 8, () -> {
            mode(WingsMode.BURN, "a second burn works after the cooldown");
            capture("burn-night");
            camera(CameraType.THIRD_PERSON_FRONT);
        });
        step("night blood burn front", 60, () -> {
            capture("burn-night-front");
            camera(CameraType.THIRD_PERSON_BACK);
        });
        step("second burn ended", 6, () -> {
            mode(WingsMode.BOOST, "the second burn ends in light escape");
            steering = false;
            server(player -> KineticSuperFlight.setActive(player, false));
        });
        step("landing ends the light", 10, () -> {
            require(!KineticFlightClient.superFlightActive(), "super flight is off after landing");
            Object wearer = wearer();
            require(wearer != null && field(wearer, "mode") == WingsMode.NORMAL, "landing ends the light escape");
            server(player -> {
                player.teleportTo(player.getX(), center.y, player.getZ());
                require(KineticSuperFlight.setActive(player, true), "super flight switches on again");
            });
        });
        step("night flight", 16, () -> steer(3.0F));
        step("night wings", 24, () -> {
            mode(WingsMode.NORMAL, "the client sees normal flight at night");
            measured(2.0D, "normal flight is back at twice creative speed");
            capture("normal-night");
            camera(CameraType.THIRD_PERSON_FRONT);
        });
        step("night wings front", 3, () -> {
            capture("normal-night-front");
            camera(CameraType.THIRD_PERSON_BACK);
            // Letting go of the keys: the flyer glides on in the flight pose, slowing, then hovers upright again.
            steering = false;
        });
        step("gliding on", 30, () -> require(Minecraft.getInstance().player.isFallFlying(),
                "letting go of the keys keeps the flight pose while gliding to a stop"));
        step("hovering again", 4, () -> {
            var player = Minecraft.getInstance().player;
            require(!player.isFallFlying() && KineticFlightClient.superFlightActive(), "after gliding to a stop the flyer hovers upright");
            require(player.getDeltaMovement().length() < 0.05D, "the hover is still (moving " + player.getDeltaMovement().length() + ")");
            server(serverPlayer -> back(serverPlayer).getStacks().setStackInSlot(0, ItemStack.EMPTY));
        });
        step("taken off", 10, () -> {
            server(player -> {
                require(KineticSuperFlight.available(player) == originalSuperAvailable,
                        "taking the wings off leaves super flight as available as before");
                if (!originalSuperAvailable) require(!KineticSuperFlight.active(player), "taking the wings off ends super flight");
                require(player.getAbilities().mayfly == originalMayfly, "the wings never touched creative flight");
            });
            Object wearer = wearer();
            require(wearer == null || !boolField(wearer, "worn"), "the client sees the wings taken off");
            require(KineticFlightClient.superFlightOverride() == 0.0D, "the client hands super flight back");
            KineticFlightClient.applySuperFlightState(true);
            superCenter = Minecraft.getInstance().player.position().subtract(SUPER_RADIUS, 0.0D, 0.0D);
            superAngle = 0.0D;
            superSpeed = 0.0D;
        });
        step("super flight hover", 4, () -> {
            require(KineticFlightClient.superFlightActive(), "super flight is on in this client");
            superSpeed = 4.0D;
        });
        step("super flight takeoff", 22, () -> {
            capture("super-takeoff");
            superSpeed = 15.0D;
        });
        step("super flight vapour cone", 3, () -> {
            capture("super-vapor-cone");
            superSpeed = 18.0D;
        });
        step("super flight sonic boom", 25, () -> {
            capture("super-sonic-boom");
            superSpeed = 34.0D;
        });
        step("super flight plasma", 4, () -> {
            capture("super-plasma");
            camera(CameraType.FIRST_PERSON);
        });
        step("super flight plasma first person", 5, () -> {
            capture("super-plasma-first-person");
            camera(CameraType.THIRD_PERSON_BACK);
            superSpeed = 0.0D;
        });
    }

    private static void step(String name, int waitAfter, Action action) {
        STEPS.add(new Step(name, waitAfter, action));
    }

    private static void tick() {
        var mc = Minecraft.getInstance();
        if (finished || mc.player == null || mc.level == null || mc.getSingleplayerServer() == null) return;
        // Steering holds forward and turns, so super flight itself flies the player; positions measure the speed.
        if (superCenter != null) superFly(mc);
        else if (steering) steerTick(mc);
        else mc.options.keyUp.setDown(false);
        if (--wait > 0) return;
        if (index >= STEPS.size()) {
            finish();
            return;
        }
        Step step = STEPS.get(index++);
        try {
            step.action().run();
            LOG.info("WINGS_RUNTIME_STEP {} ok", step.name());
            wait = step.waitAfter();
        } catch (Throwable error) {
            failures++;
            LOG.error("WINGS_RUNTIME_STEP_FAIL step=" + step.name(), error);
            finish();
        }
    }

    /** Holds forward and turns {@code degrees} a tick; the speed is measured afresh from here on. */
    private static void steer(float degrees) {
        steering = true;
        turn = degrees;
        TRACK.clear();
    }

    private static void steerTick(Minecraft mc) {
        var player = mc.player;
        mc.options.keyUp.setDown(true);
        player.setYRot(player.getYRot() + turn);
        player.setXRot(0.0F);
        TRACK.addLast(player.position());
        while (TRACK.size() > 12) TRACK.removeFirst();
    }

    /** The average distance flown per tick over the last ticks must be the multiplier times creative flight speed. */
    private static void measured(double multiplier, String message) {
        require(TRACK.size() >= 6, message + " (not enough movement to measure)");
        Vec3[] points = TRACK.toArray(new Vec3[0]);
        double total = 0.0D;
        for (int index = 1; index < points.length; index++) total += points[index].distanceTo(points[index - 1]);
        double perTick = total / (points.length - 1);
        double expected = CREATIVE_SPEED * multiplier;
        require(Math.abs(perTick - expected) <= expected * 0.2D,
                message + String.format(" (%.2f blocks a tick, expected %.2f)", perTick, expected));
        LOG.info("WINGS_RUNTIME_SPEED {} x{} measured={} blocks/tick", message, multiplier, String.format("%.3f", perTick));
    }

    /** Places the player further along the super-flight circle each tick, facing along it. */
    private static void superFly(Minecraft mc) {
        var player = mc.player;
        superAngle += superSpeed / SUPER_RADIUS;
        Vec3 target = superCenter.add(Math.cos(superAngle) * SUPER_RADIUS, 0.0D, Math.sin(superAngle) * SUPER_RADIUS);
        player.getAbilities().flying = true;
        player.setDeltaMovement(Vec3.ZERO);
        player.setPos(target.x, target.y, target.z);
        float yaw = (float) Math.toDegrees(Math.atan2(Math.sin(superAngle), Math.cos(superAngle)));
        player.setYRot(yaw);
        player.setYHeadRot(yaw);
        player.setYBodyRot(yaw);
        player.setXRot(6.0F);
    }

    private static void server(Consumer<ServerPlayer> action) {
        var mc = Minecraft.getInstance();
        var server = mc.getSingleplayerServer();
        server.submit(() -> action.accept(server.getPlayerList().getPlayer(mc.player.getUUID()))).join();
    }

    private static ICurioStacksHandler back(ServerPlayer player) {
        //? if >=1.21 {
        /*var inventory = CuriosApi.getCuriosInventory(player);
        *///?} else {
        var inventory = CuriosApi.getCuriosInventory(player).resolve();
        //?}
        return inventory.flatMap(handler -> handler.getStacksHandler("back")).orElseThrow(() -> new AssertionError("no back slot"));
    }

    private static long dayTime(ServerPlayer player) {
        //? if >=26.1 {
        /*return player.level().getOverworldClockTime();
        *///?} else {
        return player.serverLevel().getDayTime();
        //?}
    }

    /** 26.1 keeps the time in world clocks without a setter, so it goes through the time command there. */
    private static void setDayTime(ServerPlayer player, long time) {
        //? if >=26.1 {
        /*var server = player.level().getServer();
        server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withSuppressedOutput(), "time set " + time);
        *///?} else {
        player.serverLevel().setDayTime(time);
        //?}
    }

    private static void mode(WingsMode expected, String message) throws Exception {
        Object wearer = wearer();
        require(wearer != null && boolField(wearer, "worn") && boolField(wearer, "flying"), message + " (worn and flying)");
        Object actual = field(wearer, "mode");
        require(actual == expected, message + " (mode " + actual + ")");
    }

    private static int trail() throws Exception {
        Object wearer = wearer();
        return wearer == null ? 0 : ((Collection<?>) field(wearer, "trail")).size();
    }

    private static Object wearer() throws Exception {
        var method = WingsClient.class.getDeclaredMethod("wearer", int.class);
        method.setAccessible(true);
        return method.invoke(null, Minecraft.getInstance().player.getId());
    }

    private static Object field(Object owner, String name) throws Exception {
        var field = owner.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(owner);
    }

    private static boolean boolField(Object owner, String name) throws Exception {
        return (Boolean) field(owner, name);
    }

    private static int intField(Object owner, String name) throws Exception {
        return (Integer) field(owner, name);
    }

    private static void camera(CameraType type) {
        Minecraft.getInstance().options.setCameraType(type);
    }

    private static void capture(String name) throws Exception {
        var mc = Minecraft.getInstance();
        Path path = Path.of(ROOT, "wings-" + name + ".png");
        Files.createDirectories(path.getParent());
        //? if >=26.1 {
        /*// The 26.1 screenshot is read back from the GPU later; the file is written when it arrives.
        Screenshot.takeScreenshot(mc.getMainRenderTarget(), image -> {
            try (image) {
                image.writeToFile(path);
            } catch (Exception error) {
                LOG.error("WINGS_RUNTIME_CAPTURE_FAIL " + name, error);
            }
        });
        *///?} else {
        try (var image = Screenshot.takeScreenshot(mc.getMainRenderTarget())) {
            image.writeToFile(path);
        }
        //?}
        captures++;
        LOG.info("WINGS_RUNTIME_CAPTURE {} image={}x{}", name, mc.getWindow().getWidth(), mc.getWindow().getHeight());
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
        checks++;
    }

    private static void finish() {
        finished = true;
        steering = false;
        restore();
        String result = failures == 0 && index >= STEPS.size() ? "PASS" : "FAIL";
        LOG.info("WINGS_RUNTIME_{} checks={} captures={} failures={} restored={}", result, checks, captures, failures, restored);
        try {
            Path output = Path.of(ROOT);
            Files.createDirectories(output);
            Files.writeString(output.resolve("result.txt"), result + " checks=" + checks + " captures=" + captures + System.lineSeparator());
        } catch (Exception error) {
            LOG.error("WINGS_RUNTIME_RESULT_FAIL", error);
        }
        KineticClientRuntime.stopClient();
    }

    private static void restore() {
        var mc = Minecraft.getInstance();
        superCenter = null;
        mc.options.keyUp.setDown(false);
        if (KineticFlightClient.superFlightActive()) KineticFlightClient.applySuperFlightState(false);
        if (originalCamera != null) camera(originalCamera);
        if (originalMode == null) return;
        CuriosConfig.wtwBurnSeconds = originalBurnSeconds;
        CuriosConfig.wtwBurnCooldown = originalBurnCooldown;
        try {
            server(player -> {
                back(player).getStacks().setStackInSlot(0, originalBack.copy());
                player.setGameMode(originalMode);
                player.setHealth(originalHealth);
                setDayTime(player, originalTime);
                player.teleportTo(originalPosition.x, originalPosition.y, originalPosition.z);
                player.setDeltaMovement(Vec3.ZERO);
                KineticSuperFlight.setActive(player, originalSuperActive);
                player.getAbilities().flying = originalFlying && player.getAbilities().mayfly;
                player.onUpdateAbilities();
            });
            restored = true;
        } catch (Throwable error) {
            LOG.error("WINGS_RUNTIME_RESTORE_FAIL", error);
        }
        if (mc.player != null) mc.player.setDeltaMovement(Vec3.ZERO);
    }
}
