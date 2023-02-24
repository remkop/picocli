package picocli.annotation.processing.tests;

import com.google.common.collect.ImmutableList;
import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.hamcrest.MatcherAssert;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import picocli.CommandLine;
import picocli.codegen.annotation.processing.AbstractCommandSpecProcessor;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;
import static picocli.annotation.processing.tests.Resources.slurp;
import static picocli.annotation.processing.tests.Resources.slurpAll;
import static picocli.annotation.processing.tests.YamlAssert.compareCommandYamlDump;

public class AbstractCommandSpecProcessorTest {
    static Locale old;

    @BeforeClass
    public static void beforeClass() {
        old = Locale.getDefault();
        Locale.setDefault(Locale.UK); // #715 get English diagnostic messages
    }

    @AfterClass
    public static void afterClass() {
        Locale.setDefault(old); // #715 get English diagnostic messages
    }

    @Test
    public void testCommandWithMixin() {
        Compilation compilation = compareCommandYamlDump(slurp("/picocli/examples/mixin/CommandWithMixin.yaml"),
                JavaFileObjects.forResource("picocli/examples/mixin/CommandWithMixin.java"),
                JavaFileObjects.forResource("picocli/examples/mixin/CommonOption.java"));

        assertOnlySourceVersionWarning(compilation);
    }

    @Test
    public void testSubcommands() {
        Compilation compilation = compareCommandYamlDump(slurp("/picocli/examples/subcommands/FileUtils.yaml"),
                JavaFileObjects.forResource("picocli/examples/subcommands/ParentCommandDemo.java"));

        assertOnlySourceVersionWarning(compilation);
    }

    @Test
    public void testSetterMethodOnClass() {
        List<String> expected = slurpAll("/picocli/examples/annotatedmethods/CPrimitives.yaml",
                "/picocli/examples/annotatedmethods/CPrimitivesWithDefault.yaml",
                "/picocli/examples/annotatedmethods/CObjectsWithDefaults.yaml",
                "/picocli/examples/annotatedmethods/CObjects.yaml"
        );

        Compilation compilation = compareCommandYamlDump(expected,
                JavaFileObjects.forResource(
                        "picocli/examples/annotatedmethods/AnnotatedClassMethodOptions.java"));

        assertOnlySourceVersionWarning(compilation);
    }

    @Test
    public void testGetterMethodOnInterface() {
        List<String> expected = slurpAll("/picocli/examples/annotatedmethods/IFPrimitives.yaml",
                "/picocli/examples/annotatedmethods/IFPrimitivesWithDefault.yaml",
                "/picocli/examples/annotatedmethods/IFObjects.yaml",
                "/picocli/examples/annotatedmethods/IFObjectsWithDefault.yaml");

        Compilation compilation = compareCommandYamlDump(expected,
                JavaFileObjects.forResource(
                        "picocli/examples/annotatedmethods/AnnotatedInterfaceMethodOptions.java"));

        assertOnlySourceVersionWarning(compilation);
    }

    @Test
    public void testInvalidAnnotationsOnInterface() {
        CommandSpec2YamlProcessor processor = new CommandSpec2YamlProcessor();
        Compilation compilation =
                javac()
                        .withProcessors(processor)
                        .compile(JavaFileObjects.forResource(
                                "picocli/examples/annotatedmethods/InvalidAnnotatedInterfaceMethodOptions.java"));

        assertThat(compilation).failed();
        ImmutableList<Diagnostic<? extends JavaFileObject>> errors = compilation.errors();
        assertEquals("expected error count", 3, errors.size());

        for (Diagnostic<? extends JavaFileObject> diag : errors) {
            MatcherAssert.assertThat(diag.getMessage(Locale.ENGLISH),
                    containsString("Invalid picocli annotation on interface field"));
        }
        //assertThat(compilation).hadErrorContaining("Invalid picocli annotation on interface field");
    }

