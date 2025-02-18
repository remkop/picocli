package picocli;

import org.junit.Ignore;
import org.junit.Test;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class Issue2349 {
    static class TestParser {
        static class Args {
            @ArgGroup(exclusive = false, multiplicity = "1..4")
            private List<Group> groups;
        }

        static class Group {
            @Option(names = "--type", required = true)
            private String type;

            @Option(names = "--actions", defaultValue = "CREATE,UPDATE,DELETE", split = ",")
            private Set<String> actions;

            public Group() {}

            Group(String type, Set<String> actions) {
                this.type = type;
                this.actions = actions;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                Group group = (Group) o;
                return equals(type, group.type) && equals(actions, group.actions);
            }
            public static boolean equals(Object a, Object b) {
                return (a == b) || (a != null && a.equals(b));
            }

            @Override
            public int hashCode() {
                return Arrays.hashCode(new Object[] {type, actions});
            }

            @Override
            public String toString() {
                return String.format("%s %s", type, actions);
            }
        }
    }

    @Ignore("https://github.com/remkop/picocli/issues/2349")
    @Test
    public void issue2349() {
        String[] rawArgs = new String[]{
            "--type=ONE", "--actions=CREATE,DELETE",
            "--type=TWO", "--actions=CREATE",
            "--type=THREE",
            "--type=FOUR", "--actions=CREATE,UPDATE"
        };
        TestParser.Args args = new TestParser.Args();
        new CommandLine(args).parseArgs(rawArgs);

        TestParser.Group[] expected = new TestParser.Group[] {
            new TestParser.Group("ONE", new HashSet<String>(Arrays.asList("CREATE", "DELETE"))),
            new TestParser.Group("TWO", new HashSet<String>(Arrays.asList("CREATE"))),
            new TestParser.Group("THREE", new HashSet<String>(Arrays.asList("CREATE", "UPDATE", "DELETE"))),
            new TestParser.Group("FOUR",  new HashSet<String>(Arrays.asList("CREATE", "UPDATE"))),
        };

        for (int i = 0; i < expected.length; i++) {
            TestParser.Group actual = args.groups.get(i);
            assertEquals("group[" + i + "]", expected[i], actual);
        }
    }
}
