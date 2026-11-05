package dev.xyat.adventuresystems.curios.windthunderwings.client;

import dev.xyat.adventuresystems.curios.config.CuriosConfig;
import dev.xyat.adventuresystems.curios.init.Items;
import dev.xyat.adventuresystems.curios.windthunderwings.WingsMode;
import dev.xyat.adventuresystems.curios.windthunderwings.WingsNetwork;
import dev.xyat.adventuresystems.text.AdventureText;
import dev.xyat.kineticcore.api.client.event.KineticClientEvents;
import dev.xyat.kineticcore.api.client.gui.render.KineticGraphics;
import dev.xyat.kineticcore.api.client.input.KineticKeyBindings;
import dev.xyat.kineticcore.api.client.sound.KineticClientSounds;
import dev.xyat.kineticcore.api.client.tooltip.KineticItemTooltips;
import dev.xyat.kineticcore.api.flight.KineticFlightClient;
import dev.xyat.kineticcore.api.runtime.KineticClientRuntime;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 风雷翅的客户端：两个快捷键（遁光、燃血），各佩戴者的同步状态、翅膀展开与扇动、尾迹与灵光碎点，把服务端给的速度倍率交给
 * 超人飞行执行，音效（随扇动节拍的振翅、起飞展翼、遁光化光与收光、燃血），以及第一人称的边缘光色、速度线与倒计时。
 * <p>遁光与血影遁只在真正飞动时出现：静止悬停时现出身形与双翼；一动起来，光华一闪、灵光四溅，人化作遁光；停下时遁光收敛、
 * 光点上浮，现出身形。画面见 {@link WingsRenderer} 与 {@link WingsModel}。
 */
public final class WingsClient {
    private static final Map<Integer, Wearer> WEARERS = new HashMap<>();
    /** Speed (blocks a tick) above which the light forms, and below which it gives the body back. */
    private static final double LIGHT_ON_SPEED = 0.3D;
    private static final double LIGHT_OFF_SPEED = 0.12D;
    private static final int MAX_MOTES = 220;
    private static KineticClientSounds.Loop rush;
    private static KineticKeyBindings.Binding boostKey;
    private static KineticKeyBindings.Binding burnKey;

    private WingsClient() {
    }

    /** 一名佩戴者在本客户端的状态。 */
    static final class Wearer {
        boolean worn;
        boolean flying;
        WingsMode mode = WingsMode.NORMAL;
        int burnTicks;
        /** 服务端给的当前速度倍率（相对创造飞行）。 */
        float speedMultiplier;
        /** 计时（tick），用于光效与界面的节奏。 */
        int flapTicks;
        /** 扇动相位（弧度）与上一 tick 的值，用于帧间插值；每过一个周期（下扑开始）响一声振翅。 */
        double flapPhase;
        double previousFlapPhase;
        /** 翅膀张开程度：0 收拢在背后，1 完全展开；previousSpread 用于帧间插值。 */
        float spread;
        float previousSpread;
        /** 姿势：0 直立悬停，1 平飞滑翔；previousGlide 用于帧间插值。 */
        float glide;
        float previousGlide;
        /** 是否已化作遁光（遁光或燃血模式下飞动时），以及光的浓度（0 现出身形，1 完全化光）。 */
        boolean lightOn;
        float light;
        float previousLight;
        /** 翅膀展开的时刻，用于展翼时的雷光；0 表示没有。 */
        long unfoldNanos;
        /** 化光（起步）与收光（停下）的时刻、位置与颜色。 */
        long lightStartNanos;
        long lightStopNanos;
        Vec3 lightStartAt;
        int lightRgb;
        Vec3 lastPosition;
        Vec3 direction = new Vec3(0.0D, 0.0D, 1.0D);
        double speed;
        final ArrayDeque<TrailPoint> trail = new ArrayDeque<>();
        final ArrayDeque<Mote> motes = new ArrayDeque<>();
    }

    /** 尾迹上的一点：胸口位置、两侧气流出口、时间、颜色、宽度、是遁光（true）还是普通飞行的气流，以及是否血影遁。 */
    record TrailPoint(Vec3 position, Vec3 leftTip, Vec3 rightTip, long nanos, int rgb, double width, boolean light,
                      boolean burn) {
    }

    /** 一粒灵光碎点：位置、速度、已存在与总寿命（tick）、颜色。 */
    static final class Mote {
        Vec3 position;
        Vec3 previous;
        Vec3 velocity;
        int age;
        final int life;
        final int rgb;
        final double size;

