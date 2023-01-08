package sdl;

public class Main {

  public static void main(String[] args) throws Exception {

    LogJavassist.convert("sdl.LogTarget");
    LogTarget logTarget = new LogTarget();
    logTarget.helloWorldLoop();
  }

}
