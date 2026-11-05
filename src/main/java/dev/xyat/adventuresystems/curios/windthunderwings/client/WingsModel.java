package dev.xyat.adventuresystems.curios.windthunderwings.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.xyat.adventuresystems.curios.CuriosModule;
import dev.xyat.kineticcore.api.resource.KineticResourceIds;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * 风雷翅的三维模型，按动画里雷鹏骨翅的样子做成一对大鸟的翅膀。每侧一根三段翼骨（肩、肘、腕）；腕上十根初级飞羽，每根分
 * 根部与梢部两节，梢部微微后弯，越往外越长越张开，末端分开如指；肘上十根宽阔的次级飞羽；肩上五根三级飞羽；飞羽之上叠着
 * 大覆羽与小覆羽两层，腕前还有两根小翼羽。羽片宽 6 像素、彼此交叠、内外羽片不对称，左右翅镜像。
 * <p>扇动按大鸟的节奏：下扑快（四成周期）、上抬慢；上抬时肘与腕收拢、初级飞羽扭开透风，下扑时整片羽扇张开。羽毛随风轻颤，
 * 飞得越快颤得越明显。悬停时双翼在背后上下扇动；平飞时双翼上扬滑翔，扇得更慢更轻。起飞时从背后生长展开，落地时收拢。
 * 坐标为玩家模型空间（像素，y 向下，背后为 +z，+x 为玩家左侧）。
 */
final class WingsModel {
    static final net.minecraft.resources.ResourceLocation TEXTURE =
            KineticResourceIds.of(CuriosModule.MODID, "textures/entity/wind_thunder_wings.png");
    static final net.minecraft.resources.ResourceLocation GLOW =
            KineticResourceIds.of(CuriosModule.MODID, "textures/entity/wind_thunder_wings_glow.png");
    /** Flap phase per tick (radians): about 1.4 s a beat when hovering, 2.2 s in level flight. */
    static final double HOVER_RATE = Math.PI * 2.0D / 28.0D;
    static final double GLIDE_RATE = Math.PI * 2.0D / 44.0D;
    /** Share of the cycle spent on the downstroke. */
    private static final double DOWNSTROKE = 0.4D;
    private static final int FULL_BRIGHT = 0xF000F0;
    private static final String[] SIDES = {"left", "right"};
    /** Primaries are two 12-pixel segments; the others are one blade of their kind's length. */
    private static final int PRIMARY_SEGMENT = 12;
    private static final int FEATHER_WIDTH = 6;

    private enum Bone { SHOULDER, ELBOW, WRIST }

    /** Each kind's texture column (declared units: the front face, and the back face beside it) and length. */
    private enum Kind {
        PRIMARY(0, 24), SECONDARY(12, 20), TERTIAL(24, 16), GREATER_COVERT(36, 12), LESSER_COVERT(48, 8), ALULA(48, 8);

        final int column;
        final int length;

        Kind(int column, int length) {
            this.column = column;
            this.length = length;
        }
    }

    /** One feather's place: along the bone (px), size, splay out toward the tip (degrees), depth (px, + is on top), lean. */
    private record Spec(Kind kind, Bone bone, float along, float scale, float splay, float depth, float lean) {
    }

    /** A built feather: its part (and the tip segment of a primary), side, layout and a seed for its flutter. */
    private record Feather(ModelPart part, ModelPart tip, float side, Spec spec, float seed) {
    }

    private static final List<Spec> LAYOUT = layout();
    private static final List<Feather> FEATHERS = new ArrayList<>();
    private static ModelPart root;

    private WingsModel() {
    }