        Mote(Vec3 position, Vec3 velocity, int life, int rgb, double size) {
            this.position = position;
            this.previous = position;
            this.velocity = velocity;
            this.life = life;
            this.rgb = rgb;
            this.size = size;
        }
    }

    public static void install() {
        boostKey = KineticKeyBindings.builder("key.adventuresystems.wings_boost")
                .category("key.categories.adventuresystems")
                .context(KineticKeyBindings.Context.IN_GAME)
                .keyboard(KineticKeyBindings.Key.R)
                .onPressed(() -> {
                    if (!localWorn()) return false;
                    WingsNetwork.requestBoost();
                    return true;
                })
                .register();
        burnKey = KineticKeyBindings.builder("key.adventuresystems.wings_burn")
                .category("key.categories.adventuresystems")
                .context(KineticKeyBindings.Context.IN_GAME)
                .keyboard(KineticKeyBindings.Key.H)
                .onPressed(() -> {
                    if (!localWorn()) return false;
                    WingsNetwork.requestBurn();
                    return true;
                })
                .register();
        KineticClientEvents.onTick(KineticClientEvents.TickPhase.END, WingsClient::tick);
        // The wings with the entities; the light after clouds and weather, which would otherwise be drawn over it.
        KineticClientEvents.onLevelRender(KineticClientEvents.LevelRenderStage.AFTER_ENTITIES, WingsRenderer::renderModel);
        KineticClientEvents.onLevelRender(KineticClientEvents.LevelRenderStage.AFTER_WEATHER, WingsRenderer::render);
        KineticClientEvents.onPlayerRenderBefore(context -> {
            Wearer wearer = WEARERS.get(context.player().getId());
            // 化作遁光时只见光，不见人。
            if (wearer != null && wearer.worn && Math.max(wearer.light, wearer.previousLight) > 0.5F) {
                context.cancel();
            }
        });
        KineticClientEvents.onHudRender(KineticClientEvents.HudStage.HOTBAR, WingsClient::hud);
        KineticClientEvents.onLogout(WingsClient::clear);
        KineticItemTooltips.onBuild(WingsClient::tooltip);
    }

    static Wearer wearer(int entityId) {
        return WEARERS.get(entityId);
    }

    static Map<Integer, Wearer> wearers() {
        return WEARERS;
    }

    /** 服务端同步来的状态。本人的速度倍率交给超人飞行执行。 */
    public static void applyState(WingsNetwork.State state) {
        Wearer wearer = WEARERS.computeIfAbsent(state.entityId(), id -> new Wearer());
        WingsMode next = WingsMode.of(state.mode());
        boolean wasWorn = wearer.worn;
        boolean wasFlying = wearer.flying;
        WingsMode before = wearer.mode;
        wearer.worn = state.worn();
        wearer.flying = state.flying();
        wearer.mode = next;
        wearer.burnTicks = state.burnTicks();
        wearer.speedMultiplier = state.speed();
        Player local = KineticClientRuntime.localPlayer();
        if (local != null && local.getId() == state.entityId()) {
            KineticFlightClient.overrideSuperFlight(state.worn() ? state.speed() : 0.0D);
        }
        if (!wasWorn || !state.worn()) {
            return;
        }
        if (state.flying() && !wasFlying) {
            sound(state.entityId(), "wings.unfold", 0.9F, 1.0F);
        }
        if (next != before) {
            // 燃血是以血换遁：心跳与轰鸣；转入遁光只是蓄势待发，真正化光在动起来的那一刻。
            if (next == WingsMode.BURN) {
                sound(state.entityId(), "wings.burn", 1.2F, 1.0F);
            } else if (next == WingsMode.BOOST && before == WingsMode.NORMAL) {
                sound(state.entityId(), "wings.ready", 0.8F, 1.0F);
            } else if (before == WingsMode.BURN) {
                sound(state.entityId(), "wings.burn_end", 0.9F, 1.0F);
            }
        }
    }

    private static void sound(int entityId, String id, float volume, float pitch) {
        ClientLevel level = KineticClientRuntime.currentLevel();
        if (!CuriosConfig.wtwSounds || level == null || !(level.getEntity(entityId) instanceof Player player)) {
            return;
        }
        KineticClientSounds.play(player.position().add(0.0D, player.getBbHeight() * 0.5D, 0.0D), "adventuresystems:" + id, volume, pitch);
    }

    private static boolean localWorn() {
        Player local = KineticClientRuntime.localPlayer();
        Wearer wearer = local == null ? null : WEARERS.get(local.getId());
        return wearer != null && wearer.worn;
    }

