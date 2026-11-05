package dev.xyat.adventuresystems.curios.windthunderwings.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.xyat.adventuresystems.curios.config.CuriosConfig;
import dev.xyat.adventuresystems.curios.windthunderwings.WingsMode;
import dev.xyat.kineticcore.api.client.event.KineticClientEvents;
import dev.xyat.kineticcore.api.client.render.KineticWorldRender;
import dev.xyat.kineticcore.api.runtime.KineticClientRuntime;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * 风雷翅的画面。翅膀是三维模型（{@link WingsModel}），随实体一起绘制；化作遁光时翅膀与身形一同淡去。其余是叠加在世界上的光：
 * <ul>
 * <li>普通飞行：细细的青白电弧沿翼骨与羽毛游走，两侧拖出细长气流。</li>
 * <li>遁光与血影遁（只在飞动时）：白热光核、拉长的彩色光身、流转的光晕；身后一道柔和的光带，灵光碎点不断脱落飘散；
 * 血影遁另有一层血雾。没有闪电。</li>
 * <li>化光：一道光华与向外扩散的光环；收光：光环收拢、光华一闪、光点上浮，现出身形。</li>
 * </ul>
 * 每帧先画着色批（普通透明混合，白天也保持本色），再画发光批（叠加混合的高光）。
 */
final class WingsRenderer {
    private static final int MAX_TRAIL = 160;
    private static final long SAMPLE_NANOS = 40_000_000L;
    private static final long UNFOLD_NANOS = 300_000_000L;
    private static final long LIGHT_START_NANOS = 420_000_000L;
    private static final long LIGHT_STOP_NANOS = 650_000_000L;
    /** How far out (model pixels) the wind streams of normal flight leave, about the middle of the spread wing. */
    private static final float WAKE_SPAN = 24.0F;
    /** Normal flight leaves wind streams only above this speed (blocks a tick); hovering leaves none. */
    private static final double WAKE_SPEED = 0.5D;

    private WingsRenderer() {
    }

    /** One wearer drawn this frame. */
    private record Drawn(Player player, WingsClient.Wearer wearer, BodyPose pose, Vec3 chest, Vec3[] left, Vec3[] right,
                         Vec3 leftWake, Vec3 rightWake, boolean active, boolean own, long now, float light, float partialTick) {
    }

    private static float light(WingsClient.Wearer wearer, float partialTick) {
        return wearer.previousLight + (wearer.light - wearer.previousLight) * partialTick;
    }

    // ---------------------------------------------------------------- model

    /** The wing model, drawn with the entities so the body hides what is behind it; it fades out as the light forms. */
    static void renderModel(KineticClientEvents.LevelRenderContext context) {
        ClientLevel level = KineticClientRuntime.currentLevel();
        Player local = KineticClientRuntime.localPlayer();
        if (level == null || local == null || WingsClient.wearers().isEmpty() || !CuriosConfig.wtwShowWings) {
            return;
        }
        float partialTick = context.partialTick();
        boolean firstPerson = KineticClientRuntime.cameraType().isFirstPerson();
        Vec3 camera;
        try (KineticWorldRender.GlowBatch probe = KineticWorldRender.beginGlowBatch(context)) {
            camera = probe.cameraPosition();
        }
        for (Player player : level.players()) {
            WingsClient.Wearer wearer = WingsClient.wearer(player.getId());
            if (wearer == null || !wearer.worn || player.isInvisible() || (player == local && firstPerson)) {
                continue;
            }
            float alpha = 1.0F - Math.min(1.0F, light(wearer, partialTick) * 2.0F);
            if (Math.max(wearer.spread, wearer.previousSpread) < 0.02F || alpha < 0.02F) {
                continue;
            }
            WingsModel.animate(wearer, partialTick);
            PoseStack stack = context.poseStack();
            stack.pushPose();
            BodyPose.of(player, partialTick).apply(stack, camera);
            BlockPos at = BlockPos.containing(player.getEyePosition(partialTick));
            int light = (level.getBrightness(LightLayer.BLOCK, at) << 4) | (level.getBrightness(LightLayer.SKY, at) << 20);
            // Shafts and veins flicker with the thunder crawling over the wings.
            float flicker = 0.55F + 0.45F * (float) Math.abs(Math.sin(System.nanoTime() / 4.7E7D + player.getId()));
            WingsModel.render(stack, context.bufferSource(), CuriosConfig.wtwWingColor, CuriosConfig.wtwThunderColor, light,
                    flicker * alpha, alpha);
            stack.popPose();
        }
    }