    @Test
    public void testInvalidAnnotationCombinations() {
        CommandSpec2YamlProcessor processor = new CommandSpec2YamlProcessor();
        Compilation compilation =
                javac()
                        .withProcessors(processor)
                        .compile(JavaFileObjects.forResource(
                                "picocli/examples/validation/Invalid.java"));

        assertThat(compilation).failed();

        List<String> expected = new ArrayList<String>(Arrays.asList(
            "Subcommand is missing @Command annotation with a name attribute",
            "Subcommand @Command annotation should have a name attribute",
            "@Mixin must have a declared type, not int",
            "invalidOptionAndMixin cannot have both @picocli.CommandLine.Option and @picocli.CommandLine.Mixin annotations",
            "invalidOptionAndParentCommand cannot have both @picocli.CommandLine.Option and @picocli.CommandLine.ParentCommand annotations",
            "invalidParametersAndMixin cannot have both @picocli.CommandLine.Parameters and @picocli.CommandLine.Mixin annotations",
            "invalidParametersAndParentCommand cannot have both @picocli.CommandLine.Parameters and @picocli.CommandLine.ParentCommand annotations",
            "invalidSpecAndMixin cannot have both @picocli.CommandLine.Mixin and @picocli.CommandLine.Spec annotations",
            "invalidOptionAndSpec cannot have both @picocli.CommandLine.Option and @picocli.CommandLine.Spec annotations",
            "invalidParametersAndSpec cannot have both @picocli.CommandLine.Parameters and @picocli.CommandLine.Spec annotations",
            "invalidOptionAndParameters cannot have both @picocli.CommandLine.Option and @picocli.CommandLine.Parameters annotations",
            "invalidParentCommandAndMixin cannot have both @picocli.CommandLine.Mixin and @picocli.CommandLine.ParentCommand annotations",
            "invalidSpecAndParentCommand cannot have both @picocli.CommandLine.ParentCommand and @picocli.CommandLine.Spec annotations",
            "invalidUnmatchedAndSpec cannot have both @picocli.CommandLine.Spec and @picocli.CommandLine.Unmatched annotations",
            "invalidUnmatchedAndMixin cannot have both @picocli.CommandLine.Mixin and @picocli.CommandLine.Unmatched annotations",
            "invalidOptionAndUnmatched cannot have both @picocli.CommandLine.Option and @picocli.CommandLine.Unmatched annotations",
            "invalidParametersAndUnmatched cannot have both @picocli.CommandLine.Parameters and @picocli.CommandLine.Unmatched annotations",
            "invalidUnmatchedAndParentCommand cannot have both @picocli.CommandLine.ParentCommand and @picocli.CommandLine.Unmatched annotations",
            "@Unmatched must be of type String[] or List<String> but was: int",
            "@Unmatched must be of type String[] or List<String> but was: int",
            "@Unmatched must be of type String[] or List<String> but was: int",
            "@Unmatched must be of type String[] or List<String> but was: int",
            "@Unmatched must be of type String[] or List<String> but was: java.lang.Integer"
        ));
        List<String> reportedTwiceOnJava9Plus = new ArrayList<>(Arrays.asList(
                "@Mixin must have a declared type, not int"
                ));
        validateErrorMessages(compilation, expected, reportedTwiceOnJava9Plus);
    }

    @Test
    public void testInvalidAnnotationCombinations2() {
        Compilation compilation =
                javac()
                        .withProcessors(new CommandSpec2YamlProcessor())
                        .compile(JavaFileObjects.forResource(
                                "picocli/examples/validation/Invalid2.java"));

        assertThat(compilation).failed();

        List<String> expected = new ArrayList<String>(Arrays.asList(
                "invalidNegatableShouldBeBoolean must be a boolean: only boolean options can be negatable.",
                "FATAL ERROR: picocli.CommandLine$InitializationException: Only boolean options can be negatable, but field int picocli.examples.validation.Invalid2.invalidNegatableShouldBeBoolean is of type int"
        ));
        validateErrorMessages(compilation, expected);
    }

    @Test
    public void testInvalidAnnotationCombinations3() {
        Compilation compilation =
                javac()
                        .withProcessors(new CommandSpec2YamlProcessor())
                        .compile(JavaFileObjects.forResource(
                                "picocli/examples/validation/Invalid3.java"));

        assertThat(compilation).failed();

        List<String> expected = new ArrayList<String>(Arrays.asList(
                "Only getter or setter methods can be annotated with @Option, but invalidNeitherGetterNorSetter is neither."
        ));
        List<String> optional = new ArrayList<>(Arrays.asList(
                "Only getter or setter methods can be annotated with @Option, but invalidNeitherGetterNorSetter is neither."
        ));
        validateErrorMessages(compilation, expected, optional);
    }

    @Test
    public void testInvalidAnnotationCombinations4() {
        Compilation compilation =
                javac()
                        .withProcessors(new CommandSpec2YamlProcessor())
                        .compile(JavaFileObjects.forResource(
                                "picocli/examples/validation/Invalid4.java"));

        assertThat(compilation).failed();

        List<String> expected = new ArrayList<String>(Arrays.asList(
                "invalidUsageHelpShouldBeBoolean must be a boolean: a command can have max one usageHelp boolean flag that triggers display of the usage help message.",
                "invalidVersionHelpShouldBeBoolean must be a boolean: a command can have max one versionHelp boolean flag that triggers display of the version information.",
                "An option can be usageHelp or versionHelp, but invalidDuplicateUsageAndVersionHelp is both.",
                "An command can only have one usageHelp option, but Invalid4 has 3."

        ));
        List<String> optional = new ArrayList<>(Arrays.asList(
                "An command can only have one versionHelp option, but Invalid4 has 3."
        ));
        validateErrorMessages(compilation, expected, optional);
    }