    private static void clear() {
        WEARERS.clear();
        KineticFlightClient.overrideSuperFlight(0.0D);
        if (rush != null) {
            rush.end();
            rush = null;
        }
    }

    static int lightColor(WingsMode mode) {
        return mode == WingsMode.BURN ? CuriosConfig.wtwBurnColor : CuriosConfig.wtwBoostColor;
    }

    private static void tick() {
        ClientLevel level = KineticClientRuntime.currentLevel();
        if (level == null || KineticClientRuntime.paused()) {
            return;
        }
        WEARERS.entrySet().removeIf(entry -> {
            if (!(level.getEntity(entry.getKey()) instanceof Player player)) return true;
            Wearer wearer = entry.getValue();
            Vec3 position = player.position();
            if (wearer.lastPosition != null) {
                Vec3 delta = position.subtract(wearer.lastPosition);
                wearer.speed = delta.length();
                if (wearer.speed > 0.03D) wearer.direction = delta.normalize();
            }
            wearer.lastPosition = position;
            if (wearer.burnTicks > 0) wearer.burnTicks--;
            Vec3 chest = position.add(0.0D, player.getBbHeight() * 0.55D, 0.0D);
            updateLight(entry.getKey(), wearer, chest);
            // 展开与收拢有一个过渡；化作遁光时翅膀收起，现出身形时重新展开。
            wearer.previousSpread = wearer.spread;
            wearer.previousGlide = wearer.glide;
            wearer.previousFlapPhase = wearer.flapPhase;
            boolean showWings = wearer.worn && wearer.flying && !wearer.lightOn;
            float target = showWings ? 1.0F : 0.0F;
            // The thunder flash of unfolding greets a take-off, not the wings coming back out of the light.
            if (target > 0.0F && wearer.spread < 0.05F && System.nanoTime() - wearer.lightStopNanos > 1_500_000_000L) {
                wearer.unfoldNanos = System.nanoTime();
            }
            wearer.spread += (target - wearer.spread) * (target > wearer.spread ? 0.16F : 0.45F);
            wearer.glide += ((player.isFallFlying() ? 1.0F : 0.0F) - wearer.glide) * 0.2F;
            wearer.flapTicks++;
            if (wearer.flying) {
                // 悬停时一拍约 1.4 秒，平飞滑翔时约 2.2 秒；每拍下扑开始时响一声振翅。
                double rate = WingsModel.HOVER_RATE + (WingsModel.GLIDE_RATE - WingsModel.HOVER_RATE) * wearer.glide;
                double before = Math.floor(wearer.flapPhase / (Math.PI * 2.0D));
                wearer.flapPhase += rate;
                if (Math.floor(wearer.flapPhase / (Math.PI * 2.0D)) != before && showWings && wearer.spread > 0.6F
                        && CuriosConfig.wtwShowWings) {
                    sound(entry.getKey(), "wings.flap", 0.6F, 0.92F + ThreadLocalRandom.current().nextFloat() * 0.14F);
                }
                // 风雷翅上偶有细细的雷声，遁光中没有。
                ThreadLocalRandom random = ThreadLocalRandom.current();
                if (showWings && CuriosConfig.wtwSparks && random.nextInt(90) == 0) {
                    sound(entry.getKey(), "wings.crackle", 0.3F, 0.85F + random.nextFloat() * 0.3F);
                }
            }
            tickMotes(wearer);
            return !wearer.worn && wearer.trail.isEmpty() && wearer.motes.isEmpty();
        });
        rushSound();
    }