    // ---------------------------------------------------------------- light

    static void render(KineticClientEvents.LevelRenderContext context) {
        ClientLevel level = KineticClientRuntime.currentLevel();
        Player local = KineticClientRuntime.localPlayer();
        if (level == null || local == null || WingsClient.wearers().isEmpty()) {
            return;
        }
        float partialTick = context.partialTick();
        boolean firstPerson = KineticClientRuntime.cameraType().isFirstPerson();
        long now = System.nanoTime();
        long trailNanos = (long) (Math.max(0.05D, CuriosConfig.wtwTrailSeconds) * 1.0E9D);
        List<Drawn> drawn = new ArrayList<>();
        for (Player player : level.players()) {
            WingsClient.Wearer wearer = WingsClient.wearer(player.getId());
            if (wearer == null) {
                continue;
            }
            BodyPose pose = BodyPose.of(player, partialTick);
            WingsModel.animate(wearer, partialTick);
            Vec3[] left = WingsModel.edge(pose, 0);
            Vec3[] right = WingsModel.edge(pose, 1);
            Vec3 chest = pose.point(0.0F, 6.0F, 0.0F);
            // The wind streams leave from fixed points beside the body, not the beating tips (which would zigzag them).
            Vec3 leftWake = pose.point(WAKE_SPAN, 3.0F, 8.0F);
            Vec3 rightWake = pose.point(-WAKE_SPAN, 3.0F, 8.0F);
            boolean active = wearer.worn && wearer.flying;
            if (active) {
                sample(wearer, chest, leftWake, rightWake, now);
            }
            prune(wearer, now, trailNanos);
            drawn.add(new Drawn(player, wearer, pose, chest, left, right, leftWake, rightWake, active,
                    player == local && firstPerson, now, light(wearer, partialTick), partialTick));
        }
        if (drawn.isEmpty()) {
            return;
        }
        // The coloured body first, then the bright highlights over it.
        try (KineticWorldRender.GlowBatch tint = KineticWorldRender.beginTintBatch(context)) {
            for (Drawn entry : drawn) {
                draw(tint, entry, now, trailNanos, false);
            }
        }
        try (KineticWorldRender.GlowBatch glow = KineticWorldRender.beginGlowBatch(context)) {
            for (Drawn entry : drawn) {
                draw(glow, entry, now, trailNanos, true);
            }
        }
    }

    private static void draw(KineticWorldRender.GlowBatch batch, Drawn entry, long now, long trailNanos, boolean glow) {
        trail(batch, entry, now, trailNanos, glow);
        if (glow) {
            motes(batch, entry);
        }
        if (!entry.own()) {
            lightStart(batch, entry, glow);
            lightStop(batch, entry, glow);
        }
        if (!entry.active() || entry.own()) {
            return;
        }
        if (entry.light() > 0.01F) {
            lightBody(batch, entry, glow);
        }
        WingsClient.Wearer wearer = entry.wearer();
        // The wings' thunder waits a moment after the light gathers back, so no lightning is ever seen around the light.
        boolean afterLight = now - wearer.lightStopNanos < 1_500_000_000L;
        if (glow && CuriosConfig.wtwShowWings && wearer.spread > 0.6F && entry.light() < 0.3F && !afterLight) {
            unfold(batch, entry);
            if (CuriosConfig.wtwSparks) {
                wingThunder(batch, entry);
            }
        }
    }

    // ---------------------------------------------------------------- trail

    private static void sample(WingsClient.Wearer wearer, Vec3 chest, Vec3 leftTip, Vec3 rightTip, long now) {
        boolean light = wearer.lightOn;
        if (!light && wearer.speed < WAKE_SPEED) {
            return;
        }
        WingsClient.TrailPoint last = wearer.trail.peekLast();
        if (last != null && last.position().distanceToSqr(chest) < 0.15D * 0.15D && now - last.nanos() < SAMPLE_NANOS) {
            return;
        }
        boolean burn = light && wearer.mode == WingsMode.BURN;
        int rgb = light ? WingsClient.lightColor(wearer.mode) : CuriosConfig.wtwWingColor;
        double width = (light ? (burn ? 1.0D : 0.8D) : 0.16D) * Math.max(0.1D, CuriosConfig.wtwTrailWidth);
        wearer.trail.addLast(new WingsClient.TrailPoint(chest, leftTip, rightTip, now, rgb, width, light, burn));
        while (wearer.trail.size() > MAX_TRAIL) {
            wearer.trail.removeFirst();
        }
    }

