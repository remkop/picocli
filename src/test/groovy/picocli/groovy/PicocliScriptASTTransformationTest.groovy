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

package picocli.groovy

import org.junit.Ignore;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URI;

import org.junit.Test;

import groovy.lang.GroovyShell;
import groovy.transform.SourceURI;

import static org.junit.Assert.*;

public class PicocliScriptASTTransformationTest {
    @SourceURI
    URI sourceURI

    @Test
    void testHelp() {
        GroovyShell shell = new GroovyShell()
        shell.context.setVariable('args',
                ["--xyz"] as String[])
        ByteArrayOutputStream bytes = new ByteArrayOutputStream()
        System.setErr(new PrintStream(bytes))
        def result = shell.evaluate '''
@Command(name = "test-command", description = "tests help from a command script")
@PicocliScript
import groovy.transform.Field
import picocli.groovy.PicocliScript
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters

@Parameters(description = "some parameters")
@Field List<String> parameters

@Option(names = ["-cp", "--codepath"], description = "the codepath")
@Field List<String> codepath = []
'''

        def string = bytes.toString("UTF-8")
        assert string == String.format("" +
                "args: [--xyz]%n" +
                "Unmatched argument [--xyz]%n" +
                "Usage: test-command [-cp=<codepath>]... [<parameters>]...%n" +
                "tests help from a command script%n" +
                "      [<parameters>]...       some parameters%n" +
                "      -cp, --codepath=<codepath>%n" +
                "                              the codepath%n")
    }

    @Test
    void testAnnotatedPackage() {
        GroovyShell shell = new GroovyShell()
        shell.context.setVariable('args',
                ["-cp", "A", "-cp", "B"] as String[])
        ByteArrayOutputStream bytes = new ByteArrayOutputStream()
        System.setErr(new PrintStream(bytes))
        def result = shell.evaluate '''
@Command(name = "test-command", description = "tests help from a command script")
@PicocliScript
package anypackage;
import groovy.transform.Field
import picocli.groovy.PicocliScript
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters

@Parameters(description = "some parameters")
@Field List<String> parameters

@Option(names = ["-cp", "--codepath"], description = "the codepath")
@Field List<String> codepath = []

assert this.scriptCommandLine.commandName == "test-command"
codepath
'''
        assert result == ["A", "B"]
    }

    @Test
    void testAnnotatedImport() {
        GroovyShell shell = new GroovyShell()
        shell.context.setVariable('args',
                ["-cp", "A", "-cp", "B"] as String[])
        ByteArrayOutputStream bytes = new ByteArrayOutputStream()
        System.setErr(new PrintStream(bytes))
        def result = shell.evaluate '''
@Command(name = "test-command", description = "tests help from a command script")
@PicocliScript
import groovy.transform.Field
import picocli.groovy.PicocliScript
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters

@Parameters(description = "some parameters")
@Field List<String> parameters

@Option(names = ["-cp", "--codepath"], description = "the codepath")
@Field List<String> codepath = []

assert this.scriptCommandLine.commandName == "test-command"
codepath
'''
        assert result == ["A", "B"]
    }

    @Test
    void testAnnotatedImportWithValue() {
        GroovyShell shell = new GroovyShell()
        shell.context.setVariable('args',
                ["-cp", "A", "-cp", "B"] as String[])
        ByteArrayOutputStream bytes = new ByteArrayOutputStream()
        System.setErr(new PrintStream(bytes))
        def result = shell.evaluate '''
@Command(name = "test-command", description = "tests help from a command script")
@PicocliScript(picocli.groovy.PicocliBaseScript)
import groovy.transform.Field
import picocli.groovy.PicocliScript
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters

@Parameters(description = "some parameters")
@Field List<String> parameters

@Option(names = ["-cp", "--codepath"], description = "the codepath")
@Field List<String> codepath = []

assert this.scriptCommandLine.commandName == "test-command"
codepath
'''
        assert result == ["A", "B"]
    }

    @Test
    void testAnnotatedLocalVariable() {
        GroovyShell shell = new GroovyShell()
        shell.context.setVariable('args',
                ["-cp", "A", "-cp", "B"] as String[])
        ByteArrayOutputStream bytes = new ByteArrayOutputStream()
        System.setErr(new PrintStream(bytes))
        def result = shell.evaluate '''
import groovy.transform.Field
import picocli.groovy.PicocliBaseScript
import picocli.groovy.PicocliScript
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters

@Command(name = "test-command", description = "tests help from a command script")
@PicocliScript PicocliBaseScript cli;

@Parameters(description = "some parameters")
@Field List<String> parameters

@Option(names = ["-cp", "--codepath"], description = "the codepath")
@Field List<String> codepath = []

assert this.scriptCommandLine.commandName == "test-command"
codepath
'''
        assert result == ["A", "B"]
    }

    @Ignore
    @Test
    void testAnnotatedType() {
        GroovyShell shell = new GroovyShell()
        shell.context.setVariable('args',
                ["-cp", "A", "-cp", "B"] as String[])
        ByteArrayOutputStream bytes = new ByteArrayOutputStream()
        System.setErr(new PrintStream(bytes))
        def result = shell.evaluate '''
import groovy.transform.Field
import picocli.groovy.PicocliBaseScript
import picocli.groovy.PicocliScript
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters

@Command(name = "test-command", description = "tests help from a command script")
@PicocliScript class Arg {};

@Parameters(description = "some parameters")
@Field List<String> parameters

@Option(names = ["-cp", "--codepath"], description = "the codepath")
@Field List<String> codepath = []

assert this.scriptCommandLine.commandName == "test-command"
codepath
'''
        assert result == ["A", "B"]
    }

}