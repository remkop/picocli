package picocli.annotation.processing.tests;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.Test;

import javax.annotation.processing.Processor;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

public class Issue1319Test {
    @Test
    public void testIssue1319() {
        Processor processor = new AnnotatedCommandSourceGeneratorProcessor();
        Compilation compilation =
                javac()
                        .withProcessors(processor)
                        .compile(JavaFileObjects.forResource(
                                "picocli/issue1319/InheritHelpApp.java"));

        assertThat(compilation).succeeded();
    }
}
