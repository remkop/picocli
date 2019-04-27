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
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.contrib.java.lang.system.SystemOutRule;
import picocli.CommandLine.AbstractParseResultHandler;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.IExecutionExceptionHandler;
import picocli.CommandLine.IExitCodeExceptionMapper;
import picocli.CommandLine.IExitCodeGenerator;
import picocli.CommandLine.IParameterExceptionHandler;
import picocli.CommandLine.Model.CommandSpec;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import static java.lang.String.format;
import static org.junit.Assert.*;
import static picocli.CommandLine.Command;
import static picocli.CommandLine.ExecutionException;
import static picocli.CommandLine.Help;
import static picocli.CommandLine.IExecutionStrategy;
import static picocli.CommandLine.Option;
import static picocli.CommandLine.ParameterException;
import static picocli.CommandLine.Parameters;
import static picocli.CommandLine.ParseResult;
import static picocli.CommandLine.RunAll;
import static picocli.CommandLine.RunFirst;
import static picocli.CommandLine.RunLast;
import static picocli.CommandLine.Spec;

public class ExecuteTest {
    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");

    @Rule
    public final SystemErrRule systemErrRule = new SystemErrRule().enableLog().muteForSuccessfulTests();

    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule().enableLog().muteForSuccessfulTests();

    interface Factory { Object create(); }
    @Test
    public void testExecutionStrategyRunXxxFailsIfNotRunnableOrCallable() {
        @Command class App {
            @Parameters String[] params;
        }
        Factory factory = new Factory() {
            public Object create() {return new App();}
        };
        String[] args = { "abc" };
        verifyAllFail(factory, "Parsed command (picocli.ExecuteTest$",
                ") is not a Method, Runnable or Callable", args);
    }

    @Test
    public void testExecutionStrategyRunXxxCatchesAndRethrowsExceptionFromRunnable() {
        @Command class App implements Runnable {
            @Parameters String[] params;
            public void run() { throw new IllegalStateException("TEST EXCEPTION"); }
        }
        Factory factory = new Factory() {
            public Object create() {return new App();}
        };
        verifyAllFail(factory, "TEST EXCEPTION", "", new String[0]);
    }

    @Test
    public void testExecutionStrategyRunXxxCatchesAndRethrowsExceptionFromCallable() {
        @Command class App implements Callable<Object> {
            @Parameters String[] params;
            public Object call() { throw new IllegalStateException("TEST EXCEPTION2"); }
        }
        Factory factory = new Factory() {
            public Object create() {return new App();}
        };
        verifyAllFail(factory, "TEST EXCEPTION2", "", new String[0]);
    }

    private void verifyAllFail(Factory factory, String prefix, String suffix, String[] args) {
        IExecutionStrategy[] strategies = new IExecutionStrategy[] {
                new RunFirst(), new RunLast(), new RunAll()
        };
        for (IExecutionStrategy strategy : strategies) {
            String descr = strategy.getClass().getSimpleName();
            try {
                new CommandLine(factory.create())
                        .setExecutionStrategy(strategy)
                        .tryExecute(args);
                fail(descr + ": expected exception");
            } catch (IllegalStateException ex) {
                String actual = ex.getMessage();
                assertTrue(descr + ": " + actual, actual.startsWith(prefix));
                assertTrue(descr + ": " + actual, actual.endsWith(suffix));
            } catch (ExecutionException ex) {
                String actual = ex.getMessage();
                assertTrue(descr + ": " + actual, actual.startsWith(prefix));
                assertTrue(descr + ": " + actual, actual.endsWith(suffix));
            } catch (Exception ex) {
                fail("Unexpected exception " + ex);
            }
        }
    }

    @Test
    public void testReturnDefaultExitCodeIfHelpRequested() {
        @Command(version = "abc 1.3.4")
        class App implements Callable<Object> {
            @Option(names = "-h", usageHelp = true) boolean requestHelp;
            @Option(names = "-V", versionHelp = true) boolean requestVersion;
            public Object call() { return "RETURN VALUE"; }
        }
        CommandLineFactory factory = new CommandLineFactory() {
            public CommandLine create() {return new CommandLine(new App());}
        };
        verifyExitCodeForBuiltInHandlers(factory, ExitCode.OK, new String[] {"-h"});
        verifyExitCodeForBuiltInHandlers(factory, ExitCode.OK, new String[] {"-V"});
    }