    @Test
    public void testInvalidSplitOnSinglValueOptionOrPositional() {
        Compilation compilation =
                javac()
                        .withProcessors(new CommandSpec2YamlProcessor())
                        .compile(JavaFileObjects.forResource(
                                "picocli/examples/validation/InvalidSplit.java"));

        assertThat(compilation).failed();

        List<String> expected = new ArrayList<String>(Arrays.asList(
                "singleOption has a split regex but is a single-value type",
                "singlePositional has a split regex but is a single-value type",
                "FATAL ERROR: picocli.CommandLine$InitializationException: Only multi-value options and positional parameters should have a split regex (this check can be disabled by setting system property 'picocli.ignore.invalid.split')"
        ));
        List<String> optional = new ArrayList<>(Arrays.asList(
        ));
        validateErrorMessages(compilation, expected, optional);
    }

    @Test
    public void testInvalidFinalOptionsAndParameters() {
        CommandSpec2YamlProcessor processor = new CommandSpec2YamlProcessor();
        Compilation compilation =
            javac()
                .withProcessors(processor)
                .compile(JavaFileObjects.forResource(
                    "picocli/examples/validation/InvalidFinal.java"));

        assertThat(compilation).failed();

        // For every primitive type + String type, the InvalidFinal class defines
        // an invalid combination of using a final field with a declared value, for each of those types.
        List<String> types = Arrays.asList(
            "boolean",
            "byte",
            "short",
            "int",
            "long",
            "char",
            "float",
            "double",
            "string"
        );

        String errorFormat = "Constant (final) primitive and String fields like %s cannot be used as %s: compile-time constant inlining may hide new values written to it.";
        List<String> expectedValidationErrors = new ArrayList<String>();
        for (String type : types) {
            String titleized = type.substring(0, 1).toUpperCase() + type.substring(1);
            String invalidOptionField = String.format("invalid%s", titleized);
            String invalidParamField = String.format("invalid%sParam", titleized);

            expectedValidationErrors.add(String.format(errorFormat, invalidOptionField, "@Option"));
            expectedValidationErrors.add(String.format(errorFormat, invalidParamField, "@Parameters"));
        }

        validateErrorMessages(compilation, expectedValidationErrors);
    }

    private void validateErrorMessages(Compilation compilation, List<String> expected) {
        validateErrorMessages(compilation, expected, Collections.emptyList());
    }

    private void validateErrorMessages(Compilation compilation, List<String> expected, List<String> optional) {
        ImmutableList<Diagnostic<? extends JavaFileObject>> errors = compilation.errors();
        for (Diagnostic<? extends JavaFileObject> diag : errors) {
            String msg = diag.getMessage(Locale.ENGLISH);
            String firstLine = msg.split("\\r?\\n")[0];
            if (!expected.remove(firstLine)) {
                assertTrue("Unexpected error: " + msg, optional.remove(firstLine));
            }
        }
        assertTrue("Expected errors: " + expected, expected.isEmpty());
    }

    @Test
    public void testCommandWithBundleLoaded() {
        CommandLine.tracer().setLevel(CommandLine.TraceLevel.DEBUG);
        AbstractCommandSpecProcessor.setLoadResourceBundles(true);
        Compilation compilation = compareCommandYamlDump(slurp("/picocli/examples/messages/CommandWithBundle.yaml"),
                JavaFileObjects.forResource("picocli/examples/messages/CommandWithBundle.java"));

        assertOnlySourceVersionWarning(compilation);
        CommandLine.tracer().setLevel(CommandLine.TraceLevel.WARN);
    }

    @Test
    public void testCommandWithBundleNotLoaded() {
        CommandLine.tracer().setLevel(CommandLine.TraceLevel.DEBUG);
        AbstractCommandSpecProcessor.setLoadResourceBundles(false);
        try {
            Compilation compilation = compareCommandYamlDump(slurp("/picocli/examples/messages/CommandWithBundle-NotLoaded.yaml"),
                JavaFileObjects.forResource("picocli/examples/messages/CommandWithBundle2.java"));

            assertOnlySourceVersionWarning(compilation);
        } finally {
            CommandLine.tracer().setLevel(CommandLine.TraceLevel.WARN);
        }
    }

    private void assertOnlySourceVersionWarning(Compilation compilation) {
        assertThat(compilation).hadWarningCount(0); // #826 version warnings are now suppressed
        // assertThat(compilation).hadWarningContaining("Supported source version 'RELEASE_6' from annotation processor 'picocli.annotation.processing.tests");
    }

}
