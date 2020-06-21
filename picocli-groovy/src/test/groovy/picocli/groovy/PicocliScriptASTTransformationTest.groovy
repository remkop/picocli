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

import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.control.ErrorCollector
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.control.messages.SyntaxErrorMessage
import org.codehaus.groovy.syntax.SyntaxException
import org.junit.Ignore
import org.junit.Rule
import org.junit.contrib.java.lang.system.ProvideSystemProperty;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URI;

import org.junit.Test;

import groovy.lang.GroovyShell;
import groovy.transform.SourceURI;

import static org.junit.Assert.*;

public class PicocliScriptASTTransformationTest {
    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");

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
                "Unknown option: '--xyz'%n" +
                "Usage: test-command [-cp=<codepath>]... [<parameters>...]%n" +
                "tests help from a command script%n" +
                "      [<parameters>...]   some parameters%n" +
                "      -cp, --codepath=<codepath>%n" +
                "                          the codepath%n")
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

assert this.commandLine.commandName == "test-command"
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

assert this.commandLine.commandName == "test-command"
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

assert this.commandLine.commandName == "test-command"
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

assert this.commandLine.commandName == "test-command"
codepath
'''
        assert result == ["A", "B"]
    }

    @Test
    void testClassCannotBeAnnotatedWithPicocliScript() {
        def script = '''
@picocli.CommandLine.Command
@picocli.groovy.PicocliScript
class Arg {};
'''
        try {
            new GroovyShell().evaluate script
            fail("Expected exception")
        } catch (MultipleCompilationErrorsException ex) {
            ErrorCollector collector = ex.errorCollector
            assert collector.errors[0] instanceof SyntaxErrorMessage
            SyntaxException syntex = ((SyntaxErrorMessage) collector.errors[0]).cause
            String expected = String.format("Annotation @PicocliScript can only be used within a Script.\n @ line 2, column 1.")
            assert expected == syntex.message
        }
    }

    @Test
    void testAnnotationCannotBeAnnotatedWithPicocliScript() {
        def script = '''
@picocli.groovy.PicocliScript
@picocli.CommandLine.Command
def a = 234
a
'''
        try {
            new GroovyShell().evaluate script
            fail("Expected exception")
        } catch (MissingPropertyException ex) {
            assertEquals("No such property: args for class: Script1", ex.getMessage())
        }
    }

    @Test
    void testPicocliScriptAnnotationValueMustBeAClassLiteral() {
        def script = '''
@Command(name = "test-command", description = "invalid annotation")
@PicocliScript("invalid string") // expect groovyc compiler error
import groovy.transform.Field
import picocli.groovy.PicocliScript
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters

@Parameters(description = "some parameters")
@Field List<String> parameters
'''
        try {
            new GroovyShell().evaluate script
            fail("Expected exception")
        } catch (MultipleCompilationErrorsException ex) {
            ErrorCollector collector = ex.errorCollector
            assert collector.errors[0] instanceof SyntaxErrorMessage
            SyntaxException syntex = ((SyntaxErrorMessage) collector.errors[0]).cause
            String expected = String.format("Annotation @PicocliScript member 'value' should be a class literal.\n @ line 3, column 16.")
            assert expected == syntex.message
        }
    }

    @Test
    void testPicocliScriptAnnotationCannotBeUsedWithAssignment() {
        def script = '''
import groovy.transform.Field
import picocli.groovy.PicocliBaseScript
import picocli.groovy.PicocliScript
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters

@Command
@PicocliScript PicocliBaseScript cli = new PicocliBaseScript() { // invalid assignment
 @Override protected Object runScriptBody(){
 return null
}};

@Parameters(description = "some parameters")
@Field List<String> parameters
'''
        try {
            new GroovyShell().evaluate script
            fail("Expected exception")
        } catch (MultipleCompilationErrorsException ex) {
            ErrorCollector collector = ex.errorCollector
            assert collector.errors[0] instanceof SyntaxErrorMessage
            SyntaxException syntex = ((SyntaxErrorMessage) collector.errors[0]).cause
            String expected = String.format("Annotation @PicocliScript not supported with variable assignment.\n @ line 9, column 1.")
            assert expected == syntex.message
        }
    }

    @Test
    void testPicocliScriptAnnotationCannotBeUsedWithMultipleAssignmentDeclaration() {
        def script = '''
import groovy.transform.Field
import picocli.groovy.PicocliBaseScript
import picocli.groovy.PicocliScript
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters

