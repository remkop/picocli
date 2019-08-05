package picocli.annotation.processing.tests;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.Ignore;
import org.junit.Test;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static picocli.annotation.processing.tests.Resources.slurp;
import static picocli.annotation.processing.tests.YamlAssert.compareCommandYamlDump;

public class Issue777Test {
    @Ignore
    @Test
    public void testIssue777() {
        Compilation compilation = compareCommandYamlDump(slurp("/picocli/issue777/MutuallyExclusiveOptionsDemo.yaml"),
                JavaFileObjects.forResource("picocli/issue777/MutuallyExclusiveOptionsDemo.java"));

        assertThat(compilation).succeeded();
    }

    @Ignore
    @Test
    public void testIssue777Composite() {
        Compilation compilation = compareCommandYamlDump(slurp("/picocli/issue777/CompositeGroupDemo.yaml"),
                JavaFileObjects.forResource("picocli/issue777/CompositeGroupDemo.java"));

        assertThat(compilation).succeeded();
    }

//    @Test
//    public void testIssue777Details() {
//        Processor processor = new AnnotatedCommandSourceGeneratorProcessor();
//        Compilation compilation =
//                javac()
//                        .withProcessors(processor)
//                        .compile(JavaFileObjects.forResource(
//                                "picocli/issue777/CompositeGroupDemo.java"));
//
//        assertThat(compilation).succeeded();
//        assertThat(compilation)
//                .generatedFile(StandardLocation.SOURCE_OUTPUT, "generated/picocli/issue777/CompositeGroupDemo.java")
//                .hasSourceEquivalentTo(JavaFileObjects.forResource("generated/picocli/issue777/CompositeGroupDemo.java"));
//
//        assertThat(compilation)
//                .generatedFile(StandardLocation.SOURCE_OUTPUT, "generated/picocli/issue777/SubCommand.java")
//                .hasSourceEquivalentTo(JavaFileObjects.forResource("generated/picocli/issue777/SubCommand.java"));
//    }
}
