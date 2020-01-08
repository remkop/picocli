package picocli.test;

import picocli.CommandLine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public abstract class Execution {
    protected int exitCode;
    protected String[] args;

    public static final class Builder {
        private File executable;
        private CommandLine commandLine;
        private boolean customizeOut;
        private boolean customizeErr;

        private Builder(CommandLine commandLine) {
            if (commandLine == null) { throw new NullPointerException("commandLine is null"); }
            this.commandLine = commandLine;
        }

        private Builder(File executable) {
            if (executable == null) { throw new NullPointerException("executable is null"); }
            //if (!executable.canExecute()) { throw new IllegalArgumentException(executable + " is not executable"); }
            if (!executable.canRead()) { throw new IllegalArgumentException(executable + " does not exist or is not readable"); }
            this.executable = executable;
        }

        public Execution execute(String... args) {
            Execution result = createExecution(args.clone());
            result.execute();
            return result;
        }

        private Execution createExecution(String[] args) {
            if (commandLine != null) {
                return new CommandLineExecution(commandLine, args)
                        .setCustomizeErr(customizeErr)
                        .setCustomizeOut(customizeOut);
            }
            List<String> command = new ArrayList<String>();
            command.add(executable.getAbsolutePath());
            for (String arg : args) {
                command.add(arg);
            }
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            return new ProcessExecution(processBuilder, args);
        }

        public void setCustomizeOut(boolean customizeOut) {
            this.customizeOut = customizeOut;
        }

        public void setCustomizeErr(boolean customizeErr) {
            this.customizeErr = customizeErr;
        }
    }

    protected abstract void execute();
    protected abstract boolean isAlive();
    protected abstract int getExitCode();
    protected abstract String getSystemOutString();
    protected abstract String getSystemErrString();

    public static Builder builder(CommandLine commandLine) {
        return new Builder(commandLine);
    }

    public Execution assertExitCode(int expectedExitCode) {
        assertEquals(expectedExitCode, getExitCode());
        return this;
    }

    public Execution assertExitCode(int expectedExitCode, String message) {
        assertEquals(message, expectedExitCode, getExitCode());
        return this;
    }

    public Execution assertSystemOut(String expectedSystemOut) {
        assertEquals(format(expectedSystemOut), getSystemOutString());
        return this;
    }

    public Execution assertSystemOut(String expectedSystemOut, String message) {
        assertEquals(message, format(expectedSystemOut), getSystemOutString());
        return this;
    }

    public Execution assertSystemErr(String expectedSystemErr) {
        assertEquals(format(expectedSystemErr), getSystemErrString());
        return this;
    }

    public Execution assertSystemErr(String expectedSystemErr, String message) {
        assertEquals(message, format(expectedSystemErr), getSystemErrString());
        return this;
    }

    private String format(String str) {
        return String.format(str);
    }
}