    private static List<Spec> layout() {
        List<Spec> specs = new ArrayList<>();
        float[] primaryScale = {0.8F, 0.86F, 0.92F, 0.98F, 1.03F, 1.07F, 1.1F, 1.1F, 1.04F, 0.94F};
        for (int i = 0; i < 10; i++) {
            specs.add(new Spec(Kind.PRIMARY, Bone.WRIST, 0.2F + i * 0.95F, primaryScale[i], 8.0F + i * 6.0F,
                    i % 2 == 0 ? 0.0F : -0.05F, 1.0F - i * 0.04F));
        }
        for (int i = 0; i < 10; i++) {
            specs.add(new Spec(Kind.SECONDARY, Bone.ELBOW, 0.4F + i * 0.98F, 0.9F + i * 0.012F, i * 0.9F,
                    i % 2 == 0 ? 0.0F : -0.05F, 1.0F));
        }
        for (int i = 0; i < 5; i++) {
            specs.add(new Spec(Kind.TERTIAL, Bone.SHOULDER, 1.0F + i * 1.8F, 0.86F + i * 0.035F, -8.0F + i * 2.0F, 0.12F, 0.75F));
        }
        for (int i = 0; i < 8; i++) {
            specs.add(new Spec(Kind.GREATER_COVERT, Bone.ELBOW, 0.6F + i * 1.2F, 0.95F, 2.0F + i * 0.8F, 0.3F, 0.85F));
        }
        for (int i = 0; i < 6; i++) {
            specs.add(new Spec(Kind.GREATER_COVERT, Bone.WRIST, 0.4F + i * 1.4F, 0.95F, 12.0F + i * 7.0F, 0.3F, 0.85F));
        }
        for (int i = 0; i < 7; i++) {
            specs.add(new Spec(Kind.LESSER_COVERT, Bone.ELBOW, 0.8F + i * 1.35F, 1.0F, 2.0F + i, 0.48F, 0.6F));
        }
        for (int i = 0; i < 4; i++) {
            specs.add(new Spec(Kind.LESSER_COVERT, Bone.WRIST, 0.5F + i * 2.0F, 1.0F, 10.0F + i * 10.0F, 0.48F, 0.6F));
            specs.add(new Spec(Kind.LESSER_COVERT, Bone.SHOULDER, 1.0F + i * 2.0F, 0.95F, -4.0F + i * 2.0F, 0.48F, 0.5F));
        }
        for (int i = 0; i < 2; i++) {
            specs.add(new Spec(Kind.ALULA, Bone.WRIST, 0.2F + i * 0.8F, 0.8F - i * 0.12F, -22.0F + i * 8.0F, 0.55F, 0.4F));
        }
        return specs;
    }

    private static ModelPart root() {
        if (root == null) {
            MeshDefinition mesh = new MeshDefinition();
            PartDefinition parts = mesh.getRoot();
            wing(parts, 0, 1.0F);
            wing(parts, 1, -1.0F);
            root = LayerDefinition.create(mesh, 64, 64).bakeRoot();
            for (int side = 0; side < 2; side++) {
                float s = side == 0 ? 1.0F : -1.0F;
                for (int index = 0; index < LAYOUT.size(); index++) {
                    Spec spec = LAYOUT.get(index);
                    ModelPart part = bone(side, spec.bone()).getChild(featherName(side, index));
                    ModelPart tip = spec.kind() == Kind.PRIMARY ? part.getChild("tip") : null;
                    FEATHERS.add(new Feather(part, tip, s, spec, (index * 0.618F + side * 0.37F) % 1.0F));
                }
            }
        }
        return root;
    }

    private static ModelPart bone(int side, Bone bone) {
        ModelPart shoulder = root.getChild(SIDES[side] + "_shoulder");
        if (bone == Bone.SHOULDER) return shoulder;
        ModelPart elbow = shoulder.getChild(SIDES[side] + "_elbow");
        return bone == Bone.ELBOW ? elbow : elbow.getChild(SIDES[side] + "_wrist");
    }

    private static String featherName(int side, int index) {
        return SIDES[side] + "_feather" + index;
    }

    private static void wing(PartDefinition parts, int side, float s) {
        String name = SIDES[side];
        PartDefinition shoulder = parts.addOrReplaceChild(name + "_shoulder", boneBox(9.0F, 2.0F, 1.4F, s),
                PartPose.offset(2.0F * s, 2.0F, 2.6F));
        PartDefinition elbow = shoulder.addOrReplaceChild(name + "_elbow", boneBox(10.0F, 1.6F, 1.2F, s),
                PartPose.offset(9.0F * s, 0.0F, 0.0F));
        PartDefinition wrist = elbow.addOrReplaceChild(name + "_wrist", boneBox(9.0F, 1.2F, 1.0F, s),
                PartPose.offset(10.0F * s, 0.0F, 0.0F));
        for (int index = 0; index < LAYOUT.size(); index++) {
            Spec spec = LAYOUT.get(index);
            PartDefinition bone = switch (spec.bone()) {
                case SHOULDER -> shoulder;
                case ELBOW -> elbow;
                case WRIST -> wrist;
            };
            PartPose pose = PartPose.offset(spec.along() * s, 0.3F, spec.depth());
            if (spec.kind() == Kind.PRIMARY) {
                PartDefinition base = bone.addOrReplaceChild(featherName(side, index), blade(spec.kind().column, 0, PRIMARY_SEGMENT, s), pose);
                base.addOrReplaceChild("tip", blade(spec.kind().column, PRIMARY_SEGMENT, PRIMARY_SEGMENT, s),
                        PartPose.offset(0.0F, PRIMARY_SEGMENT, 0.0F));
            } else {
                bone.addOrReplaceChild(featherName(side, index), blade(spec.kind().column, 0, spec.kind().length, s), pose);
            }
        }
    }

