package picocli.test;

import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;

public class CommandLineExecution extends Execution {
    private final CommandLine commandLine;
    private ByteArrayOutputStream out;
    private ByteArrayOutputStream err;
    private StringWriter outWriter;
    private StringWriter errWriter;
    private boolean customizeOut;
    private boolean customizeErr;
    private boolean alive;

    CommandLineExecution(CommandLine commandLine, String[] args) {
        if (commandLine == null) { throw new NullPointerException("commandLine is null"); }
        if (args == null) { throw new NullPointerException("args array is null"); }
        this.commandLine = commandLine;
        this.args = args;
    }

    CommandLineExecution setCustomizeOut(boolean customizeOut) {
        this.customizeOut = customizeOut;
        return this;
    }

    CommandLineExecution setCustomizeErr(boolean customizeErr) {
        this.customizeErr = customizeErr;
        return this;
    }

    protected void execute() {
        out = new ByteArrayOutputStream();
        err = new ByteArrayOutputStream();
        PrintStream oldOut = System.out;
        PrintStream oldErr = System.err;
        try {
            System.setOut(new PrintStream(out));
            System.setErr(new PrintStream(err));
            if (customizeOut) {
                outWriter = new StringWriter();
                commandLine.setOut(new PrintWriter(outWriter));
            }
            if (customizeErr) {
                errWriter = new StringWriter();
                commandLine.setErr(new PrintWriter(errWriter));
            }
            alive = true;
            exitCode = commandLine.execute(args);
        } finally {
            alive = false;
            System.setOut(oldOut);
            System.setOut(oldErr);
        }
    }

    protected boolean isAlive() {
        return alive;
    }

    public String getSystemOutString() {
        return this.out.toString();
    }

    public String getSystemErrString() {
        return this.err.toString();
    }

    protected int getExitCode() {
        return this.exitCode;
    }
}
