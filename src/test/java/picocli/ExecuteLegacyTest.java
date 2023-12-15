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
import org.junit.contrib.java.lang.system.Assertion;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.rules.TestRule;
import picocli.CommandLine.Model.CommandSpec;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import static java.lang.String.format;
import static org.junit.Assert.*;
import static picocli.CommandLine.Command;
import static picocli.CommandLine.DefaultExceptionHandler;
import static picocli.CommandLine.ExecutionException;
import static picocli.CommandLine.Help;
import static picocli.CommandLine.IExceptionHandler2;
import static picocli.CommandLine.IParseResultHandler;
import static picocli.CommandLine.IParseResultHandler2;
import static picocli.CommandLine.InitializationException;
import static picocli.CommandLine.Option;
import static picocli.CommandLine.ParameterException;
import static picocli.CommandLine.Parameters;
import static picocli.CommandLine.ParseResult;
import static picocli.CommandLine.RunAll;
import static picocli.CommandLine.RunFirst;
import static picocli.CommandLine.RunLast;
import static picocli.CommandLine.Spec;

@SuppressWarnings("deprecation")
public class ExecuteLegacyTest {
    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    // allows tests to set any kind of properties they like, without having to individually roll them back
    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");

    @Rule
    public final SystemErrRule systemErrRule = new SystemErrRule().enableLog().muteForSuccessfulTests();

    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule().enableLog().muteForSuccessfulTests();


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
        verifyAllFail(factory, "Parsed command (picocli.ExecuteLegacyTest$", ") is not a Method, Runnable or Callable", args);
    }

    @Test
    public void testParseWithHandlerRunXxxWithSubcommandFailsWithMissingSubcommandIfNotRunnableOrCallable() {
        @Command class App {
            @Parameters String[] params;
            @Command void sub() {}
        }
        Factory factory = new Factory() {
            public Object create() {return new App();}
        };
        String expected = String.format("" +
                "Missing required subcommand%n" +
                "Usage: <main class> [<params>...] [COMMAND]%n" +
                "      [<params>...]%n" +
                "Commands:%n" +
                "  sub%n");

        IParseResultHandler[] handlers = new IParseResultHandler[] {
                new RunFirst(), new RunLast(), new RunAll()
        };
        for (IParseResultHandler handler : handlers) {
            String[] args = { "abc" };
            this.systemErrRule.clearLog();
            new CommandLine(factory.create()).parseWithHandler(handler, System.err, args);
            assertEquals(expected, systemErrRule.getLog());
        }
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
        verifyAllFail(factory, "Error while running command (picocli.ExecuteLegacyTest$",
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
        verifyAllFail(factory, "Error while calling command (picocli.ExecuteLegacyTest$",
                "): java.lang.IllegalStateException: TEST EXCEPTION2", new String[0]);
    }

    private void verifyAllFail(Factory factory, String prefix, String suffix, String[] args) {
        verifyAllFail(factory, prefix, suffix, args, ExecutionException.class);
    }

    @SuppressWarnings("deprecation")
    private void verifyAllFail(Factory factory, String prefix, String suffix, String[] args, Class<? extends Exception> xClass) {
        IParseResultHandler[] handlers = new IParseResultHandler[] {
                new RunFirst(), new RunLast(), new RunAll()
        };
        for (IParseResultHandler handler : handlers) {
            String descr = handler.getClass().getSimpleName();
            try {
                new CommandLine(factory.create()).parseWithHandler(handler, System.out, args);
                fail(descr + ": expected exception");
            } catch (Exception ex) {
                assertTrue("Exception class " + ex.getClass().getSimpleName(), xClass.isAssignableFrom(ex.getClass()));
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
        verifyReturnValueForBuiltInHandlers(factory, Collections.emptyList(), new String[] {"-h"});
        verifyReturnValueForBuiltInHandlers(factory, Collections.emptyList(), new String[] {"-V"});
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
        verifyReturnValueForBuiltInHandlers(factory, Arrays.asList("RETURN VALUE"), new String[0]);
    }

    interface CommandLineFactory {
        CommandLine create();
    }

    @SuppressWarnings("deprecation")
    private void verifyReturnValueForBuiltInHandlers(CommandLineFactory factory, Object expected, String[] args) {
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
    public void testParseWithHandler2RunXxxReturnsNullIfHelpRequested() {
        @Command(version = "abc 1.3.4")
        class App implements Callable<Object> {
            @Option(names = "-h", usageHelp = true) boolean requestHelp;
            @Option(names = "-V", versionHelp = true) boolean requestVersion;
            public Object call() { return "RETURN VALUE"; }
        }
        CommandLineFactory factory = new CommandLineFactory() {
            public CommandLine create() {return new CommandLine(new App());}
        };
        verifyReturnValueForBuiltInHandlers2(factory, null, new String[] {"-h"});
        verifyReturnValueForBuiltInHandlers2(factory, null, new String[] {"-V"});
    }

    @Test
    public void testParseWithHandle2rRunXxxReturnsCallableResult() {
        @Command
        class App implements Callable<Object> {
            public Object call() { return "RETURN VALUE"; }
        }
        CommandLineFactory factory = new CommandLineFactory() {
            public CommandLine create() {return new CommandLine(new App());}
        };
        verifyReturnValueForBuiltInHandlers2(factory, Arrays.asList("RETURN VALUE"), new String[0]);
    }

    private void verifyReturnValueForBuiltInHandlers2(CommandLineFactory factory, Object expected, String[] args) {
        IParseResultHandler2<?>[] handlers = new IParseResultHandler2[] {
                new RunFirst(), new RunLast(), new RunAll()
        };
        PrintStream out = new PrintStream(new ByteArrayOutputStream());
        for (IParseResultHandler2<?> handler : handlers) {
            String descr = handler.getClass().getSimpleName();
            Object actual = factory.create().parseWithHandler(handler, args);
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

        Object actual1 = factory.create().parseWithHandler(new RunFirst(), new String[] {"sub"});
        assertEquals("RunFirst: return value", Arrays.asList("RETURN VALUE"), actual1);

        Object actual2 = factory.create().parseWithHandler(new RunLast(), new String[] {"sub"});
        assertEquals("RunLast: return value", Arrays.asList("SUB RETURN VALUE"), actual2);

        Object actual3 = factory.create().parseWithHandler(new RunAll(), new String[] {"sub"});
        assertEquals("RunAll: return value", Arrays.asList("RETURN VALUE", "SUB RETURN VALUE"), actual3);
    }
    @Test
    public void testParseWithHandler2RunXxxReturnsCallableResultWithSubcommand() {
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

        Object actual1 = factory.create().parseWithHandler(new RunFirst(), new String[]{"sub"});
        assertEquals("RunFirst: return value", Arrays.asList("RETURN VALUE"), actual1);

        Object actual2 = factory.create().parseWithHandler(new RunLast(), new String[]{"sub"});
        assertEquals("RunLast: return value", Arrays.asList("SUB RETURN VALUE"), actual2);

        Object actual3 = factory.create().parseWithHandler(new RunAll(), new String[]{"sub"});
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
        StringPrintStream sps = new StringPrintStream();
        System.setErr(sps.stream());
        CommandLine.run(new App(), System.err, "-number", "not a number");
        System.setErr(oldErr);

        assertFalse(runWasCalled[0]);
        assertEquals(String.format(
                "Invalid value for option '-number': 'not a number' is not an int%n" +
                        "Usage: <main class> [-number=<number>]%n" +
                        "      -number=<number>%n"), sps.toString());
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
        StringPrintStream sps = new StringPrintStream();
        System.setErr(sps.stream());
        Boolean callResult = CommandLine.call(new App(), System.err, "-number", "not a number");
        System.setErr(oldErr);

        assertNull(callResult);
        assertEquals(String.format(
                "Invalid value for option '-number': 'not a number' is not an int%n" +
                        "Usage: <main class> [-number=<number>]%n" +
                        "      -number=<number>%n"), sps.toString());
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
        new CommandLine(new App()).parseWithHandler(new RunFirst().andExit(23), new String[]{});
    }

    @Test
    public void testExitCodeFromParseResultHandler2() {
        @Command class App implements Runnable {
            public void run() {
            }
        }
        MyHandler handler = new MyHandler();
        new CommandLine(new App()).parseWithHandler(handler.andExit(23), new String[]{});
        assertEquals(23, handler.exitCode);
    }
    static class MyHandler extends RunLast {
        int exitCode;

        @Override
        protected void exit(int exitCode) {
            this.exitCode = exitCode;
        }
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
                                                    defaultExceptionHandler().andExit(25));
        assertEquals(format("" +
                "blah%n"
                ), systemErrRule.getLog());
    }

    @SuppressWarnings("deprecation")
    private DefaultExceptionHandler<List<Object>> defaultExceptionHandler() {
        return new DefaultExceptionHandler<List<Object>>();
    }

    @Test
    public void testExitCodeFromExceptionHandler2() {
        @Command class App implements Runnable {
            public void run() {
                throw new ParameterException(new CommandLine(this), "blah");
            }
        }
        CustomExceptionHandler<List<Object>> handler = new CustomExceptionHandler<List<Object>>();
        new CommandLine(new App()).parseWithHandlers(new RunFirst().andExit(23), handler.andExit(25));
        assertEquals(format("" +
                        "blah%n" +
                        "Usage: <main class>%n"), systemErrRule.getLog());
        assertEquals(25, handler.exitCode);
    }

    @SuppressWarnings("deprecation")
    static class CustomExceptionHandler<R> extends DefaultExceptionHandler<R> {
        int exitCode;

        @Override
        protected void exit(int exitCode) {
            this.exitCode = exitCode;
        }
    }

    @Test
    public void testExitCodeFromExceptionHandler3() {
        @Command class App implements Runnable {
            public void run() {
                throw new ParameterException(new CommandLine(this), "blah");
            }
        }
        CustomNoThrowExceptionHandler<List<Object>> handler = new CustomNoThrowExceptionHandler<List<Object>>();
        new CommandLine(new App()).parseWithHandlers(new RunFirst().andExit(23), handler.andExit(25));
        assertEquals(format("" +
                "blah%n" +
                "Usage: <main class>%n"), systemErrRule.getLog());
        assertEquals(25, handler.exitCode);
    }

    @SuppressWarnings("deprecation")
    static class CustomNoThrowExceptionHandler<R> extends DefaultExceptionHandler<R> {
        int exitCode;
        ExecutionException caught;

        @Override
        protected R throwOrExit(ExecutionException ex) {
            try {
                super.throwOrExit(ex);
            } catch (ExecutionException caught) {
                this.caught = caught;
            }
            return null;
        }

        @Override
        protected void exit(int exitCode) {
            this.exitCode = exitCode;
        }
    }

    @Test
    public void testWithoutSystemExitForOtherExceptions() {
        @Command class App implements Runnable {
            public void run() {
                throw new RuntimeException("blah");
            }
        }
        CustomNoThrowExceptionHandler<List<Object>> handler = new CustomNoThrowExceptionHandler<List<Object>>();
        new CommandLine(new App()).parseWithHandlers(new RunFirst().andExit(23), handler);
        assertTrue(handler.caught.getCause() instanceof RuntimeException);
        assertEquals("blah", handler.caught.getCause().getMessage());
    }

    @Test
    public void testSystemExitForOtherExceptions() {
        @Command class App implements Runnable {
            public void run() {
                throw new RuntimeException("blah");
            }
        }
        exit.expectSystemExitWithStatus(25);
        exit.checkAssertionAfterwards(new Assertion() {
            public void checkAssertion() {
                String actual = systemErrRule.getLog();
                assertTrue(actual.startsWith("picocli.CommandLine$ExecutionException: Error while running command (picocli.ExecuteLegacyTest"));
                assertTrue(actual.contains("java.lang.RuntimeException: blah"));
            }
        });
        new CommandLine(new App()).parseWithHandlers(new RunFirst().andExit(23),
                defaultExceptionHandler().andExit(25));
    }

    @Test(expected = InternalError.class)
    public void testNoSystemExitForErrors() {
        @Command class App implements Runnable {
            public void run() {
                throw new InternalError("blah");
            }
        }
        new CommandLine(new App()).parseWithHandlers(new RunFirst().andExit(23),
                defaultExceptionHandler().andExit(25));
    }

    @Command(name = "mycmd", mixinStandardHelpOptions = true, version = "MyCallable-1.0")
    static class MyCallable implements Callable<Object> {
        @Option(names = "-x", description = "this is an option")
        String option;
        public Object call() { throw new IllegalStateException("this is a test"); }
    }

    @Command(name = "mycmd", mixinStandardHelpOptions = true, version = "MyRunnable-1.0")
    static class MyRunnable implements Runnable {
        @Option(names = "-x", description = "this is an option")
        String option;
        public void run() { throw new IllegalStateException("this is a test"); }
    }
    private static final String MYCALLABLE_USAGE = format("" +
            "Usage: mycmd [-hV] [-x=<option>]%n" +
            "  -h, --help      Show this help message and exit.%n" +
            "  -V, --version   Print version information and exit.%n" +
            "  -x=<option>     this is an option%n");

    private static final String INVALID_INPUT = format("" +
            "Unmatched argument at index 0: 'invalid input'%n");

    private static final String MYCALLABLE_INVALID_INPUT = INVALID_INPUT + MYCALLABLE_USAGE;

    private static final String MYCALLABLE_USAGE_ANSI = Help.Ansi.ON.new Text(format("" +
            "Usage: @|bold mycmd|@ [@|yellow -hV|@] [@|yellow -x|@=@|italic <option>|@]%n" +
            "  @|yellow -h|@, @|yellow --help|@      Show this help message and exit.%n" +
            "  @|yellow -V|@, @|yellow --version|@   Print version information and exit.%n" +
            "  @|yellow -x|@=@|italic <option>|@     this is an option%n")).toString();

    private static final String INVALID_INPUT_ANSI = Help.Ansi.ON.new Text(format("" +
            "@|fg(red),bold Unmatched argument at index 0: 'invalid input'|@%n")).toString();

    @Test
    public void testCall1WithInvalidInput() {
        CommandLine.call(new MyCallable(), "invalid input");
        assertEquals(MYCALLABLE_INVALID_INPUT, systemErrRule.getLog());
        assertEquals("", systemOutRule.getLog());
    }

    @Test
    public void testCall2WithInvalidInput() {
        CommandLine.call(new MyCallable(), System.out, "invalid input");
        assertEquals(MYCALLABLE_INVALID_INPUT, systemErrRule.getLog());
        assertEquals("", systemOutRule.getLog());
    }

    @Test
    public void testCall3WithInvalidInput() {
        CommandLine.call(new MyCallable(), System.out, Help.Ansi.ON, "invalid input");
        assertEquals(INVALID_INPUT_ANSI + MYCALLABLE_USAGE_ANSI, systemErrRule.getLog());
        assertEquals("", systemOutRule.getLog());
    }

    @Test
    public void testCall4WithInvalidInput() {
        CommandLine.call(new MyCallable(), System.out, System.err, Help.Ansi.ON, "invalid input");
        assertEquals(INVALID_INPUT_ANSI + MYCALLABLE_USAGE_ANSI, systemErrRule.getLog());
        assertEquals("", systemOutRule.getLog());
    }

    @Test
    public void testCall4WithInvalidInput_ToStdout() {
        CommandLine.call(new MyCallable(), System.out, System.out, Help.Ansi.ON, "invalid input");
        assertEquals("", systemErrRule.getLog());
        assertEquals(INVALID_INPUT_ANSI + MYCALLABLE_USAGE_ANSI, systemOutRule.getLog());
    }

    @Test
    public void testCall1DefaultExceptionHandlerRethrows() {
        try {
            CommandLine.call(new MyCallable(), "-x abc");
            fail("Expected exception");
        } catch (ExecutionException ex) {
            String cmd = ((Object) ex.getCommandLine().getCommand()).toString();
            String msg = "Error while calling command (" + cmd + "): java.lang.IllegalStateException: this is a test";
            assertEquals(msg, ex.getMessage());
        }
        assertEquals("", systemErrRule.getLog());
        assertEquals("", systemOutRule.getLog());
    }


    @Test
    public void testCall1WithHelpRequest() {
        CommandLine.call(new MyCallable(), "--help");
        assertEquals("", systemErrRule.getLog());
        assertEquals(MYCALLABLE_USAGE, systemOutRule.getLog());
    }

    @Test
    public void testCall2WithHelpRequest() {
        CommandLine.call(new MyCallable(), System.out, "--help");
        assertEquals("", systemErrRule.getLog());
        assertEquals(MYCALLABLE_USAGE, systemOutRule.getLog());
    }

    @Test
    public void testCall3WithHelpRequest() {
        CommandLine.call(new MyCallable(), System.out, Help.Ansi.ON, "--help");
        assertEquals("", systemErrRule.getLog());
        assertEquals(MYCALLABLE_USAGE_ANSI, systemOutRule.getLog());
    }

    @Test
    public void testCall3WithHelpRequest_ToStderr() {
        CommandLine.call(new MyCallable(), System.err, Help.Ansi.ON, "--help");
        assertEquals(MYCALLABLE_USAGE_ANSI, systemErrRule.getLog());
        assertEquals("", systemOutRule.getLog());
    }

    @Test
    public void testCall3WithHelpRequest_ToCustomStream() {
        StringPrintStream sps = new StringPrintStream();
        CommandLine.call(new MyCallable(), sps.stream(), Help.Ansi.ON, "--help");
        assertEquals(MYCALLABLE_USAGE_ANSI, sps.toString());
        assertEquals("", systemErrRule.getLog());
        assertEquals("", systemOutRule.getLog());
    }

    @Test
    public void testCall4WithHelpRequest() {
        CommandLine.call(new MyCallable(), System.out, System.err, Help.Ansi.ON, "--help");
        assertEquals("", systemErrRule.getLog());
        assertEquals(MYCALLABLE_USAGE_ANSI, systemOutRule.getLog());
    }

    @Test
    public void testCall4WithHelpRequest_ToStderr() {
        CommandLine.call(new MyCallable(), System.err, System.out, Help.Ansi.ON, "--help");
        assertEquals(MYCALLABLE_USAGE_ANSI, systemErrRule.getLog());
        assertEquals("", systemOutRule.getLog());
    }

    @Test
    public void testCall4WithHelpRequest_ToCustomStream() {
        StringPrintStream sps = new StringPrintStream();
        CommandLine.call(new MyCallable(), sps.stream(), System.out, Help.Ansi.ON, "--help");
        assertEquals(MYCALLABLE_USAGE_ANSI, sps.toString());
        assertEquals("", systemErrRule.getLog());
        assertEquals("", systemOutRule.getLog());
    }
    //---

    @Test
    public void testRun1WithInvalidInput() {
        CommandLine.run(new MyRunnable(), "invalid input");
        assertEquals(MYCALLABLE_INVALID_INPUT, systemErrRule.getLog());
        assertEquals("", systemOutRule.getLog());
    }

    @Test
    public void testRun2WithInvalidInput() {
        CommandLine.run(new MyRunnable(), System.out, "invalid input");
        assertEquals(MYCALLABLE_INVALID_INPUT, systemErrRule.getLog());
        assertEquals("", systemOutRule.getLog());
    }

    @Test
    public void testRun3WithInvalidInput() {
        CommandLine.run(new MyRunnable(), System.out, Help.Ansi.ON, "invalid input");
        assertEquals(INVALID_INPUT_ANSI + MYCALLABLE_USAGE_ANSI, systemErrRule.getLog());
        assertEquals("", systemOutRule.getLog());
    }

    @Test
    public void testRun4WithInvalidInput() {
        CommandLine.run(new MyRunnable(), System.out, System.err, Help.Ansi.ON, "invalid input");
        assertEquals(INVALID_INPUT_ANSI + MYCALLABLE_USAGE_ANSI, systemErrRule.getLog());
        assertEquals("", systemOutRule.getLog());
    }

    @Test
    public void testRun4WithInvalidInput_ToStdout() {
        CommandLine.run(new MyRunnable(), System.out, System.out, Help.Ansi.ON, "invalid input");
        assertEquals("", systemErrRule.getLog());
        assertEquals(INVALID_INPUT_ANSI + MYCALLABLE_USAGE_ANSI, systemOutRule.getLog());
    }

    @Test
    public void testRun1WithHelpRequest() {
        CommandLine.run(new MyRunnable(), "--help");
        assertEquals("", systemErrRule.getLog());
        assertEquals(MYCALLABLE_USAGE, systemOutRule.getLog());
    }

    @Test
    public void testRun2WithHelpRequest() {
        CommandLine.run(new MyRunnable(), System.out, "--help");
        assertEquals("", systemErrRule.getLog());
        assertEquals(MYCALLABLE_USAGE, systemOutRule.getLog());
    }

    @Test
    public void testRun3WithHelpRequest() {
        CommandLine.run(new MyRunnable(), System.out, Help.Ansi.ON, "--help");
        assertEquals("", systemErrRule.getLog());
        assertEquals(MYCALLABLE_USAGE_ANSI, systemOutRule.getLog());
    }

    @Test
    public void testRun3WithHelpRequest_ToStderr() {
        CommandLine.run(new MyRunnable(), System.err, Help.Ansi.ON, "--help");
        assertEquals(MYCALLABLE_USAGE_ANSI, systemErrRule.getLog());
        assertEquals("", systemOutRule.getLog());
    }

    @Test
    public void testRun3WithHelpRequest_ToCustomStream() {
        StringPrintStream sps = new StringPrintStream();
        CommandLine.run(new MyRunnable(), sps.stream(), Help.Ansi.ON, "--help");
        assertEquals(MYCALLABLE_USAGE_ANSI, sps.toString());
        assertEquals("", systemErrRule.getLog());
        assertEquals("", systemOutRule.getLog());
    }

    @Test
    public void testRun4WithHelpRequest() {
        CommandLine.run(new MyRunnable(), System.out, System.err, Help.Ansi.ON, "--help");
        assertEquals("", systemErrRule.getLog());
        assertEquals(MYCALLABLE_USAGE_ANSI, systemOutRule.getLog());
    }

    @Test
    public void testRun4WithHelpRequest_ToStderr() {
        CommandLine.run(new MyRunnable(), System.err, System.out, Help.Ansi.ON, "--help");
        assertEquals(MYCALLABLE_USAGE_ANSI, systemErrRule.getLog());
        assertEquals("", systemOutRule.getLog());
    }

    @Test
    public void testRun4WithHelpRequest_ToCustomStream() {
        StringPrintStream sps = new StringPrintStream();
        CommandLine.run(new MyRunnable(), sps.stream(), System.out, Help.Ansi.ON, "--help");
        assertEquals(MYCALLABLE_USAGE_ANSI, sps.toString());
        assertEquals("", systemErrRule.getLog());
        assertEquals("", systemOutRule.getLog());
    }

    @Test
    public void testCallWithFactory() {
        Runnable[] variations = new Runnable[] {
                new Runnable() {public void run() {CommandLine.call(MyCallable.class, new InnerClassFactory(this), "-x", "a");}},
                new Runnable() {public void run() {CommandLine.call(MyCallable.class, new InnerClassFactory(this), System.out, "-x", "a");}},
                new Runnable() {public void run() {CommandLine.call(MyCallable.class, new InnerClassFactory(this), System.out, Help.Ansi.OFF, "-x", "a");}},
                new Runnable() {public void run() {CommandLine.call(MyCallable.class, new InnerClassFactory(this), System.out, System.out, Help.Ansi.OFF, "-x", "a");}},
        };
        for (Runnable r : variations) {
            try {
                r.run();
                fail("Expected exception");
            } catch (ExecutionException ex) {
                assertTrue(ex.getMessage().startsWith("Error while calling command (picocli.ExecuteLegacyTest$MyCallable"));
                assertTrue(ex.getCause() instanceof IllegalStateException);
                assertEquals("this is a test", ex.getCause().getMessage());
            }
        }
    }

    @Test
    public void testCallWithFactoryVersionHelp() {
        CommandLine.call(MyCallable.class, new InnerClassFactory(this), "--version");
        assertEquals(String.format("MyCallable-1.0%n"), systemOutRule.getLog());
        assertEquals("", systemErrRule.getLog());
        systemOutRule.clearLog();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final PrintStream ps = new PrintStream(baos);
        Runnable[] variations = new Runnable[] {
                new Runnable() {public void run() {CommandLine.call(MyCallable.class, new InnerClassFactory(this), ps, "--version");}},
                new Runnable() {public void run() {CommandLine.call(MyCallable.class, new InnerClassFactory(this), ps, Help.Ansi.OFF, "--version");}},
                new Runnable() {public void run() {CommandLine.call(MyCallable.class, new InnerClassFactory(this), ps, System.out, Help.Ansi.OFF, "--version");}},
        };
        for (Runnable r : variations) {
            assertEquals("", baos.toString());
            r.run();
            assertEquals(String.format("MyCallable-1.0%n"), baos.toString());
            baos.reset();
            assertEquals("", systemErrRule.getLog());
            assertEquals("", systemOutRule.getLog());
        }
    }

    @Test
    public void testCallWithFactoryInvalidInput() {
        String expected = String.format("" +
                "Missing required parameter for option '-x' (<option>)%n" +
                "Usage: mycmd [-hV] [-x=<option>]%n" +
                "  -h, --help      Show this help message and exit.%n" +
                "  -V, --version   Print version information and exit.%n" +
                "  -x=<option>     this is an option%n");
        Runnable[] variations = new Runnable[] {
                new Runnable() {public void run() {CommandLine.call(MyCallable.class, new InnerClassFactory(this), "-x");}},
                new Runnable() {public void run() {CommandLine.call(MyCallable.class, new InnerClassFactory(this), System.out, "-x");}},
                new Runnable() {public void run() {CommandLine.call(MyCallable.class, new InnerClassFactory(this), System.out, Help.Ansi.OFF, "-x");}},
        };
        for (Runnable r : variations) {
            assertEquals("", systemErrRule.getLog());
            assertEquals("", systemOutRule.getLog());
            systemErrRule.clearLog();
            systemOutRule.clearLog();
            r.run();
            assertEquals(expected, systemErrRule.getLog());
            assertEquals("", systemOutRule.getLog());

            systemErrRule.clearLog();
            systemOutRule.clearLog();
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final PrintStream ps = new PrintStream(baos);
        CommandLine.call(MyCallable.class, new InnerClassFactory(this), System.out, ps, Help.Ansi.OFF, "-x");
        assertEquals(expected, baos.toString());
        assertEquals("", systemOutRule.getLog());
        assertEquals("", systemErrRule.getLog());
    }

    @Test
    public void testRunWithFactory() {
        Runnable[] variations = new Runnable[] {
                new Runnable() {public void run() {CommandLine.run(MyRunnable.class, new InnerClassFactory(this), "-x", "a");}},
                new Runnable() {public void run() {CommandLine.run(MyRunnable.class, new InnerClassFactory(this), System.out, "-x", "a");}},
                new Runnable() {public void run() {CommandLine.run(MyRunnable.class, new InnerClassFactory(this), System.out, Help.Ansi.OFF, "-x", "a");}},
                new Runnable() {public void run() {CommandLine.run(MyRunnable.class, new InnerClassFactory(this), System.out, System.out, Help.Ansi.OFF, "-x", "a");}},
        };
        for (Runnable r : variations) {
            try {
                r.run();
                fail("Expected exception");
            } catch (ExecutionException ex) {
                assertTrue(ex.getMessage(), ex.getMessage().startsWith("Error while running command (picocli.ExecuteLegacyTest$MyRunnable"));
                assertTrue(ex.getCause() instanceof IllegalStateException);
                assertEquals("this is a test", ex.getCause().getMessage());
            }
        }
    }

    @Test
    public void testRunWithFactoryVersionHelp() {
        CommandLine.run(MyRunnable.class, new InnerClassFactory(this), "--version");
        assertEquals(String.format("MyRunnable-1.0%n"), systemOutRule.getLog());
        assertEquals("", systemErrRule.getLog());
        systemOutRule.clearLog();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final PrintStream ps = new PrintStream(baos);
        Runnable[] variations = new Runnable[] {
                new Runnable() {public void run() {CommandLine.run(MyRunnable.class, new InnerClassFactory(this), ps, "--version");}},
                new Runnable() {public void run() {CommandLine.run(MyRunnable.class, new InnerClassFactory(this), ps, Help.Ansi.OFF, "--version");}},
                new Runnable() {public void run() {CommandLine.run(MyRunnable.class, new InnerClassFactory(this), ps, System.out, Help.Ansi.OFF, "--version");}},
        };
        for (Runnable r : variations) {
            assertEquals("", baos.toString());
            r.run();
            assertEquals(String.format("MyRunnable-1.0%n"), baos.toString());
            baos.reset();
            assertEquals("", systemErrRule.getLog());
            assertEquals("", systemOutRule.getLog());
        }
    }

    @Test
    public void testRunWithFactoryInvalidInput() {
        String expected = String.format("" +
                "Missing required parameter for option '-x' (<option>)%n" +
                "Usage: mycmd [-hV] [-x=<option>]%n" +
                "  -h, --help      Show this help message and exit.%n" +
                "  -V, --version   Print version information and exit.%n" +
                "  -x=<option>     this is an option%n");
        Runnable[] variations = new Runnable[] {
                new Runnable() {public void run() {CommandLine.run(MyRunnable.class, new InnerClassFactory(this), "-x");}},
                new Runnable() {public void run() {CommandLine.run(MyRunnable.class, new InnerClassFactory(this), System.out, "-x");}},
                new Runnable() {public void run() {CommandLine.run(MyRunnable.class, new InnerClassFactory(this), System.out, Help.Ansi.OFF, "-x");}},
        };
        for (Runnable r : variations) {
            assertEquals("", systemErrRule.getLog());
            assertEquals("", systemOutRule.getLog());
            systemErrRule.clearLog();
            systemOutRule.clearLog();
            r.run();
            assertEquals(expected, systemErrRule.getLog());
            assertEquals("", systemOutRule.getLog());

            systemErrRule.clearLog();
            systemOutRule.clearLog();
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final PrintStream ps = new PrintStream(baos);
        CommandLine.run(MyRunnable.class, new InnerClassFactory(this), System.out, ps, Help.Ansi.OFF, "-x");
        assertEquals(expected, baos.toString());
        assertEquals("", systemOutRule.getLog());
        assertEquals("", systemErrRule.getLog());
    }

    @Test
    public void testExecutionExceptionIfRunnableThrowsExecutionException() {
        @Command
        class App implements Runnable {
            @Spec CommandSpec spec;
            public void run() {
                throw new ExecutionException(spec.commandLine(), "abc");
            }
        }
        try {
            CommandLine.run(new App());
            fail("Expected exception");
        } catch (ExecutionException ex) {
            assertEquals("abc", ex.getMessage());
        }
    }

    @Test
    public void testExecutionExceptionIfCallableThrowsExecutionException() {
        @Command
        class App implements Callable<Void> {
            @Spec CommandSpec spec;
            public Void call() {
                throw new ExecutionException(spec.commandLine(), "abc");
            }
        }
        try {
            CommandLine.call(new App());
            fail("Expected exception");
        } catch (ExecutionException ex) {
            assertEquals("abc", ex.getMessage());
        }
    }

    @Test
    public void testParameterExceptionIfRunnablePrintsUsageHelp() {
        @Command
        class App implements Runnable {
            @Spec CommandSpec spec;
            public void run() {
                throw new ParameterException(spec.commandLine(), "xxx");
            }
        }
        CommandLine.run(new App());
        String expected = String.format("" +
                "xxx%n" +
                "Usage: <main class>%n");
        assertEquals(expected, systemErrRule.getLog());
    }

    @Test
    public void testParameterExceptionIfCallablePrintsUsageHelp() {
        @Command
        class App implements Callable<Void> {
            @Spec CommandSpec spec;
            public Void call() {
                throw new ParameterException(spec.commandLine(), "xxx");
            }
        }
        CommandLine.call(new App());
        String expected = String.format("" +
                "xxx%n" +
                "Usage: <main class>%n");
        assertEquals(expected, systemErrRule.getLog());
    }

    @Test
    public void testRunAllSelf() {
        RunAll runAll = new RunAll();
        assertSame(runAll, runAll.self());
    }

    @Test
    public void testHandlerThrowsOtherException() {
        @Command
        class App { }

        try {
            new CommandLine(new App()).parseWithHandler(new IParseResultHandler2<Object>() {
                public Object handleParseResult(ParseResult parseResult) throws ExecutionException {
                    throw new IllegalArgumentException("abc");
                }
            }, new String[0]);
            fail("Expected exception");
        } catch (IllegalArgumentException ex) {
            assertEquals("abc", ex.getMessage());
        }
    }

    @Test
    public void testHandlerThrowsExecutionException() {
        @Command
        class App { }

        try {
            new CommandLine(new App()).parseWithHandler(new IParseResultHandler2<Object>() {
                public Object handleParseResult(ParseResult parseResult) throws ExecutionException {
                    throw new ExecutionException(new CommandLine(new App()), "xyz");
                }
            }, new String[0]);
            fail("Expected exception");
        } catch (ExecutionException ex) {
            assertEquals("xyz", ex.getMessage());
        }
    }

    @Test
    public void testHandlerThrowsExecutionException2() {
        @Command
        class App { }

        IParseResultHandler2<Void> handler = new IParseResultHandler2<Void>() {
            public Void handleParseResult(ParseResult parseResult) throws ExecutionException {
                throw new ExecutionException(new CommandLine(new App()), "xyz");
            }
        };
        IExceptionHandler2<Void> exceptionHandler = new IExceptionHandler2<Void>() {
            public Void handleParseException(ParameterException ex, String[] args) {
                return null;
            }
            public Void handleExecutionException(ExecutionException ex, ParseResult parseResult) {
                throw ex;
            }
        };
        try {
            new CommandLine(new App()).parseWithHandlers(handler, exceptionHandler, new String[0]);
            fail("Expected exception");
        } catch (ExecutionException ex) {
            assertEquals("xyz", ex.getMessage());
        }
    }

    @Command
    static class Executable implements Runnable, Callable<Void> {

        @Option(names = "-x") int x;

        public void run() { }

        public Void call() throws Exception {
            return null;
        }
    }

    @Test
    public void testCallNullResult() {
        Object result = CommandLine.call(new Executable(), "-x");
        assertNull(result);
    }

    @Test
    public void testCallableClassNullResult() {
        Object result = CommandLine.call(Executable.class, CommandLine.defaultFactory(), "-x");
        assertNull(result);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDefaultExceptionHandler_handleExecutionException() {
        final int[] outerExitCode = {-1};
        DefaultExceptionHandler h = new DefaultExceptionHandler() {
            @Override
            protected void exit(int exitCode) {
                outerExitCode[0] = exitCode;
            }
        };
        h.andExit(9876);

        @Command class App {}
        CommandLine cmd = new CommandLine(new App());
        ExecutionException ex = new ExecutionException(cmd, "boo");
        Object result = h.handleExecutionException(ex, null);
        assertNull(result);
        assertEquals(9876, outerExitCode[0]);
    }
}
