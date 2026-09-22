package dev.xyat.adventuresystems.ftb.client;

import dev.xyat.adventuresystems.ftb.network.FTBSubmitLimitNetwork;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import dev.xyat.kineticcore.api.runtime.KineticClientRuntime;

public final class FTBVirtualItemClientState {
    private static final long QUERY_INTERVAL_MS = 250L;
    private static final Map<Long, State> STATES = new ConcurrentHashMap<>();
    private static long connectionRevision = Long.MIN_VALUE;

    private FTBVirtualItemClientState() {
    }

    public static Snapshot request(long taskId) {
        ensureSession();
        State state = STATES.computeIfAbsent(taskId, ignored -> new State());
        long now = System.currentTimeMillis();
        boolean send = false;

        synchronized (state) {
            if (state.lastRequestMs == 0L || now < state.lastRequestMs || now - state.lastRequestMs >= QUERY_INTERVAL_MS) {
                state.lastRequestMs = now;
                send = true;
            }
        }

        if (send) {
            FTBSubmitLimitNetwork.sendVirtualItemCountQuery(taskId);
        }

        return new Snapshot(state.supported, state.count);
    }

    public static void accept(long taskId, boolean supported, long count) {
        ensureSession();
        State state = STATES.computeIfAbsent(taskId, ignored -> new State());
        state.supported = supported;
        state.count = Math.max(0L, count);
    }

    private static void ensureSession() {
        long currentRevision = KineticClientRuntime.connectionRevision();
        if (currentRevision == connectionRevision) return;
        synchronized (FTBVirtualItemClientState.class) {
            if (currentRevision != connectionRevision) {
                STATES.clear();
                connectionRevision = currentRevision;
            }
        }
    }

    public record Snapshot(boolean supported, long count) {
    }

    private static final class State {
        private volatile boolean supported;
        private volatile long count;
        private long lastRequestMs;
    }
}