    private static CubeListBuilder boneBox(float length, float height, float thickness, float s) {
        return CubeListBuilder.create().texOffs(0, 40)
                .addBox(s > 0 ? 0.0F : -length, -height / 2.0F, -thickness / 2.0F, length, height, thickness);
    }

    /** A flat feather blade hanging from its root; the right wing's blades are mirrored so both wings match. */
    private static CubeListBuilder blade(int column, int row, int length, float s) {
        return CubeListBuilder.create().texOffs(column, row).mirror(s < 0)
                .addBox(-FEATHER_WIDTH / 2.0F, 0.0F, 0.0F, FEATHER_WIDTH, length, 0.0F);
    }

    /** Wing elevation over one beat: +1 at the top, -1 at the bottom; a quick downstroke and a slow upstroke. */
    static double stroke(double phase) {
        double u = phase / (Math.PI * 2.0D);
        u -= Math.floor(u);
        if (u < DOWNSTROKE) {
            return Math.cos(Math.PI * u / DOWNSTROKE);
        }
        return -Math.cos(Math.PI * (u - DOWNSTROKE) / (1.0D - DOWNSTROKE));
    }

    /** How folded the hand is: 0 through the downstroke, rising to 1 in the middle of the upstroke. */
    private static double fold(double phase) {
        double u = phase / (Math.PI * 2.0D);
        u -= Math.floor(u);
        return u < DOWNSTROKE ? 0.0D : Math.sin(Math.PI * (u - DOWNSTROKE) / (1.0D - DOWNSTROKE));
    }

