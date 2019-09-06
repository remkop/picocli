package picocli.codegen.aot.graalvm.processor;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.Test;

import javax.tools.JavaFileManager;
import javax.tools.StandardLocation;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;
import static org.junit.Assert.*;
import static picocli.codegen.aot.graalvm.processor.NativeImageConfigGeneratorProcessor.OPTION_PROJECT;
import static picocli.codegen.aot.graalvm.processor.ProxyConfigGen.OPTION_INTERFACE_CLASSES;
import static picocli.codegen.aot.graalvm.processor.ResourceConfigGen.OPTION_BUNDLES;
import static picocli.codegen.aot.graalvm.processor.ResourceConfigGen.OPTION_RESOURCE_REGEX;
import static picocli.annotation.processing.tests.Resources.slurp;

public class NativeImageConfigGeneratorProcessorTest {

    private void assertNoGeneratedFile(Compilation compilation, JavaFileManager.Location location, String path) {
        boolean success = true;
        try {
            assertThat(compilation).generatedFile(location, path);
            success = false;
        } catch (AssertionError expected) {
        }
        if (!success) {
            fail("Found unexpected generated file: " + location + path);
        }
    }

    private void expectGeneratedWithNotes(Compilation compilation, String[][] allParams) {
        for (String[] params : allParams) {
            boolean generated = Boolean.parseBoolean(params[2]);
            if (generated) {
                assertThat(compilation)
                        .generatedFile(StandardLocation.CLASS_OUTPUT,
                                "META-INF/native-image/picocli-generated/" + params[1]);
                assertThat(compilation).hadNoteContaining(String.format(
                        "%s writing to: CLASS_OUTPUT/META-INF/native-image/picocli-generated/%s", params[0], params[1]));
            } else {
                assertThat(compilation).hadNoteContaining(params[0] + " is not enabled");
                assertNoGeneratedFile(compilation, StandardLocation.CLASS_OUTPUT,
                        "META-INF/native-image/picocli-generated/" + params[1]);
            }
        }
        assertThat(compilation).hadNoteCount(allParams.length);
    }

    @Test
    public void testNothingDisabledByDefault() {
        NativeImageConfigGeneratorProcessor processor = new NativeImageConfigGeneratorProcessor();
        Compilation compilation =
                javac()
                        .withProcessors(processor)
                        .compile(JavaFileObjects.forResource(
                                "picocli/examples/subcommands/ParentCommandDemo.java"));
        assertThat(compilation).succeeded();
        String[][] allParams = {
                { ReflectConfigGen.class.getSimpleName(),  "reflect-config.json",  "true" },
                { ResourceConfigGen.class.getSimpleName(), "resource-config.json", "true" },
                { ProxyConfigGen.class.getSimpleName(),    "proxy-config.json",    "true" },
        };
        expectGeneratedWithNotes(compilation, allParams);
    }

    @Test
    public void testOptionDisableReflect() {
        NativeImageConfigGeneratorProcessor processor = new NativeImageConfigGeneratorProcessor();
        Compilation compilation =
                javac()
                        .withProcessors(processor)
                        .withOptions("-A" + ReflectConfigGen.OPTION_DISABLE) // no value
                        .compile(JavaFileObjects.forResource(
                                "picocli/examples/subcommands/ParentCommandDemo.java"));
        assertThat(compilation).succeeded();
        String[][] allParams = {
                { ReflectConfigGen.class.getSimpleName(),  "reflect-config.json",  "false" },
                { ResourceConfigGen.class.getSimpleName(), "resource-config.json", "true" },
                { ProxyConfigGen.class.getSimpleName(),    "proxy-config.json",    "true" },
        };
        expectGeneratedWithNotes(compilation, allParams);
    }

    @Test
    public void testOptionDisableResource() {
        NativeImageConfigGeneratorProcessor processor = new NativeImageConfigGeneratorProcessor();
        Compilation compilation =
                javac()
                        .withProcessors(processor)
                        .withOptions("-A" + ResourceConfigGen.OPTION_DISABLE) // no value
                        .compile(JavaFileObjects.forResource(
                                "picocli/examples/subcommands/ParentCommandDemo.java"));
        assertThat(compilation).succeeded();
        String[][] allParams = {
                { ReflectConfigGen.class.getSimpleName(),  "reflect-config.json",  "true" },
                { ResourceConfigGen.class.getSimpleName(), "resource-config.json", "false" },
                { ProxyConfigGen.class.getSimpleName(),    "proxy-config.json",    "true" },
        };
        expectGeneratedWithNotes(compilation, allParams);
    }

    @Test
    public void testOptionDisableProxy() {
        NativeImageConfigGeneratorProcessor processor = new NativeImageConfigGeneratorProcessor();
        Compilation compilation =
                javac()
                        .withProcessors(processor)
                        .withOptions("-A" + ProxyConfigGen.OPTION_DISABLE) // no value
                        .compile(JavaFileObjects.forResource(
                                "picocli/examples/subcommands/ParentCommandDemo.java"));
        assertThat(compilation).succeeded();
        String[][] allParams = {
                { ReflectConfigGen.class.getSimpleName(),  "reflect-config.json",  "true" },
                { ResourceConfigGen.class.getSimpleName(), "resource-config.json", "true" },
                { ProxyConfigGen.class.getSimpleName(),    "proxy-config.json",    "false" },
        };
        expectGeneratedWithNotes(compilation, allParams);
    }