    private static void prune(WingsClient.Wearer wearer, long now, long trailNanos) {
        Iterator<WingsClient.TrailPoint> iterator = wearer.trail.iterator();
        while (iterator.hasNext()) {
            WingsClient.TrailPoint point = iterator.next();
            // The light lingers a little longer than the wind.
            if (now - point.nanos() <= (point.light() ? trailNanos * 3 / 2 : trailNanos)) {
                break;
            }
            iterator.remove();
        }
    }

    /**
     * The trail in runs of one kind: the light of a boost or burn runs from the body, the wind of normal flight as two
     * thin streams beside it. The newest run reaches the wearer, so it never lags behind.
     */
    private static void trail(KineticWorldRender.GlowBatch batch, Drawn entry, long now, long trailNanos, boolean glow) {
        WingsClient.Wearer wearer = entry.wearer();
        if (wearer.trail.isEmpty()) {
            return;
        }
        List<WingsClient.TrailPoint> run = new ArrayList<>();
        WingsClient.TrailPoint previous = null;
        for (WingsClient.TrailPoint point : wearer.trail) {
            if (previous != null && previous.light() != point.light()) {
                drawRun(batch, entry, run, false, now, trailNanos, glow);
                run.clear();
                run.add(previous);
            }
            run.add(point);
            previous = point;
        }
        boolean connect = entry.active() && previous.light() == wearer.lightOn;
        drawRun(batch, entry, run, connect, now, trailNanos, glow);
    }

    private static void drawRun(KineticWorldRender.GlowBatch batch, Drawn entry, List<WingsClient.TrailPoint> run, boolean connect,
                                long now, long trailNanos, boolean glow) {
        int size = run.size() + (connect ? 1 : 0);
        if (size < 2) {
            return;
        }
        WingsClient.TrailPoint newest = run.get(run.size() - 1);
        boolean light = newest.light();
        long life = light ? trailNanos * 3 / 2 : trailNanos;
        Vec3[] chest = new Vec3[size];
        Vec3[] leftTip = new Vec3[size];
        Vec3[] rightTip = new Vec3[size];
        double[] half = new double[size];
        double[] fade = new double[size];
        double[] fromHead = new double[size];
        int[] rgb = new int[size];
        for (int index = 0; index < size; index++) {
            boolean head = index == run.size();
            WingsClient.TrailPoint point = run.get(Math.min(index, run.size() - 1));
            chest[index] = head ? entry.chest() : point.position();
            leftTip[index] = head ? entry.leftWake() : point.leftTip();
            rightTip[index] = head ? entry.rightWake() : point.rightTip();
            fade[index] = head ? 1.0D : fade(now - point.nanos(), life);
            half[index] = point.width() * 0.5D;
            rgb[index] = point.rgb();
        }
        // Thin at the head, full width a little behind it, narrowing again as it fades.
        double distance = 0.0D;
        for (int index = size - 1; index >= 0; index--) {
            if (index < size - 1) {
                distance += chest[index].distanceTo(chest[index + 1]);
            }
            fromHead[index] = distance;
            double grow = smooth(Math.min(1.0D, distance / (light ? 0.9D : 0.6D)));
            half[index] *= (0.2D + 0.8D * grow) * (0.3D + 0.7D * fade[index]);
        }
        Vec3 skipNear = entry.own() ? batch.cameraPosition() : null;
        if (!light) {
            ribbon(batch, leftTip, half, rgb, fade, skipNear, glow, false);
            ribbon(batch, rightTip, half, rgb, fade, skipNear, glow, false);
            return;
        }
        // At speed the samples lie blocks apart; a spline through them keeps the light a smooth curve when turning.
        Smooth path = Smooth.of(chest, half, fade, fromHead, rgb);
        ribbon(batch, path.points, path.half, path.rgb, path.fade, skipNear, glow, true);
        if (!glow && newest.burn()) {
            bloodMist(batch, path.points, path.half, path.rgb, path.fade, skipNear);
        }
    }

