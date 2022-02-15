/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package picocli.groovy

import groovy.transform.SourceURI
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.contrib.java.lang.system.ProvideSystemProperty
import picocli.CommandLine
import picocli.CommandLine.ExecutionException

import static org.junit.Assert.assertEquals
import static org.junit.Assert.fail

/**
 * @author Remko Popma
 * @since 4.6
 */
public class PicocliBaseScript2Test {
    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");

    @SourceURI URI sourceURI

    @Test
    void testParameterizedScript() {
        GroovyShell shell = new GroovyShell()
        shell.context.setVariable('args',
                ["--codepath", "/usr/x.jar", "-cp", "/bin/y.jar", "-cp", "z", "--", "placeholder", "another"] as String[])
        def result = shell.evaluate '''
@groovy.transform.BaseScript(picocli.groovy.PicocliBaseScript2)
import groovy.transform.Field
import picocli.CommandLine

@CommandLine.Parameters
@Field List<String> parameters

@CommandLine.Option(names = ["-cp", "--codepath"])
@Field List<String> codepath = []

assert parameters == ['placeholder', 'another']
assert codepath == ['/usr/x.jar', '/bin/y.jar', 'z']

[parameters.size(), codepath.size()]
'''
        assert result == [2, 3]
    }

    @Test
    void testSimpleCommandScript2() {
        GroovyShell shell = new GroovyShell()
        shell.context.setVariable('args',
                [ "--codepath", "/usr/x.jar", "placeholder", "-cp", "/bin/y.jar", "another" ] as String[])
        def result = shell.evaluate(new File(new File(sourceURI).parentFile, 'SimpleCommandScript2Test.groovy'))
        assert result == [777]
    }

    @Test
    void testRunnableSubcommand() {
        GroovyShell shell = new GroovyShell()
        shell.context.setVariable('args',
                [ "-verbose=2", "commit", "--amend", "--author=Remko", "MultipleCommandScriptTest2.groovy"] as String[])
        def result = shell.evaluate(new File(new File(sourceURI).parentFile, 'MultipleCommandScriptTest2.groovy'))
        assert result == null
    }

    @Test
    void testCallableSubcommand() {
        GroovyShell shell = new GroovyShell()
        shell.context.setVariable('args',
                [ "-verbose=2", "add", "-i", "zoos" ] as String[])
        def result = shell.evaluate(new File(new File(sourceURI).parentFile, 'MultipleCommandScriptTest2.groovy'))
        assert result == ["zoos"]
    }

    @Test
    void testScriptInvalidInputUsageHelpToStderr() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream()
        System.setErr(new PrintStream(baos))

