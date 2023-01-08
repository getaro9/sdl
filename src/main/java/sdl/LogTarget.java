package sdl;

public class LogTarget {

  public void helloWorldLoop() {

    int i = 0;
    while (i < 3) {
      System.out.println(helloWorld());
      i++;
    }

  }

  private String helloWorld() {
    return "Hello World";
  }

  private String helloJavassist() {
    return "Hello Javassist";
  }
}
