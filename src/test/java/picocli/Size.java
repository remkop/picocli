package picocli;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Size {
    public static void main(String[] args) {
        String device = args.length > 0 ? args[0] : "/dev/tty";
        tput_cols(device);
        stty_size(device);
        ascii_signals(device);
    }

    ///bin/stty -echo; /bin/stty -icanon; /bin/stty min 1; java WinSize; /bin/stty echo; /bin/stty icanon;
    private static void ascii_signals(final String device) {
        final StringBuilder sb = new StringBuilder();
        Thread t = new Thread(new Runnable() {
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


                String[] signals = new String[] {
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
        t.start();
        String size = sb.toString();
        if (sb.length() == 0) {
            System.err.println("ascii sequence gave no result");
        } else {
            int rows = Integer.parseInt(size.substring(size.indexOf("\u001b[") + 2, size.indexOf(';')));
            int cols = Integer.parseInt(size.substring(size.indexOf(';') + 1, size.indexOf('R')));
            System.err.printf("ascii commands found: rows = %s, cols = %s%n", rows, cols);
        }
    }

    private static void tput_cols(final String device) {
        final AtomicInteger size = new AtomicInteger(-1);
        Thread t = new Thread(new Runnable() {
            public void run() {
                String line = exec("sh", "-c", "tput cols 2> " + device);
                size.set(Integer.valueOf(line.trim()));
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

    private static void stty_size(final String device) {
        final AtomicBoolean done = new AtomicBoolean();
        final String[] line = new String[1];
        final Thread t = new Thread(new Runnable() {
            public void run() {
//                line[0] = exec("/bin/stty", "-a", "-F", "/dev/tty");
//                line[0] = exec("sh", "-c", "stty -a -F " + device + " 2> " + device);
                line[0] = exec("sh", "-c", "stty -a -F " + device + " 2> " + device);
                done.set(true);
            }
        });
        t.start();
        long timeout = System.currentTimeMillis() + 2000;
        do {
            if (!done.get()) { break; }
            try {Thread.sleep(25);} catch (InterruptedException ignored) {}
        } while (System.currentTimeMillis() < timeout);

        if (line[0] == null) {
            System.out.printf("`stty -a -F %s` timed out%n", device);
        } else {
            System.out.printf("`stty -a -F %s` found: %s%n", device, line[0]);
        }
    }

    private static String exec(String... cmd) {
        try {
            return tryExec(cmd);
        } catch (Exception e) {
            e.printStackTrace();
            throw new InternalError(e.toString());
        }
    }

    private static String tryExec(String[] cmd) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(cmd);
        Process process = pb.start();
        ByteArrayOutputStream stdoutBuffer = new ByteArrayOutputStream();
        InputStream stdout = process.getInputStream();
        process.waitFor();
        int readByte = stdout.read();
        while (readByte >= 0) {
            stdoutBuffer.write(readByte);
            readByte = stdout.read();
        }
        process.destroy();
        return stdoutBuffer.toString();
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
}
