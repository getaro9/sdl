package sdl;

import java.util.ArrayList;
import java.util.List;

public class LogTarget {

  public void helloWorldLoop() {

    List<String> list = new ArrayList<String>();
    list.add("a");

    for (String e : list) {
      System.out.println(e);

      String emp = "";
    }

    int i = 0;
    while (i < 3) {
      System.out.println(helloWorld());
      System.out.println(helloWorld());

      for (int ii = 0; ii < 4; ii++) {
        System.out.println(helloWorld());
      }
      i++;
    }

    while (i < 5) {
      i++;
    }

    // 対応していない: 処理がない
    for (int ii = 0; ii < 4; ii++) {
    }

    // 対応していない: i++ がブロック末尾でない
    while (i < 5) {
      System.out.println(helloWorld());
      System.out.println(helloWorld());
      i++;
      for (int ii = 0; ii < 4; ii++) {
        System.out.println(helloWorld());
      }
    }

  }

  private String helloWorld() {
    return "Hello World";
  }

  private String helloJavassist() {
    return "Hello Javassist";
  }
}
