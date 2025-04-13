package picocli.issue2344;

import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(name = "Demo CLI Tool",
    description = "Short demo to showcase problem negatable option",
    mixinStandardHelpOptions = true)
public class Application implements Runnable {
    @Mixin
    private WrapperClass wrapper;

    @Override
    public void run() {
        System.out.println(wrapper.getCustom().flag1);
        System.out.println(wrapper.getCustom().flag2);
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Application()).execute(args);
        System.exit(exitCode);
    }
}

@Command(name = "mixinwrapper") //I've seen this annotation on the Mixin in issue #2309, but doesn't change the error
class WrapperClass {
    @ArgGroup(exclusive = false)
    MyArgGroup custom = new MyArgGroup();

    public MyArgGroup getCustom() {
        return custom;
    }

    static class MyArgGroup {
        @Option(names = "--flag1", description = "first cool flag",
            negatable = false, defaultValue = "true")
        boolean flag1;

        @Option(names = "--flag2", description = "second cool flag",
            negatable = true, defaultValue = "true")
        boolean flag2;
    }
}
