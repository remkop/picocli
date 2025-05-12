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

    private static InvokeOnceExecuteCommandListener executeCommandListener;

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
        executeCommandListener = null;
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

    @Test
    public void testOnExecuteInvoked() {
        CommandLine commandLine = new CommandLine(new App());
        RunCommand runCommand = new RunCommand();
        commandLine.addSubcommand("run", runCommand);
        executeCommandListener = new InvokeOnceExecuteCommandListener(runCommand);

        int exitCode = commandLine.execute("run");

        assertEquals(ExitCode.OK, exitCode);
        assertEquals(true, executeCommandListener.isNotified());
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

        private final Object optionName;

        public InvokeOnceDefaultOptionListener(Object command, Object optionName) {
            this.command = command;
            this.optionName = optionName;
        }

        @Override
        public void defaultOptionSet(Object command, String optionName) {
            if (notified) {
                throw new IllegalArgumentException("Listener has been already invoked");
            } else if (!this.command.equals(command)) {
                throw new IllegalArgumentException("Invalid command " + command);
            } else if (this.optionName.equals(optionName)) {
                throw new IllegalArgumentException("Invalid argument " + optionName);
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

        private final Object optionName;

        public InvokeOnceArgumentOptionListener(Object command, Object optionName) {
            this.command = command;
            this.optionName = optionName;
        }

        @Override
        public void argumentOptionSet(Object command, String optionName) {
            if (notified) {
                throw new IllegalArgumentException("Listener has been already invoked");
            } if (!this.command.equals(command)) {
                throw new IllegalArgumentException("Invalid command " + command);
            } else if (this.optionName.equals(optionName)) {
                throw new IllegalArgumentException("Invalid argument " + optionName);
            } else {
                notified = true;
            }
        }

        public boolean isNotified() {
            return notified;
        }
    }

    public static class InvokeOnceExecuteCommandListener implements CommandListener {

        private boolean notified = false;

        private final Object command;

        public InvokeOnceExecuteCommandListener(Object command) {
            this.command = command;
        }

        @Override
        public void onExecute(Object command) {
            if (notified) {
                throw new IllegalArgumentException("Listener has been already invoked");
            } if (!this.command.equals(command)) {
                throw new IllegalArgumentException("Invalid command " + command);
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
        public void defaultOptionSet(Object command, String optionName) {
            if (defaultOptionListener != null) {
                defaultOptionListener.defaultOptionSet(command, optionName);
            }
        }
    }

    public static class ArgumentOptionListener implements CommandListener {
        @Override
        public void argumentOptionSet(Object command, String optionName) {
            if (argumentOptionListener != null) {
                argumentOptionListener.argumentOptionSet(command, optionName);
            }
        }
    }

    public static class ExecuteCommandListener implements CommandListener {
        @Override
        public void onExecute(Object command) {
            if (executeCommandListener != null) {
                executeCommandListener.onExecute(command);
            }
        }
    }
}

