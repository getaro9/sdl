package sdl.intercepter.javassist;

import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

public class InsertLogReplaceEditor extends ExprEditor {

  /**
   * メソッド呼び出しのときに呼ばれる
   */
  @Override
  public void edit(MethodCall m) throws CannotCompileException {

    // 呼び出すメソッド名がhelloWorldだったら呼び出し時に文字列を出力する
    String name = m.getMethodName();
    if (name.equals("helloWorld")) {
      m.replace("""
              {
                System.out.println(\"** method before **\");
                $_ = $proceed($$);
                System.out.println(\"** method after **\");
              }
          """);
    }
  }

}
