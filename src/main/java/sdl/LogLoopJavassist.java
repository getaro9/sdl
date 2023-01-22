package sdl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.ProtectionDomain;

import javassist.ClassPool;
import javassist.CtClass;
import sdl.intercepter.asm.CheckLoops;

public class LogLoopJavassist {

  /**
   * クラスの内容をJavassistで変更し、クラスローダーに登録する。
   *
   * @param className 変更したいクラスの名前
   */
  public static void convert(String className) throws Exception {

    // 変更したいクラスを見つける
    ClassPool classPool = ClassPool.getDefault();

    // CtClass cc = classPool.get(className);
    // CtClassからバイト配列を取得して、ASMを通した後だと、変更時にcheckModify()で無理なので、
    // 先にASMを通してバイト配列を取得する

    String[] splitedClsName = className.split("\\.");
    var clsName = splitedClsName[splitedClsName.length - 1];

    // ASMを通して、バイト配列を取得する
    CheckLoops checkLoops = new CheckLoops();
    byte[] bytes = checkLoops.checkForLoops(LogLoopJavassist.class.getResourceAsStream(clsName + ".class"));

    // バイト配列からJavassistでの操作用クラスを取得
    InputStream ins = new ByteArrayInputStream(bytes);
    CtClass cc = classPool.makeClass(ins);

    // クラスローダーに登録する
    Class<?> thisClass = LogLoopJavassist.class.getClass();
    ClassLoader loader = thisClass.getClassLoader();
    ProtectionDomain domain = thisClass.getProtectionDomain();
    cc.toClass(loader, domain);
    cc.writeFile(); // 変換後クラスファイルを書き出す
  }

}
