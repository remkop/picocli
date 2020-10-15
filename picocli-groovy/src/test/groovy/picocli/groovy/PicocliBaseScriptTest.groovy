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

import java.nio.charset.Charset

import static org.junit.Assert.*

/**
 * @author Jim White
 * @author Remko Popma
 * @since 2.0
 */
public class PicocliBaseScriptTest {
    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");

    @SourceURI URI sourceURI

    @Test
    void testParameterizedScript() {
        GroovyShell shell = new GroovyShell()
        shell.context.setVariable('args',
                ["--codepath", "/usr/x.jar", "-cp", "/bin/y.jar", "-cp", "z", "--", "placeholder", "another"] as String[])
        def result = shell.evaluate '''
@groovy.transform.BaseScript(picocli.groovy.PicocliBaseScript)
import groovy.transform.Field
import picocli.CommandLine

@CommandLine.Parameters
@Field List<String> parameters

@CommandLine.Option(names = ["-cp", "--codepath"])
@Field List<String> codepath = []

//println parameters
//println codepath

assert parameters == ['placeholder', 'another']
assert codepath == ['/usr/x.jar', '/bin/y.jar', 'z']

[parameters.size(), codepath.size()]
'''
        assert result == [2, 3]
    }

    @Test
    void testSimpleCommandScript() {
        GroovyShell shell = new GroovyShell()
        shell.context.setVariable('args',
                [ "--codepath", "/usr/x.jar", "placeholder", "-cp", "/bin/y.jar", "another" ] as String[])
        def result = shell.evaluate(new File(new File(sourceURI).parentFile, 'SimpleCommandScriptTest.groovy'))
        assert result == [777]
    }

    @Test
    void testRunnableSubcommand() {
        GroovyShell shell = new GroovyShell()
        shell.context.setVariable('args',
                [ "-verbose=2", "commit", "--amend", "--author=Remko", "MultipleCommandScriptTest.groovy"] as String[])
        def result = shell.evaluate(new File(new File(sourceURI).parentFile, 'MultipleCommandScriptTest.groovy'))
        assert result == ["MultipleCommandScriptTest.groovy"]
    }

    @Test
    void testCallableSubcommand() {
        GroovyShell shell = new GroovyShell()
        shell.context.setVariable('args',
                [ "-verbose=2", "add", "-i", "zoos" ] as String[])
        def result = shell.evaluate(new File(new File(sourceURI).parentFile, 'MultipleCommandScriptTest.groovy'))
        assert result == ["zoos"]
    }

    @Test
    void testScriptInvalidInputUsageHelpToStderr() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream()
        System.setErr(new PrintStream(baos))

