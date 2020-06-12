package picocli;

import org.junit.Test;

import java.util.concurrent.Callable;

import static org.junit.Assert.assertEquals;
import static picocli.CommandLine.*;

public class ConstructorInjectedOptionsTest {

  @Command
  static class TestCommandNoArgConstructor implements Callable<Integer> {
    public TestCommandNoArgConstructor() {
      System.out.println("hi");
    }
    @Override
    public Integer call() {
      return 0; // success
    }
  }

  @Test
  public void testInvocationWithCommandArgumentAsType() {
    int result = new CommandLine(TestCommandNoArgConstructor.class).execute();
    assertEquals(0, result);
  }

  @Command
  static class TestCommandWithArgConstructor implements Callable<Integer> {
    private final int num;
    public TestCommandWithArgConstructor(@Option(names = "-i") int num) {
      this.num = num;
    }

    @Override
    public Integer call() {
      return num;
    }
  }

  @Test
  public void testInvocationWithCommandArgumentAsTypeWithArg() {
    CommandLine commandLine = new CommandLine(TestCommandWithArgConstructor.class);
    int result = commandLine.execute("-i", "42");
    assertEquals(42, result);
  }
}

