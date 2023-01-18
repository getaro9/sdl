package sdl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Hashtable;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.Loader;
import javassist.NotFoundException;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.Opcode;
import javassist.bytecode.analysis.ControlFlow;
import javassist.bytecode.analysis.ControlFlow.Block;
import javassist.expr.ExprEditor;
import sdl.intercepter.asm.AsmSampleClass;
import sdl.intercepter.javassist.InsertLogReplaceEditor;

public class LogJavassist {

  /**
   * クラスの内容をJavassistで変更し、クラスローダーに登録する。
   *
   * @param className 変更したいクラスの名前
   */
  public static void convert(String className) throws Exception {

    // 変更したいクラスを見つける
    ClassPool classPool = ClassPool.getDefault();
    CtClass cc = classPool.get(className);

    // 変更用ルーチンを呼び出す
    ExprEditor editor = new InsertLogReplaceEditor(); // 自作の変更用クラス
    cc.instrument(editor);

    // メソッド前後に処理を挟む
    CtMethod method = cc.getDeclaredMethod("helloWorld");
    method.insertBefore("{ System.out.println(\"-- method before --\"); }");
    method.insertAfter("{ System.out.println(\"-- method after --\"); }");

    // クラスローダーに登録する
    Class<?> thisClass = LogJavassist.class.getClass();
    ClassLoader loader = thisClass.getClassLoader();
    ProtectionDomain domain = thisClass.getProtectionDomain();
    cc.toClass(loader, domain);
    cc.writeFile(); // 変換後クラスファイルを書き出す
  }

  /**
   * ASMで作成したクラスファイルをJavassistを使用して、インスタンス化してクラスローダーに登録している。
   * 同じ完全修飾名のクラスが複数あるときのクラスローダーの扱いを理解していない。
   *
   * @param bytes
   * @throws IOException
   * @throws RuntimeException
   * @throws CannotCompileException
   * @throws InstantiationException
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   * @throws NoSuchMethodException
   * @throws NotFoundException
   * @throws ClassNotFoundException
   */
  public static void generateNewClass(byte[] bytes)
      throws IOException, RuntimeException, CannotCompileException, InstantiationException, IllegalAccessException,
      InvocationTargetException, NoSuchMethodException, NotFoundException, ClassNotFoundException {

    ClassPool cp = new ClassPool();
    SimpleClassLoader assistloader = new SimpleClassLoader(cp);

    // クラス定義が重複しないようにしたくて、JavassistのLoaderを使用している。
    // loadClass()でのクラス検索順序が親からではなく、自身(というかClassPool)から始めるので先に改変したクラスを取得できる？
    // このあたりの動きが理解できていない

    // あってもなくても動作に変わりがなかった
    // cp.appendClassPath("C:/Users/TAKAHIRO/git/sdl/sdl/");

    // クラスにメソッドを追加し、それを使用するなら、サブクラスを作成するのがよいみたい
    // CtClass parent = cp.get("sdl.intercepter.asm.AsmSampleClass");
    // CtClass proxy = cp.makeClass("sdl.intercepter.asm.AsmSampleClass$$Proxy", parent);

    // ASMで作成したクラスファイルを読み込む
    InputStream ins = new ByteArrayInputStream(bytes);
    CtClass cc = cp.makeClass(ins);
    assistloader.classNames.put(cc.getSimpleName(), cc.getName());

    // クラスの完全修飾名
    System.out.println(cc.getName());
    // 完全修飾名だと、なぜかgetDeclaredMethodsが失敗するため単純名を設定
    cc.setName("AsmSampleClass");

    // Javassistのメソッドによるメソッド取得が意図通りであることの確認
    CtMethod[] ms = cc.getDeclaredMethods();

    // Class<?> cls1 = (Class<?>) cp.toClass(cc, null, assistloader, null);
    // ccが完全修飾名のときは、JavassistのLoaderのclassesにも、sourceのclassesにも完全修飾名で登録される
    // ccが単純名のときは、JavassistのLoaderのclassesには登録されず、sourceのclassesには単純名で登録される
    Class<AsmSampleClass> cls1 = (Class<AsmSampleClass>) assistloader.loadClass("AsmSampleClass");

    // ccが完全修飾名のときでも、内部処理で単純名で探そうとするので失敗
    // ネイティブコードなので、なぜそうなるのかわからない
    // なので、しかたなくccを単純名にしている
    Method[] methods1 = cls1.getDeclaredMethods();

    // システムクラスローダーに登録して、クラスを呼び出している
    // ただし、Class<AsmSampleClass> proxyClass = (Class<AsmSampleClass>) cp.toClass(cc); と記述すると、
    // class AsmSampleClass cannot be cast to class sdl.intercepter.asm.AsmSampleClass とエラーになる
    // AsmSampleClassが別クラスとして認識されている。 仕組みを理解できていない
    Class<?> proxyClass = (Class<AsmSampleClass>) cp.toClass(cc);
    Method[] methods2 = proxyClass.getDeclaredMethods();
    // 上記の理由によりvarでないと無理
    var asmSampleClass = proxyClass.getDeclaredConstructor().newInstance();

    // // ASMによって追加されたメソッドを呼び出せることを確認
    Method method = proxyClass.getDeclaredMethod("showTwo");
    method.invoke(asmSampleClass, null);

    // Javassistのクラスローダーに登録する
    // LinkageError: attempted duplicate class definition が発生するときを理解していない
    CodeSigner[] codeSigners = null;
    CodeSource codeSource = new CodeSource(new URL("file:/C:/Users/TAKAHIRO/git/sdl/"), codeSigners);
    ProtectionDomain domain = new ProtectionDomain(codeSource, null);
    cc.toClass(assistloader, domain);
    // たぶん、上記と同じような処理？
    // Class<?> proxyClass = (Class<?>) cp.toClass(cc, null, assistloader, null);
  }

  public static class SimpleClassLoader extends Loader {

    public Hashtable<String, String> classNames = new Hashtable<String, String>();

    public SimpleClassLoader(ClassPool cp) {
      super(cp);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve)
        throws ClassFormatError, ClassNotFoundException {

      // クラスをロードするときの名前をとにかく完全修飾名にしても、
      // getDeclaredMethods0のネイティブコード内で単純名にされるので意味なかった
      if (classNames.get(name) != null) {
        name = classNames.get(name);
      }

      return super.loadClass(name, resolve);
    }
  }

}