        GroovyShell shell = new GroovyShell()
        shell.context.setVariable('args', ["--unknownOption"] as String[])
        shell.evaluate '''
@picocli.groovy.PicocliScript
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
@picocli.groovy.PicocliScript
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
@picocli.groovy.PicocliScript

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
        def result = shell.evaluate(new File(new File(sourceURI).parentFile, 'ScriptWithoutImports.groovy'))
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
@picocli.groovy.PicocliScript
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
        ByteArrayOutputStream baos = new ByteArrayOutputStream()
        System.setErr(new PrintStream(baos))

        String script = '''
@picocli.CommandLine.Command
@picocli.groovy.PicocliScript
import groovy.transform.Field
import picocli.CommandLine

throw new IllegalStateException("Hi this is a test exception")
'''
        GroovyShell shell = new GroovyShell()
        shell.context.setVariable('args', [] as String[])
        try {
            shell.evaluate script
            fail("Expected exception")
        } catch (ExecutionException ex) {
            assert "java.lang.IllegalStateException: Hi this is a test exception" == ex.getMessage()
            assert ex.getCause() instanceof IllegalStateException
        }
    }

    @Test
    void testScriptExecutionException() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream()
        System.setErr(new PrintStream(baos))

        String script = '''
@picocli.CommandLine.Command
@picocli.groovy.PicocliScript
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
@picocli.groovy.PicocliScript
import picocli.CommandLine

public Object handleExecutionException(CommandLine commandLine, String[] args, Exception ex) {
    return ex
}
    
throw new CommandLine.ExecutionException(new CommandLine(this), "Hi this is a test handleExecutionException")
'''
        GroovyShell shell = new GroovyShell()
        shell.context.setVariable('args', [] as String[])
        def result = shell.evaluate script
        assert result instanceof ExecutionException
        assert "Hi this is a test handleExecutionException" == result.getMessage()
        assert result.getCause() == null
    }

    @Test
    void testScriptBindingNullCommandLine() {

        Binding binding = new Binding()
        binding.setProperty("commandLine", null)
        ByteArrayOutputStream baos = new ByteArrayOutputStream()
        System.setErr(new PrintStream(baos))

        String script = '''
@picocli.CommandLine.Command
@picocli.groovy.PicocliScript
import groovy.transform.Field
import picocli.CommandLine

throw new IllegalStateException("Hi this is a test exception")
'''
        GroovyShell shell = new GroovyShell(binding)
        shell.context.setVariable('args', [] as String[])
        try {
            shell.evaluate script
            fail("Expected exception")
        } catch (ExecutionException ex) {
            assert "java.lang.IllegalStateException: Hi this is a test exception" == ex.getMessage()
        }
    }

    private class Params {
        @CommandLine.Parameters String[] positional
        @CommandLine.Option(names = "-o") option
    }

    @Test
    void testScriptBindingCommandLine() {

        CommandLine commandLine = new CommandLine(new Params())
        Binding binding = new Binding()
        binding.setProperty("commandLine", commandLine)
        ByteArrayOutputStream baos = new ByteArrayOutputStream()
        System.setErr(new PrintStream(baos))

        String script = '''
@picocli.CommandLine.Command
@picocli.groovy.PicocliScript
import groovy.transform.Field
import picocli.CommandLine

throw new IllegalStateException("Hi this is a test exception")
'''
        GroovyShell shell = new GroovyShell(binding)
        shell.context.setVariable('args', ["-o=hi", "123"] as String[])
        try {
            shell.evaluate script
            fail("Expected exception")
        } catch (ExecutionException ex) {
            assert "java.lang.IllegalStateException: Hi this is a test exception" == ex.getMessage()
        }
        Params params = commandLine.command
        assert params.option == "hi"
        assert params.positional.contains("123")
    }

    @Test
    void testCommandLinePropertyIsSetByBaseScript() {
        def script = '''
@picocli.CommandLine.Command
@picocli.groovy.PicocliScript
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
@picocli.groovy.PicocliScript
import groovy.transform.Field

@Option(names = ['-g', '--greeting'], description = 'Type of greeting')
@Field String greeting = 'Hello\'

println "${greeting} world!"

class Message {
    String greeting
    String target
}
'''
        GroovyShell shell = new GroovyShell(new Binding())
        shell.context.setVariable('args', ["-g", "Hi"] as String[])

        ByteArrayOutputStream baos = new ByteArrayOutputStream()
        System.setOut(new PrintStream(baos))
        shell.evaluate script
        assertEquals("Hi world!", baos.toString().trim())
    }


    @Test
    public void testSubcommandMethods1191() {

        String script = '''
//@Grab('info.picocli:picocli-groovy:4.5.1')
//@GrabConfig(systemClassLoader = true)
@Command(name = "picocli",
        mixinStandardHelpOptions = true,
        version = '1.0.0',
        subcommands = [ HelpCommand.class ],
        description = 'sub command test')

@picocli.groovy.PicocliScript
import static picocli.CommandLine.*

@Override
Object run() {
    return getOrCreateCommandLine().execute(getScriptArguments())
}

@Command(description = "Record changes to the repository")
void commit(@Option(names = ["-m", "--message"]) String commitMessage,
            @Option(names = "--squash", paramLabel = "<commit>") String squash,
            @Parameters(paramLabel = "<file>") File[] files) {

    println "commit ${files}"
}

println "done"
'''
        GroovyShell shell = new GroovyShell(new Binding())
        shell.context.setVariable('args', ["commit", "picocli.groovy"] as String[])

        ByteArrayOutputStream baos = new ByteArrayOutputStream()
        System.setOut(new PrintStream(baos))
        Object result = shell.evaluate script
        assertEquals("commit [picocli.groovy]", baos.toString().trim())
        assertEquals(0, result)
    }
}
