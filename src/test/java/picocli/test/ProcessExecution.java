package picocli.test;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

public class ProcessExecution extends Execution {
    private final ProcessBuilder processBuilder;
    private Process process;

    public ProcessExecution(ProcessBuilder processBuilder, String[] args) {
        if (processBuilder == null) { throw new NullPointerException("commandLine is null"); }
        if (args == null) { throw new NullPointerException("args array is null"); }
        this.processBuilder = processBuilder;
        this.args = args;
    }

    protected void execute() {
        try {
            process = processBuilder.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected boolean isAlive() {
        try {
            Method isAlive = Process.class.getDeclaredMethod("isAlive");
            return (Boolean) isAlive.invoke(process);
        } catch (Exception e) {
            try {
                getExitCode();
                return true;
            } catch (IllegalThreadStateException ex) {
                return false;
            }
        }
    }

    protected int getExitCode() {
        return process.exitValue();
    }

    public String getSystemOutString() {
        try {
            return readFully(process.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getSystemErrString() {
        try {
            return readFully(process.getErrorStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String readFully(InputStream in) throws IOException {
        byte[] buff = new byte[10 * 1024];
        int len = 0;
        int total = 0;
        while ((len = in.read(buff, total, buff.length - total)) > 0) {
            total += len;
        }
        return new String(buff, 0, total);
    }
}