    @Test
    public void testReturnExitCodeFromAnnotationIfHelpRequested_NonNumericCallable() {
        @Command(version = "abc 1.3.4", exitCodeOnUsageHelp = 234, exitCodeOnVersionHelp = 543)
        class App implements Callable<Object> {
            @Option(names = "-h", usageHelp = true) boolean requestHelp;
            @Option(names = "-V", versionHelp = true) boolean requestVersion;
            public Object call() { return "RETURN VALUE"; }
        }
        CommandLineFactory factory = new CommandLineFactory() {
            public CommandLine create() {return new CommandLine(new App());}
        };
        verifyExitCodeForBuiltInHandlers(factory, 234, new String[] {"-h"});
        verifyExitCodeForBuiltInHandlers(factory, 543, new String[] {"-V"});
    }

    @Test
    public void testReturnExitCodeFromAnnotationIfHelpRequested_NumericCallable() {
        @Command(version = "abc 1.3.4", exitCodeOnUsageHelp = 234, exitCodeOnVersionHelp = 543)
        class App implements Callable<Object> {
            @Option(names = "-h", usageHelp = true) boolean requestHelp;
            @Option(names = "-V", versionHelp = true) boolean requestVersion;
            public Object call() { return 999; } // ignored (not executed)
        }
        CommandLineFactory factory = new CommandLineFactory() {
            public CommandLine create() {return new CommandLine(new App());}
        };
        verifyExitCodeForBuiltInHandlers(factory, 234, new String[] {"-h"});
        verifyExitCodeForBuiltInHandlers(factory, 543, new String[] {"-V"});
    }

    @Test
    public void testReturnDefaultExitCodeOnSuccess() {
        @Command
        class App implements Callable<Object> {
            public Object call() { return "RETURN VALUE"; }
        }
        CommandLineFactory factory = new CommandLineFactory() {
            public CommandLine create() {return new CommandLine(new App());}
        };
        verifyExitCodeForBuiltInHandlers(factory, ExitCode.OK, new String[0]);
    }

    @Test
    public void testReturnExitCodeFromAnnotationOnSuccess_NonNumericCallable() {
        @Command(exitCodeOnSuccess = 123)
        class App implements Callable<Object> {
            public Object call() { return "RETURN VALUE"; }
        }
        CommandLineFactory factory = new CommandLineFactory() {
            public CommandLine create() {return new CommandLine(new App());}
        };
        verifyExitCodeForBuiltInHandlers(factory, 123, new String[0]);
    }

    @Test
    public void testReturnExitCodeFromAnnotationOnSuccess_NumericCallable() {
        @Command(exitCodeOnSuccess = 123)
        class App implements Callable<Object> {
            public Object call() { return 987; }
        }
        CommandLineFactory factory = new CommandLineFactory() {
            public CommandLine create() {return new CommandLine(new App());}
        };
        verifyExitCodeForBuiltInHandlers(factory, 987, new String[0]);
    }

    interface CommandLineFactory {
        CommandLine create();
    }

    private void verifyExitCodeForBuiltInHandlers(CommandLineFactory factory, int expected, String[] args) {
        IExecutionStrategy[] strategies = new IExecutionStrategy[] {
                new RunFirst(), new RunLast(), new RunAll()
        };
        for (IExecutionStrategy strategy : strategies) {
            String descr = strategy.getClass().getSimpleName();
            int actual = factory.create().setExecutionStrategy(strategy).execute(args);
            assertEquals(descr + ": return value", expected, actual);
        }
        for (IExecutionStrategy strategy : strategies) {
            String descr = strategy.getClass().getSimpleName();
            try {
                int actual = factory.create().setExecutionStrategy(strategy).tryExecute(args);
                assertEquals(descr + ": return value", expected, actual);
            } catch (Exception ex) {
                fail("Unexpected exception " + ex);
            }
        }
    }

