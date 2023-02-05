package picocli.terminal;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Command(mixinStandardHelpOptions = true)
public class Size implements Runnable {
    @Option(names = {"-c", "--tput-cols"}, description = "tput cols")
    boolean tputCols;

    @Option(names = {"-a", "--stty-a"}, description = {"stty -a -F $1", "(for the specified device)"})
    boolean sttyAllFull;

    @Option(names = {"-x", "--ascii-signals"}, description = {"Send ASCII signals like lanterna"})
    boolean asciiSignals;

    @Option(names = {"-s", "--shell"}, description = "Execute command in `sh -c $@` subshell")
    boolean shell;

    @Option(names = {"-i", "--redirect-input"}, description = "Redirect stdin from the specified device in processbuilder")
    boolean redirectInput;

    @Option(names = {"-o", "--redirect-output"}, description = "Redirect stderr output to device in processbuilder")
    boolean redirectOutput;

    @Option(names = {"-l", "--readLine"}, description = "Read input from process line by line")
    boolean readLine;

    @Option(names = {"-m", "--mode"}, description = "Use `cmd.exe mode con` to get terminal size")
    boolean mode;

    @Parameters(index = "0", arity = "0..1", description = "device, default: ${DEFAULT-VALUE}")
    String device = "/dev/tty";

    @Override
    public void run() {
        if (mode) {
            modeCon();
        }
        if (tputCols) {
            tput_cols(device);
        }
        if (sttyAllFull) {
            stty_size(device);
        }
        if (asciiSignals) {
            ascii_signals(device);
        }
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Size()).execute(args);
        System.exit(exitCode);
    }

    private void tput_cols(final String device) {
        final AtomicInteger size = new AtomicInteger(-1);
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                String line = exec("tput", "cols");
//                String line = exec("sh", "-c", "tput cols 2> " + device);
                size.set(Integer.valueOf(line.trim()));
            }
        });
        t.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                e.printStackTrace();
            }
        });
        t.start();
        long timeout = System.currentTimeMillis() + 2000;
        do {
            if (size.intValue() >= 0) { break; }
            try {Thread.sleep(25);} catch (InterruptedException ignored) {}
        } while (System.currentTimeMillis() < timeout);

        System.out.println("Value of env var COLUMNS: " + System.getenv("COLUMNS"));
        if (size.intValue() < 0) {
            System.out.println("`tput cols` timed out");
        } else {
            System.out.println("`tput cols` found size: " + size);
        }
    }

    private void modeCon() {
        final AtomicBoolean done = new AtomicBoolean();
        AtomicReference<String> line = new AtomicReference<>();
        final Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                String res = exec("cmd.exe", "/c", "mode con");
                line.set(res);
                System.out.println(res);
                done.set(true);
            }
        });
        t.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                e.printStackTrace();
            }
        });
        t.start();
        long timeout = System.currentTimeMillis() + 2000;
        do {
            if (done.get()) { break; }
            try {Thread.sleep(25);} catch (InterruptedException ignored) {}
        } while (System.currentTimeMillis() < timeout);

        if (line.get() == null) {
            System.out.printf("command timed out%n");
        } else {
            System.out.printf("command found: %s%n", line.get());
        }
    }

    private void stty_size(final String device) {
        final AtomicBoolean done = new AtomicBoolean();
        AtomicReference<String> line = new AtomicReference<>();
        final Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
//                line[0] = exec("/bin/stty", "-a", "-F", "/dev/tty");
//                line[0] = exec("sh", "-c", "stty -a -F " + device + " 2> " + device);
                //line[0] = exec("sh", "-c", "stty -a -F " + device + " 2> " + device);
                String res = exec(System.getProperty("stty", "stty"), "-a", "-F", device);
                line.set(res);
                System.out.println(res);
                done.set(true);
            }
        });
        t.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                e.printStackTrace();
            }
        });
        t.start();
        long timeout = System.currentTimeMillis() + 2000;
        do {
            if (done.get()) { break; }
            try {Thread.sleep(25);} catch (InterruptedException ignored) {}
        } while (System.currentTimeMillis() < timeout);

        if (line.get() == null) {
            System.out.printf("command timed out%n");
        } else {
            System.out.printf("command found: %s%n", line.get());
        }
    }

    private String exec(String... cmd) {
        try {
            return tryExec(cmd);
        } catch (Exception e) {
            e.printStackTrace();
            throw new InternalError(e.toString());
        }
    }

    private String tryExec(String[] cmd) throws IOException, InterruptedException {
        ProcessBuilder pb;
        if (shell) {
            if (redirectInput) {
                //Here's what we try to do, but that is Java 7+ only:
                // processBuilder.redirectInput(ProcessBuilder.Redirect.from(ttyDev));
                //instead, for Java 6, we join the cmd into a scriptlet with redirection
                //and replace cmd by a call to sh with the scriptlet:
                StringBuilder sb = new StringBuilder();
                for (String arg : cmd) { sb.append(arg).append(' '); }
                sb.append("< ").append(device);
                cmd = new String[] { "sh", "-c", sb.toString() };
            } else if (redirectOutput) {
                //Here's what we try to do, but that is Java 7+ only:
                // processBuilder.redirectInput(ProcessBuilder.Redirect.from(ttyDev));
                //instead, for Java 6, we join the cmd into a scriptlet with redirection
                //and replace cmd by a call to sh with the scriptlet:
                StringBuilder sb = new StringBuilder();
                for (String arg : cmd) { sb.append(arg).append(' '); }
                sb.append("2> ").append(device);
                cmd = new String[] { "sh", "-c", sb.toString() };
            }
            pb = new ProcessBuilder(cmd);
            System.out.println("Running command: " + pb.command());
            System.out.flush();
        } else {
            pb = new ProcessBuilder(cmd);
            System.out.println("Running command: " + pb.command());
            System.out.flush();
            if (redirectInput) {
                System.out.println("Redirecting input from " + device);
                System.out.flush();
                pb.redirectInput(ProcessBuilder.Redirect.from(new File(device)));
            } else if (redirectOutput) {
                System.out.println("Redirecting stdin, stderr and stdout output to inherit");
                System.out.flush();
                pb.redirectError(ProcessBuilder.Redirect.INHERIT);
                pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
//                pb.redirectInput(ProcessBuilder.Redirect.INHERIT);
            }
        }
        Process process = pb.start();
        InputStream stdout = process.getInputStream();
        if (readLine) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stdout));
            StringBuilder builder = new StringBuilder();
            String line;
            while((line = reader.readLine()) != null) {
                builder.append(line);
            }
            reader.close();
            //process.destroy();
            return builder.toString();
        } else {
            ByteArrayOutputStream stdoutBuffer = new ByteArrayOutputStream();
            int readByte = stdout.read();
            while (readByte >= 0) {
                stdoutBuffer.write(readByte);
                readByte = stdout.read();
            }
            //process.destroy();
            return stdoutBuffer.toString();
        }
