package sdl;

import java.security.ProtectionDomain;

import javassist.ClassPool;
import javassist.CtClass;
import sdl.intercepter.javassist.MethodReplaceEditor;

public class LogJavassist {

  /**
   * クラスの内容を変更する。
   *
   * @param className 変更したいクラスの名前
   */
  public static void convert(String className) throws Exception {

    // 変更したいクラスを見つける
    ClassPool classPool = ClassPool.getDefault();
    CtClass cc = classPool.get(className);

    // 変更用ルーチンを呼び出す
    MethodReplaceEditor editor = new MethodReplaceEditor(); //自作の変更用クラス
    cc.instrument(editor);

    // クラスローダーに登録する
    Class<?> thisClass = LogJavassist.class.getClass();
    ClassLoader loader = thisClass.getClassLoader();
    ProtectionDomain domain = thisClass.getProtectionDomain();
    cc.toClass(loader, domain);
  }
}