    @Test
    public void testRunsRunnableIfParseSucceeds() throws Exception {
        final int[] runWasCalled = {0};
        @Command class App implements Runnable {
            public void run() {
                runWasCalled[0]++;
            }
        }
        new CommandLine(new App()).execute();
        assertEquals(1, runWasCalled[0]);

        new CommandLine(new App()).tryExecute();
        assertEquals(2, runWasCalled[0]);
    }

    @Test
    public void testCallsCallableIfParseSucceeds() throws Exception {
        final int[] runWasCalled = {0};
        @Command class App implements Callable<Object> {
            public Object call() {
                return runWasCalled[0]++;
            }
        }
        new CommandLine(new App()).execute();
        assertEquals(1, runWasCalled[0]);

        new CommandLine(new App()).tryExecute();
        assertEquals(2, runWasCalled[0]);
    }

    @Test
    public void testInvokesMethodIfParseSucceeds() throws Exception {
        final int[] runWasCalled = {0};
        @Command class App {
            @Command
            public Object mySubcommand() {
                return runWasCalled[0]++;
            }
        }
        new CommandLine(new App()).execute("mySubcommand");
        assertEquals(1, runWasCalled[0]);

        new CommandLine(new App()).tryExecute("mySubcommand");
        assertEquals(2, runWasCalled[0]);
    }

    @Test
    public void testPrintErrorOnInvalidInput() throws Exception {
        final int[] runWasCalled = {0};
        class App implements Runnable {
            @Option(names = "-number") int number;
            public void run() {
                runWasCalled[0]++;
            }
        }
        {
            StringWriter sw = new StringWriter();
            new CommandLine(new App()).setErr(new PrintWriter(sw)).execute("-number", "not a number");

            assertEquals(0, runWasCalled[0]);
            assertEquals(String.format(
                    "Invalid value for option '-number': 'not a number' is not an int%n" +
                            "Usage: <main class> [-number=<number>]%n" +
                            "      -number=<number>%n"), sw.toString());
        }
        {
            StringWriter sw = new StringWriter();
            new CommandLine(new App()).setErr(new PrintWriter(sw)).tryExecute("-number", "not a number");

            assertEquals(0, runWasCalled[0]);
            assertEquals(String.format(
                    "Invalid value for option '-number': 'not a number' is not an int%n" +
                            "Usage: <main class> [-number=<number>]%n" +
                            "      -number=<number>%n"), sw.toString());
        }
    }

    @Test
    public void testReturnDefaultExitCodeOnInvalidInput() throws Exception {
        class App implements Callable<Boolean> {
            @Option(names = "-number") int number;
            public Boolean call() { return true; }
        }
        {
            int exitCode = new CommandLine(new App()).execute("-number", "not a number");
            assertEquals(ExitCode.USAGE, exitCode);
        }
        {
            int exitCode = new CommandLine(new App()).tryExecute("-number", "not a number");
            assertEquals(ExitCode.USAGE, exitCode);
        }
    }

    @Test
    public void testReturnExitCodeFromAnnotationOnInvalidInput_NumericCallable() throws Exception {
        @Command(exitCodeOnInvalidInput = 987)
        class App implements Callable<Boolean> {
            @Option(names = "-number") int number;
            public Boolean call() { return true; }
        }
        {
            int exitCode = new CommandLine(new App()).execute("-number", "not a number");
            assertEquals(987, exitCode);
        }
        {
            int exitCode = new CommandLine(new App()).tryExecute("-number", "not a number");
            assertEquals(987, exitCode);
        }
    }

    @Test
    public void testExitCodeFromParameterExceptionHandlerHandler() {
        @Command class App implements Runnable {
            public void run() {
                throw new ParameterException(new CommandLine(this), "blah");
            }
        }
        CustomParameterExceptionHandler handler = new CustomParameterExceptionHandler();
        int exitCode = new CommandLine(new App()).setParameterExceptionHandler(handler).execute();
        assertEquals(format("" +
                "Hi, this is my custom error message%n"), systemErrRule.getLog());
        assertEquals(125, exitCode);
    }

