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
import static picocli.codegen.aot.graalvm.processor.NativeImageConfigGeneratorProcessor.OPTION_PROJECT;

public class Issue1444Test {
    @Test
    public void testIssue1444() {
        Processor processor = new AnnotatedCommandSourceGeneratorProcessor();
        Compilation compilation =
                javac()
                        .withProcessors(processor)
                        .compile(JavaFileObjects.forResource(
                                "picocli/issue1444super/ConcreteCommand.java"));

        assertThat(compilation).succeeded();
    }

    @Ignore
    @Test
    public void testGenerateReflectConfigIssue1444CompileOnlyConcreteSubclass() {
        NativeImageConfigGeneratorProcessor processor = new NativeImageConfigGeneratorProcessor();
        Compilation compilation =
                javac()
                        .withProcessors(processor)
                        .withOptions("-A" + OPTION_PROJECT + "=issue1444super")
                        .compile(JavaFileObjects.forSourceLines(
                                "picocli.issue1444super.ConcreteCommand",
                                slurp("/picocli/issue1444super/ConcreteCommand.java"))//,
                        );
        assertThat(compilation).succeeded();
        assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT, "META-INF/native-image/picocli-generated/issue1444super/reflect-config.json")
                .contentsAsUtf8String().isEqualTo(slurp("/picocli/issue1444super/issue1444-reflect-config.json"));
        assertThat(compilation).hadWarningCount(0); // #826 version warnings are now suppressed
    }

    @Test
    public void testGenerateReflectConfigIssue1444CompileBoth() {
        NativeImageConfigGeneratorProcessor processor = new NativeImageConfigGeneratorProcessor();
        Compilation compilation =
            javac()
                .withProcessors(processor)
                .withOptions("-A" + OPTION_PROJECT + "=issue1444super")
                .compile(JavaFileObjects.forSourceLines(
                        "picocli.issue1444super.ConcreteCommand",
                        slurp("/picocli/issue1444super/ConcreteCommand.java")),
                    JavaFileObjects.forSourceLines(
                        "picocli.issue1444super.AbstractCommand",
                        slurp("/picocli/issue1444super/AbstractCommand.java"))//,
                );
        assertThat(compilation).succeeded();
        assertThat(compilation)
            .generatedFile(StandardLocation.CLASS_OUTPUT, "META-INF/native-image/picocli-generated/issue1444super/reflect-config.json")
            .contentsAsUtf8String().isEqualTo(slurp("/picocli/issue1444super/issue1444-reflect-config.json"));
        assertThat(compilation).hadWarningCount(0); // #826 version warnings are now suppressed
    }
}
