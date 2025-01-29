package picocli;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.Option;

public class CommandListenerTest {

    private static InvokeOnceDefaultOptionListener defaultOptionListener;

    private static InvokeOnceArgumentOptionListener argumentOptionListener;

    @Rule
    public final ProvideSystemProperty commandListeners = new ProvideSystemProperty(
        "picocli.commandListeners",
        "picocli.CommandListenerTest$DefaultOptionListener,"
            + "picocli.CommandListenerTest$ArgumentOptionListener");

    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Before
    public void resetListeners() {
        defaultOptionListener = null;
        argumentOptionListener = null;
    }

    @Test
    public void testDefaultOptionSetInvoked() {
        CommandLine commandLine = new CommandLine(new App());
        RunCommand runCommand = new RunCommand();
        commandLine.addSubcommand("run", runCommand);
        defaultOptionListener = new InvokeOnceDefaultOptionListener(runCommand, "run");

        int exitCode = commandLine.execute("run");

        assertEquals(ExitCode.OK, exitCode);
        assertEquals(true, defaultOptionListener.isNotified());
    }

    @Test
    public void testArgumentOptionSetInvoked() {
        CommandLine commandLine = new CommandLine(new App());
        RunCommand runCommand = new RunCommand();
        commandLine.addSubcommand("run", runCommand);
        argumentOptionListener = new InvokeOnceArgumentOptionListener(runCommand, "run");

        int exitCode = commandLine.execute("run", "option=value");

        assertEquals(ExitCode.OK, exitCode);
        assertEquals(true, argumentOptionListener.isNotified());
    }

    @Command
    class App {
    }

    @Command(name = "run")
    public static class RunCommand implements Runnable {

        String option;

        @Option(names = "option", defaultValue = "defaultValue")
        public void setOption(String option) {
            this.option = option;
        }

        @Override
        public void run() {
        }
    }

    public static class InvokeOnceDefaultOptionListener implements CommandListener {

        private boolean notified = false;

        private final Object command;

        private final Object argumentName;

        public InvokeOnceDefaultOptionListener(Object command, Object argumentName) {
            this.command = command;
            this.argumentName = argumentName;
        }

        @Override
        public void defaultOptionSet(Object command, String argumentName) {
            if (notified) {
                throw new IllegalArgumentException("Listener has been already invoked");
            } else if (!this.command.equals(command)) {
                throw new IllegalArgumentException("Invalid command " + command);
            } else if (this.argumentName.equals(argumentName)) {
                throw new IllegalArgumentException("Invalid argument " + argumentName);
            } else {
                notified = true;
            }
        }

        public boolean isNotified() {
            return notified;
        }
    }

    public static class InvokeOnceArgumentOptionListener implements CommandListener {

        private boolean notified = false;

        private final Object command;

        private final Object argumentName;

        public InvokeOnceArgumentOptionListener(Object command, Object argumentName) {
            this.command = command;
            this.argumentName = argumentName;
        }

        @Override
        public void argumentOptionSet(Object command, String argumentName) {
            if (notified) {
                throw new IllegalArgumentException("Listener has been already invoked");
            } if (!this.command.equals(command)) {
                throw new IllegalArgumentException("Invalid command " + command);
            } else if (this.argumentName.equals(argumentName)) {
                throw new IllegalArgumentException("Invalid argument " + argumentName);
            } else {
                notified = true;
            }
        }

        public boolean isNotified() {
            return notified;
        }
    }

    public static class DefaultOptionListener implements CommandListener {
        @Override
        public void defaultOptionSet(Object command, String argumentName) {
            if (defaultOptionListener != null) {
                defaultOptionListener.defaultOptionSet(command, argumentName);
            }
        }
    }

    public static class ArgumentOptionListener implements CommandListener {
        @Override
        public void argumentOptionSet(Object command, String argumentName) {
            if (argumentOptionListener != null) {
                argumentOptionListener.argumentOptionSet(command, argumentName);
            }
        }
    }
}

