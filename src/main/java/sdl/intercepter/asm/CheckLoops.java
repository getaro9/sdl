package sdl.intercepter.asm;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.GeneratorAdapter;

public class CheckLoops {

  private List<LoopInfo> loopInfos;

  public byte[] checkForLoops(Path classFile) {

    try (InputStream inputStream = Files.newInputStream(classFile)) {

      ClassReader cr = new ClassReader(inputStream);

      // 読み取ったクラス内容をもとにクラス作成クラスを作成するみたい
      final ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

      LoopClassVisitor classVisitor = new LoopClassVisitor(cw);

      cr.accept(classVisitor, ClassReader.EXPAND_FRAMES);
      loopInfos = classVisitor.loopInfos;

      return cw.toByteArray();

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public byte[] checkForLoops(InputStream inputStream) {

    try {
      ClassReader cr = new ClassReader(inputStream);

      // 読み取ったクラス内容をもとにクラス作成クラスを作成するみたい
      final ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

      LoopClassVisitor classVisitor = new LoopClassVisitor(cw);

      cr.accept(classVisitor, ClassReader.EXPAND_FRAMES);
      loopInfos = classVisitor.loopInfos;

      return cw.toByteArray();

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * 対象クラスが持つループブロックをチェックして情報を返す。
   *
   * @param bytes
   * @return
   */
  public List<LoopInfo> checkForLoops(byte[] bytes) {

    ClassReader cr = new ClassReader(bytes);
    final ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
    LoopClassVisitor classVisitor = new LoopClassVisitor(cw);

    cr.accept(classVisitor, ClassReader.EXPAND_FRAMES);
    loopInfos = classVisitor.loopInfos;

    return loopInfos;
  }

  public class LoopClassVisitor extends ClassVisitor {

    List<LoopInfo> loopInfos = new ArrayList<LoopInfo>();

    public LoopClassVisitor(ClassWriter cw) {
      super(Opcodes.ASM9, cw);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
        String[] exceptions) {

      MethodVisitor v1 = super.visitMethod(access, name, descriptor, signature, exceptions);

      LoopMethodVisitor v2 = new LoopMethodVisitor(v1, access, name, descriptor, signature, exceptions, loopInfos);

      return v2;
    }

  }

  public class LoopMethodVisitor extends GeneratorAdapter {

    private List<Label> visitedLabels;
    private List<AbstractMap.SimpleEntry<Label, Integer>> visitedLinLabelPairs;
    List<LoopInfo> loopInfos;

    public LoopMethodVisitor(MethodVisitor delegate, int access, String name, String descriptor, String signature,
        String[] exceptions, List<LoopInfo> loopInfos) {
      super(Opcodes.ASM9, delegate, access, name, descriptor);
      visitedLabels = new ArrayList<>();
      visitedLinLabelPairs = new ArrayList<SimpleEntry<Label, Integer>>();
      this.loopInfos = loopInfos;
    }

    @Override
    public void visitLineNumber(final int line, final Label start) {
      // System.out.println("lnLabel: " + start.toString());

      visitedLabels.add(start);

      SimpleEntry<Label, Integer> pair = new AbstractMap.SimpleEntry<Label, Integer>(start, line);
      visitedLinLabelPairs.add(pair);

      super.visitLineNumber(line, start);
    }

    @Override
    public void visitLabel(final Label label) {
      // System.out.println("vLabel: " + label.toString());

      visitedLabels.add(label);
      super.visitLabel(label);
    }

    @Override
    public void visitJumpInsn(final int opcode, final Label label) {
      // System.out.println("Label: " + label.toString());

      if (visitedLabels.contains(label)) {
        System.out.println("Op: " + opcode + ", GOTO to previous command - possible looped execution");

        LoopInfo loopInfo = new LoopInfo(
            super.getName(),
            visitedLinLabelPairs.get(visitedLinLabelPairs.size() - 1).getValue(),
            visitedLinLabelPairs.get(visitedLinLabelPairs.size() - 2).getValue());

        loopInfos.add(loopInfo);

        super.visitJumpInsn(opcode, label);
        addLoopEndInstruction();
      } else {
        super.visitJumpInsn(opcode, label);
      }
    }

    private void addLoopEndInstruction() {
      super.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
      super.visitLdcInsn("Loop End");
      super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println",
          "(Ljava/lang/String;)V", false);
    }

  }

  public List<LoopInfo> getLoopInfos() {
    return loopInfos;
  }

  public static record LoopInfo(String methodNeme, int startLine, int endLine) {
  }

}
