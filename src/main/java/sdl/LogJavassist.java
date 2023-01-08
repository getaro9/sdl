package sdl;

import java.security.ProtectionDomain;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

public class LogJavassist extends ExprEditor {

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
    LogJavassist logJavassist = new LogJavassist(); //自作の変更用クラス
    cc.instrument(logJavassist);

    // クラスローダーに登録する
    Class<?> thisClass = LogJavassist.class.getClass();
    ClassLoader loader = thisClass.getClassLoader();
    ProtectionDomain domain = thisClass.getProtectionDomain();
    cc.toClass(loader, domain);
  }

  /**
   * メソッド呼び出しのときに呼ばれる
   */
  @Override
  public void edit(MethodCall m) throws CannotCompileException {

    // 呼び出すメソッド名がhelloWorldだったら呼び出し内容を変更する
    String name = m.getMethodName();
    if (name.equals("helloWorld")) {
      //      m.replace("$0.helloJavassist($$)");
      m.replace("$_ = \"hello javassist?\";");
    }
  }
}
