package sdl.intercepter.asm;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class CheckLoops {

  public byte[] checkForLoops(Path classFile) {

    try (InputStream inputStream = Files.newInputStream(classFile)) {
      return checkForLoops(inputStream);

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public byte[] checkForLoops(InputStream inputStream) {

    try {
      ClassReader cr = new ClassReader(inputStream);

      // 読み取ったクラス内容をもとにクラス作成クラスを作成するみたい
      final ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);

      ClassNode cn = new LoopClassNode(cw);
      ClassVisitor classVisitor = new LoopClassVisitor(cn);

      cr.accept(classVisitor, ClassReader.EXPAND_FRAMES);

      return cw.toByteArray();

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static class LoopClassNode extends ClassNode {

    public LoopClassNode(ClassVisitor cv) {
      super(Opcodes.ASM9);
      this.cv = cv;
    }

    // Node系APIはこのメソッドをオーバライドしないとダメっぽい
    @Override
    public void visitEnd() {
      // 親クラスのメソッド実装を呼び出す(実際の状況に応じて、保持または削除を選択する)
      super.visitEnd();
      // 後続のMethodVisitorに渡す
      if (cv != null) {
        accept(cv);
      }
    }
  }

  public class LoopClassVisitor extends ClassVisitor {

    public LoopClassVisitor(ClassNode cn) {
      super(Opcodes.ASM9, cn);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {

      MethodVisitor mv1 = super.visitMethod(access, name, descriptor, signature, exceptions);
      MethodVisitor mv2 = new LoopMethodNode(access, name, descriptor, signature, exceptions, mv1);
      MethodVisitor mv3 = new LoopMethodVisitor(mv2, access, name, descriptor, signature, exceptions);

      return mv3;
    }
  }

  public static class LoopMethodNode extends MethodNode {

    public LoopMethodNode(int access, String name, String descriptor, String signature, String[] exceptions,
        MethodVisitor mv) {
      super(Opcodes.ASM9, access, name, descriptor, signature, exceptions);
      this.mv = mv;
    }

    @Override
    public void visitEnd() {
      // 独自の処理は下記のように書く
      // var insList = ((MethodNode) mv).instructions;
      // InsnList il = new InsnList();
      // il.add(new FieldInsnNode(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;"));
      // il.add(new LdcInsnNode("Method Enter"));
      // il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false));
      // insList.add(il);

      // 親クラスのメソッド実装を呼び出す(実際の状況に応じて、保持または削除を選択する)
      super.visitEnd();

      // 後続のMethodVisitorに渡す
      if (mv != null) {
        accept(mv);
      }
    }
  }

  public class LoopMethodVisitor extends GeneratorAdapter {

    private List<Label> visitedLabels;
    private List<AbstractMap.SimpleEntry<Label, Integer>> visitedLinLabelPairs;

    public LoopMethodVisitor(MethodVisitor delegate, int access, String name, String descriptor, String signature,
        String[] exceptions) {
      super(Opcodes.ASM9, delegate, access, name, descriptor);
      visitedLabels = new ArrayList<>();
      visitedLinLabelPairs = new ArrayList<SimpleEntry<Label, Integer>>();
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
        // System.out.println("Op: " + opcode + ", GOTO to previous command - possible looped execution");

        InsnList insList = ((MethodNode) mv).instructions;
        var insStream = StreamSupport.stream(insList.spliterator(), false);

        // ジャンプ先の命令をメソッド内の命令群から探している
        var startLabel = insStream
            .filter(ins -> ins instanceof LineNumberNode)
            .filter(ins -> ((LineNumberNode) ins).line == visitedLinLabelPairs.get(visitedLinLabelPairs.size() - 1).getValue())
            .findFirst();

        // ループスタートのログ出力
        if (startLabel.isPresent()) {
          InsnList il = createLoopStartInstruction();
          // ジャンプ先の命令の前にログ出力命令を追加
          insList.insertBefore(startLabel.get(), il);
        }

        super.visitJumpInsn(opcode, label);
        // ループエンドのログ出力
        addLoopEndInstruction();
      } else {
        super.visitJumpInsn(opcode, label);
      }
    }

    private InsnList createLoopStartInstruction() {
      InsnList il = new InsnList();
      il.add(new LineNumberNode(11, new LabelNode()));
      il.add(new FieldInsnNode(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;"));
      il.add(new LdcInsnNode("Loop Start"));
      il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false));
      return il;
    }

    private void addLoopEndInstruction() {
      super.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
      super.visitLdcInsn("Loop End");
      super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
    }

    // 不要な処理だけど、下記コメントを残すため
    @Override
    public void visitEnd() {
      // InsnListに対してここで処理しても影響しないっぽい
      super.visitEnd();
    }
  }

}
