package picocli.annotation.processing.tests;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.Test;

import javax.annotation.processing.Processor;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

public class Issue2430Test
{
    @Test
    public void testSpecInArgGroupClassUsedViaMixin() {
        Processor processor = new AnnotatedCommandSourceGeneratorProcessor();
        Compilation compilation =
            javac()
                .withProcessors(processor)
                .compile(JavaFileObjects.forResource(
                    "picocli/issue2430/Main.java"));

        assertThat(compilation).succeeded();
    }

    @Test
    public void testSpecInArgGroupClassUsedDirectlyInCommand() {
        Processor processor = new AnnotatedCommandSourceGeneratorProcessor();
        Compilation compilation =
            javac()
                .withProcessors(processor)
                .compile(JavaFileObjects.forResource(
                    "picocli/issue2430/SpecInArgGroup.java"));

        assertThat(compilation).succeeded();
    }
}
