/*
   Copyright 2017 Remko Popma

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package picocli;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.contrib.java.lang.system.SystemOutRule;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.Callable;

import static org.junit.Assert.*;
import static picocli.CommandLine.*;
import static picocli.HelpTestUtil.setTraceLevel;

public class CommandLineParseWithHandlersTest {
    @Rule
    public final SystemErrRule systemErrRule = new SystemErrRule().enableLog().muteForSuccessfulTests();

    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule().enableLog().muteForSuccessfulTests();

    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    interface Factory { Object create(); }
    @Test
    public void testParseWithHandlerRunXxxFailsIfNotRunnableOrCallable() {
        @Command class App {
            @Parameters String[] params;
        }
        Factory factory = new Factory() {
            public Object create() {return new App();}
        };
        String[] args = { "abc" };
        verifyAllFail(factory, "Parsed command (picocli.CommandLineParseWithHandlersTest$", ") is not Runnable or Callable", args);
    }

    @Test
    public void testParseWithHandlerRunXxxCatchesAndRethrowsExceptionFromRunnable() {
        @Command class App implements Runnable {
            @Parameters String[] params;
            public void run() { throw new IllegalStateException("TEST EXCEPTION"); }
        }
        Factory factory = new Factory() {
            public Object create() {return new App();}
        };
        verifyAllFail(factory, "Error while running command (picocli.CommandLineParseWithHandlersTest$",
                "): java.lang.IllegalStateException: TEST EXCEPTION", new String[0]);
    }

    @Test
    public void testParseWithHandlerRunXxxCatchesAndRethrowsExceptionFromCallable() {
        @Command class App implements Callable<Object> {
            @Parameters String[] params;
            public Object call() { throw new IllegalStateException("TEST EXCEPTION2"); }
        }
        Factory factory = new Factory() {
            public Object create() {return new App();}
        };
        verifyAllFail(factory, "Error while calling command (picocli.CommandLineParseWithHandlersTest$",
                "): java.lang.IllegalStateException: TEST EXCEPTION2", new String[0]);
    }

    private void verifyAllFail(Factory factory, String prefix, String suffix, String[] args) {
        IParseResultHandler[] handlers = new IParseResultHandler[] {
                new RunFirst(), new RunLast(), new RunAll()
        };
        for (IParseResultHandler handler : handlers) {
            String descr = handler.getClass().getSimpleName();
            try {
                new CommandLine(factory.create()).parseWithHandler(handler, System.out, args);
                fail(descr + ": expected exception");
            } catch (ExecutionException ex) {
                String actual = ex.getMessage();
                assertTrue(descr + ": " + actual, actual.startsWith(prefix));
                assertTrue(descr + ": " + actual, actual.endsWith(suffix));
            }
        }
    }

    @Test
    public void testParseWithHandlerRunXxxReturnsEmptyListIfHelpRequested() {
        @Command(version = "abc 1.3.4")
        class App implements Callable<Object> {
            @Option(names = "-h", usageHelp = true) boolean requestHelp;
            @Option(names = "-V", versionHelp = true) boolean requestVersion;
            public Object call() { return "RETURN VALUE"; }
        }
        CommandLineFactory factory = new CommandLineFactory() {
            public CommandLine create() {return new CommandLine(new App());}
        };
        verifyReturnValue(factory, Collections.emptyList(), new String[] {"-h"});
        verifyReturnValue(factory, Collections.emptyList(), new String[] {"-V"});
    }

    @Test
    public void testParseWithHandlerRunXxxReturnsCallableResult() {
        @Command
        class App implements Callable<Object> {
            public Object call() { return "RETURN VALUE"; }
        }
        CommandLineFactory factory = new CommandLineFactory() {
            public CommandLine create() {return new CommandLine(new App());}
        };
        verifyReturnValue(factory, Arrays.asList("RETURN VALUE"), new String[0]);
    }

    interface CommandLineFactory {
        CommandLine create();
    }

    private void verifyReturnValue(CommandLineFactory factory, Object expected, String[] args) {
        IParseResultHandler[] handlers = new IParseResultHandler[] {
                new RunFirst(), new RunLast(), new RunAll()
        };
        PrintStream out = new PrintStream(new ByteArrayOutputStream());
        for (IParseResultHandler handler : handlers) {
            String descr = handler.getClass().getSimpleName();
            Object actual = factory.create().parseWithHandler(handler, out, args);
            assertEquals(descr + ": return value", expected, actual);
        }
    }
    @Test
    public void testParseWithHandlerRunXxxReturnsCallableResultWithSubcommand() {
        @Command
        class App implements Callable<Object> {
            public Object call() { return "RETURN VALUE"; }
        }
        @Command(name = "sub")
        class Sub implements Callable<Object> {
            public Object call() { return "SUB RETURN VALUE"; }
        }
        CommandLineFactory factory = new CommandLineFactory() {
            public CommandLine create() {return new CommandLine(new App()).addSubcommand("sub", new Sub());}
        };
        PrintStream out = new PrintStream(new ByteArrayOutputStream());

        Object actual1 = factory.create().parseWithHandler(new RunFirst(), out, new String[] {"sub"});
        assertEquals("RunFirst: return value", Arrays.asList("RETURN VALUE"), actual1);

        Object actual2 = factory.create().parseWithHandler(new RunLast(), out, new String[] {"sub"});
        assertEquals("RunLast: return value", Arrays.asList("SUB RETURN VALUE"), actual2);

        Object actual3 = factory.create().parseWithHandler(new RunAll(), out, new String[] {"sub"});
        assertEquals("RunAll: return value", Arrays.asList("RETURN VALUE", "SUB RETURN VALUE"), actual3);
    }

    @Test
    public void testRunCallsRunnableIfParseSucceeds() {
        final boolean[] runWasCalled = {false};
        @Command class App implements Runnable {
            public void run() {
                runWasCalled[0] = true;
            }
        }
        CommandLine.run(new App(), System.err);
        assertTrue(runWasCalled[0]);
    }

    @Test
    public void testRunPrintsErrorIfParseFails() throws UnsupportedEncodingException {
        final boolean[] runWasCalled = {false};
        class App implements Runnable {
            @Option(names = "-number") int number;
            public void run() {
                runWasCalled[0] = true;
            }
        }
        PrintStream oldErr = System.err;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setErr(new PrintStream(baos, true, "UTF8"));
        CommandLine.run(new App(), System.err, "-number", "not a number");
        System.setErr(oldErr);

        String result = baos.toString("UTF8");
        assertFalse(runWasCalled[0]);
        assertEquals(String.format(
                "Could not convert 'not a number' to int for option '-number': java.lang.NumberFormatException: For input string: \"not a number\"%n" +
                        "Usage: <main class> [-number=<number>]%n" +
                        "      -number=<number>%n"), result);
    }

    @Test(expected = InitializationException.class)
    public void testRunRequiresAnnotatedCommand() {
        class App implements Runnable {
            public void run() { }
        }
        CommandLine.run(new App(), System.err);
    }

    @Test
    public void testCallReturnsCallableResultParseSucceeds() throws Exception {
        @Command class App implements Callable<Boolean> {
            public Boolean call() { return true; }
        }
        assertTrue(CommandLine.call(new App(), System.err));
    }

    @Test
    public void testCallReturnsNullAndPrintsErrorIfParseFails() throws Exception {
        class App implements Callable<Boolean> {
            @Option(names = "-number") int number;
            public Boolean call() { return true; }
        }
        PrintStream oldErr = System.err;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setErr(new PrintStream(baos, true, "UTF8"));
        Boolean callResult = CommandLine.call(new App(), System.err, "-number", "not a number");
        System.setErr(oldErr);

        String result = baos.toString("UTF8");
        assertNull(callResult);
        assertEquals(String.format(
                "Could not convert 'not a number' to int for option '-number': java.lang.NumberFormatException: For input string: \"not a number\"%n" +
                        "Usage: <main class> [-number=<number>]%n" +
                        "      -number=<number>%n"), result);
    }

    @Test(expected = InitializationException.class)
    public void testCallRequiresAnnotatedCommand() throws Exception {
        class App implements Callable<Object> {
            public Object call() { return null; }
        }
        CommandLine.call(new App(), System.err);
    }

    @Test
    public void testExitCodeFromParseResultHandler() {
        @Command class App implements Runnable {
            public void run() {
            }
        }
        exit.expectSystemExitWithStatus(23);
        new CommandLine(new App()).parseWithHandler(new RunFirst().andExit(23));
    }

    @Test
    public void testExitCodeFromExceptionHandler() {
        @Command class App implements Runnable {
            public void run() {
                throw new ParameterException(new CommandLine(this), "blah");
            }
        }
        exit.expectSystemExitWithStatus(25);
        new CommandLine(new App()).parseWithHandlers(new RunFirst().andExit(23),
                                                    new DefaultExceptionHandler().andExit(25));
    }

}