    static class CustomParameterExceptionHandler implements IParameterExceptionHandler {
        public int handleParseException(ParameterException ex, String[] args) throws Exception {
            ex.getCommandLine().getErr().println("Hi, this is my custom error message");
            return 125;
        }
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
            "Unmatched argument: invalid input%n");

    private static final String MYCALLABLE_INVALID_INPUT = INVALID_INPUT + MYCALLABLE_USAGE;

    private static final String MYCALLABLE_USAGE_ANSI = Help.Ansi.ON.new Text(format("" +
            "Usage: @|bold mycmd|@ [@|yellow -hV|@] [@|yellow -x|@=@|italic <option>|@]%n" +
            "  @|yellow -h|@, @|yellow --help|@      Show this help message and exit.%n" +
            "  @|yellow -V|@, @|yellow --version|@   Print version information and exit.%n" +
            "  @|yellow -x|@=@|italic <|@@|italic option>|@     this is an option%n")).toString();

    @Test
    public void testExecuteWithInvalidInput() {
        int exitCode = new CommandLine(new MyCallable()).execute("invalid input");
        assertEquals(MYCALLABLE_INVALID_INPUT, systemErrRule.getLog());
        assertEquals("", systemOutRule.getLog());
        assertEquals(ExitCode.USAGE, exitCode);
    }

    @Test
    public void testExecuteWithInvalidInput_Ansi_ON() {
        new CommandLine(new MyCallable())
                .setColorScheme(Help.defaultColorScheme(Help.Ansi.ON)).execute("invalid input");
        assertEquals(INVALID_INPUT + MYCALLABLE_USAGE_ANSI, systemErrRule.getLog());
        assertEquals("", systemOutRule.getLog());
    }

    @Test
    public void testExecuteWithInvalidInput_Ansi_ON_CustomErr() {
        new CommandLine(new MyCallable())
                .setErr(new PrintWriter(System.out, true))
                .setColorScheme(Help.defaultColorScheme(Help.Ansi.ON)).execute("invalid input");
        assertEquals("", systemErrRule.getLog());
        assertEquals(INVALID_INPUT + MYCALLABLE_USAGE_ANSI, systemOutRule.getLog());
    }

    @Test
    public void testErrIsSystemErrByDefault() {
        new CommandLine(new MyCallable()).getErr().println("hi");
        assertEquals(String.format("hi%n"), systemErrRule.getLog());
        assertEquals("", systemOutRule.getLog());
    }

    @Test
    public void testOutIsSystemOutByDefault() {
        new CommandLine(new MyCallable()).getOut().println("hi");
        assertEquals("", systemErrRule.getLog());
        assertEquals(String.format("hi%n"), systemOutRule.getLog());
    }

    @Test
    public void testExitCodeExceptionMapper_nullByDefault() {
        CommandLine cmd = new CommandLine(new MyCallable());
        assertNull(cmd.getExitCodeExceptionMapper());
    }

    @Test
    public void testExitCodeExceptionMapper() {
        @Command
        class MyCommand implements Callable  {
            public Void call() throws IOException {
                throw new IOException("error");
            }
        }
        IExitCodeExceptionMapper mapper = new IExitCodeExceptionMapper() {
            public int getExitCode(Throwable t) {
                if (t instanceof IOException && "error".equals(t.getMessage())) {
                    return 123;
                }
                return 987;
            }
        };
        CommandLine cmd = new CommandLine(new MyCommand());
        cmd.setExitCodeExceptionMapper(mapper);
        int exitCode = cmd.execute();
        assertEquals(123, exitCode);
    }

    @Test
    public void testExecuteCallableThrowsException() {
        int exitCode = new CommandLine(new MyCallable()).execute("-x", "abc");
        String cmd = "mycmd";
        String msg = "java.lang.IllegalStateException: this is a test";
        assertTrue(systemErrRule.getLog().startsWith(msg));
        assertEquals(ExitCode.SOFTWARE, exitCode);
    }


