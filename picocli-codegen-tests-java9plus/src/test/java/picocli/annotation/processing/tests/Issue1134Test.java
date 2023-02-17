package picocli.annotation.processing.tests;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.Test;
import picocli.codegen.annotation.processing.AbstractCommandSpecProcessor;

import javax.annotation.processing.Processor;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;
import static picocli.annotation.processing.tests.Resources.slurp;
import static picocli.annotation.processing.tests.YamlAssert.compareCommandYamlDump;

public class Issue1134Test {
    @Test
    public void testIssue1134() {
        Processor processor = new AnnotatedCommandSourceGeneratorProcessor();
        Compilation compilation =
                javac()
                        .withProcessors(processor)
                        .compile(JavaFileObjects.forResource(
                                "picocli/issue1134/Issue1134.java"));

        assertThat(compilation).succeeded();
    }

    @Test
    public void testIssue1134Details() {
        AbstractCommandSpecProcessor.setLoadResourceBundles(true);

        Compilation compilation = compareCommandYamlDump(slurp("/picocli/issue1134/Issue1134.yaml"),
                JavaFileObjects.forResource("picocli/issue1134/Issue1134.java"));

        assertOnlySourceVersionWarning(compilation);
    }

    private void assertOnlySourceVersionWarning(Compilation compilation) {
        assertThat(compilation).hadWarningCount(0); // #826 version warnings are now suppressed
        // assertThat(compilation).hadWarningContaining("Supported source version 'RELEASE_6' from annotation processor 'picocli.annotation.processing.tests");
    }
}
