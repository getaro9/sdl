package sdl;

import java.security.ProtectionDomain;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.expr.ExprEditor;
import sdl.intercepter.javassist.InsertLogReplaceEditor;

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
    ExprEditor editor = new InsertLogReplaceEditor(); //自作の変更用クラス
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
}