    @Test
    public void testExecuteCallableWithHelpRequest() {
        int exitCode = new CommandLine(new MyCallable()).execute("--help");
        assertEquals("", systemErrRule.getLog());
        assertEquals(MYCALLABLE_USAGE, systemOutRule.getLog());
        assertEquals(ExitCode.OK, exitCode);
    }

    @Test
    public void testExecuteCallableWithHelpRequest_Ansi_OFF_ToCustomWriter() {
        StringWriter sw = new StringWriter();
        int exitCode = new CommandLine(new MyCallable())
                .setOut(new PrintWriter(sw))
                .execute("--help");
        assertEquals("", systemErrRule.getLog());
        assertEquals("", systemOutRule.getLog());
        assertEquals(MYCALLABLE_USAGE, sw.toString());
        assertEquals(ExitCode.OK, exitCode);
    }

    @Test
    public void testExecuteCallableWithHelpRequest_Ansi_ON_ToCustomWriter() {
        StringWriter sw = new StringWriter();
        new CommandLine(new MyCallable())
                .setOut(new PrintWriter(sw))
                .setColorScheme(Help.defaultColorScheme(Help.Ansi.ON))
                .execute("--help");
        assertEquals("", systemErrRule.getLog());
        assertEquals("", systemOutRule.getLog());
        assertEquals(MYCALLABLE_USAGE_ANSI, sw.toString());
    }

    @Test
    public void testCallWithFactory() {
        try {
            new CommandLine(MyCallable.class, new InnerClassFactory(this)).tryExecute("-x", "a");
            fail("Expected exception");
        } catch (IllegalStateException ex) {
            assertEquals("this is a test", ex.getMessage());
        } catch (Exception ex) {
            fail("Unexpected exception " + ex);
        }
    }

    @Test
    public void testRunWithFactory() {
        try {
            new CommandLine(MyRunnable.class, new InnerClassFactory(this)).tryExecute("-x", "a");
            fail("Expected exception");
        } catch (IllegalStateException ex) {
            assertEquals("this is a test", ex.getMessage());
        } catch (Exception ex) {
            fail("Unexpected exception " + ex);
        }
    }

    @Test
    public void testCallWithFactoryVersionHelp() {
        new CommandLine(MyCallable.class, new InnerClassFactory(this)).execute("--version");
        assertEquals(String.format("MyCallable-1.0%n"), systemOutRule.getLog());
        assertEquals("", systemErrRule.getLog());
    }

    @Test
    public void testCallWithFactoryInvalidInput() {
        String expected = String.format("" +
                "Missing required parameter for option '-x' (<option>)%n" +
                "Usage: mycmd [-hV] [-x=<option>]%n" +
                "  -h, --help      Show this help message and exit.%n" +
                "  -V, --version   Print version information and exit.%n" +
                "  -x=<option>     this is an option%n");
        new CommandLine(MyCallable.class, new InnerClassFactory(this)).execute("-x");
        assertEquals(expected, systemErrRule.getLog());
        assertEquals("", systemOutRule.getLog());
    }

    @Test
    public void testRunWithFactoryVersionHelp() {
        new CommandLine(MyRunnable.class, new InnerClassFactory(this)).execute("--version");
        assertEquals(String.format("MyRunnable-1.0%n"), systemOutRule.getLog());
        assertEquals("", systemErrRule.getLog());
    }

    @Test
    public void testRunWithFactoryInvalidInput() {
        String expected = String.format("" +
                "Missing required parameter for option '-x' (<option>)%n" +
                "Usage: mycmd [-hV] [-x=<option>]%n" +
                "  -h, --help      Show this help message and exit.%n" +
                "  -V, --version   Print version information and exit.%n" +
                "  -x=<option>     this is an option%n");
        new CommandLine(MyRunnable.class, new InnerClassFactory(this)).execute("-x");
        assertEquals(expected, systemErrRule.getLog());
        assertEquals("", systemOutRule.getLog());
    }

