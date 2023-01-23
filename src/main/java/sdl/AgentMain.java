package sdl;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

import sdl.intercepter.asm.CheckLoops;

public class AgentMain {

  public static void premain(String agentArgs, Instrumentation instrumentation) {

    System.out.println("Run SdlAgent");

    instrumentation.addTransformer(new ClassFileTransformer() {

      public byte[] transform(
          ClassLoader loader,
          String className,
          Class<?> classBeingRedefined,
          ProtectionDomain protectionDomain,
          byte[] classfileBuffer) throws IllegalClassFormatException {

        // ひとまず除外するパッケージ
        if (!className.matches("^jdk/.*")
            && !className.matches("^java/.*")
            && !className.matches("^javax/.*")
            && !className.matches("^sun/.*")
            && !className.matches("^org/junit/.*")
            && !className.matches("^org/apiguardian/.*")
            && !className.matches("^org/opentest4j/.*")
            && !className.matches("^org/eclipse/jdt/.*")) {
          try {
            // ループログ出力するように書き換え
            return CheckLoops.checkForLoops(classfileBuffer);

          } catch (Exception ex) {
            IllegalClassFormatException e = new IllegalClassFormatException();
            e.initCause(ex);
            throw e;
          }
        } else {
          return null;
        }
      }
    });
  }

}
