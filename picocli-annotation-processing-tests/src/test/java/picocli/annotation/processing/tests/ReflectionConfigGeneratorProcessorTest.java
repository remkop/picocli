package picocli.annotation.processing.tests;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.Test;
import picocli.codegen.aot.graalvm.processor.ReflectionConfigGeneratorProcessor;

import javax.tools.StandardLocation;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;
import static picocli.codegen.util.Resources.slurp;

public class ReflectionConfigGeneratorProcessorTest {

    @Test
    public void generateExampleFullJava8Api() {
        ReflectionConfigGeneratorProcessor processor = new ReflectionConfigGeneratorProcessor();
        Compilation compilation =
                javac()
                        .withProcessors(processor)
                        .withOptions("-Arelative.path=example/full")
                        .compile(JavaFileObjects.forSourceLines(
                                "picocli.codegen.graalvm.example.ExampleFull",
                                slurp("/picocli/codegen/graalvm/example/ExampleFull.java")));
        assertThat(compilation).succeeded();
        assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT, "META-INF/native-image/picocli-generated/example/full/reflect-config.json")
                .contentsAsUtf8String().isEqualTo(slurp("/picocli/codegen/graalvm/example/FullExample-reflect-config.json"));
    }

    @Test
    public void generateNestedWithSubcommands() {
        ReflectionConfigGeneratorProcessor processor = new ReflectionConfigGeneratorProcessor();
        Compilation compilation =
                javac()
                        .withProcessors(processor)
                        .compile(JavaFileObjects.forResource(
                                "picocli/examples/subcommands/ParentCommandDemo.java"));
        assertThat(compilation).succeeded();
        assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT, "META-INF/native-image/picocli-generated/reflect-config.json")
                .contentsAsUtf8String().isEqualTo(slurp("/generated/META-INF/native-image/picocli-generated/ParentCommandDemo-reflect-config.json"));
    }

    @Test
    public void generateInterface() {
        ReflectionConfigGeneratorProcessor processor = new ReflectionConfigGeneratorProcessor();
        Compilation compilation =
                javac()
                        .withProcessors(processor)
                        .withOptions("-Arelative.path=a/b/c")
                        .compile(JavaFileObjects.forSourceLines(
                                "picocli.codegen.graalvm.example.ExampleInterface",
                                slurp("/picocli/codegen/graalvm/example/ExampleInterface.java")));
        assertThat(compilation).succeeded();
        assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT, "META-INF/native-image/picocli-generated/a/b/c/reflect-config.json")
                .contentsAsUtf8String().isEqualTo(slurp("/picocli/codegen/graalvm/example/example-interface-reflect.json"));
    }
}