package picocli.annotation.processing.tests;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.Test;
import picocli.codegen.aot.graalvm.processor.DynamicProxyConfigGeneratorProcessor;

import javax.tools.StandardLocation;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;
import static picocli.codegen.aot.graalvm.processor.AbstractConfigGeneratorProcessor.OPTION_RELATIVE_PATH;
import static picocli.codegen.aot.graalvm.processor.DynamicProxyConfigGeneratorProcessor.OPTION_INTERFACE_CLASSES;
import static picocli.codegen.util.Resources.slurp;

public class DynamicProxyConfigGeneratorProcessorTest {

    @Test
    public void generateInterface() {
        DynamicProxyConfigGeneratorProcessor processor = new DynamicProxyConfigGeneratorProcessor();
        Compilation compilation =
                javac()
                        .withProcessors(processor)
                        .withOptions("-Arelative.path=a/b/c")
                        .compile(JavaFileObjects.forSourceLines(
                                "picocli.codegen.graalvm.example.ExampleInterface",
                                slurp("/picocli/codegen/graalvm/example/ExampleInterface.java")));
        assertThat(compilation).succeeded();
        assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT, "META-INF/native-image/picocli-generated/a/b/c/proxy-config.json")
                .contentsAsUtf8String().isEqualTo(slurp("/picocli/codegen/graalvm/example/example-interface-proxy.json"));
    }

    @Test
    public void generateAdditionalInterface() {
        DynamicProxyConfigGeneratorProcessor processor = new DynamicProxyConfigGeneratorProcessor();
        Compilation compilation =
                javac()
                        .withProcessors(processor)
                        .withOptions("-A" + OPTION_RELATIVE_PATH + "=a/b/c",
                                "-A" + OPTION_INTERFACE_CLASSES + "=com.abc.Interface1,com.xyz.Interface2")
                        .compile(JavaFileObjects.forSourceLines(
                                "picocli.codegen.graalvm.example.ExampleInterface",
                                slurp("/picocli/codegen/graalvm/example/ExampleInterface.java")));
        assertThat(compilation).succeeded();
        assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT, "META-INF/native-image/picocli-generated/a/b/c/proxy-config.json")
                .contentsAsUtf8String().isEqualTo(slurp("/picocli/codegen/graalvm/example/example-additional-interface-proxy.json"));
    }
}