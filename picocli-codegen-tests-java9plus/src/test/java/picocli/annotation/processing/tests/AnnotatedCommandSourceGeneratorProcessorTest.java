package picocli.annotation.processing.tests;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.Ignore;
import org.junit.Test;

import javax.tools.StandardLocation;

import java.io.IOException;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;
import static org.junit.Assert.*;

public class AnnotatedCommandSourceGeneratorProcessorTest {

    @Ignore
    @Test
    public void generate() throws IOException {
        AnnotatedCommandSourceGeneratorProcessor processor = new AnnotatedCommandSourceGeneratorProcessor();
        Compilation compilation =
                javac()
                        .withProcessors(processor)
                        .compile(JavaFileObjects.forResource(
                                "picocli/examples/subcommands/ParentCommandDemo.java"));
        assertThat(compilation).succeeded();
//        assertThat(compilation)
//                .generatedSourceFile("GeneratedHelloWorld")
//                .hasSourceEquivalentTo(JavaFileObjects.forResource("GeneratedHelloWorld.java"));
        String generated1 = compilation.generatedFile(StandardLocation.SOURCE_OUTPUT, "GeneratedHelloWorld.java").get().getCharContent(false).toString();
        String expected1 = JavaFileObjects.forResource("GeneratedHelloWorld.java").getCharContent(false).toString();
        assertEquals(expected1.replaceAll("\\r?\\n", "\n"), generated1.replaceAll("\\r?\\n", "\n"));
    }

    @Test
    public void generate1() throws IOException {
        AnnotatedCommandSourceGeneratorProcessor processor = new AnnotatedCommandSourceGeneratorProcessor();
        Compilation compilation =
                javac()
                        .withProcessors(processor)
                        .compile(JavaFileObjects.forResource(
                                "picocli/codegen/aot/graalvm/Example.java"));
        assertThat(compilation).succeeded();
//        assertThat(compilation)
//                .generatedFile(StandardLocation.SOURCE_OUTPUT, "generated/picocli/codegen/aot/graalvm/Example.java")
//                .hasSourceEquivalentTo(JavaFileObjects.forResource("generated/picocli/codegen/aot/graalvm/Example.java"));
        String generated1 = compilation.generatedFile(StandardLocation.SOURCE_OUTPUT, "generated/picocli/codegen/aot/graalvm/Example.java").get().getCharContent(false).toString();
        String expected1 = JavaFileObjects.forResource("generated/picocli/codegen/aot/graalvm/Example.java").getCharContent(false).toString();
        assertEquals(expected1.replaceAll("\\r?\\n", "\n"), generated1.replaceAll("\\r?\\n", "\n"));
    }

    //@Ignore("TODO field constant values")
    @Test
    public void generateNested() throws IOException {
        AnnotatedCommandSourceGeneratorProcessor processor = new AnnotatedCommandSourceGeneratorProcessor();
        Compilation compilation =
                javac()
                        .withProcessors(processor)
                        .compile(JavaFileObjects.forResource(
                                "picocli/examples/PopulateFlagsMain.java"));
        assertThat(compilation).succeeded();
//        assertThat(compilation)
//                .generatedFile(StandardLocation.SOURCE_OUTPUT, "generated/picocli/examples/PopulateFlagsMain.java")
//                .hasSourceEquivalentTo(JavaFileObjects.forResource("generated/picocli/examples/PopulateFlagsMain.java"));
        String generated1 = compilation.generatedFile(StandardLocation.SOURCE_OUTPUT, "generated/picocli/examples/PopulateFlagsMain.java").get().getCharContent(false).toString();
        String expected1 = JavaFileObjects.forResource("generated/picocli/examples/PopulateFlagsMain.java").getCharContent(false).toString();
        assertEquals(expected1.replaceAll("\\r?\\n", "\n"), generated1.replaceAll("\\r?\\n", "\n"));
    }

    @Ignore
    @Test
    public void generateNested2() throws IOException {
        AnnotatedCommandSourceGeneratorProcessor processor = new AnnotatedCommandSourceGeneratorProcessor();
        Compilation compilation =
                javac()
                        .withProcessors(processor)
                        .compile(JavaFileObjects.forResource(
                                "picocli/examples/subcommands/ParentCommandDemo.java"));
        assertThat(compilation).succeeded();
//        assertThat(compilation)
//                .generatedFile(StandardLocation.SOURCE_OUTPUT, "generated/examples/subcommands/ParentCommandDemo.java")
//                .hasSourceEquivalentTo(JavaFileObjects.forResource("generated/examples/subcommands/ParentCommandDemo.java"));
        String generated1 = compilation.generatedFile(StandardLocation.SOURCE_OUTPUT, "generated/examples/subcommands/ParentCommandDemo.java").get().getCharContent(false).toString();
        String expected1 = JavaFileObjects.forResource("generated/examples/subcommands/ParentCommandDemo.java").getCharContent(false).toString();
        assertEquals(expected1.replaceAll("\\r?\\n", "\n"), generated1.replaceAll("\\r?\\n", "\n"));
    }
}
