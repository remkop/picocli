package picocli.annotation.processing.tests;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.Test;
import picocli.codegen.aot.graalvm.processor.ResourceConfigGeneratorProcessor;

import javax.tools.StandardLocation;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;
import static picocli.codegen.aot.graalvm.processor.AbstractConfigGeneratorProcessor.OPTION_RELATIVE_PATH;
import static picocli.codegen.aot.graalvm.processor.ResourceConfigGeneratorProcessor.OPTION_BUNDLES;
import static picocli.codegen.aot.graalvm.processor.ResourceConfigGeneratorProcessor.OPTION_RESOURCE_REGEX;
import static picocli.codegen.util.Resources.slurp;

public class ResourceConfigGeneratorProcessorTest {

    @Test
    public void generateInterface() {
        ResourceConfigGeneratorProcessor processor = new ResourceConfigGeneratorProcessor();
        Compilation compilation =
                javac()
                        .withProcessors(processor)
                        .withOptions("-A" + OPTION_RELATIVE_PATH + "=x/y/z",
                                "-A" + OPTION_BUNDLES + "=some.extra.bundle",
                                "-A" + OPTION_RESOURCE_REGEX + "=^ExtraPattern$")
                        .compile(JavaFileObjects.forSourceLines(
                                "picocli.codegen.graalvm.example.ExampleFull",
                                slurp("/picocli/codegen/graalvm/example/ExampleFull.java")));
        assertThat(compilation).succeeded();
        assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT, "META-INF/native-image/picocli-generated/x/y/z/resource-config.json")
                .contentsAsUtf8String().isEqualTo(slurp("/picocli/codegen/graalvm/example/example-resource.json"));
    }
}