    /** A trail resampled along a Catmull-Rom spline about every half block, its other values interpolated alongside. */
    private record Smooth(Vec3[] points, double[] half, double[] fade, double[] fromHead, int[] rgb) {
        static Smooth of(Vec3[] rawPoints, double[] rawHalf, double[] rawFade, double[] rawFromHead, int[] rawRgb) {
            // Points on top of each other (the newest sample and the head) would fold the spline back on itself.
            List<Integer> keep = new ArrayList<>();
            for (int index = 0; index < rawPoints.length; index++) {
                boolean last = index == rawPoints.length - 1;
                if (!keep.isEmpty() && rawPoints[index].distanceToSqr(rawPoints[keep.get(keep.size() - 1)]) < 0.0225D) {
                    if (last) keep.set(keep.size() - 1, index);
                    continue;
                }
                keep.add(index);
            }
            Vec3[] points = new Vec3[keep.size()];
            double[] half = new double[keep.size()];
            double[] fade = new double[keep.size()];
            double[] fromHead = new double[keep.size()];
            int[] rgb = new int[keep.size()];
            for (int index = 0; index < keep.size(); index++) {
                int from = keep.get(index);
                points[index] = rawPoints[from];
                half[index] = rawHalf[from];
                fade[index] = rawFade[from];
                fromHead[index] = rawFromHead[from];
                rgb[index] = rawRgb[from];
            }
            List<Vec3> outPoints = new ArrayList<>();
            List<double[]> outValues = new ArrayList<>();
            List<Integer> outRgb = new ArrayList<>();
            for (int index = 0; index + 1 < points.length; index++) {
                Vec3 p0 = points[Math.max(index - 1, 0)];
                Vec3 p1 = points[index];
                Vec3 p2 = points[index + 1];
                Vec3 p3 = points[Math.min(index + 2, points.length - 1)];
                int steps = Math.max(1, Math.min(8, (int) Math.ceil(p1.distanceTo(p2) / 0.5D)));
                for (int step = 0; step < steps; step++) {
                    double u = step / (double) steps;
                    outPoints.add(catmullRom(p0, p1, p2, p3, u));
                    outValues.add(new double[]{Mth.lerp(u, half[index], half[index + 1]), Mth.lerp(u, fade[index], fade[index + 1]),
                            Mth.lerp(u, fromHead[index], fromHead[index + 1])});
                    outRgb.add(rgb[index]);
                }
            }
            int last = points.length - 1;
            outPoints.add(points[last]);
            outValues.add(new double[]{half[last], fade[last], fromHead[last]});
            outRgb.add(rgb[last]);
            int size = outPoints.size();
            double[] h = new double[size];
            double[] f = new double[size];
            double[] d = new double[size];
            int[] c = new int[size];
            for (int index = 0; index < size; index++) {
                h[index] = outValues.get(index)[0];
                f[index] = outValues.get(index)[1];
                d[index] = outValues.get(index)[2];
                c[index] = outRgb.get(index);
            }
            return new Smooth(outPoints.toArray(new Vec3[0]), h, f, d, c);
        }

        private static Vec3 catmullRom(Vec3 p0, Vec3 p1, Vec3 p2, Vec3 p3, double u) {
            double u2 = u * u;
            double u3 = u2 * u;
            return p0.scale(-0.5D * u3 + u2 - 0.5D * u)
                    .add(p1.scale(1.5D * u3 - 2.5D * u2 + 1.0D))
                    .add(p2.scale(-1.5D * u3 + 2.0D * u2 + 0.5D * u))
                    .add(p3.scale(0.5D * u3 - 0.5D * u2));
        }
    }

