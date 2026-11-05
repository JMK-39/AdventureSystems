package dev.xyat.adventuresystems.curios.windthunderwings;

import dev.xyat.adventuresystems.curios.config.CuriosConfig;
import dev.xyat.adventuresystems.text.AdventureText;
import dev.xyat.kineticcore.api.event.KineticEventPriority;
import dev.xyat.kineticcore.api.flight.KineticFlightSources;
import dev.xyat.kineticcore.api.flight.KineticSuperFlight;
import dev.xyat.kineticcore.api.player.KineticPlayerMessages;
import dev.xyat.kineticcore.api.server.event.KineticServerEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 风雷翅的服务端逻辑：佩戴时授予 KineticCore 的超人飞行（双击跳跃起飞，飞行、姿势与操控都由超人飞行负责），
 * 处理遁光加速开关与燃血，并把佩戴、飞行、状态与速度倍率同步给自己和附近的玩家。速度由客户端的超人飞行按倍率执行。
 */
public final class WingsServer {
    static final String SOURCE = "adventuresystems:wind_thunder_wings";
    /** 旧版本给予创造飞行时用的来源名，佩戴时清理掉。 */
    private static final String LEGACY_FLIGHT_SOURCE = "wind_thunder_wings";
    private static final int RESYNC_TICKS = 40;
    private static final Map<UUID, State> STATES = new HashMap<>();

    private WingsServer() {
    }

    /** 一名佩戴者的状态。 */
    private static final class State {
        WingsMode mode = WingsMode.NORMAL;
        boolean boostBeforeBurn;
        int burnTicks;
        int cooldownTicks;
        boolean wasFlying;
        boolean sentFlying;
        WingsMode sentMode;
        float sentSpeed = -1.0F;
        int resyncTicks;
    }

    public static void install() {
        WingsNetwork.register();
        KineticServerEvents.onPlayerLogout(KineticEventPriority.NORMAL, player -> STATES.remove(player.getUUID()));
    }

    public static void equipped(LivingEntity entity) {
        if (entity instanceof ServerPlayer player) {
            KineticFlightSources.removeSource(player, LEGACY_FLIGHT_SOURCE);
            state(player);
        }
    }

    public static void unequipped(LivingEntity entity) {
        if (!(entity instanceof ServerPlayer player)) {
            return;
        }
        KineticSuperFlight.grant(player, SOURCE, false);
        STATES.remove(player.getUUID());
        WingsNetwork.sendState(player, false, false, WingsMode.NORMAL, 0, 0.0F);
    }

    public static void tick(LivingEntity entity) {
        if (!(entity instanceof ServerPlayer player)) {
            return;
        }
        State state = state(player);
        // Attributes are rebuilt on respawn and dimension change; granting again is a no-op when already there.
        KineticSuperFlight.grant(player, SOURCE, true);
        if (state.cooldownTicks > 0) {
            state.cooldownTicks--;
        }
        if (state.mode == WingsMode.BURN && --state.burnTicks <= 0) {
            state.mode = state.boostBeforeBurn ? WingsMode.BOOST : WingsMode.NORMAL;
            message(player, "burn_end");
        }
        boolean flying = KineticSuperFlight.active(player);
        // 落地即收起遁光；燃血的时限照常计算。
        if (!flying && state.wasFlying && state.mode == WingsMode.BOOST) {
            state.mode = WingsMode.NORMAL;
        }
        state.wasFlying = flying;
        float speed = (float) speed(state.mode);
        if (flying != state.sentFlying || state.mode != state.sentMode || speed != state.sentSpeed || --state.resyncTicks <= 0) {
            WingsNetwork.sendState(player, true, flying, state.mode, state.mode == WingsMode.BURN ? state.burnTicks : 0, speed);
            state.sentFlying = flying;
            state.sentMode = state.mode;
            state.sentSpeed = speed;
            state.resyncTicks = RESYNC_TICKS;
        }
    }

    /** 切换遁光加速；未在飞行时先起飞。燃血期间改的是燃血结束后回到的状态。 */
    static void toggleBoost(ServerPlayer player) {
        State state = STATES.get(player.getUUID());
        if (state == null) {
            return;
        }
        if (state.mode == WingsMode.BURN) {
            state.boostBeforeBurn = !state.boostBeforeBurn;
            message(player, state.boostBeforeBurn ? "boost_on" : "boost_off");
            return;
        }
        if (state.mode == WingsMode.BOOST) {
            state.mode = WingsMode.NORMAL;
            message(player, "boost_off");
            return;
        }
        if (!takeOff(player)) {
            return;
        }
        state.mode = WingsMode.BOOST;
        message(player, "boost_on");
    }

    /** 燃血：直接扣除最大生命值的一定比例，换取一段时间的极速；生命过低或冷却中时拒绝。未在飞行时先起飞。 */
    static void burn(ServerPlayer player) {
        State state = STATES.get(player.getUUID());
        if (state == null || state.mode == WingsMode.BURN) {
            return;
        }
        if (state.cooldownTicks > 0) {
            message(player, "burn_cooldown", (state.cooldownTicks + 19) / 20);
            return;
        }
        float max = player.getMaxHealth();
        double minimum = clamp(CuriosConfig.wtwBurnMinHealth, 0.0D, 1.0D);
        if (player.getHealth() < max * minimum) {
            message(player, "burn_low_health", Math.round(minimum * 100.0D));
            return;
        }
        if (!takeOff(player)) {
            return;
        }
        float cost = (float) (max * clamp(CuriosConfig.wtwBurnHealthCost, 0.0D, 1.0D));
        // 不会因燃血而死：至少留 1 点生命。
        player.setHealth(Math.max(1.0F, player.getHealth() - cost));
        int seconds = Math.max(1, CuriosConfig.wtwBurnSeconds);
        state.boostBeforeBurn = state.mode == WingsMode.BOOST;
        state.mode = WingsMode.BURN;
        state.burnTicks = seconds * 20;
        state.cooldownTicks = state.burnTicks + Math.max(0, CuriosConfig.wtwBurnCooldown) * 20;
        message(player, "burn_on", seconds);
    }

    /** 确保已在超人飞行；当前无法飞行（游泳、骑乘等）时提示并返回 false。 */
    private static boolean takeOff(ServerPlayer player) {
        KineticSuperFlight.grant(player, SOURCE, true);
        if (KineticSuperFlight.active(player) || KineticSuperFlight.setActive(player, true)) {
            return true;
        }
        message(player, "cannot_fly");
        return false;
    }

    private static State state(ServerPlayer player) {
        return STATES.computeIfAbsent(player.getUUID(), id -> {
            KineticSuperFlight.grant(player, SOURCE, true);
            return new State();
        });
    }

    private static double speed(WingsMode mode) {
        double value = switch (mode) {
            case NORMAL -> CuriosConfig.wtwNormalSpeed;
            case BOOST -> CuriosConfig.wtwBoostSpeed;
            case BURN -> CuriosConfig.wtwBurnSpeed;
        };
        return clamp(value, 1.0D, 100.0D);
    }

    private static void message(ServerPlayer player, String key, Object... args) {
        KineticPlayerMessages.display(player, AdventureText.translatable("msg.adventuresystems.wind_thunder_wings." + key, args), true);
    }

    private static double clamp(double value, double min, double max) {
        return Double.isFinite(value) ? Math.max(min, Math.min(max, value)) : min;
    }
}
