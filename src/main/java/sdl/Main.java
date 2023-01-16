package sdl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import sdl.intercepter.asm.TransformerSample;

public class Main {

  public static void main(String[] args) throws Exception {

    LogJavassist.convert("sdl.LogTarget");
    LogTarget logTarget = new LogTarget();
    logTarget.helloWorldLoop();
    
    byte[] bytes = TransformerSample
        .transform(Files
            .readAllBytes(Path.of("C:/Users/TAKAHIRO/git/sdl/target/classes/sdl/intercepter/asm/AsmSampleClass.class")));
    Path path = Paths.get("C:/Users/TAKAHIRO/git/sdl/AsmSampleClass.class");
    Files.write(path, bytes);
  }

}
