package picocli;

import org.junit.Test;

import java.io.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * This JUnit test kicks off the autocompletion tests written in Tcl and Except
 * using the DejaGnu test framework, and passes if these tests all pass
 * (evidenced by the last line of output from the DejaGnu tests
 * to start with "# of expected passes").
 * <p>
 * In order for this test to run, the following packages must be installed:
 * </p>
 * <ul>
 *     <li>dejagnu</li>
 *     <li>tcllib</li>
 *     <li>expect</li>
 *     <li>libtcl8.6</li>
 *     <li>tcl-expect</li>
 *     <li>tcl8.6</li>
 * </ul>
 * <p>
 * This test will only run on unix-based systems where the above packages are installed.
 * (For example, the tests will run on WSL - Windows Subsystem for Linux - if the above
 * packages are installed.)
 * If dejagnu is not installed, this test is ignored.
 * </p><p>
 * The starting point of the completion tests is the {@code dejagnu.tests/runCompletion} script.
 * This is a wrapper script that starts the dejagnu {@code runtest} with option {@code --tool completion}.
 * </p><p>
 * The dejagnu tests themselves live in {@code dejagnu.tests/completion};
 * the *.exp scripts in that directory source the completion scripts, and then delegate to
 * an accompanying *.exp script in the {@code dejagnu.tests/lib/completions} directory
 * where the various completion scenarios are verified.
 * </p><p>
 * You can run the DejaGnu completion tests manually as follows:
 * </p>
 * <pre>
 * cd src/test/dejagnu.tests
 * ./runCompletion
 * </pre>
 * <p>
 * To get more verbose output, add the {@code -v} option. Adding this option multiple times
 * increases the verbosity.
 * Log files by default are written to a directory named {@code log} in the current directory.
 * This can be changed with the {@code --log-dir} option. For example:
 * </p>
 * <pre>
 * # create completion.log and completion.sum log files in the build/dejagnu-logs directory
 * cd src/test/dejagnu.tests
 * ./runCompletion -v --log-dir ../../../build/dejagnu-logs
 * </pre>
 */
public class AutoCompleteDejaGnuTest {

    @Test
    public void tryRunDejaGnuCompletionTests() throws Exception {
        //System.out.println(System.getProperty("user.dir"));

        // ignores test if dejagnu not installed
        org.junit.Assume.assumeTrue("dejagnu must be installed to run this test", isDejaGnuInstalled());
        runDejaGnuCompletionTests();
    }

    private void runDejaGnuCompletionTests() throws Exception {
        final File testDir = new File("src/test/dejagnu.tests");
        assertTrue(testDir.getAbsolutePath() + " should exist", testDir.exists());
        File runCompletionScript = new File(testDir, "runCompletion");
        assertTrue(runCompletionScript.getAbsolutePath() + " should exist", runCompletionScript.exists());

        final int TIMEOUT_RUNTEST_COMPLETION = 60; // how many seconds to wait for the `./runCompletion` process to complete
        final AtomicInteger runtestExitStatus = new AtomicInteger(Integer.MIN_VALUE);
        final AtomicReference<Process> process = new AtomicReference<Process>();
        final AtomicReference<Exception> exception = new AtomicReference<Exception>();
        final CountDownLatch latch = new CountDownLatch(1);
        final StringWriter sw = new StringWriter();
        final AtomicReference<String> lastLine = new AtomicReference<String>();
        Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
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
        assertTrue("runCompletion completed within " + TIMEOUT_RUNTEST_COMPLETION + " seconds", done);
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
