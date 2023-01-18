package sdl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import sdl.intercepter.asm.TransformerSample;

public class Main {

  public static void main(String[] args) throws Exception {

    // Javassitでメソッドを変更して登録
    LogJavassist.convert("sdl.LogTarget");
    LogTarget logTarget = new LogTarget();
    logTarget.helloWorldLoop();

    // ASMでメソッドを新規追加
    byte[] bytes = TransformerSample
        .transform(Files
            .readAllBytes(
                Path.of("C:/Users/TAKAHIRO/git/sdl/target/classes/sdl/intercepter/asm/AsmSampleClass.class")));

    // byte[]配列をFileに保存
    Path path = Paths.get("sdl/intercepter/asm/AsmSampleClass.class");
    Files.write(path, bytes);

    // byte[]配列をインスタンス化して登録
    LogJavassist.generateNewClass(bytes);
  }

}