    @Test
    public void testExecutionExceptionIfRunnableThrowsExecutionException() throws Exception {
        @Command
        class App implements Runnable {
            @Spec CommandSpec spec;
            public void run() {
                throw new ExecutionException(spec.commandLine(), "abc");
            }
        }
        try {
            new CommandLine(new App()).tryExecute();
            fail("Expected exception");
        } catch (ExecutionException ex) {
            assertEquals("abc", ex.getMessage());
        }
    }

    @Test
    public void testExecutionExceptionIfCallableThrowsExecutionException() throws Exception {
        @Command
        class App implements Callable<Void> {
            @Spec CommandSpec spec;
            public Void call() {
                throw new ExecutionException(spec.commandLine(), "abc");
            }
        }
        try {
            new CommandLine(new App()).tryExecute();
            fail("Expected exception");
        } catch (ExecutionException ex) {
            assertEquals("abc", ex.getMessage());
        }
    }

    @Test
    public void testNoParameterExceptionIfCallableThrowsParameterException() throws Exception {
        @Command
        class App implements Callable<Void> {
            @Spec CommandSpec spec;
            public Void call() {
                throw new ParameterException(spec.commandLine(), "xxx");
            }
        }
        int exitCode = new CommandLine(new App()).tryExecute();
        assertEquals(ExitCode.USAGE, exitCode);
    }

    @Test
    public void testRunAllSelf() {
        RunAll runAll = new RunAll();
        assertSame(runAll, runAll.self());
    }

    @Test
    public void testExecuteWhenExecutionStrategyThrowsOtherException() {
        @Command
        class App { }

        class FailingExecutionStrategy implements IExecutionStrategy {
            public int execute(ParseResult parseResult) throws ExecutionException {
                throw new IllegalArgumentException("abc");
            }
        }

        CommandLine cmd = new CommandLine(new App()).setExecutionStrategy(new FailingExecutionStrategy());
        assertEquals(ExitCode.SOFTWARE, cmd.execute());

        String prefix = String.format("" +
                "java.lang.IllegalArgumentException: abc%n" +
                "\tat picocli.ExecuteTest$1FailingExecutionStrategy.execute(ExecuteTest.java");
        assertTrue(systemErrRule.getLog().startsWith(prefix));

        systemErrRule.clearLog();
        systemOutRule.clearLog();
        try {
            cmd.tryExecute();
            fail("Expected exception");
        } catch (IllegalArgumentException ex) {
            assertEquals("abc", ex.getMessage());
        } catch (Exception ex) {
            fail("Unexpected exception " + ex);
        }
    }

    @Test
    public void testExecuteWhenExecutionStrategyThrowsExecutionException() {
        @Command
        class App { }

        class FailingExecutionStrategy implements IExecutionStrategy {
            public int execute(ParseResult parseResult) throws ExecutionException {
                throw new ExecutionException(new CommandLine(new App()), "abc");
            }
        }

        CommandLine cmd = new CommandLine(new App()).setExecutionStrategy(new FailingExecutionStrategy());
        assertEquals(ExitCode.SOFTWARE, cmd.execute());
        try {
            cmd.tryExecute();
            fail("Expected exception");
        } catch (ExecutionException ex) {
            assertEquals("abc", ex.getMessage());
        } catch (Exception ex) {
            fail("Unexpected exception " + ex);
        }
    }

    @Test
    public void testExecutionExceptionHandlerCanChangeExitCode() throws Exception {
        @Command
        class App { }

        IExecutionStrategy handler = new IExecutionStrategy() {
            public int execute(ParseResult parseResult) throws ExecutionException, ParameterException {
                throw new ExecutionException(new CommandLine(new App()), "xyz");
            }
        };
        IExecutionExceptionHandler exceptionHandler = new IExecutionExceptionHandler() {
            public int handleExecutionException(ExecutionException ex, ParseResult parseResult) {
                return 9876;
            }
        };
        CommandLine cmd = new CommandLine(new App()).setExecutionStrategy(handler).setExecutionExceptionHandler(exceptionHandler);
        assertEquals(9876, cmd.execute());
        assertEquals(9876, cmd.tryExecute());
    }

