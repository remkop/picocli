package picocli;

import org.junit.Test;

import java.io.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AutoCompleteDejaGnuTest {

    @Test
    public void tryRunDejaGnuCompletionTests() throws Exception {
        //System.out.println(System.getProperty("user.dir"));
        if (isDejaGnuInstalled()) {
            runDejaGnuCompletionTests();
        }
    }

    private void runDejaGnuCompletionTests() throws Exception {
        final int TIMEOUT_RUNTEST_COMPLETION = 15; // how many seconds to wait for the `./runCompletion` process to complete
        final AtomicInteger runtestExitStatus = new AtomicInteger(Integer.MIN_VALUE);
        final AtomicReference<Process> process = new AtomicReference<Process>();
        final AtomicReference<Exception> exception = new AtomicReference<Exception>();
        final CountDownLatch latch = new CountDownLatch(1);
        final StringWriter sw = new StringWriter();
        final AtomicReference<String> lastLine = new AtomicReference<String>();
        Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    File testDir = new File("src/test/dejagnu.tests");
                    assertTrue(testDir.getAbsolutePath() + " should exist", testDir.exists());

                    ProcessBuilder pb = new ProcessBuilder("./runCompletion");
                    pb.directory(testDir);
                    process.set(pb.start());
                    runtestExitStatus.set(process.get().waitFor());

                    // read process output
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.get().getInputStream()));
                    PrintWriter pw = new PrintWriter(sw);
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        lastLine.set(line);
                        pw.println(line);
                    }
                    reader.close();
                } catch (Exception ex) {
                    exception.set(ex);
                }
                latch.countDown();
            }
        }, "runtest-thread");
        thread.setDaemon(true);
        thread.start();

        boolean done = latch.await(TIMEOUT_RUNTEST_COMPLETION, TimeUnit.SECONDS);
        if (!done) {
            if (process.get() != null) {
                process.get().destroy();
            }
        }
        if (exception.get() != null) {
            throw exception.get();
        }
        assertTrue("runCompletion completed within 15 seconds", done);
        assertEquals("runCompletion exit code", 0, runtestExitStatus.get());
        String msg = "Expected completion test output to end with '# of expected passes', but got:" + System.getProperty("line.separator");
        assertTrue(msg + sw.toString(), lastLine.get() != null && lastLine.get().contains("# of expected passes"));
    }

    private boolean isDejaGnuInstalled() throws Exception {
        final int TIMEOUT_RUNTEST_VERSION = 5; // how many seconds to wait for the `runtest --version` process to complete
        final AtomicInteger runtestExitStatus = new AtomicInteger(Integer.MIN_VALUE);
        final AtomicReference<Process> process = new AtomicReference<Process>();
        final AtomicReference<Exception> exception = new AtomicReference<Exception>();
        final CountDownLatch latch = new CountDownLatch(1);
        Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    ProcessBuilder pb = new ProcessBuilder("runtest", "--version");
                    process.set(pb.start());
                    runtestExitStatus.set(process.get().waitFor());
                } catch (Exception ex) {
                    exception.set(ex);
                }
                latch.countDown();
            }
        }, "runtest-version-thread");
        thread.setDaemon(true);
        thread.start();

        boolean done = latch.await(TIMEOUT_RUNTEST_VERSION, TimeUnit.SECONDS);
        if (!done) {
            if (process.get() != null) {
                process.get().destroy();
            }
        }
        if (exception.get() != null) {
            return false;
        }
        return done && runtestExitStatus.get() == 0;
    }
}
