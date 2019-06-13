package picocli;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicInteger;

public class Size {
    public static void main(String[] args) throws Exception {
        final AtomicInteger size = new AtomicInteger(-1);
        Thread t = new Thread(new Runnable() {
            public void run() {
                Process proc = null;
                BufferedReader reader = null;
                try {
                    proc = Runtime.getRuntime().exec(new String[] { "bash", "-c", "tput cols 2> /dev/tty" });
                    reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                    size.set(Integer.valueOf(reader.readLine()));
                } catch (Exception ignored) { // nothing to do...
                } finally {
                    if (proc != null) { proc.destroy(); }
                    if (reader != null) { try { reader.close(); } catch (Exception ignored) {} }
                }
            }
        });
        t.start();
        long now = System.currentTimeMillis();
        do {
            if (size.intValue() >= 0) { break; }
            Thread.sleep(25);
        } while (System.currentTimeMillis() < now + 1000);

        if (size.intValue() < 0) {
            System.out.println("Timed out");
        } else {
            System.out.println("Found size: " + size);
        }
    }
}