    /**
     * A camera-facing ribbon with soft edges. Neighbouring segments share their edge, so there are no seams; the tint
     * pass draws the coloured body, the glow pass a narrower bright core (and for light a white-hot line).
     */
    private static void ribbon(KineticWorldRender.GlowBatch batch, Vec3[] points, double[] half, int[] rgb, double[] fade,
                               Vec3 skipNear, boolean glow, boolean light) {
        Vec3[] side = sides(batch, points);
        if (!glow) {
            soft(batch, points, side, half, 1.0D, rgb, 0.0D, fade, light ? 0.55D : 0.3D, skipNear);
            return;
        }
        soft(batch, points, side, half, 0.45D, rgb, 0.45D, fade, light ? 0.5D : 0.32D, skipNear);
        if (light) {
            soft(batch, points, side, half, 0.12D, rgb, 0.85D, fade, 0.75D, skipNear);
        }
    }

    /** 血雾：血影遁的光带外裹着一层暗红的雾，随光带渐散。 */
    private static void bloodMist(KineticWorldRender.GlowBatch batch, Vec3[] path, double[] half, int[] rgb, double[] fade,
                                  Vec3 skipNear) {
        for (int index = 0; index < path.length; index += 3) {
            if (skipNear != null && path[index].distanceToSqr(skipNear) < 4.0D) {
                continue;
            }
            double near = nearCamera(batch, path[index]);
            batch.glow(path[index], half[index] * 2.4D + 0.2D, argb(mix(rgb[index], 0x000000, 0.45D), 0.22D * fade[index] * near));
        }
    }

    private static Vec3[] sides(KineticWorldRender.GlowBatch batch, Vec3[] points) {
        Vec3 camera = batch.cameraPosition();
        Vec3[] side = new Vec3[points.length];
        Vec3 last = null;
        for (int index = 0; index < points.length; index++) {
            Vec3 tangent = points[Math.min(index + 1, points.length - 1)].subtract(points[Math.max(index - 1, 0)]);
            Vec3 across = tangent.cross(camera.subtract(points[index]));
            if (across.lengthSqr() < 1.0E-10D) {
                across = last != null ? last : batch.cameraUp();
            } else {
                across = across.normalize();
            }
            // Never let the ribbon flip over between two points.
            if (last != null && across.dot(last) < 0.0D) {
                across = across.scale(-1.0D);
            }
            side[index] = last = across;
        }
        return side;
    }

    /** Two quads per segment, from a transparent edge to the opaque centre line and out to the other edge. */
    private static void soft(KineticWorldRender.GlowBatch batch, Vec3[] points, Vec3[] side, double[] half, double widthScale,
                             int[] rgb, double whiten, double[] fade, double alpha, Vec3 skipNear) {
        for (int index = 0; index + 1 < points.length; index++) {
            Vec3 a = points[index];
            Vec3 b = points[index + 1];
            if (skipNear != null && a.distanceToSqr(skipNear) < 2.25D) {
                continue;
            }
            double nearA = nearCamera(batch, a);
            double nearB = nearCamera(batch, b);
            if (nearA <= 0.0D && nearB <= 0.0D) {
                continue;
            }
            Vec3 edgeA = side[index].scale(half[index] * widthScale);
            Vec3 edgeB = side[index + 1].scale(half[index + 1] * widthScale);
            int colorA = argb(mix(rgb[index], 0xFFFFFF, whiten), alpha * fade[index] * nearA);
            int colorB = argb(mix(rgb[index + 1], 0xFFFFFF, whiten), alpha * fade[index + 1] * nearB);
            int clearA = colorA & 0xFFFFFF;
            int clearB = colorB & 0xFFFFFF;
            batch.quad(a.subtract(edgeA), a, b, b.subtract(edgeB), clearA, colorA, colorB, clearB);
            batch.quad(a, a.add(edgeA), b.add(edgeB), b, colorA, clearA, clearB, colorB);
        }
    }

    /**
     * 1 for points well away from the camera, fading to 0 within a couple of blocks of it: in third person the trail
     * runs straight past the camera behind the wearer, and up close its quads would fill the screen.
     */
    private static double nearCamera(KineticWorldRender.GlowBatch batch, Vec3 point) {
        double distance = point.distanceTo(batch.cameraPosition());
        return smooth(Math.max(0.0D, Math.min(1.0D, (distance - 1.5D) / 2.5D)));
    }

    private static double fade(long age, long life) {
        double t = Math.max(0.0D, Math.min(1.0D, (double) age / life));
        return Math.pow(1.0D - t, 1.6D);
    }

    // ---------------------------------------------------------------- motes

