package picocli.annotation.processing.tests;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.Ignore;
import org.junit.Test;
import picocli.codegen.aot.graalvm.processor.NativeImageConfigGeneratorProcessor;

import javax.annotation.processing.Processor;
import javax.tools.StandardLocation;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;
import static picocli.annotation.processing.tests.Resources.slurp;
import static picocli.annotation.processing.tests.YamlAssert.compareCommandYamlDump;
import static picocli.codegen.aot.graalvm.processor.NativeImageConfigGeneratorProcessor.OPTION_PROJECT;

public class Issue1151Test {
    @Test
    public void testIssue1151() {
        Processor processor = new AnnotatedCommandSourceGeneratorProcessor();
        Compilation compilation =
                javac()
                        .withProcessors(processor)
                        .compile(JavaFileObjects.forResource(
                                "picocli/issue1151/Issue1151CommandWithManPageGeneratorSubcommand.java"));

        assertThat(compilation).succeeded();
    }


    @Test
    public void testGenerateReflectConfigIssue1151() {
        NativeImageConfigGeneratorProcessor processor = new NativeImageConfigGeneratorProcessor();
        Compilation compilation =
                javac()
                        .withProcessors(processor)
                        .withOptions("-A" + OPTION_PROJECT + "=issue1151")
                        .compile(JavaFileObjects.forSourceLines(
                                "picocli.issue1151.Issue1151CommandWithManPageGeneratorSubcommand",
                                slurp("/picocli/issue1151/Issue1151CommandWithManPageGeneratorSubcommand.java"))//,
                        );
        assertThat(compilation).succeeded();
        assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT, "META-INF/native-image/picocli-generated/issue1151/reflect-config.json")
                .contentsAsUtf8String().isEqualTo(slurp("/picocli/issue1151/issue1151-reflect-config.json"));
        assertThat(compilation).hadWarningCount(0); // #826 version warnings are now suppressed
    }

}