@Command
@PicocliScript def (cli1, cli2) = [new PicocliBaseScript() { // invalid assignment
 @Override protected Object runScriptBody(){
 return null
}},
new PicocliBaseScript() { // invalid assignment
 @Override protected Object runScriptBody(){
 return null
}}]

@Parameters(description = "some parameters")
@Field List<String> parameters
'''
        try {
            new GroovyShell().evaluate script
            fail("Expected exception")
        } catch (MultipleCompilationErrorsException ex) {
            ErrorCollector collector = ex.errorCollector
            assert collector.errors[0] instanceof SyntaxErrorMessage
            SyntaxException syntex = ((SyntaxErrorMessage) collector.errors[0]).cause
            String expected = String.format("Annotation @PicocliScript not supported with multiple assignment notation.\n @ line 9, column 1.")
            assert expected == syntex.message
        }
    }

    @Test
    void testPicocliScriptAnnotationCannotHaveAValueWhenUsedOnDeclaration() {
        def script = '''
import groovy.transform.Field
import picocli.groovy.PicocliBaseScript
import picocli.groovy.PicocliScript
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters

@Command
@PicocliScript(picocli.groovy.PicocliBaseScript.class)
PicocliBaseScript cli;

@Parameters(description = "some parameters")
@Field List<String> parameters
'''
        try {
            new GroovyShell().evaluate script
            fail("Expected exception")
        } catch (MultipleCompilationErrorsException ex) {
            ErrorCollector collector = ex.errorCollector
            assert collector.errors[0] instanceof SyntaxErrorMessage
            SyntaxException syntex = ((SyntaxErrorMessage) collector.errors[0]).cause
            String expected = String.format("Annotation @PicocliScript cannot have member 'value' if used on a declaration.\n @ line 10, column 16.")
            assert expected == syntex.message
        }
    }

    @Test
    void testPicocliScriptAnnotationValueMustInheritFromScript() {
        def script = '''
@PicocliScript(Long.class) // invalid script class
import groovy.transform.Field
import picocli.groovy.PicocliScript
import picocli.CommandLine.Parameters

@Parameters(description = "some parameters")
@Field List<String> parameters
'''
        try {
            new GroovyShell().evaluate script
            fail("Expected exception")
        } catch (MultipleCompilationErrorsException ex) {
            ErrorCollector collector = ex.errorCollector
            assert collector.errors[0] instanceof SyntaxErrorMessage
            SyntaxException syntex = ((SyntaxErrorMessage) collector.errors[0]).cause
            String expected = String.format("Declared type java.lang.Long -> java.lang.Long does not extend groovy.lang.Script class!\n @ line 2, column 1.")
            assert expected.split("@")[0] == syntex.message.split("@")[0]
        }
    }

    @Test
    void testPicocliScriptAnnotationWithCustomScriptBody() {
        def script = '''
@picocli.groovy.PicocliScript(groovy.lang.Script)
import groovy.transform.Field
import picocli.CommandLine.Parameters

@Parameters(description = "some parameters")
@Field List<String> parameters

123
'''
        def result = new GroovyShell().evaluate script
        assert 123 == result
    }

    @Test
    void testBaseScriptAnnotationDoesNotTransformPicocliCommand() {
        def script = '''
@picocli.CommandLine.Command
@groovy.transform.BaseScript
import groovy.transform.Field
import picocli.CommandLine.Parameters

@Parameters(description = "some parameters")
@Field List<String> parameters

123
'''
        try {
            new GroovyShell().evaluate script
            fail("expected exception")
        } catch (Throwable ok) {
        }
    }

    @Test
    void testIsCustomScriptBodyMethod() {
        PicocliScriptASTTransformation trans = new PicocliScriptASTTransformation()
        assert !trans.isCustomScriptBodyMethod(null)

        ClassNode returnType = new ClassNode(String.class)
        MethodNode run = new MethodNode("run", 0, returnType, new Parameter[0], new ClassNode[0], (Statement) null)
        run.declaringClass = ClassHelper.SCRIPT_TYPE

        assert !trans.isCustomScriptBodyMethod(run)

        Parameter p = new Parameter(new ClassNode(String.class), "param")
        run.parameters = [p].toArray(new Parameter[0])
        assert trans.isCustomScriptBodyMethod(run)

        MethodNode other = new MethodNode("other", 0, returnType, new Parameter[0], new ClassNode[0], (Statement) null)
        other.declaringClass = ClassHelper.SCRIPT_TYPE

        assert trans.isCustomScriptBodyMethod(other)
    }
}