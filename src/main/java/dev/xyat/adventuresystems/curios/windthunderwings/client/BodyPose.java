package dev.xyat.adventuresystems.curios.windthunderwings.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.xyat.kineticcore.api.flight.KineticFlightClient;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * 玩家模型的身体变换，按原版玩家渲染器的顺序复刻：身体朝向，滑翔姿势的俯仰与侧倾，超人飞行的横滚，缩放与模型偏移。
 * 翅膀模型与遁光都挂在这个变换上，所以站立、超人飞行的平飞姿势和横滚都会带着它们一起动。
 * 模型空间以像素（1/16 格）为单位，y 向下，背后为 +z，原点在身体部件的轴心（脖子处）。
 */
final class BodyPose {
    private final Vec3 position;
    private final float bodyYaw;
    private final float pitch;
    private final float bank;
    private final float roll;
    private final float scale;
    private final Matrix4f pixels;

    private BodyPose(Vec3 position, float bodyYaw, float pitch, float bank, float roll, float scale) {
        this.position = position;
        this.bodyYaw = bodyYaw;
        this.pitch = pitch;
        this.bank = bank;
        this.roll = roll;
        this.scale = scale;
        this.pixels = new Matrix4f()
                .translation((float) position.x, (float) position.y, (float) position.z)
                .rotateY((float) Math.toRadians(180.0F - bodyYaw))
                .rotateX((float) Math.toRadians(pitch))
                .rotateY(bank)
                .rotateY((float) Math.toRadians(roll))
                .scale(-scale, -scale, scale)
                .translate(0.0F, -1.501F, 0.0F)
                .scale(1.0F / 16.0F);
    }

    static BodyPose of(Player player, float partialTick) {
        Vec3 position = player.getPosition(partialTick);
        float bodyYaw = Mth.rotLerp(partialTick, player.yBodyRotO, player.yBodyRot);
        float pitch = 0.0F;
        float bank = 0.0F;
        float roll = 0.0F;
        if (player.isFallFlying()) {
            float ticks = player.getFallFlyingTicks() + partialTick;
            float blend = Mth.clamp(ticks * ticks / 100.0F, 0.0F, 1.0F);
            pitch = blend * (-90.0F - player.getViewXRot(partialTick));
            // Vanilla banks the gliding body toward the direction it is actually moving.
            Vec3 view = player.getViewVector(partialTick);
            Vec3 motion = player.getDeltaMovement();
            double motionSqr = motion.horizontalDistanceSqr();
            double viewSqr = view.horizontalDistanceSqr();
            if (motionSqr > 0.0D && viewSqr > 0.0D) {
                double cos = (motion.x * view.x + motion.z * view.z) / Math.sqrt(motionSqr * viewSqr);
                double side = motion.x * view.z - motion.z * view.x;
                bank = (float) (Math.signum(side) * Math.acos(Mth.clamp(cos, -1.0D, 1.0D)));
            }
            roll = KineticFlightClient.superFlightPlayerRoll(player, partialTick);
        }
        return new BodyPose(position, bodyYaw, pitch, bank, roll, 0.9375F * player.getScale());
    }

    /** A point given in model pixels, in world space. */
    Vec3 point(float x, float y, float z) {
        Vector3f out = pixels.transformPosition(x, y, z, new Vector3f());
        return new Vec3(out.x, out.y, out.z);
    }

    /** Pushes the body transform onto a pose stack relative to {@code origin}, in blocks as model parts expect. */
    void apply(PoseStack stack, Vec3 origin) {
        stack.translate(position.x - origin.x, position.y - origin.y, position.z - origin.z);
        stack.mulPose(Axis.YP.rotationDegrees(180.0F - bodyYaw));
        stack.mulPose(Axis.XP.rotationDegrees(pitch));
        stack.mulPose(Axis.YP.rotation(bank));
        stack.mulPose(Axis.YP.rotationDegrees(roll));
        stack.scale(-scale, -scale, scale);
        stack.translate(0.0F, -1.501F, 0.0F);
    }
}