    /** 灵光碎点：小小的光点，越老越暗越小。 */
    private static void motes(KineticWorldRender.GlowBatch batch, Drawn entry) {
        Vec3 camera = batch.cameraPosition();
        float partialTick = entry.partialTick();
        for (WingsClient.Mote mote : entry.wearer().motes) {
            Vec3 at = mote.previous.add(mote.position.subtract(mote.previous).scale(partialTick));
            if (at.distanceToSqr(camera) < 2.25D) {
                continue;
            }
            double t = (mote.age + partialTick) / mote.life;
            double alpha = 0.95D * Math.pow(Math.max(0.0D, 1.0D - t), 1.5D) * Math.min(1.0D, (mote.age + partialTick) / 2.0D);
            batch.glow(at, mote.size * (1.0D - 0.5D * t), argb(mix(mote.rgb, 0xFFFFFF, 0.55D), alpha));
        }
    }

    // ---------------------------------------------------------------- the light

    /**
     * 遁光的光身：沿飞行方向拉长的彩色光身与光晕（着色批），白热的光核与拉长的亮芯，几点光华绕着光核流转（发光批）。
     * 血影遁的光身更大，外裹一层暗红的雾。浓度随化光、收光平滑变化。
     */
    private static void lightBody(KineticWorldRender.GlowBatch batch, Drawn entry, boolean glow) {
        WingsClient.Wearer wearer = entry.wearer();
        boolean burn = wearer.mode == WingsMode.BURN;
        int rgb = WingsClient.lightColor(wearer.mode);
        double light = entry.light();
        Vec3 center = entry.chest();
        Vec3 dir = wearer.direction;
        double size = burn ? 1.15D : 1.0D;
        double seconds = entry.now() / 1.0E9D;
        if (!glow) {
            if (burn) {
                batch.glow(center, 1.7D * size, argb(mix(rgb, 0x000000, 0.45D), 0.35D * light));
            }
            batch.glow(center, 1.0D * size, argb(rgb, 0.5D * light));
            batch.glow(center.subtract(dir.scale(0.7D)), 0.85D * size, argb(rgb, 0.38D * light));
            batch.glow(center.subtract(dir.scale(1.4D)), 0.65D * size, argb(rgb, 0.26D * light));
            comet(batch, center, dir, 3.2D * size, 0.55D * size, rgb, 0.0D, 0.6D * light);
            return;
        }
        double pulse = 1.0D + 0.08D * Math.sin(seconds * 9.0D + entry.player().getId());
        batch.glow(center, 1.7D * size * pulse, argb(mix(rgb, 0xFFFFFF, 0.25D), 0.22D * light));
        comet(batch, center, dir, 2.4D * size, 0.3D * size, rgb, 0.6D, 0.75D * light);
        comet(batch, center, dir, 1.6D * size, 0.1D * size, rgb, 0.95D, 0.9D * light);
        batch.glow(center, 0.4D * size, argb(0xFFFFFF, 0.95D * light));
        batch.glow(center.add(dir.scale(0.3D)), 0.22D, argb(0xFFFFFF, 0.8D * light));
        // 光华流转：几点光绕着光核转。
        Vec3 side = dir.cross(new Vec3(0.0D, 1.0D, 0.0D));
        side = side.lengthSqr() < 1.0E-6D ? new Vec3(1.0D, 0.0D, 0.0D) : side.normalize();
        Vec3 up = side.cross(dir).normalize();
        for (int index = 0; index < 4; index++) {
            double angle = seconds * (burn ? 7.0D : 5.0D) + index * Math.PI / 2.0D;
            Vec3 at = center.add(side.scale(Math.cos(angle) * 0.5D * size)).add(up.scale(Math.sin(angle) * 0.5D * size))
                    .subtract(dir.scale(0.15D * Math.sin(angle * 2.0D)));
            batch.glow(at, 0.1D, argb(mix(rgb, 0xFFFFFF, 0.6D), 0.85D * light));
        }
    }

