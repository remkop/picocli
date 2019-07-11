package picocli.annotation.processing.tests;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.Ignore;
import org.junit.Test;

import javax.tools.StandardLocation;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

public class AnnotatedCommandSourceGeneratorProcessorTest {

    @Ignore
    @Test
    public void generate() {
        AnnotatedCommandSourceGeneratorProcessor processor = new AnnotatedCommandSourceGeneratorProcessor();
        Compilation compilation =
                javac()
                        .withProcessors(processor)
                        .compile(JavaFileObjects.forResource(
                                "picocli/examples/subcommands/ParentCommandDemo.java"));
        assertThat(compilation).succeeded();
        assertThat(compilation)
                .generatedSourceFile("GeneratedHelloWorld")
                .hasSourceEquivalentTo(JavaFileObjects.forResource("GeneratedHelloWorld.java"));
    }

    @Test
    public void generate1() {
        AnnotatedCommandSourceGeneratorProcessor processor = new AnnotatedCommandSourceGeneratorProcessor();
        Compilation compilation =
                javac()
                        .withProcessors(processor)
                        .compile(JavaFileObjects.forResource(
                                "picocli/codegen/aot/graalvm/Example.java"));
        assertThat(compilation).succeeded();
        assertThat(compilation)
                .generatedFile(StandardLocation.SOURCE_OUTPUT, "generated/picocli/codegen/aot/graalvm/Example.java")
                .hasSourceEquivalentTo(JavaFileObjects.forResource("generated/picocli/codegen/aot/graalvm/Example.java"));
    }

    //@Ignore("TODO field constant values")
    @Test
    public void generateNested() {
        AnnotatedCommandSourceGeneratorProcessor processor = new AnnotatedCommandSourceGeneratorProcessor();
        Compilation compilation =
                javac()
                        .withProcessors(processor)
                        .compile(JavaFileObjects.forResource(
                                "picocli/examples/PopulateFlagsMain.java"));
        assertThat(compilation).succeeded();
        assertThat(compilation)
                .generatedFile(StandardLocation.SOURCE_OUTPUT, "generated/picocli/examples/PopulateFlagsMain.java")
                .hasSourceEquivalentTo(JavaFileObjects.forResource("generated/picocli/examples/PopulateFlagsMain.java"));
    }

    @Ignore
    @Test
    public void generateNested2() {
        AnnotatedCommandSourceGeneratorProcessor processor = new AnnotatedCommandSourceGeneratorProcessor();
        Compilation compilation =
                javac()
                        .withProcessors(processor)
                        .compile(JavaFileObjects.forResource(
                                "picocli/examples/subcommands/ParentCommandDemo.java"));
        assertThat(compilation).succeeded();
        assertThat(compilation)
                .generatedFile(StandardLocation.SOURCE_OUTPUT, "generated/examples/subcommands/ParentCommandDemo.java")
                .hasSourceEquivalentTo(JavaFileObjects.forResource("generated/examples/subcommands/ParentCommandDemo.java"));
    }
}