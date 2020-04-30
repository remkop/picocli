package picocli.test;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

public class UsageSplitExecution {

    @Command(name = "UsageSplit")
    static class UsageSplitCommand implements Runnable {

      @Option(names = "v", split = "\\|", usageSplit = "|")
      String args[] = {};
      @Override
      public void run() {

      }
    }

    public static void main (String []args) {
      CommandLine commandLine = new CommandLine(new UsageSplitCommand());
      commandLine.usage(commandLine.getOut());
    }
}