    /** A comet shape: a little ahead of the centre, widest at it, tapering to nothing {@code length} behind. */
    private static void comet(KineticWorldRender.GlowBatch batch, Vec3 center, Vec3 dir, double length, double width, int rgb,
                              double whiten, double alpha) {
        Vec3[] points = {center.add(dir.scale(0.4D)), center, center.subtract(dir.scale(length * 0.35D)), center.subtract(dir.scale(length))};
        double[] half = {width * 0.2D, width, width * 0.6D, 0.0D};
        double[] fade = {0.6D, 1.0D, 0.6D, 0.0D};
        int[] colors = {rgb, rgb, rgb, rgb};
        soft(batch, points, sides(batch, points), half, 1.0D, colors, whiten, fade, alpha, null);
    }

    /** 化光：在起步处一道光华，一圈光环向外扩散开。 */
    private static void lightStart(KineticWorldRender.GlowBatch batch, Drawn entry, boolean glow) {
        WingsClient.Wearer wearer = entry.wearer();
        long age = entry.now() - wearer.lightStartNanos;
        if (wearer.lightStartAt == null || wearer.lightStartNanos == 0L || age < 0L || age > LIGHT_START_NANOS) {
            return;
        }
        double t = (double) age / LIGHT_START_NANOS;
        Vec3 at = wearer.lightStartAt;
        // Close to the camera (third person, right behind the wearer) the flash would fill the screen: keep it small.
        double fade = (1.0D - t) * nearCamera(batch, at);
        int rgb = wearer.lightRgb;
        if (!glow) {
            batch.glow(at, 0.7D + 0.9D * t, argb(rgb, 0.32D * fade));
            return;
        }
        batch.glow(at, 0.35D + 0.7D * Math.sqrt(t), argb(0xFFFFFF, 0.7D * fade * fade));
        ring(batch, at, 0.3D + 1.9D * (1.0D - Math.pow(1.0D - t, 3.0D)), 0.35D * (1.0D - t) + 0.08D, mix(rgb, 0xFFFFFF, 0.5D), 0.55D * fade);
    }

    /** 收光：光环向身体收拢，光华一闪，现出身形（光点由客户端放出）。 */
    private static void lightStop(KineticWorldRender.GlowBatch batch, Drawn entry, boolean glow) {
        WingsClient.Wearer wearer = entry.wearer();
        long age = entry.now() - wearer.lightStopNanos;
        if (wearer.lightStopNanos == 0L || age < 0L || age > LIGHT_STOP_NANOS) {
            return;
        }
        double t = (double) age / LIGHT_STOP_NANOS;
        Vec3 at = entry.chest();
        int rgb = wearer.lightRgb;
        if (!glow) {
            batch.glow(at, 1.2D * (1.0D - t) + 0.3D, argb(rgb, 0.4D * (1.0D - t)));
            return;
        }
        if (t < 0.45D) {
            double gather = t / 0.45D;
            ring(batch, at, 1.8D - 1.5D * smooth(gather), 0.35D, mix(rgb, 0xFFFFFF, 0.5D), 0.75D * (0.4D + 0.6D * gather));
        }
        double flash = Math.max(0.0D, 1.0D - Math.abs(t - 0.45D) / 0.35D);
        batch.glow(at, 0.6D + 0.6D * flash, argb(mix(rgb, 0xFFFFFF, 0.7D), 0.85D * flash));
    }

    /** A soft camera-facing ring of light: clear at both edges, brightest along the middle of its band. */
    private static void ring(KineticWorldRender.GlowBatch batch, Vec3 center, double radius, double band, int rgb, double alpha) {
        Vec3 right = batch.cameraRight();
        Vec3 up = batch.cameraUp();
        double inner = Math.max(0.0D, radius - band * 0.5D);
        double outer = radius + band * 0.5D;
        int bright = argb(rgb, alpha);
        int clear = rgb & 0xFFFFFF;
        int segments = 40;
        for (int index = 0; index < segments; index++) {
            double a0 = index * Math.PI * 2.0D / segments;
            double a1 = (index + 1) * Math.PI * 2.0D / segments;
            Vec3 d0 = right.scale(Math.cos(a0)).add(up.scale(Math.sin(a0)));
            Vec3 d1 = right.scale(Math.cos(a1)).add(up.scale(Math.sin(a1)));
            batch.quad(center.add(d0.scale(inner)), center.add(d0.scale(radius)), center.add(d1.scale(radius)), center.add(d1.scale(inner)),
                    clear, bright, bright, clear);
            batch.quad(center.add(d0.scale(radius)), center.add(d0.scale(outer)), center.add(d1.scale(outer)), center.add(d1.scale(radius)),
                    bright, clear, clear, bright);
        }
    }

