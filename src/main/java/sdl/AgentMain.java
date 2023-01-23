package sdl;

import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        // 設定ファイル読み込みする前にこれだけは除外しておかないとデバッグ実行時にClassCircularityErrorが発生
        if (className.matches("^jdk/internal/loader/URLClassPath.*")
            || className.matches("^jdk/internal/loader/FileURLMapper.*")
            || className.matches("^java/security/PrivilegedExceptionAction.*")
            || className.matches("^sun/net/www/protocol/jar/Handler.*")) {
          return null;
        }

        // 設定ファイルを読み込んで、除外するパッケージ・クラスのパターンを取得
        Optional<Properties> opProperties = readConfigFile();
        Optional<Pattern> excludePackagesPattern = opProperties
            .map(properties -> properties.getProperty("exclude_packages"))
            .map(excludePackagesStr -> excludePackagesStr.replaceAll(", ", "|"))
            .map(excludePackagesStrRe -> Pattern.compile(excludePackagesStrRe));

        // 除外対象のパッケージ・クラスかを判定
        if (excludePackagesPattern.isPresent()) {
          Matcher matcher = excludePackagesPattern.get().matcher(className);
          if (matcher.matches()) {
            return null;
          }
        }

        try {
          // ループログ出力するように書き換え
          return CheckLoops.checkForLoops(classfileBuffer);

        } catch (Exception ex) {
          IllegalClassFormatException e = new IllegalClassFormatException();
          e.initCause(ex);
          throw e;
        }

      }
    });
  }

  private static Optional<Properties> readConfigFile() {

    Properties properties = new Properties();

    var url = AgentMain.class.getClassLoader().getResource("sdl.properties");
    if (url == null) {
      return Optional.empty();
    }

    try (InputStream inputStream = url.openStream()) {
      properties.load(inputStream);
    } catch (IOException e) {
      e.printStackTrace();
    }

    return Optional.of(properties);
  }

}
