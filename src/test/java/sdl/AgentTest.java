package sdl;

import org.junit.jupiter.api.Test;

public class AgentTest {

  @Test
  void agentTest() {
    System.out.println("TEST");

    LogLoopTarget logLoopTarget = new LogLoopTarget();
    logLoopTarget.helloWorldLoop();
  }

}
