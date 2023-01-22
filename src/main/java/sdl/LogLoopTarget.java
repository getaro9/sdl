package sdl;

import java.util.ArrayList;
import java.util.List;

public class LogLoopTarget {
  public void helloWorldLoop() {

    List<String> list = new ArrayList<String>();
    list.add("a");

    for (String e : list) {
      System.out.println(e);

      String emp = "";
    }

    int i = 0;
    while (i < 3) {
      System.out.println(subLoop());
      System.out.println(subLoop());

      for (int ii = 0; ii < 4; ii++) {
        System.out.println(subLoop());
      }
      i++;
    }

    int iii = 0;
    while (iii < 5) {
      String emp = "";
      iii++;
    }
  }

  private String subLoop() {
    for (int ii = 0; ii < 2; ii++) {
      System.out.println("subLoop");
    }
    return "subLoop End";
  }

  private String helloJavassist() {
    return "Hello Javassist";
  }
}
