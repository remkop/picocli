package picocli.examples.arggroup;

import picocli.CommandLine;
import picocli.CommandLine.*;
import picocli.CommandLine.Model.CommandSpec;

// see https://stackoverflow.com/questions/61963756/picocli-dependent-arguments-in-arggroup
public class SO61963756 implements Runnable {

    static class MyGroupX {
        @Option(names="-A1", required=false) boolean A1;
        @Option(names="-A2", required=false) boolean A2;
    }

    static class MyGroup {
        @Option(names="-A", required=true) boolean A;
        @ArgGroup(exclusive=false, multiplicity="1") MyGroupX myGroupX;
    }

    @ArgGroup(exclusive=false) MyGroup myGroup;

    @Spec CommandSpec spec;

    @Override
    public void run() {
        System.out.printf("OK: %s%n", spec.commandLine().getParseResult().originalArgs());
    }

    public static void main(String[] args) {
        //test: these should be valid
        new CommandLine(new SO61963756()).execute();
        new CommandLine(new SO61963756()).execute("-A -A1".split(" "));
        new CommandLine(new SO61963756()).execute("-A -A2".split(" "));
        new CommandLine(new SO61963756()).execute("-A -A1 -A2".split(" "));

        //test: these should FAIL
        new CommandLine(new SO61963756()).execute("-A");
        new CommandLine(new SO61963756()).execute("-A1");
        new CommandLine(new SO61963756()).execute("-A2");
        new CommandLine(new SO61963756()).execute("-A1 -A2".split(" "));
    }
}