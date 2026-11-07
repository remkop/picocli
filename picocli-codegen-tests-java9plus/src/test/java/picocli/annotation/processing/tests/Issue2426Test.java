package picocli.annotation.processing.tests;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.Test;

import javax.annotation.processing.Processor;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

public class Issue2426Test {

    /**
     * A command that declares {@code mixinStandardHelpOptions = true} and a custom {@code @Option}
     * with a name that overlaps with the standard help options (-h, --help, -V, --version)
     * should be reported as a compile-time error: picocli would otherwise silently omit the
     * standard help options, and GraalVM native images would fail at runtime with a cryptic
     * {@code InitializationException: picocli.CommandLine$AutoHelpMixin is not a command}.
     */
    @Test
    public void testConflictingOptionName() {
        Processor processor = new AnnotatedCommandSourceGeneratorProcessor();
        Compilation compilation =
                javac()
                        .withProcessors(processor)
                        .compile(JavaFileObjects.forResource(
                                "picocli/issue2426/App.java"));

        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("conflict with the standard help option names");
    }

    @Test
    public void testNoConflictingOptionName() {
        Processor processor = new AnnotatedCommandSourceGeneratorProcessor();
        Compilation compilation =
                javac()
                        .withProcessors(processor)
                        .compile(JavaFileObjects.forResource(
                                "picocli/issue2426/AppNoConflict.java"));

        assertThat(compilation).succeeded();
        assertThat(compilation).hadWarningCount(0);
    }
}
