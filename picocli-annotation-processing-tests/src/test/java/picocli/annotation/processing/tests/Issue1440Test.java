package picocli.annotation.processing.tests;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.Ignore;
import org.junit.Test;

import javax.annotation.processing.Processor;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

public class Issue1440Test {
    //@Ignore("https://github.com/remkop/picocli/issues/1440")
    @Test
    public void testIssue1440() {
        Processor processor = new AnnotatedCommandSourceGeneratorProcessor();
        Compilation compilation =
                javac()
                        .withProcessors(processor)
                        .compile(JavaFileObjects.forResource(
                                "picocli/issue1440inheritedoptions/Command1.java"),
                            JavaFileObjects.forResource(
                                "picocli/issue1440inheritedoptions/Command2.java"));

        assertThat(compilation).succeeded();
    }
}
