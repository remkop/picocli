package picocli.codegen.aot.graalvm.processor;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.Test;

import javax.tools.StandardLocation;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;
import static picocli.codegen.aot.graalvm.processor.NativeImageConfigGeneratorProcessor.OPTION_PROJECT;
import static picocli.codegen.aot.graalvm.processor.ProxyConfigGen.OPTION_INTERFACE_CLASSES;
import static picocli.codegen.aot.graalvm.processor.ResourceConfigGen.OPTION_BUNDLES;
import static picocli.codegen.aot.graalvm.processor.ResourceConfigGen.OPTION_RESOURCE_REGEX;
import static picocli.codegen.util.Resources.slurp;

public class NativeImageConfigGeneratorProcessorTest {

    @Test
    public void testNothingDisabledByDefault() {
        NativeImageConfigGeneratorProcessor processor = new NativeImageConfigGeneratorProcessor();
        Compilation compilation =
                javac()
                        .withProcessors(processor)
                        .compile(JavaFileObjects.forResource(
                                "picocli/examples/subcommands/ParentCommandDemo.java"));
        assertThat(compilation).succeeded();
        String[] all = {
                "reflect-config.json",
                "proxy-config.json",
                "resource-config.json",
        };
        for (String file : all) {
            assertThat(compilation)
                    .generatedFile(StandardLocation.CLASS_OUTPUT,
                            "META-INF/native-image/picocli-generated/" + file);
        }
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
        String[] all = {
                //"reflect-config.json",
                "proxy-config.json",
                "resource-config.json",
        };
        for (String file : all) {
            assertThat(compilation)
                    .generatedFile(StandardLocation.CLASS_OUTPUT,
                            "META-INF/native-image/picocli-generated/" + file);
        }
        assertThat(compilation).hadNoteContaining(ReflectConfigGen.class.getSimpleName() + " is available but not enabled");
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
        String[] all = {
                "reflect-config.json",
                "proxy-config.json",
                //"resource-config.json",
        };
        for (String file : all) {
            assertThat(compilation)
                    .generatedFile(StandardLocation.CLASS_OUTPUT,
                            "META-INF/native-image/picocli-generated/" + file);
        }
        assertThat(compilation).hadNoteContaining(ResourceConfigGen.class.getSimpleName() + " is available but not enabled");
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
        String[] all = {
                "reflect-config.json",
                //"proxy-config.json",
                "resource-config.json",
        };
        for (String file : all) {
            assertThat(compilation)
                    .generatedFile(StandardLocation.CLASS_OUTPUT,
                            "META-INF/native-image/picocli-generated/" + file);
        }
        assertThat(compilation).hadNoteContaining(ProxyConfigGen.class.getSimpleName() + " is available but not enabled");
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
}