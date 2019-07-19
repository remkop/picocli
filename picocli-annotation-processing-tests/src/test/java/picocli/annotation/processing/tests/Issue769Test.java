package picocli.annotation.processing.tests;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.Test;

import javax.annotation.processing.Processor;
import javax.tools.StandardLocation;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

public class Issue769Test {
    @Test
    public void testIssue769() {
        CommandSpec2YamlProcessor processor = new CommandSpec2YamlProcessor();
        Compilation compilation =
                javac()
                        .withProcessors(processor)
                        .compile(JavaFileObjects.forResource(
                                "picocli/issue769/MyMixin.java"));

        assertThat(compilation).succeeded();
    }

    @Test
    public void testIssue769Details() {
        Processor processor = new AnnotatedCommandSourceGeneratorProcessor();
        Compilation compilation =
                javac()
                        .withProcessors(processor)
                        .compile(JavaFileObjects.forResource(
                                "picocli/issue769/MyMixin.java"));

        assertThat(compilation).succeeded();
        assertThat(compilation)
                .generatedFile(StandardLocation.SOURCE_OUTPUT, "generated/picocli/issue769/MyMixin.java")
                .hasSourceEquivalentTo(JavaFileObjects.forResource("generated/picocli/issue769/MyMixin.java"));

        assertThat(compilation)
                .generatedFile(StandardLocation.SOURCE_OUTPUT, "generated/picocli/issue769/SubCommand.java")
                .hasSourceEquivalentTo(JavaFileObjects.forResource("generated/picocli/issue769/SubCommand.java"));
    }
}
