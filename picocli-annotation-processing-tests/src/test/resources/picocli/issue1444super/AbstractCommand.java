package picocli.issue1444super;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ArgGroup;

//@Command
public abstract class AbstractCommand implements Runnable {
    @ArgGroup(heading = "Optional login session name:%n", order = 1000)
    protected LoginSessionNameOptions loginSessionNameOptions;

    private static class LoginSessionNameOptions {
        @Option(names = {"--login-session-name", "-n"}, required = false, defaultValue = "default")
        protected String loginSessionName;
    }

    public void run() {}
}