    /**
     * Poses the wings for one wearer. Upright (hovering) the wings beat up and down behind the back; in the flight pose
     * the body lies forward, so the same beat turns on the other axis and the wings are held up in a soaring dihedral.
     * {@code glide} blends the two. Folded ({@code spread} 0) the wings lie small against the back.
     */
    static void animate(WingsClient.Wearer wearer, float partialTick) {
        ModelPart model = root();
        float spread = Mth.lerp(partialTick, wearer.previousSpread, wearer.spread);
        float glide = Mth.lerp(partialTick, wearer.previousGlide, wearer.glide);
        double phase = Mth.lerp(partialTick, wearer.previousFlapPhase, wearer.flapPhase);
        double power = spread * Mth.lerp(glide, 1.0F, 0.55F);
        double beat = stroke(phase) * power;
        double lagElbow = stroke(phase - 0.55D) * power;
        double lagWrist = stroke(phase - 1.1D) * power;
        double hand = fold(phase) * power;
        // Upright: z turns the wing up and down (seen from behind), y sweeps the tips back.
        // Lying forward: y turns the wing up and down, z sweeps it toward the head or the feet.
        // Hovering the arm stays above the horizontal: raised high at the top of the beat, level at the bottom, swept back
        // on the upstroke and forward on the downstroke.
        double hoverLift = 0.6D + 0.3D * beat;
        double hoverSweep = 0.45D - 0.2D * beat;
        double glideLift = 0.34D + 0.42D * beat;
        double glideSweep = 0.1D - 0.06D * beat;
        float lift = (float) Mth.lerp(glide, hoverLift, glideSweep);
        float sweep = (float) Mth.lerp(glide, hoverSweep, glideLift);
        float size = 0.25F + 0.75F * Mth.clamp(spread * 1.3F, 0.0F, 1.0F);
        for (int side = 0; side < 2; side++) {
            float s = side == 0 ? 1.0F : -1.0F;
            ModelPart shoulder = model.getChild(SIDES[side] + "_shoulder");
            ModelPart elbow = shoulder.getChild(SIDES[side] + "_elbow");
            ModelPart wrist = elbow.getChild(SIDES[side] + "_wrist");
            shoulder.xScale = size;
            shoulder.yScale = size;
            shoulder.zScale = size;
            // Folded: hanging down along the back, tips tucked behind.
            shoulder.zRot = -s * Mth.lerp(spread, -1.25F, lift);
            shoulder.yRot = -s * Mth.lerp(spread, 0.15F, sweep);
            // "Up" is the joint's up-and-down turn (with the trailing lag), "back" folds the hand toward the tail.
            // Upright, the arm arches up and the hand curves down at the tip; lying forward the wing stays flatter.
            float elbowUp = Mth.lerp(spread, 0.4F, Mth.lerp(glide, 0.06F, -0.08F)) + (float) ((lagElbow - beat) * 0.3D);
            float wristUp = Mth.lerp(spread, 0.35F, Mth.lerp(glide, -0.3F, -0.2F)) + (float) ((lagWrist - lagElbow) * 0.4D);
            float elbowBack = (float) (hand * 0.35D);
            float wristBack = (float) (hand * 0.55D);
            // Lying forward, folding toward the feet is a negative turn about z.
            elbow.zRot = -s * Mth.lerp(glide, elbowUp, -elbowBack * 0.7F);
            elbow.yRot = -s * Mth.lerp(glide, elbowBack, elbowUp);
            wrist.zRot = -s * Mth.lerp(glide, wristUp, -wristBack * 0.7F);
            wrist.yRot = -s * Mth.lerp(glide, wristBack, wristUp);
        }
        // Feathers lean back from the bone so the wing has depth; on the upstroke the primaries twist open to let air
        // through, on the downstroke the whole hand fans out. Every feather trembles a little in the wind.
        double fan = 1.0D + 0.18D * Math.max(0.0D, -beat) - 0.12D * hand;
        float lean = Mth.lerp(glide, 0.55F, 0.08F);
        double seconds = (wearer.flapTicks + partialTick) / 20.0D;
        double wind = 0.006D + 0.022D * Math.min(1.0D, wearer.speed / 1.2D);
        for (Feather feather : FEATHERS) {
            Spec spec = feather.spec();
            boolean primary = spec.kind() == Kind.PRIMARY;
            double flutter = Math.sin(seconds * (13.0D + 7.0D * feather.seed()) + feather.seed() * 40.0D) * wind;
            ModelPart part = feather.part();
            part.zRot = (float) (-feather.side() * Math.toRadians(spec.splay() * fan));
            part.xRot = (float) (lean * spec.lean() + (primary ? hand * 0.2D : 0.0D) + flutter * 0.5D);
            part.yRot = primary ? (float) (-feather.side() * hand * 0.1D) : 0.0F;
            part.yScale = spec.scale();
            if (feather.tip() != null) {
                // The outer half of a primary bends back under the air, more in level flight and on the downstroke.
                feather.tip().xRot = (float) (0.1D + 0.12D * glide + 0.1D * Math.max(0.0D, -beat) + flutter * 1.6D);
                feather.tip().zRot = (float) (-feather.side() * Math.toRadians(3.0D + spec.splay() * 0.08D));
            }
        }
    }

    /**
     * Draws the posed wings: solid feathers in the wing colour under the world's light at {@code alpha}, then the
     * shafts and veins glowing in {@code glowRgb} at {@code glowStrength}.
     */
    static void render(PoseStack stack, MultiBufferSource.BufferSource buffers, int wingRgb, int glowRgb, int packedLight,
                       float glowStrength, float alpha) {
        ModelPart model = root();
        //? if >=26.1 {
        /*// 26.1's eyes type blends translucently; the energy swirl (charged creeper) is the additive one.
        var base = net.minecraft.client.renderer.rendertype.RenderTypes.entityTranslucent(TEXTURE);
        var glow = net.minecraft.client.renderer.rendertype.RenderTypes.energySwirl(GLOW, 0.0F, 0.0F);
        *///?} else {
        var base = net.minecraft.client.renderer.RenderType.entityTranslucent(TEXTURE);
        var glow = net.minecraft.client.renderer.RenderType.eyes(GLOW);
        //?}
        draw(model, stack, buffers.getBuffer(base), wingRgb, packedLight, alpha);
        buffers.endBatch(base);
        draw(model, stack, buffers.getBuffer(glow), scale(glowRgb, glowStrength), FULL_BRIGHT, 1.0F);
        buffers.endBatch(glow);
    }

