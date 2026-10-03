import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/** Standalone tests have no game Mixin launcher. Apply only the common mixin's formatter call. */
public final class AdventureTextTestAgent {
    public static void premain(String arguments, Instrumentation instrumentation) {
        instrumentation.addTransformer(new ClassFileTransformer() {
            @Override public byte[] transform(ClassLoader loader, String name, Class<?> type,
                                               ProtectionDomain domain, byte[] bytes) {
                if (!name.equals("net/minecraft/network/chat/contents/TranslatableContents")) return null;
                ClassReader reader = new ClassReader(bytes);
                ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
                int[] patched = {0};
                reader.accept(new ClassVisitor(Opcodes.ASM9, writer) {
                    @Override public MethodVisitor visitMethod(int access, String method, String descriptor,
                                                               String signature, String[] exceptions) {
                        MethodVisitor visitor = super.visitMethod(access, method, descriptor, signature, exceptions);
                        if (!method.equals("decomposeTemplate") || !descriptor.equals("(Ljava/lang/String;Ljava/util/function/Consumer;)V")) return visitor;
                        patched[0]++;
                        return new MethodVisitor(Opcodes.ASM9, visitor) {
                            @Override public void visitCode() {
                                super.visitCode();
                                super.visitVarInsn(Opcodes.ALOAD, 0);
                                super.visitFieldInsn(Opcodes.GETFIELD, name, "key", "Ljava/lang/String;");
                                super.visitVarInsn(Opcodes.ALOAD, 1);
                                super.visitVarInsn(Opcodes.ALOAD, 0);
                                super.visitFieldInsn(Opcodes.GETFIELD, name, "args", "[Ljava/lang/Object;");
                                super.visitVarInsn(Opcodes.ALOAD, 2);
                                super.visitMethodInsn(Opcodes.INVOKESTATIC, "dev/xyat/adventuresystems/text/AdventureText",
                                        "decomposeTemplate", "(Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;Ljava/util/function/Consumer;)Z", false);
                                Label vanilla = new Label();
                                super.visitJumpInsn(Opcodes.IFEQ, vanilla);
                                super.visitInsn(Opcodes.RETURN);
                                super.visitLabel(vanilla);
                                super.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
                            }
                        };
                    }
                }, 0);
                if (patched[0] != 1) throw new IllegalStateException("Expected one vanilla translation decomposition method");
                System.setProperty("adventure.text.testAgent", "applied");
                return writer.toByteArray();
            }
        });
    }
}