    /**
     * 化光与收光。遁光或燃血模式下飞动起来（超过起光速度）时化作遁光：一道光华，灵光向四周溅开；速度降到收光速度以下时
     * 遁光收敛，光点向上飘散，现出身形。光的浓度平滑过渡，化光快、收光稍慢。
     */
    private static void updateLight(int entityId, Wearer wearer, Vec3 chest) {
        wearer.previousLight = wearer.light;
        boolean can = wearer.worn && wearer.flying && wearer.mode != WingsMode.NORMAL;
        boolean moving = wearer.speed > (wearer.lightOn ? LIGHT_OFF_SPEED : LIGHT_ON_SPEED);
        boolean next = can && moving;
        if (next != wearer.lightOn) {
            wearer.lightOn = next;
            wearer.lightRgb = lightColor(wearer.mode);
            boolean burn = wearer.mode == WingsMode.BURN;
            if (next) {
                wearer.lightStartNanos = System.nanoTime();
                wearer.lightStartAt = chest;
                burst(wearer, chest, 18, 0.16D, 0.0D, wearer.lightRgb);
                sound(entityId, "wings.escape", burn ? 1.2F : 1.0F, burn ? 0.8F : 1.0F);
            } else {
                wearer.lightStopNanos = System.nanoTime();
                burst(wearer, chest, 24, 0.07D, 0.05D, wearer.lightRgb);
                sound(entityId, "wings.escape_end", 0.9F, burn ? 0.85F : 1.0F);
            }
        }
        float target = wearer.lightOn ? 1.0F : 0.0F;
        wearer.light += (target - wearer.light) * (target > wearer.light ? 0.5F : 0.3F);
        if (wearer.lightOn && wearer.light > 0.5F) {
            // 遁光身后不断脱落的灵光碎点，向后漂散。
            ThreadLocalRandom random = ThreadLocalRandom.current();
            boolean burn = wearer.mode == WingsMode.BURN;
            for (int index = 0, count = burn ? 5 : 3; index < count; index++) {
                Vec3 at = chest.add(random.nextGaussian() * 0.3D, random.nextGaussian() * 0.3D, random.nextGaussian() * 0.3D);
                Vec3 drift = wearer.direction.scale(-0.06D - random.nextDouble() * 0.08D)
                        .add(random.nextGaussian() * 0.02D, random.nextGaussian() * 0.02D + 0.005D, random.nextGaussian() * 0.02D);
                addMote(wearer, new Mote(at, drift, 14 + random.nextInt(14), wearer.lightRgb, 0.05D + random.nextDouble() * 0.05D));
            }
        }
    }

