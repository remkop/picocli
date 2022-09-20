package picocli;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

// https://github.com/remkop/picocli/issues/1815
public class Issue1815Group {
    @Command(name = "groups", mixinStandardHelpOptions = true,
        description = "groups fun")
    static class CmdTest implements Callable<Integer> {

        @ArgGroup(validate = false, heading = "GROUP A%n", multiplicity = "0..1")
        private List<GroupA> a = new ArrayList<GroupA>();

//        @ArgGroup(validate = true, exclusive = false, heading = "GROUP A%n", multiplicity = "0..1")
//        private GroupA a = new GroupA();

        @ArgGroup(validate = false, heading = "GROUP B%n")
        private GroupB b = new GroupB();

        public Integer call() {
            System.out.println(a);
            System.out.println(b);
            return 0;
        }

        public static void main(String[] args) {
            int exitCode = new CommandLine(new CmdTest()).execute(args);
            System.exit(exitCode);
        }
    }
    static class GroupA {
        @CommandLine.Option(
            names = {"--a-str"},
            paramLabel = "<NETWORK>",
            //required = true,
            arity = "1")
        private String str = null;

        @CommandLine.Option(
            names = {"--a-bool"},
            paramLabel = "<BOOLEAN>",
            arity = "1",
            //required = true,
            fallbackValue = "true")
        private boolean bool = false;

        public GroupA() {
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("GroupA{");
            sb.append("str='").append(str).append('\'');
            sb.append(", bool=").append(bool);
            sb.append('}');
            return sb.toString();
        }
    }
    static class GroupB {
        @CommandLine.Option(
            names = {"--b-bool"},
            paramLabel = "<BOOLEAN>",
            showDefaultValue = CommandLine.Help.Visibility.ALWAYS,
            fallbackValue = "true",
            arity = "1")
        private boolean b = false;

        public GroupB() {
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("GroupB{");
            sb.append("b=").append(b);
            sb.append('}');
            return sb.toString();
        }
    }
}
