package sdl.intercepter.asm;

public class AsmSampleClass {

  public void showOne() {
    System.out.println("Show One Method");
  }

  public static void main(String[] args) {
    AsmSampleClass c = new AsmSampleClass();
    c.showOne();
  }

}