    /** A ring of motes thrown out from {@code at}, rising by {@code lift} a tick. */
    private static void burst(Wearer wearer, Vec3 at, int count, double speed, double lift, int rgb) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int index = 0; index < count; index++) {
            Vec3 out = new Vec3(random.nextGaussian(), random.nextGaussian() * 0.6D, random.nextGaussian());
            if (out.lengthSqr() < 1.0E-4D) continue;
            out = out.normalize().scale(speed * (0.6D + random.nextDouble() * 0.6D)).add(0.0D, lift, 0.0D);
            addMote(wearer, new Mote(at.add(out.scale(2.0D)), out, 18 + random.nextInt(16), rgb, 0.06D + random.nextDouble() * 0.06D));
        }
    }

    private static void addMote(Wearer wearer, Mote mote) {
        if (wearer.motes.size() >= MAX_MOTES) wearer.motes.removeFirst();
        wearer.motes.addLast(mote);
    }

    private static void tickMotes(Wearer wearer) {
        Iterator<Mote> iterator = wearer.motes.iterator();
        while (iterator.hasNext()) {
            Mote mote = iterator.next();
            mote.previous = mote.position;
            mote.position = mote.position.add(mote.velocity);
            mote.velocity = mote.velocity.scale(0.9D).add(0.0D, 0.003D, 0.0D);
            if (++mote.age >= mote.life) iterator.remove();
        }
    }

    /** 本人化作遁光时的呼啸声。 */
    private static void rushSound() {
        Player local = KineticClientRuntime.localPlayer();
        Wearer wearer = local == null ? null : WEARERS.get(local.getId());
        boolean wanted = CuriosConfig.wtwSounds && wearer != null && wearer.worn && wearer.lightOn;
        if (!wanted) {
            if (rush != null) {
                rush.end();
                rush = null;
            }
            return;
        }
        if (rush == null || !rush.playing()) {
            rush = KineticClientSounds.loop("adventuresystems:wings.escape_loop", () -> {
                Player player = KineticClientRuntime.localPlayer();
                Wearer current = player == null ? null : WEARERS.get(player.getId());
                if (current == null || !current.worn || !current.lightOn) return null;
                // 血影遁的呼啸更响、更低沉。
                boolean burn = current.mode == WingsMode.BURN;
                float strength = Math.max(0.0F, current.light);
                return new KineticClientSounds.LoopState(player.getEyePosition(), (burn ? 0.8F : 0.55F) * strength, burn ? 0.82F : 1.0F);
            });
        }
    }

    private static void hud(KineticGraphics graphics, float partialTick) {
        Player local = KineticClientRuntime.localPlayer();
        Wearer wearer = local == null ? null : WEARERS.get(local.getId());
        if (wearer == null || !wearer.worn || !wearer.flying || KineticClientRuntime.guiHidden()
                || KineticClientRuntime.currentScreen() != null) {
            return;
        }
        int width = KineticClientRuntime.guiScaledWidth();
        int height = KineticClientRuntime.guiScaledHeight();
        boolean burn = wearer.mode == WingsMode.BURN;
        int rgb = lightColor(wearer.mode);
        float light = wearer.previousLight + (wearer.light - wearer.previousLight) * partialTick;
        if (KineticClientRuntime.cameraType().isFirstPerson() && light > 0.02F) {
            // 化作遁光时看不见自己，只有屏幕边缘的光色与掠过的光线；血影遁时边缘随心跳明暗。
            double pulse = burn ? 0.75D + 0.25D * Math.sin((wearer.flapTicks + partialTick) * 0.45D) : 1.0D;
            int edge = (int) ((burn ? 0x70 : 0x48) * pulse * light);
            int band = Math.max(12, height / 5);
            graphics.fillGradient(0, 0, width, band, (edge << 24) | (rgb & 0xFFFFFF), rgb & 0xFFFFFF);
            graphics.fillGradient(0, height - band, width, height, rgb & 0xFFFFFF, (edge << 24) | (rgb & 0xFFFFFF));
            speedLines(graphics, width, height, wearer.flapTicks, (burn ? 1.0D : 0.6D) * light, rgb);
        }
        if (KineticClientRuntime.cameraType().isFirstPerson()) {
            // 化光与收光的一闪。
            long now = System.nanoTime();
            flash(graphics, width, height, now - wearer.lightStartNanos, 260_000_000L, 0.45D, mix(rgb, 0xFFFFFF, 0.6D));
            flash(graphics, width, height, now - wearer.lightStopNanos, 420_000_000L, 0.25D, mix(rgb, 0xFFFFFF, 0.75D));
        }
        if (burn && wearer.burnTicks > 0) {
            Component text = AdventureText.translatable("hud.adventuresystems.wind_thunder_wings.burn",
                    String.format(java.util.Locale.ROOT, "%.1f", wearer.burnTicks / 20.0D));
            graphics.centeredText(text, width / 2, height - 62, 0xFF000000 | rgb, true);
        }
    }

    private static void flash(KineticGraphics graphics, int width, int height, long age, long length, double strength, int rgb) {
        if (age < 0L || age > length) {
            return;
        }
        double t = 1.0D - (double) age / length;
        int alpha = (int) (255 * strength * t * t);
        graphics.fill(0, 0, width, height, (alpha << 24) | (rgb & 0xFFFFFF));
    }

    /** Streaks from the screen edge toward the centre in the light's colour, redrawn at new angles every tick. */
    private static void speedLines(KineticGraphics graphics, int width, int height, int ticks, double strength, int rgb) {
        java.util.Random random = new java.util.Random(ticks * 31L);
        float reach = (float) Math.hypot(width, height) * 0.5F;
        int color = mix(rgb, 0xFFFFFF, 0.55D);
        float centerX = width / 2.0F;
        float centerY = height / 2.0F;
        for (int index = 0, lines = 18 + (int) (22 * strength); index < lines; index++) {
            float angle = random.nextFloat() * 360.0F;
            int inner = (int) (reach * (0.55F - 0.15F * (float) strength) + random.nextFloat() * reach * 0.15F);
            int alpha = (int) (255 * (0.1D + 0.3D * strength * (0.5D + random.nextDouble() * 0.5D)));
            graphics.push();
            graphics.translate(centerX, centerY);
            graphics.rotate(angle);
            graphics.fill(inner, 0, (int) (reach * 1.1F), 1, (alpha << 24) | color);
            graphics.pop();
        }
    }

    static int mix(int rgb, int other, double amount) {
        int r = (int) (((rgb >> 16) & 0xFF) * (1.0D - amount) + ((other >> 16) & 0xFF) * amount);
        int g = (int) (((rgb >> 8) & 0xFF) * (1.0D - amount) + ((other >> 8) & 0xFF) * amount);
        int b = (int) ((rgb & 0xFF) * (1.0D - amount) + (other & 0xFF) * amount);
        return (r << 16) | (g << 8) | b;
    }

    private static void tooltip(ItemStack stack, List<Component> lines) {
        if (Items.WIND_THUNDER_WINGS.get() != stack.getItem()) {
            return;
        }
        String key = "tip.adventuresystems.curios.wind_thunder_wings.";
        lines.add(AdventureText.translatable(key + "title"));
        lines.add(AdventureText.translatable(key + "desc1"));
        lines.add(AdventureText.translatable(key + "desc2", boostKey.translatedKeyMessage()));
        lines.add(AdventureText.translatable(key + "desc3", burnKey.translatedKeyMessage()));
        lines.add(AdventureText.translatable(key + "desc4"));
    }
}
