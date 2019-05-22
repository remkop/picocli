package picocli.annotation.processing.tests;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.Ignore;
import org.junit.Test;
import picocli.codegen.annotation.processing.AnnotatedCommandSourceGeneratorProcessor;
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
                        .withOptions("-AgroupId=example", "-AartifactId=full")
                        .compile(JavaFileObjects.forSourceLines(
                                "picocli.codegen.graalvm.example.ExampleFull",
                                slurp("/picocli/codegen/graalvm/example/ExampleFull.java")));
        assertThat(compilation).succeeded();
        assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT, "META-INF/native-image/example/full/picocli-generated/reflect-config.json")
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

    @Ignore
    @Test
    public void generate1() {
        ReflectionConfigGeneratorProcessor processor = new ReflectionConfigGeneratorProcessor();
        Compilation compilation =
                javac()
                        .withProcessors(processor)
                        .withOptions("-AgroupId=a", "-AartifactId=a")
                        .compile(JavaFileObjects.forResource(
                                "picocli/codegen/aot/graalvm/Example.java"));
        assertThat(compilation).succeeded();
        assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT, "META-INF/native-image/a/a/picocli-generated/reflect-config.json")
                .contentsAsUtf8String().isEqualTo(slurp("/generated/META-INF/native-image/a/a/picocli-generated/reflect-config.json"));
    }

    @Ignore
    @Test
    public void generateNested() {
        ReflectionConfigGeneratorProcessor processor = new ReflectionConfigGeneratorProcessor();
        Compilation compilation =
                javac()
                        .withProcessors(processor)
                        .withOptions("-AgroupId=b", "-AartifactId=b")
                        .compile(JavaFileObjects.forResource(
                                "picocli/examples/PopulateFlagsMain.java"));
        assertThat(compilation).succeeded();
        assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT, "META-INF/native-image/b/b/picocli-generated/reflect-config.json")
                .contentsAsUtf8String().isEqualTo(slurp("/generated/META-INF/native-image/b/b/picocli-generated/reflect-config.json"));
    }

    @Ignore
    @Test
    public void generateNested2() {
        ReflectionConfigGeneratorProcessor processor = new ReflectionConfigGeneratorProcessor();
        Compilation compilation =
                javac()
                        .withProcessors(processor)
                        .withOptions("-AgroupId=c", "-AartifactId=c")
                        .compile(JavaFileObjects.forResource(
                                "picocli/examples/subcommands/ParentCommandDemo.java"));
        assertThat(compilation).succeeded();
        assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT, "META-INF/native-image/c/c/picocli-generated/reflect-config.json")
                .contentsAsUtf8String().isEqualTo(slurp("/generated/META-INF/native-image/c/c/picocli-generated/reflect-config.json"));
    }
}