    private static int scale(int rgb, float k) {
        int r = (int) Math.min(255.0F, ((rgb >> 16) & 0xFF) * k);
        int g = (int) Math.min(255.0F, ((rgb >> 8) & 0xFF) * k);
        int b = (int) Math.min(255.0F, (rgb & 0xFF) * k);
        return (r << 16) | (g << 8) | b;
    }

    private static void draw(ModelPart model, PoseStack stack, VertexConsumer consumer, int rgb, int light, float alpha) {
        //? if >=1.21 {
        /*model.render(stack, consumer, light, OverlayTexture.NO_OVERLAY, ((int) (alpha * 255.0F) << 24) | (rgb & 0xFFFFFF));
        *///?} else {
        model.render(stack, consumer, light, OverlayTexture.NO_OVERLAY,
                ((rgb >> 16) & 0xFF) / 255.0F, ((rgb >> 8) & 0xFF) / 255.0F, (rgb & 0xFF) / 255.0F, alpha);
        //?}
    }

    /** The tips of one posed wing's primaries and secondaries in world space, wingtip first. Side 0 is the wearer's left. */
    static Vec3[] featherTips(BodyPose pose, int side) {
        root();
        List<Vec3> tips = new ArrayList<>();
        for (int pass = 0; pass < 2; pass++) {
            Kind kind = pass == 0 ? Kind.PRIMARY : Kind.SECONDARY;
            for (int index = LAYOUT.size() - 1; index >= 0; index--) {
                Spec spec = LAYOUT.get(index);
                if (spec.kind() != kind) continue;
                PoseStack stack = new PoseStack();
                pose.apply(stack, Vec3.ZERO);
                ModelPart shoulder = root.getChild(SIDES[side] + "_shoulder");
                shoulder.translateAndRotate(stack);
                ModelPart elbow = shoulder.getChild(SIDES[side] + "_elbow");
                elbow.translateAndRotate(stack);
                ModelPart bone = elbow;
                if (spec.bone() == Bone.WRIST) {
                    bone = elbow.getChild(SIDES[side] + "_wrist");
                    bone.translateAndRotate(stack);
                }
                ModelPart part = bone.getChild(featherName(side, index));
                part.translateAndRotate(stack);
                if (kind == Kind.PRIMARY) {
                    part.getChild("tip").translateAndRotate(stack);
                    tips.add(at(stack, 0.0F, PRIMARY_SEGMENT));
                } else {
                    tips.add(at(stack, 0.0F, kind.length));
                }
            }
        }
        return tips.toArray(new Vec3[0]);
    }

    /**
     * The leading edge of one posed wing in world space (shoulder, elbow, wrist, hand end) followed by the wingtip
     * (end of the longest primaries). Side 0 is the wearer's left.
     */
    static Vec3[] edge(BodyPose pose, int side) {
        float s = side == 0 ? 1.0F : -1.0F;
        PoseStack stack = new PoseStack();
        pose.apply(stack, Vec3.ZERO);
        ModelPart shoulder = root().getChild(SIDES[side] + "_shoulder");
        ModelPart elbow = shoulder.getChild(SIDES[side] + "_elbow");
        ModelPart wrist = elbow.getChild(SIDES[side] + "_wrist");
        Vec3[] points = new Vec3[5];
        shoulder.translateAndRotate(stack);
        points[0] = at(stack, 0.0F, 0.0F);
        elbow.translateAndRotate(stack);
        points[1] = at(stack, 0.0F, 0.0F);
        wrist.translateAndRotate(stack);
        points[2] = at(stack, 0.0F, 0.0F);
        points[3] = at(stack, 9.0F * s, 0.0F);
        points[4] = at(stack, 14.0F * s, 24.0F);
        return points;
    }

    private static Vec3 at(PoseStack stack, float xPixels, float yPixels) {
        Vector3f out = stack.last().pose().transformPosition(xPixels / 16.0F, yPixels / 16.0F, 0.0F, new Vector3f());
        return new Vec3(out.x, out.y, out.z);
    }
}
