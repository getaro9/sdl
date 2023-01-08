package sdl.intercepter.javassist;

import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

public class MethodReplaceEditor extends ExprEditor {

  /**
   * メソッド呼び出しのときに呼ばれる
   */
  @Override
  public void edit(MethodCall m) throws CannotCompileException {

    // 呼び出すメソッド名がhelloWorldだったら呼び出し内容をhelloJavassistメソッドに変更する
    String name = m.getMethodName();
    if (name.equals("helloWorld")) {
      //      m.replace("$0.helloJavassist($$)");
      m.replace("$_ = $0.helloJavassist();");
    }
  }

}