    @Test
    public void testDefaultExecutionExceptionHandlerRethrowsExceptions() throws Exception {
        @Command
        class App { }
        CommandLine cmd = new CommandLine(new App());
        ExecutionException ex = new ExecutionException(cmd, "", new InterruptedException("blah"));
        try {
            cmd.getExecutionExceptionHandler().handleExecutionException(ex, null);
            fail("Expected exception");
        } catch (InterruptedException e) {
            assertEquals("blah", e.getMessage());
        }
    }

    @Test
    public void testDefaultExecutionExceptionHandlerRethrowsExecutionExceptions() throws Exception {
        @Command
        class App { }
        CommandLine cmd = new CommandLine(new App());
        ExecutionException ex = new ExecutionException(cmd, "exception without a Cause");
        try {
            cmd.getExecutionExceptionHandler().handleExecutionException(ex, null);
            fail("Expected exception");
        } catch (ExecutionException e) {
            assertEquals("exception without a Cause", e.getMessage());
        }
    }

    @Test
    public void testDefaultExecutionExceptionHandlerRethrowsErrors() throws Exception {
        @Command
        class App { }
        CommandLine cmd = new CommandLine(new App());
        ExecutionException ex = new ExecutionException(cmd, "", new InternalError("blah"));
        try {
            cmd.getExecutionExceptionHandler().handleExecutionException(ex, null);
            fail("Expected error");
        } catch (InternalError e) {
            assertEquals("blah", e.getMessage());
        }
    }

    @Test
    public void testDefaultExecutionExceptionHandlerDoesNotThrowThrowables() throws Exception {
        @Command
        class App { }
        CommandLine cmd = new CommandLine(new App());
        ExecutionException ex = new ExecutionException(cmd, "", new Throwable("blah"));
        try {
            cmd.getExecutionExceptionHandler().handleExecutionException(ex, null);
            fail("Expected error");
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            assertFalse(cause instanceof Exception);
            assertFalse(cause instanceof Error);
            assertEquals("blah", cause.getMessage());
        }
    }

    @Test
    public void testPrintHelpIfRequested() {
        ParseResult parseResult = ParseResult.builder(new CommandLine(CommandSpec.create()).getCommandSpec()).build();
        assertFalse(CommandLine.printHelpIfRequested(parseResult));
    }

    @Test
    public void testParameterExceptionHandlerThrowsException() {
        IParameterExceptionHandler pah = new IParameterExceptionHandler() {
            public int handleParseException(ParameterException ex, String[] args) throws Exception {
                throw new IllegalStateException("blah");
            }
        };
        @Command class App implements Runnable {
            @Option(names = "-x") int x;
            public void run() { }
        }
        CommandLine cmd = new CommandLine(new App());
        cmd.setParameterExceptionHandler(pah);
        int exitCode = cmd.execute("-x");
        assertEquals(ExitCode.USAGE, exitCode);
        String expected = String.format("" +
                "java.lang.IllegalStateException: blah%n" +
                "\tat %s.handleParseException(", pah.getClass().getName());
        assertTrue(systemErrRule.getLog().startsWith(expected));
        assertEquals("", systemOutRule.getLog());
    }

    @Test
    public void testParameterExceptionHandlerThrowsException_WithMapper() {
        IParameterExceptionHandler pah = new IParameterExceptionHandler() {
            public int handleParseException(ParameterException ex, String[] args) throws Exception {
                throw new IllegalStateException("blah");
            }
        };
        @Command class App implements Runnable {
            @Option(names = "-x") int x;
            public void run() { }
        }
        IExitCodeExceptionMapper mapper = new IExitCodeExceptionMapper() {
            public int getExitCode(Throwable exception) {
                if (exception instanceof IllegalStateException) { return 1; }
                if (exception instanceof ParameterException) { return 2; }
                return 3;
            }
        };
        CommandLine cmd = new CommandLine(new App());
        cmd.setParameterExceptionHandler(pah);
        cmd.setExitCodeExceptionMapper(mapper);
        int exitCode = cmd.execute("-x");
        assertEquals(1, exitCode);
    }