    // ---------------------------------------------------------------- wings

    /** The wings unfold out of the back in a flash, with arcs running out to the wingtips. */
    private static void unfold(KineticWorldRender.GlowBatch batch, Drawn entry) {
        WingsClient.Wearer wearer = entry.wearer();
        long age = entry.now() - wearer.unfoldNanos;
        if (wearer.unfoldNanos == 0L || age < 0L || age > UNFOLD_NANOS) {
            return;
        }
        double fade = 1.0D - (double) age / UNFOLD_NANOS;
        int color = CuriosConfig.wtwThunderColor;
        batch.glow(entry.left()[0].add(entry.right()[0]).scale(0.5D), 1.2D, argb(mix(color, 0xFFFFFF, 0.5D), 0.8D * fade));
        Random random = new Random(entry.player().getId() * 53L + entry.now() / 50_000_000L);
        for (Vec3[] edge : new Vec3[][]{entry.left(), entry.right()}) {
            fineArc(batch, edge[0], edge[3], color, random, fade);
            fineArc(batch, edge[1], edge[4], color, random, fade);
        }
    }

    /**
     * Fine blue-white thunder crawling over each wing: short arcs running along a stretch of the bone, or from the
     * bone partway down a feather. They stay on the wing, flicker on and off and are struck afresh about ten times a
     * second.
     */
    private static void wingThunder(KineticWorldRender.GlowBatch batch, Drawn entry) {
        long strike = entry.now() / 95_000_000L;
        Random random = new Random(entry.player().getId() * 7919L + strike * 31L);
        int color = CuriosConfig.wtwThunderColor;
        for (int side = 0; side < 2; side++) {
            Vec3[] edge = side == 0 ? entry.left() : entry.right();
            Vec3[] tips = WingsModel.featherTips(entry.pose(), side);
            if (random.nextInt(5) < 3) {
                int from = random.nextInt(3);
                fineArc(batch, edge[from], edge[from + 1], color, random, 1.0D);
            }
            if (random.nextInt(5) < 2) {
                Vec3 root = edge[1 + random.nextInt(2)];
                Vec3 tip = tips[random.nextInt(tips.length)];
                fineArc(batch, root, root.add(tip.subtract(root).scale(0.4D + random.nextDouble() * 0.3D)), color, random, 1.0D);
            }
        }
    }

    /** A thin crawling arc: short jagged steps, a hot core in a faint halo. */
    private static void fineArc(KineticWorldRender.GlowBatch batch, Vec3 from, Vec3 to, int rgb, Random random, double strength) {
        int segments = Math.max(3, (int) Math.ceil(from.distanceTo(to) / 0.12D));
        Vec3 previous = from;
        int core = mix(rgb, 0xFFFFFF, 0.65D);
        for (int index = 1; index <= segments; index++) {
            Vec3 point = from.add(to.subtract(from).scale(index / (double) segments));
            if (index < segments) {
                point = point.add(random.nextGaussian() * 0.03D, random.nextGaussian() * 0.03D, random.nextGaussian() * 0.03D);
            }
            line(batch, previous, point, 0.016D, argb(core, 0.9D * strength));
            line(batch, previous, point, 0.055D, argb(rgb, 0.25D * strength));
            previous = point;
        }
    }

    private static void line(KineticWorldRender.GlowBatch batch, Vec3 a, Vec3 b, double width, int argb) {
        Vec3 side = b.subtract(a).cross(batch.cameraPosition().subtract(a));
        if (side.lengthSqr() < 1.0E-10D) {
            return;
        }
        side = side.normalize().scale(width * 0.5D);
        batch.quad(a.subtract(side), a.add(side), b.add(side), b.subtract(side), argb);
    }

    private static double smooth(double t) {
        return t * t * (3.0D - 2.0D * t);
    }

    private static int argb(int rgb, double alpha) {
        int a = (int) Math.round(Math.max(0.0D, Math.min(1.0D, alpha)) * 255.0D);
        return (a << 24) | (rgb & 0xFFFFFF);
    }

    private static int mix(int rgb, int other, double amount) {
        return WingsClient.mix(rgb, other, amount);
    }
}
