package picocli.annotation.processing.tests;

import com.google.common.collect.ImmutableList;
import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.hamcrest.MatcherAssert;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.*;
import static picocli.annotation.processing.tests.Resources.slurp;
import static picocli.annotation.processing.tests.Resources.slurpAll;

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
        Compilation compilation = compareCommandDump(slurp("/picocli/examples/mixin/CommandWithMixin.yaml"),
                JavaFileObjects.forResource("picocli/examples/mixin/CommandWithMixin.java"),
                JavaFileObjects.forResource("picocli/examples/mixin/CommonOption.java"));

        assertOnlySourceVersionWarning(compilation);
    }

    @Test
    public void testSubcommands() {
        Compilation compilation = compareCommandDump(slurp("/picocli/examples/subcommands/FileUtils.yaml"),
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

        Compilation compilation = compareCommandDump(expected,
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

        Compilation compilation = compareCommandDump(expected,
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
        validateErrorMessages(compilation, expected);
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
    public void testCommandWithBundle() {
        Compilation compilation = compareCommandDump(slurp("/picocli/examples/messages/CommandWithBundle.yaml"),
                JavaFileObjects.forResource("picocli/examples/messages/CommandWithBundle.java"));

        assertOnlySourceVersionWarning(compilation);
    }

    private Compilation compareCommandDump(String expected, JavaFileObject ... sourceFiles) {
        return compareCommandDump(Arrays.asList(expected), sourceFiles);
    }

    private Compilation compareCommandDump(List<String> expected, JavaFileObject ... sourceFiles) {
        CommandSpec2YamlProcessor processor = new CommandSpec2YamlProcessor();
        Compilation compilation =
                javac()
                        .withProcessors(processor)
                        .compile(sourceFiles);

        assertTrue("Expected at least " + expected.size() + " commands but found " + processor.strings.size(),
                expected.size() <= processor.strings.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEqualCommand(expected.get(i), processor.strings.get(i));
        }
        return compilation;
    }

    private void assertOnlySourceVersionWarning(Compilation compilation) {
        assertThat(compilation).hadWarningCount(1);
        assertThat(compilation).hadWarningContaining("Supported source version 'RELEASE_6' from annotation processor 'picocli.annotation.processing.tests");
    }

    private void assertEqualCommand(String expected, String actual) {
        List<String> exceptions = Arrays.asList("userObject: ", "hasInitialValue: ", "initialValue: ", "# ");
        Scanner expectedScanner = new Scanner(expected);
        Scanner actualScanner = new Scanner(actual);
        int count = 0;
        NEXT_LINE: while (actualScanner.hasNextLine()) {
            String actualLine = actualScanner.nextLine();
            assertTrue("Unexpected actual line: " + actualLine, expectedScanner.hasNextLine());
            String expectedLine = expectedScanner.nextLine();
            count++;

            String actLine = actualLine.trim();
            String expLine = expectedLine.trim();

            for (String exception : exceptions) {
                if (expLine.startsWith(exception) && actLine.startsWith(exception)) {
                    continue NEXT_LINE;
                }
            }
            if (expLine.startsWith("Mixin: ") && actLine.startsWith("Mixin: ")) {
                continue NEXT_LINE;
            }
            if (expLine.startsWith("paramLabel: '<arg") && actLine.startsWith("paramLabel: '<")) {
                continue NEXT_LINE;
            }

            if (expLine.startsWith("typeInfo: ") && actLine.startsWith("typeInfo: ")) {
                assertSimilarTypeInfo(expectedLine, actualLine);
//            } else if (expLine.startsWith("defaultValueProvider: ") && actLine.startsWith("defaultValueProvider: ")) {
//                assertSimilarDefaultValueProvider(expectedLine, actualLine);
//            } else if (expLine.startsWith("versionProvider: ") && actLine.startsWith("versionProvider: ")) {
//                assertSimilarVersionProvider(expectedLine, actualLine);
            } else if ((expLine.startsWith("getter: ") && actLine.startsWith("getter: ")) ||
                    (expLine.startsWith("setter: ") && actLine.startsWith("setter: "))) {
                assertSimilarGetterSetter(expectedLine, actualLine);
            } else {
                if (!expectedLine.equals(actualLine)) {
                    assertEquals("Difference at line " + count + ": expected ["
                                    + expLine + "] but was [" + actLine + "]",
                            expected, actual);
                }
            }
        }
        if (expectedScanner.hasNextLine()) {
            assertEquals("Actual is missing one or more lines after line " + count,
                    expected, actual);
        }
    }

    private void assertSimilarTypeInfo(String expectedLine, String actualLine) {
        final String EXPECTED_PREFIX = "typeInfo: RuntimeTypeInfo(";
        final String ACTUAL_PREFIX = "typeInfo: CompileTimeTypeInfo(";
        Assert.assertThat(expectedLine.trim(), startsWith(EXPECTED_PREFIX));
        Assert.assertThat(actualLine.trim(), startsWith(ACTUAL_PREFIX));

        String expected = expectedLine.substring(expectedLine.indexOf(EXPECTED_PREFIX) + EXPECTED_PREFIX.length());
        String actual = actualLine.substring(actualLine.indexOf(ACTUAL_PREFIX) + ACTUAL_PREFIX.length());

        String expected2 = expected.replace("class ", "");

        Pattern pattern = Pattern.compile(".*, aux=\\[(.*)\\], collection.*");
        Matcher matcher = pattern.matcher(expected2);
        assertTrue(matcher.matches());
        String group = matcher.group(1).replace(", ", ",");
        if (!actual.startsWith(group)) {
            int pos = expected2.indexOf(',');
            expected2 = expected2.substring(0, pos) + "<" + group + ">" + expected2.substring(pos);
        }
        assertEquals(expected2, actual);
    }

    private void assertSimilarDefaultValueProvider(String expectedLine, String actualLine) {
        if ("defaultValueProvider: null".equals(expectedLine.trim())) {
            assertEquals("defaultValueProvider: DefaultValueProviderMetaData[default]", actualLine.trim());
        } else {
            assertEquals("Not implemented yet", expectedLine, actualLine);
        }
    }

    private void assertSimilarVersionProvider(String expectedLine, String actualLine) {
        if ("versionProvider: null".equals(expectedLine.trim())) {
            assertEquals("versionProvider: VersionProviderMetaData[default]", actualLine.trim());
        } else {
            assertEquals("Not implemented yet", expectedLine, actualLine);
        }
    }

    private void assertSimilarGetterSetter(String expectedLine, String actualLine) {
        String expect = expectedLine.trim().substring(1);
        String actual = actualLine.trim().substring(1);
        if (expect.startsWith("etter: picocli.CommandLine.Model.FieldBinding")) {
            Assert.assertThat(actual, startsWith(
                    "etter: picocli.codegen.annotation.processing.AnnotatedElementHolder(FIELD"));
        } else if (expect.startsWith("etter: picocli.CommandLine.Model.MethodBinding")) {
            Assert.assertThat(actual, startsWith(
                    "etter: picocli.codegen.annotation.processing.AnnotatedElementHolder(METHOD"));
        } else if (expect.startsWith("etter: picocli.CommandLine$Model$PicocliInvocationHandler$ProxyBinding")) {
            Assert.assertThat(actual, startsWith(
                    "etter: picocli.codegen.annotation.processing.AnnotatedElementHolder(METHOD"));
        } else if (expect.startsWith("etter: picocli.CommandLine.Model.ObjectBinding")) {
            Assert.assertThat(actual, startsWith(
                    "etter: picocli.codegen.annotation.processing.AnnotatedElementHolder(PARAMETER"));
        } else {
            assertEquals("Not implemented yet", expect, actual);
        }
    }
}