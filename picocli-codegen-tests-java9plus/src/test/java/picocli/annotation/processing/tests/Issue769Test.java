package picocli.annotation.processing.tests;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.Test;

import javax.annotation.processing.Processor;
import javax.tools.StandardLocation;

import java.io.IOException;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;
import static org.junit.Assert.*;

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
    public void testIssue769Details() throws IOException {
        Processor processor = new AnnotatedCommandSourceGeneratorProcessor();
        Compilation compilation =
                javac()
                        .withProcessors(processor)
                        .compile(JavaFileObjects.forResource(
                                "picocli/issue769/MyMixin.java"));

        assertThat(compilation).succeeded();

//        assertThat(compilation)
//                .generatedFile(StandardLocation.SOURCE_OUTPUT, "generated/picocli/issue769/MyMixin.java")
//                .hasSourceEquivalentTo(JavaFileObjects.forResource("generated/picocli/issue769/MyMixin.java"));

        String generated1 = compilation.generatedFile(StandardLocation.SOURCE_OUTPUT, "generated/picocli/issue769/MyMixin.java").get().getCharContent(false).toString();
        String expected1 = JavaFileObjects.forResource("generated/picocli/issue769/MyMixin.java").getCharContent(false).toString();
        assertEquals(expected1.replaceAll("\\r?\\n", "\n"), generated1.replaceAll("\\r?\\n", "\n"));


//        assertThat(compilation)
//                .generatedFile(StandardLocation.SOURCE_OUTPUT, "generated/picocli/issue769/SubCommand.java")
//                .hasSourceEquivalentTo(JavaFileObjects.forResource("generated/picocli/issue769/SubCommand.java"));
        String generatedSub = compilation.generatedFile(StandardLocation.SOURCE_OUTPUT, "generated/picocli/issue769/SubCommand.java").get().getCharContent(false).toString();
        String expectedSub = JavaFileObjects.forResource("generated/picocli/issue769/SubCommand.java").getCharContent(false).toString();
        assertEquals(expectedSub.replaceAll("\\r?\\n", "\n"), generatedSub.replaceAll("\\r?\\n", "\n"));
    }
}