    @Test
    public void testModifiedExecutionStrategy() {
        @Command class App implements Runnable {
            @Option(names = "-h", usageHelp = true) boolean help;
            public void run() { }
        }
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(bout);
        ByteArrayOutputStream berr = new ByteArrayOutputStream();
        PrintStream err = new PrintStream(berr);

        CommandLine cmd = new CommandLine(new App());
        cmd.setExecutionStrategy(new RunLast().useOut(out).useErr(err).useAnsi(Help.Ansi.OFF));
        int exitCode = cmd.execute("-h");

        assertEquals(ExitCode.OK, exitCode);
        String expected = String.format("" +
                "Usage: <main class> [-h]%n" +
                "  -h%n");
        assertEquals("", berr.toString());
        assertEquals(expected, bout.toString());
    }

    @Test
    public void testAbstractParseResultHandler_extractExitCodeGenerators() {
        AbstractParseResultHandler handler = new AbstractParseResultHandler() {
            protected Object handle(ParseResult parseResult) throws ExecutionException { return null; }
            protected CommandLine.AbstractHandler self() { return null; }
        };
        assertEquals(Collections.emptyList(), handler.extractExitCodeGenerators(null));
    }

    @Test
    public void testRunLastResolveExitCodeFromIExitCodeGenerator() {
        @Command class App implements Callable<Integer>, IExitCodeGenerator {
            private final int callResult;
            private final int exitResult;
            App(int callResult, int exitResult) {
                this.callResult = callResult;
                this.exitResult = exitResult;
            }
            public Integer call() { return callResult; }
            public int getExitCode() { return exitResult; }
        }
        assertEquals(3, new CommandLine(new App(2, 3)).execute());
        assertEquals(3, new CommandLine(new App(3, 2)).execute());
        assertEquals(-3, new CommandLine(new App(-2, -3)).execute());
        assertEquals(-3, new CommandLine(new App(-3, -2)).execute());
        assertEquals(-1, new CommandLine(new App(-1, 1)).execute());
    }

    @Command(name = "flex")
    static class ExitCodeGen implements Callable<Integer>, IExitCodeGenerator {
        @Option(names = "-n") boolean negate;
        ExitCodeGen() { }
        public Integer call() { return negate ? -6 : 6; }
        public int getExitCode() { return negate ? -5 : 5; }
    }
    @Command(subcommands = ExitCodeGen.class)
    static class Cmd implements Callable<Integer>, IExitCodeGenerator {
        private final int callResult;
        private final int exitResult;
        private final int subResult;
        Cmd(int callResult, int exitResult, int subResult) {
            this.callResult = callResult;
            this.exitResult = exitResult;
            this.subResult = subResult;
        }
        public Integer call() { return callResult; }
        public int getExitCode() { return exitResult; }
        @Command public int sub() { return subResult; }
    }

    @Test
    public void testRunAllResolveExitCodeFromIExitCodeGenerator() {
        assertEquals(2,  cmd(1, -1, 2).execute("sub"));
        assertEquals(2,  cmd(-1, 1, 2).execute("sub"));
        assertEquals(8,  cmd(7, 8, 9).execute()); // ignore 9: sub not invoked
        assertEquals(9,  cmd(7, 8, 9).execute("sub"));
        assertEquals(9,  cmd(9, 8, 7).execute()); // ignore 7: sub not invoked
        assertEquals(9,  cmd(9, 8, 7).execute("flex"));
        assertEquals(6,  cmd(1, 2, 3).execute("flex"));
        assertEquals(-3, cmd(-2, -3, -4).execute()); // ignore -4: sub not invoked
        assertEquals(-4, cmd(-2, -3, -4).execute("sub"));
        assertEquals(-3, cmd(-3, -2, -1).execute("sub"));
        assertEquals(6, cmd(-3, -2, -1).execute("flex"));
        assertEquals(-6, cmd(-3, -2, -1).execute("flex", "-n"));
    }
    private static CommandLine cmd(int callResult, int exitResult, int subResult) {
        return new CommandLine(new Cmd(callResult, exitResult, subResult)).setExecutionStrategy(new RunAll());
    }
}
