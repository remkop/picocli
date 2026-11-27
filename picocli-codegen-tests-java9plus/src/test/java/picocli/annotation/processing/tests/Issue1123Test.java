package picocli.annotation.processing.tests;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.Test;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import static com.google.testing.compile.Compiler.javac;
import static org.junit.Assert.*;

public class Issue1123Test {
    @Test public void invalidInteractiveParameterHasSourceLocation() {
        assertLocated("@picocli.CommandLine.Parameters(interactive=true, arity=\"1\") String value;");
    }
    @Test public void invalidInteractiveOptionHasSourceLocation() {
        assertLocated("@picocli.CommandLine.Option(names=\"-x\", interactive=true, arity=\"1\") String value;");
    }
    @Test public void invalidArgGroupMemberHasSourceLocation() {
        assertLocated("@picocli.CommandLine.ArgGroup Group group; static class Group { @picocli.CommandLine.Option(names=\"-x\", interactive=true, arity=\"1\") String value; }");
    }
    private void assertLocated(String member) {
        JavaFileObject source = JavaFileObjects.forSourceLines("example.BrokenCommand",
                "package example;", "@picocli.CommandLine.Command class BrokenCommand {", member, "}");
        Compilation compilation = javac().withProcessors(new CommandSpec2YamlProcessor()).compile(source);
        assertEquals(Compilation.Status.FAILURE, compilation.status());
        assertEquals(1, compilation.errors().size());
        Diagnostic<? extends JavaFileObject> diagnostic = compilation.errors().get(0);
        assertNotNull("diagnostic must identify the offending source", diagnostic.getSource());
        assertEquals(source.toUri(), diagnostic.getSource().toUri());
        assertEquals(3L, diagnostic.getLineNumber());
        assertTrue(diagnostic.getMessage(null), diagnostic.getMessage(null).contains("Interactive options"));
        assertFalse(diagnostic.getMessage(null), diagnostic.getMessage(null).contains("FATAL ERROR"));
    }
}
