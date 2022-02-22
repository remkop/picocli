package picocli.examples.arggroup;

import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

import static picocli.CommandLine.Spec.Target.MIXEE;

/**
 * Shows how to use a mixin to add an arg group that keeps state in a single instance:
 * the mixin that is injected in the top-level command.
 * All other mixin instances in the subcommands do not keep any option values themselves
 * but instead delegate to the mixin in the top-level command, and store the values there.
 *
 * @see <a href="https://github.com/remkop/picocli/issues/1212">remkop/picocli#1212</a>
 */
public class ArgGroupMixinDemo2 {

    @Command(mixinStandardHelpOptions = true) // add --help and --version to all commands that have this mixin
    static class MyMixin {
        @Spec(MIXEE) CommandSpec mixee;

        private MyGroup group;

        @ArgGroup(heading = "%nShared options that work on every command:%n", validate = false)
        public void setGroup(MyGroup group) {
            this.group = group;
            group.mixin = this;
        }

        // these fields are only populated on the MyMixin instance injected in the top-level command
        String global1;
        String global2;

        public String getGlobalOption1() {
            return rootMixin().global1;
        }

        public String getGlobalOption2() {
            return rootMixin().global2;
        }

        void setGlobalOption1(String value) {
            rootMixin().global1 = value;
        }

        void setGlobalOption2(String value) {
            rootMixin().global2 = value;
        }

        MyMixin rootMixin() {
            // The below code loops over all mixins in the top-level command
            // and finds the correct one by type.
            //
            // Alternatively we could find it by the field name: `mixee.root().mixins().get("myMixin")`.
            //
            // Another alternative, if MyCommand::myMixin was annotated with `@Mixin(name = "abc")`,
            // would be to find by annotation name: `mixee.root().mixins().get("abc")`.
            //
            for (CommandSpec mixinCommand : mixee.root().mixins().values()) {
                Object obj = mixinCommand.userObject();
                if (obj instanceof MyMixin) { return (MyMixin) obj;}
            }
            throw new IllegalStateException("Root command does not have a @Mixin of type MyMixin");
        }
    }
    static class MyGroup {
        MyMixin mixin;

        @Option(names = "--global-opt-1", description = "I'm nice.")
        void setOption1(String value) {
            mixin.setGlobalOption1(value);
        }

        @Option(names = "--global-opt-2", description = "I'm nicer.")
        void setOption2(String value) {
            mixin.setGlobalOption2(value);
        }
    }

    @Command(name = "command", subcommands = {Sub1.class, Sub2.class})
    static class MyCommand implements Runnable {
        @Mixin MyMixin myMixin;

        public void run() {
            System.out.printf("global-opt-1=%s%n", myMixin.getGlobalOption1());
            System.out.printf("global-opt-2=%s%n", myMixin.getGlobalOption2());
        }
    }

    @Command(name = "sub1")
    static class Sub1 implements Runnable {
        @Mixin MyMixin myMixin;

        @Option(names = "--sub1-opt-1", description = "I'm ok.") String opt1;
        @Option(names = "--sub1-opt-2", description = "I'm oker.") String opt2;

        public void run() {
            System.out.printf("sub2-opt-1=%s%n", opt1);
            System.out.printf("sub2-opt-2=%s%n", opt2);
            System.out.printf("global-opt-1=%s%n", myMixin.getGlobalOption1());
            System.out.printf("global-opt-2=%s%n", myMixin.getGlobalOption2());
        }

        @Command
        public void sub1sub1(
            @Mixin MyMixin mixin,
            @Option(names = "--sub1-sub1-opt-1", description = "I'm sub ok.") String subopt1,
            @Option(names = "--sub1-sub1-opt-2", description = "I'm sub oker.") String subopt2
        ) {
            System.out.printf("sub1-sub1-opt-1=%s%n", subopt1);
            System.out.printf("sub1-sub1-opt-2=%s%n", subopt2);
            System.out.printf("global-opt-1=%s%n", myMixin.getGlobalOption1());
            System.out.printf("global-opt-2=%s%n", myMixin.getGlobalOption2());
        }
    }

    @Command(name = "sub2")
    static class Sub2 implements Runnable {
        @Mixin MyMixin myMixin;

        @Option(names = "--sub2-opt-1", description = "I'm ok.") String opt1;
        @Option(names = "--sub2-opt-2", description = "I'm oker.") String opt2;

        public void run() {
            System.out.printf("sub2-opt-1=%s%n", opt1);
            System.out.printf("sub2-opt-2=%s%n", opt2);
            System.out.printf("global-opt-1=%s%n", myMixin.getGlobalOption1());
            System.out.printf("global-opt-2=%s%n", myMixin.getGlobalOption2());
        }
    }

    public static void main(String[] args) {
        System.out.println("-------------------");
        new CommandLine(new MyCommand()).execute("--global-opt-1=111");

        System.out.println("-------------------");
        new CommandLine(new MyCommand()).execute(
            "--global-opt-2=222", "sub1", "--sub1-opt-1=AAA", "--global-opt-1=XXX" );

        System.out.println("-------------------");
        new CommandLine(new MyCommand()).execute(
            "--global-opt-2=222", "sub1", "--sub1-opt-2=333", "sub1sub1", "--sub1-sub1-opt-1=YYY", "--global-opt-1=XXX");
    }
}