//        ByteArrayInputStream stdoutBufferInputStream = new ByteArrayInputStream(stdoutBuffer.toByteArray());
//        BufferedReader reader = new BufferedReader(new InputStreamReader(stdoutBufferInputStream));
//        StringBuilder builder = new StringBuilder();
//        String line;
//        while((line = reader.readLine()) != null) {
//            builder.append(line);
//        }
//        reader.close();
//        return builder.toString();
    }

    ///bin/stty -echo; /bin/stty -icanon; /bin/stty min 1; java WinSize; /bin/stty echo; /bin/stty icanon;
    private void ascii_signals(final String device) {
        final StringBuilder sb = new StringBuilder();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                exec("sh", "-c", "stty -echo < " + device);
                Runtime.getRuntime().addShutdownHook(new Thread() {
                    @Override
                    public void run() {
                        exec("sh", "-c", "stty echo < " + device);
                    }
                });
                exec("sh", "-c", "stty -icanon < " + device);
                Runtime.getRuntime().addShutdownHook(new Thread() {
                    @Override
                    public void run() {
                        exec("sh", "-c", "stty icanon < " + device);
                    }
                });


                String[] signals = {
                        "\u001b[s",            // save cursor position
                        "\u001b[5000;5000H",   // move to col 5000 row 5000
                        "\u001b[6n",           // request cursor position
                        "\u001b[u",            // restore cursor position
                };
                for (String s : signals) {
                    System.out.print(s);
                }
                System.out.flush();
                try {
                    System.in.read();
                    int read = -1;
                    byte[] buff = new byte[1];
                    while ((read = System.in.read(buff, 0, 1)) != -1) {
                        sb.append((char) buff[0]);
                        //System.err.printf("Read %s chars, buf size=%s%n", read, sb.length());
                        if ('R' == buff[0]) {
                            break;
                        }
                    }
                } catch (Exception ignored) { // nothing to do...
                }
            }
        });
        t.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                e.printStackTrace();
            }
        });
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String size = sb.toString();
        if (sb.length() == 0) {
            System.err.println("ascii sequence gave no result");
        } else {
            int rows = Integer.parseInt(size.substring(size.indexOf("\u001b[") + 2, size.indexOf(';')));
            int cols = Integer.parseInt(size.substring(size.indexOf(';') + 1, size.indexOf('R')));
            System.err.printf("ascii commands found: rows = %s, cols = %s%n", rows, cols);
        }
    }
}