        GroovyShell shell = new GroovyShell()
        shell.context.setVariable('args', ["--unknownOption"] as String[])
        shell.evaluate '''
@picocli.groovy.PicocliScript2
import groovy.transform.Field
import picocli.CommandLine

@CommandLine.Option(names = ["-x", "--requiredOption"], required = true, description = "this option is required")
@Field String requiredOption
'''
        String expected = String.format("" +
                "args: [--unknownOption]%n" +
                "Missing required option: '--requiredOption=<requiredOption>'%n" +
                "Usage: Script1 -x=<requiredOption>%n" +
                "  -x, --requiredOption=<requiredOption>%n" +
                "         this option is required%n")
        assert expected == baos.toString()
    }

    @Test
    void testScriptRequestedUsageHelpToStdout() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream()
        System.setOut(new PrintStream(baos))

        GroovyShell shell = new GroovyShell()
        shell.context.setVariable('args', ["--help"] as String[])
        shell.evaluate '''
@picocli.groovy.PicocliScript2
import groovy.transform.Field
import picocli.CommandLine

@CommandLine.Option(names = ["-h", "--help"], usageHelp = true)
@Field boolean usageHelpRequested
'''
        String expected = String.format("" +
                "Usage: Script1 [-h]%n" +
                "  -h, --help%n")
        assert expected == baos.toString()
    }

    @Test
    void testPicocliScriptAnnotationOnFieldWithoutImport1() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream()
        System.setOut(new PrintStream(baos))

        GroovyShell shell = new GroovyShell()
        shell.context.setVariable('args', ["--help"] as String[])
        shell.evaluate '''
@picocli.CommandLine.Command(name = 'cmd', description = 'my description')
@picocli.groovy.PicocliScript2

@picocli.CommandLine.Option(names = ["-h", "--help"], usageHelp = true)
@groovy.transform.Field boolean usageHelpRequested
'''
        String expected = String.format("" +
                "Usage: cmd [-h]%n" +
                "my description%n" +
                "  -h, --help%n")
        assert expected == baos.toString()
    }

    @Test
    void testPicocliScriptAnnotationOnFieldWithoutImport2() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream()
        System.setOut(new PrintStream(baos))

        GroovyShell shell = new GroovyShell()
        shell.context.setVariable('args', [ "-h" ] as String[])
        def result = shell.evaluate(new File(new File(sourceURI).parentFile, 'ScriptWithoutImports2.groovy'))
        assert result == null // help is invoked so script is not run

        String expected = String.format("" +
                "Usage: cmd [-h]%n" +
                "my description%n" +
                "  -h, --help%n")
        assert expected == baos.toString()
    }

    @Test
    void testScriptRequestedVersionHelpToStdout() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream()
        System.setOut(new PrintStream(baos))

        GroovyShell shell = new GroovyShell()
        shell.context.setVariable('args', ["--version"] as String[])
        shell.evaluate '''
@picocli.CommandLine.Command(version = "best version ever v1.2.3")
@picocli.groovy.PicocliScript2
import groovy.transform.Field
import picocli.CommandLine

@CommandLine.Option(names = ["-V", "--version"], versionHelp = true)
@Field boolean usageHelpRequested
'''
        String expected = String.format("" +
                "best version ever v1.2.3%n")
        assert expected == baos.toString()
    }

    @Test
    void testScriptExecutionExceptionWrapsException() {
        String script = '''
@picocli.CommandLine.Command
@picocli.groovy.PicocliScript2
import groovy.transform.Field
import picocli.CommandLine

throw new IllegalStateException("Hi this is a test exception")
'''
        GroovyShell shell = new GroovyShell()
        shell.context.setVariable('args', [] as String[])
        try {
            shell.evaluate script
            fail("Expected exception")
        } catch (IllegalStateException ex) {
            assert "Hi this is a test exception" == ex.getMessage()
            assert ex instanceof IllegalStateException
        }
    }

    @Test
    void testScriptExecutionException() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream()
        System.setErr(new PrintStream(baos))

        String script = '''
@picocli.CommandLine.Command
@picocli.groovy.PicocliScript2
import groovy.transform.Field
import picocli.CommandLine

throw new CommandLine.ExecutionException(new CommandLine(this), "Hi this is a test ExecutionException")
'''
        GroovyShell shell = new GroovyShell()
        shell.context.setVariable('args', [] as String[])
        try {
            shell.evaluate script
            fail("Expected exception")
        } catch (ExecutionException ex) {
            assert "Hi this is a test ExecutionException" == ex.getMessage()
            assert ex.getCause() == null
        }
    }

    @Test
    void testScriptCallsHandleExecutionException() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream()
        System.setErr(new PrintStream(baos))

        String script = '''
@picocli.CommandLine.Command
@picocli.groovy.PicocliScript2
import picocli.CommandLine
    
throw new CommandLine.ExecutionException(new CommandLine(this), "Hi this is a test ExecutionException")
'''
        GroovyShell shell = new GroovyShell()
        shell.context.setVariable('args', [] as String[])
        try {
            shell.evaluate script
            fail("Expected exception")
        } catch (ExecutionException ex) {
            assert "Hi this is a test ExecutionException" == ex.getMessage()
        }
    }

    @Test
    void testScriptBindingNullCommandLine() {

        Binding binding = new Binding()
        binding.setProperty("commandLine", null)
        ByteArrayOutputStream baos = new ByteArrayOutputStream()
        System.setErr(new PrintStream(baos))

        String script = '''
@picocli.CommandLine.Command
@picocli.groovy.PicocliScript2
import groovy.transform.Field
import picocli.CommandLine

throw new IllegalStateException("Hi this is a test exception")
'''
        GroovyShell shell = new GroovyShell(binding)
        shell.context.setVariable('args', [] as String[])
        try {
            shell.evaluate script
            fail("Expected exception")
        } catch (IllegalStateException ex) {
            assert "Hi this is a test exception" == ex.getMessage()
        }
    }

    private class Params {
        @CommandLine.Parameters String[] positional
        @CommandLine.Option(names = "-o") option
    }

    @Test
    void testScriptBindingCommandLineMustBeRunnableOrCallable() {

        CommandLine commandLine = new CommandLine(new Params())
        Binding binding = new Binding()
        binding.setProperty("commandLine", commandLine)
        ByteArrayOutputStream baos = new ByteArrayOutputStream()
        System.setErr(new PrintStream(baos))

        String script = '''
@picocli.CommandLine.Command
@picocli.groovy.PicocliScript2
import groovy.transform.Field
import picocli.CommandLine

throw new IllegalStateException("THIS IS NEVER REACHED")
'''
        GroovyShell shell = new GroovyShell(binding)
        shell.context.setVariable('args', ["-o=hi", "123"] as String[])
        try {
            shell.evaluate script
            fail("Expected exception")
        } catch (ExecutionException ex) {
            String msg = ex.getMessage()
            String expectPrefix = 'Parsed command (picocli.groovy.PicocliBaseScript2Test$Params'
            String expectSuffix = ' is not a Method, Runnable or Callable'
            assert msg.startsWith(expectPrefix)
            assert msg.endsWith(expectSuffix)
        }
        Params params = commandLine.command
        assert params.option == "hi"
        assert params.positional.contains("123")
    }

    @Test
    void testCommandLinePropertyIsSetByBaseScript() {
        def script = '''
@picocli.CommandLine.Command
@picocli.groovy.PicocliScript2
import groovy.transform.Field
import picocli.CommandLine
import picocli.CommandLine.Parameters

@Parameters(description = "some parameters")
@Field List<String> parameters

@Field CommandLine commandLine;

commandLine
'''
        GroovyShell shell = new GroovyShell(new Binding())
        shell.context.setVariable('args', ["Hi"] as String[])

        ByteArrayOutputStream baos = new ByteArrayOutputStream()
        System.setOut(new PrintStream(baos))
        def result = shell.evaluate script

        assert result && result instanceof CommandLine
        assert ((CommandLine) result).parseResult.matchedPositional(0).value == ["Hi"]
    }


    @Test
    void testScriptWithInnerClass() {
        String script = '''
import static picocli.CommandLine.*
@Command(name="classyTest")
@picocli.groovy.PicocliScript2
import groovy.transform.Field

@Option(names = ['-g', '--greeting'], description = 'Type of greeting')
@Field String greeting = 'Hello\'

println "${greeting} world!"

class Message {
    String greeting
    String target
}
'''
        GroovyShell shell = new GroovyShell()
        shell.context.setVariable('args', ["-g", "Hi"] as String[])

        ByteArrayOutputStream baos = new ByteArrayOutputStream()
        System.setOut(new PrintStream(baos))
        shell.evaluate script
        assertEquals("Hi world!", baos.toString().trim())
    }

    String SCRIPT_WITH_SUBCOMMAND_METHODS = '''
@Command(name = "picocli",
        mixinStandardHelpOptions = true,
        version = '1.0.0',
        subcommands = [ HelpCommand.class ],
        description = 'sub command test')

@picocli.groovy.PicocliScript2
import static picocli.CommandLine.*

@Command(description = "Record changes to the repository")
int commit(@Option(names = ["-m", "--message"]) String commitMessage,
            @Option(names = "--squash", paramLabel = "<commit>") String squash,
            @Parameters(paramLabel = "<file>") File[] files) {

    println "commit ${files}"
    0
}

println "done"
'''

    @Test
    void testSubcommandMethods1191() {

        GroovyShell shell = new GroovyShell()
        shell.context.setVariable('args', ["commit", "picocli.groovy"] as String[])

        ByteArrayOutputStream baos = new ByteArrayOutputStream()
        System.setOut(new PrintStream(baos))
        Object result = shell.evaluate SCRIPT_WITH_SUBCOMMAND_METHODS
        assertEquals("commit [picocli.groovy]", baos.toString().trim())
        assertEquals(0, result)
    }

    @Test
    void testSubcommandMethodHelp() {
        GroovyShell shell = new GroovyShell()
        shell.context.setVariable('args', ["help", "commit"] as String[])

        ByteArrayOutputStream baos = new ByteArrayOutputStream()
        System.setOut(new PrintStream(baos))
        Object result = shell.evaluate SCRIPT_WITH_SUBCOMMAND_METHODS
        String expected = "" +
                "Usage: picocli commit [-m=<arg0>] [--squash=<commit>] [<file>...]%n" +
                "Record changes to the repository%n" +
                "      [<file>...]%n" +
                "  -m, --message=<arg0>%n" +
                "      --squash=<commit>"
        assertEquals(String.format(expected), baos.toString().trim())
        assertEquals(null, result)
    }

    @Test
    void testMultipleSubcommandMethodsHelp() {
        GroovyShell shell = new GroovyShell()
        shell.context.setVariable('args', ["--help"] as String[])

        ByteArrayOutputStream baos = new ByteArrayOutputStream()
        System.setOut(new PrintStream(baos))
        Object result = shell.evaluate SCRIPT_WITH_SUBCOMMAND_METHODS
        String expected = "" +
                "Usage: picocli [-hV] [COMMAND]%n" +
                "sub command test%n" +
                "  -h, --help      Show this help message and exit.%n" +
                "  -V, --version   Print version information and exit.%n" +
                "Commands:%n" +
                "  help    Display help information about the specified command.%n" +
                "  commit  Record changes to the repository"
        assertEquals(String.format(expected), baos.toString().trim())
        assertEquals(null, result)
    }

    @Test
    public void testBeforeParseArgs() {
        String script = '''
import picocli.CommandLine.Model.CommandSpec
import picocli.CommandLine
import static picocli.CommandLine.*

@Command(name="testBeforeParseArgs")
@picocli.groovy.PicocliScript2
import groovy.transform.Field

@picocli.CommandLine.Spec
@Field CommandSpec spec;

@Option(names = ['-g', '--greeting'], description = 'Type of greeting')
@Field String greeting = 'Hello'

println "${greeting} ${spec.name()} world!"

@Override
protected CommandLine beforeParseArgs(CommandLine customizable) {
    customizable.setCommandName("modifiedName")
    customizable.setOptionsCaseInsensitive(true)
    return super.beforeParseArgs(customizable)
}
'''
        GroovyShell shell = new GroovyShell()
        shell.context.setVariable('args', ["--GREETING", "Hi"] as String[])

        ByteArrayOutputStream baos = new ByteArrayOutputStream()
        System.setOut(new PrintStream(baos))
        shell.evaluate script
        assertEquals("Hi modifiedName world!", baos.toString().trim())
    }

    @Test
    public void testAfterExecutionHandlesExceptionAndReturnsResult() {
        String script = '''
@Command(name="testAfterExecution")
@picocli.groovy.PicocliScript2
import picocli.CommandLine
import static picocli.CommandLine.*

throw new IllegalStateException("I am illegal!!")

@Override
protected Object afterExecution(CommandLine commandLine, int exitCode, Exception exception) {
    println("exitCode=${exitCode}, exceptionMessage=${exception.getMessage()}")
    12345
}
'''
        GroovyShell shell = new GroovyShell()
        shell.context.setVariable('args', [] as String[])

        ByteArrayOutputStream baos = new ByteArrayOutputStream()
        System.setOut(new PrintStream(baos))
        def scriptResult = shell.evaluate script
        assertEquals("exitCode=1, exceptionMessage=I am illegal!!", baos.toString().trim())
        assertEquals(12345, scriptResult)
    }

    @Test
    public void testAfterExecutionMayCallSystemExit() {
        String script = '''
@Command(name="testAfterExecution", exitCodeOnExecutionException = 54321)
@picocli.groovy.PicocliScript2
import picocli.CommandLine
import static picocli.CommandLine.*

throw new IllegalStateException("I am illegal!!")

@Override
protected Object afterExecution(CommandLine commandLine, int exitCode, Exception exception) {
    println("System.exit(${exitCode})")
    //System.exit(exitCode)
    exitCode
}
'''
        GroovyShell shell = new GroovyShell()
        shell.context.setVariable('args', [] as String[])

        ByteArrayOutputStream baos = new ByteArrayOutputStream()
        System.setOut(new PrintStream(baos))
        def scriptResult = shell.evaluate script
        assertEquals("System.exit(54321)", baos.toString().trim())
        assertEquals(54321, scriptResult)
    }


    @Test
    public void testCompletionCandidatesEnum() {
        String script = '''
import picocli.CommandLine.Model.CommandSpec
import picocli.CommandLine
import static picocli.CommandLine.*

@Command(name="testCompletionCandidates")
@picocli.groovy.PicocliScript2
import groovy.transform.Field

@picocli.CommandLine.Spec
@Field CommandSpec spec;

enum MyType { A, B, C}

@Option(names = '-x')
@Field MyType x

Iterable<String> iter = spec.findOption("-x").completionCandidates()
println iter
'''
        GroovyShell shell = new GroovyShell()
        shell.context.setVariable('args', [] as String[])

        ByteArrayOutputStream baos = new ByteArrayOutputStream()
        System.setOut(new PrintStream(baos))
        shell.evaluate script
        assertEquals("[A, B, C]", baos.toString().trim())
    }

    @Test
    public void testCompletionCandidatesWithClosure() {
        String script = '''
import picocli.CommandLine.Model.CommandSpec
import picocli.CommandLine
import static picocli.CommandLine.*

@Command(name="testCompletionCandidatesWithClosure")
@picocli.groovy.PicocliScript2
import groovy.transform.Field

@picocli.CommandLine.Spec
@Field CommandSpec spec;

@Option(names = '-s', completionCandidates = {["A", "B", "C"]})
@Field String s

Iterable<String> iter = spec.findOption("-s").completionCandidates()
println iter
'''
        GroovyShell shell = new GroovyShell()
        shell.context.setVariable('args', [] as String[])

        ByteArrayOutputStream baos = new ByteArrayOutputStream()
        System.setOut(new PrintStream(baos))
        shell.evaluate script
        assertEquals("[A, B, C]", baos.toString().trim())
    }

}
