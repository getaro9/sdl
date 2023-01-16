package sdl.intercepter.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.GeneratorAdapter;

public class TransformerSample extends ClassVisitor {

  protected TransformerSample(int api, ClassVisitor cv) {
    super(api, cv);
  }

  public static byte[] transform(byte[] b) {
    // クラス構造を解析し、アクセスできるようにするパーサー
    final ClassReader classReader = new ClassReader(b);

    // 読み取ったクラス内容をもとにクラス作成クラスを作成するみたい
    final ClassWriter cw = new ClassWriter(classReader,
        ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

    // クラス構造にアクセスするClassVisitorを指定
    classReader.accept(new TransformerSample(Opcodes.ASM9, cw), ClassReader.EXPAND_FRAMES);

    return cw.toByteArray();
  }


  @Override
  public MethodVisitor visitMethod(int access, String name, String desc,
      String signature, String[] exceptions) {

    MethodVisitor v = super.visitMethod(access, name, desc, signature, exceptions);

    if (name.equals("main") && desc.equals("([Ljava/lang/String;)V")) {
      v = new MainTransformer(v, access, name, desc, signature, exceptions);
    }

    return v;
  }

  @Override
  public void visitEnd() {
    appendShowTwo();
    super.visitEnd();
  }

  private void appendShowTwo() {
    final MethodVisitor defVisitor = super.visitMethod(
        Opcodes.ACC_PUBLIC, "showTwo", "()V", null, null);

    defVisitor.visitCode();
    defVisitor.visitFieldInsn(Opcodes.GETSTATIC,
        "java/lang/System", "out", "Ljava/io/PrintStream;");
    defVisitor.visitLdcInsn("Show Two Method");
    defVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
        "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
    defVisitor.visitInsn(Opcodes.RETURN);
    defVisitor.visitMaxs(0, 0);
    defVisitor.visitEnd();
  }

  class MainTransformer extends GeneratorAdapter {

    MainTransformer(MethodVisitor delegate, int access, String name, String desc,
        String signature, String[] exceptions) {
      super(Opcodes.ASM9, delegate, access, name, desc);
    }

    @Override
    public void visitInsn(int opcode) {
      if (opcode == Opcodes.RETURN) {
        // before return insert c.showTwo();
        super.visitVarInsn(Opcodes.ALOAD, 1); // variable c
        super.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
            "AsmSampleClass", "showTwo", "()V", false);
      }
      super.visitInsn(opcode);
    }
  }

}
