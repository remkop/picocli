package picocli.codegen.aot.graalvm.processor;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.Test;

import javax.tools.Diagnostic;
import javax.tools.JavaFileManager;
import javax.tools.StandardLocation;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

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
        int noteCount = 0;
        for (String[] params : allParams) {
            String noteText = String.format(
                    "%s writing to: CLASS_OUTPUT/META-INF/native-image/picocli-generated/%s", params[0], params[1]);
            boolean generated = Boolean.parseBoolean(params[2]);
            boolean verbose = Boolean.parseBoolean(params[3]);
            if (generated) {
                assertThat(compilation)
                        .generatedFile(StandardLocation.CLASS_OUTPUT,
                                "META-INF/native-image/picocli-generated/" + params[1]);
                if (verbose) {
                    assertThat(compilation).hadNoteContaining(noteText);
                    noteCount++;
                }
            } else {
                assertNoGeneratedFile(compilation, StandardLocation.CLASS_OUTPUT,
                        "META-INF/native-image/picocli-generated/" + params[1]);
                if (verbose) {
                    assertThat(compilation).hadNoteContaining(params[0] + " is not enabled");
                    List<String> notes = compilation.diagnostics().stream()
                            .filter(d -> d.getKind() == Diagnostic.Kind.NOTE)
                            .map(d -> d.getMessage(Locale.ENGLISH))
                            .collect(Collectors.toList());
                    assertFalse(notes.stream().anyMatch(note -> note.contains(noteText)));
                    assertThat(compilation).hadNoteContainingMatch(params[0] + " is not enabled");
                    noteCount++;
                }
            }
        }
        assertThat(compilation).hadNoteCount(noteCount);
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
                { ReflectConfigGen.class.getSimpleName(),  "reflect-config.json",  "true", "false" },
                { ResourceConfigGen.class.getSimpleName(), "resource-config.json", "true", "false" },
                { ProxyConfigGen.class.getSimpleName(),    "proxy-config.json",    "true", "false" },
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
                { ReflectConfigGen.class.getSimpleName(),  "reflect-config.json",  "false", "false" },
                { ResourceConfigGen.class.getSimpleName(), "resource-config.json", "true" , "false"},
                { ProxyConfigGen.class.getSimpleName(),    "proxy-config.json",    "true" , "false"},
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
                { ReflectConfigGen.class.getSimpleName(),  "reflect-config.json",  "true" , "false"},
                { ResourceConfigGen.class.getSimpleName(), "resource-config.json", "false", "false" },
                { ProxyConfigGen.class.getSimpleName(),    "proxy-config.json",    "true" , "false"},
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
                { ReflectConfigGen.class.getSimpleName(),  "reflect-config.json",  "true" , "false"},
                { ResourceConfigGen.class.getSimpleName(), "resource-config.json", "true" , "false"},
                { ProxyConfigGen.class.getSimpleName(),    "proxy-config.json",    "false", "false" },
        };
        expectGeneratedWithNotes(compilation, allParams);
    }

    @Test
    public void testOptionVerbose() {
        runOptionVerboseTest("-A" + AbstractGenerator.OPTION_VERBOSE);
    }

    @Test
    public void testOptionVerboseEqTrue() {
        runOptionVerboseTest("-A" + AbstractGenerator.OPTION_VERBOSE + "=true");
    }

    private void runOptionVerboseTest(String option) {
        NativeImageConfigGeneratorProcessor processor = new NativeImageConfigGeneratorProcessor();
        Compilation compilation =
            javac()
                .withProcessors(processor)
                .withOptions(option)
                .compile(JavaFileObjects.forResource(
                    "picocli/examples/subcommands/ParentCommandDemo.java"));

        assertThat(compilation).succeeded();
        String[][] allParams = {
            { ReflectConfigGen.class.getSimpleName(),  "reflect-config.json",  "true", "true"},
            { ResourceConfigGen.class.getSimpleName(), "resource-config.json", "true", "true"},
            { ProxyConfigGen.class.getSimpleName(),    "proxy-config.json",    "true", "true" },
        };
        expectGeneratedWithNotes(compilation, allParams);
    }

    @Test
    public void testOptionDisableProxyVerbose() {
        NativeImageConfigGeneratorProcessor processor = new NativeImageConfigGeneratorProcessor();
        Compilation compilation =
                javac()
                        .withProcessors(processor)
                        .withOptions("-A" + ProxyConfigGen.OPTION_DISABLE, "-A" + AbstractGenerator.OPTION_VERBOSE) // no value
                        .compile(JavaFileObjects.forResource(
                                "picocli/examples/subcommands/ParentCommandDemo.java"));

        assertThat(compilation).succeeded();
        String[][] allParams = {
                { ReflectConfigGen.class.getSimpleName(),  "reflect-config.json",  "true" , "true" },
                { ResourceConfigGen.class.getSimpleName(), "resource-config.json", "true" , "true" },
                { ProxyConfigGen.class.getSimpleName(),    "proxy-config.json",    "false", "true" },
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
    public void testGenerateReflectForNestedInnerEnum() {
        NativeImageConfigGeneratorProcessor processor = new NativeImageConfigGeneratorProcessor();
        Compilation compilation =
                javac()
                        .withProcessors(processor)
                        .withOptions("-A" + OPTION_PROJECT + "=nested/enum")
                        .compile(JavaFileObjects.forSourceLines(
                                "picocli.ls.FileList",
                                slurp("/picocli/ls/FileList.java")));
        assertThat(compilation).succeeded();
        assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT, "META-INF/native-image/picocli-generated/nested/enum/reflect-config.json")
                .contentsAsUtf8String().isEqualTo(slurp("/picocli/ls/expected-reflect.json"));
    }

    @Test
    public void testGenerateReflectForNestedInnerEnumWithoutPackage() {
        NativeImageConfigGeneratorProcessor processor = new NativeImageConfigGeneratorProcessor();
        Compilation compilation =
                javac()
                        .withProcessors(processor)
                        .withOptions("-A" + OPTION_PROJECT + "=nested/enum")
                        .compile(JavaFileObjects.forSourceLines(
                                "FileList",
                                slurp("/FileList.java")));
        assertThat(compilation).succeeded();
        assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT, "META-INF/native-image/picocli-generated/nested/enum/reflect-config.json")
                .contentsAsUtf8String().isEqualTo(slurp("/expected-filelist-reflect.json"));
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
    public void testGenerateReflectConfigIssue850MissingMixin() {
        NativeImageConfigGeneratorProcessor processor = new NativeImageConfigGeneratorProcessor();
        Compilation compilation =
                javac()
                        .withProcessors(processor)
                        .withOptions("-A" + OPTION_PROJECT + "=issue850")
                        .compile(JavaFileObjects.forSourceLines(
                                "picocli.issue850missingmixin.App",
                                slurp("/picocli/issue850missingmixin/App.java")),
                                JavaFileObjects.forSourceLines(
                                        "picocli.issue850missingmixin.InitCommand",
                                        slurp("/picocli/issue850missingmixin/InitCommand.java")),
                                JavaFileObjects.forSourceLines(
                                        "picocli.issue850missingmixin.ProviderMixin",
                                        slurp("/picocli/issue850missingmixin/ProviderMixin.java"))
                                );
        assertThat(compilation).succeeded();
        assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT, "META-INF/native-image/picocli-generated/issue850/reflect-config.json")
                .contentsAsUtf8String().isEqualTo(slurp("/picocli/issue850missingmixin/issue850-reflect-config.json"));
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