    @Test
    public void testGenerateReflectJava8ApiOptionProject() {
        NativeImageConfigGeneratorProcessor processor = new NativeImageConfigGeneratorProcessor();
        Compilation compilation =
                javac()
                        .withProcessors(processor)
                        .withOptions("-A" + OPTION_PROJECT + "=example/full")
                        .compile(JavaFileObjects.forSourceLines(
                                "picocli.codegen.graalvm.example.ExampleFull",
                                slurp("/picocli/codegen/graalvm/example/ExampleFull.java")));
        assertThat(compilation).succeeded();
        assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT, "META-INF/native-image/picocli-generated/example/full/reflect-config.json")
                .contentsAsUtf8String().isEqualTo(slurp("/picocli/codegen/graalvm/example/FullExample-reflect-config.json"));
    }

    @Test
    public void testGenerateReflectNestedWithSubcommands() {
        NativeImageConfigGeneratorProcessor processor = new NativeImageConfigGeneratorProcessor();
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
    public void testGenerateReflectConfigForInterface() {
        NativeImageConfigGeneratorProcessor processor = new NativeImageConfigGeneratorProcessor();
        Compilation compilation =
                javac()
                        .withProcessors(processor)
                        .withOptions("-A" + OPTION_PROJECT + "=a/b/c")
                        .compile(JavaFileObjects.forSourceLines(
                                "picocli.codegen.graalvm.example.ExampleInterface",
                                slurp("/picocli/codegen/graalvm/example/ExampleInterface.java")));
        assertThat(compilation).succeeded();
        assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT, "META-INF/native-image/picocli-generated/a/b/c/reflect-config.json")
                .contentsAsUtf8String().isEqualTo(slurp("/picocli/codegen/graalvm/example/example-interface-reflect.json"));
    }

    @Test
    public void testGenerateProxyInterfaceOptionProject() {
        NativeImageConfigGeneratorProcessor processor = new NativeImageConfigGeneratorProcessor();
        Compilation compilation =
                javac()
                        .withProcessors(processor)
                        .withOptions("-A" + OPTION_PROJECT + "=a/b/c")
                        .compile(JavaFileObjects.forSourceLines(
                                "picocli.codegen.graalvm.example.ExampleInterface",
                                slurp("/picocli/codegen/graalvm/example/ExampleInterface.java")));
        assertThat(compilation).succeeded();
        assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT, "META-INF/native-image/picocli-generated/a/b/c/proxy-config.json")
                .contentsAsUtf8String().isEqualTo(slurp("/picocli/codegen/graalvm/example/example-interface-proxy.json"));
    }

    @Test
    public void testGenerateProxyInterfaceOptionOtherInterfaces() {
        NativeImageConfigGeneratorProcessor processor = new NativeImageConfigGeneratorProcessor();
        Compilation compilation =
                javac()
                        .withProcessors(processor)
                        .withOptions("-A" + OPTION_PROJECT + "=a/b/c",
                                "-A" + OPTION_INTERFACE_CLASSES + "=com.abc.Interface1,com.xyz.Interface2")
                        .compile(JavaFileObjects.forSourceLines(
                                "picocli.codegen.graalvm.example.ExampleInterface",
                                slurp("/picocli/codegen/graalvm/example/ExampleInterface.java")));
        assertThat(compilation).succeeded();
        assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT, "META-INF/native-image/picocli-generated/a/b/c/proxy-config.json")
                .contentsAsUtf8String().isEqualTo(slurp("/picocli/codegen/graalvm/example/example-additional-interface-proxy.json"));
    }

    @Test
    public void testGenerateResourceOptionOtherBundlesAndPatterns() {
        NativeImageConfigGeneratorProcessor processor = new NativeImageConfigGeneratorProcessor();
        Compilation compilation =
                javac()
                        .withProcessors(processor)
                        .withOptions("-A" + OPTION_PROJECT + "=x/y/z",
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

    @Test
    public void testGenerateReflectConfigArgGroup() {
        NativeImageConfigGeneratorProcessor processor = new NativeImageConfigGeneratorProcessor();
        Compilation compilation =
                javac()
                        .withProcessors(processor)
                        .withOptions("-A" + OPTION_PROJECT + "=issue793")
                        .compile(JavaFileObjects.forSourceLines(
                                "picocli.issue793.Issue793",
                                slurp("/picocli/issue793/Issue793.java")));
        assertThat(compilation).succeeded();
        assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT, "META-INF/native-image/picocli-generated/issue793/reflect-config.json")
                .contentsAsUtf8String().isEqualTo(slurp("/picocli/issue793/issue793-reflect-config.json"));
    }

    @Test
    public void testGenerateReflectConfigParamConsumer() {
        NativeImageConfigGeneratorProcessor processor = new NativeImageConfigGeneratorProcessor();
        Compilation compilation =
                javac()
                        .withProcessors(processor)
                        .withOptions("-A" + OPTION_PROJECT + "=issue803")
                        .compile(JavaFileObjects.forSourceLines(
                                "picocli.issue803.Issue803",
                                slurp("/picocli/issue803/Issue803.java")));
        assertThat(compilation).succeeded();
        assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT, "META-INF/native-image/picocli-generated/issue803/reflect-config.json")
                .contentsAsUtf8String().isEqualTo(slurp("/picocli/issue803/issue803-reflect-config.json"));
    }
}
