package picocli.annotation.processing.tests;

import com.google.testing.compile.Compilation;

import javax.tools.JavaFileObject;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.testing.compile.Compiler.javac;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public final class YamlAssert {
    private YamlAssert() {
        // this is a utility
    }

    public static Compilation compareCommandYamlDump(String expected, JavaFileObject... sourceFiles) {
        return compareCommandYamlDump(Arrays.asList(expected), sourceFiles);
    }

    public static Compilation compareCommandYamlDump(List<String> expected, JavaFileObject ... sourceFiles) {
        CommandSpec2YamlProcessor processor = new CommandSpec2YamlProcessor();
        Compilation compilation =
                javac()
                        .withProcessors(processor)
                        .compile(sourceFiles);

        assertTrue("Expected at least " + expected.size() + " commands but found " + processor.strings.size(),
                expected.size() <= processor.strings.size());
        for (int i = 0; i < expected.size(); i++) {
            YamlAssert.assertEqualCommand(expected.get(i), processor.strings.get(i));
        }
        return compilation;
    }

    public static void assertEqualCommand(String expected, String actual) {
        List<String> exceptions = Arrays.asList("userObject: ", "hasInitialValue: ", "initialValue: ", "# ");
        Scanner expectedScanner = new Scanner(expected);
        Scanner actualScanner = new Scanner(actual);
        int count = 0;
        NEXT_LINE: while (actualScanner.hasNextLine()) {
            String actualLine = actualScanner.nextLine();
            assertTrue("Unexpected actual line: " + actualLine + " in \n" + actual, expectedScanner.hasNextLine());
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

    private static void assertSimilarTypeInfo(String expectedLine, String actualLine) {
        final String EXPECTED_PREFIX = "typeInfo: RuntimeTypeInfo(";
        final String ACTUAL_PREFIX = "typeInfo: CompileTimeTypeInfo(";
        assertThat(expectedLine.trim(), anyOf(startsWith(EXPECTED_PREFIX), startsWith(ACTUAL_PREFIX)));
        assertThat(actualLine.trim(), startsWith(ACTUAL_PREFIX));

        String expected = expectedLine.trim().startsWith(EXPECTED_PREFIX)
                ? expectedLine.substring(expectedLine.indexOf(EXPECTED_PREFIX) + EXPECTED_PREFIX.length())
                : expectedLine.substring(expectedLine.indexOf(ACTUAL_PREFIX) + ACTUAL_PREFIX.length());
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

    private static void assertSimilarDefaultValueProvider(String expectedLine, String actualLine) {
        if ("defaultValueProvider: null".equals(expectedLine.trim())) {
            assertEquals("defaultValueProvider: DefaultValueProviderMetaData[default]", actualLine.trim());
        } else {
            assertEquals("Not implemented yet", expectedLine, actualLine);
        }
    }

    private static void assertSimilarVersionProvider(String expectedLine, String actualLine) {
        if ("versionProvider: null".equals(expectedLine.trim())) {
            assertEquals("versionProvider: VersionProviderMetaData[default]", actualLine.trim());
        } else {
            assertEquals("Not implemented yet", expectedLine, actualLine);
        }
    }

    private static void assertSimilarGetterSetter(String expectedLine, String actualLine) {
        String expect = expectedLine.trim().substring(1);
        String actual = actualLine.trim().substring(1);
        if (expect.startsWith("etter: picocli.CommandLine.Model.FieldBinding")) {
            assertThat(actual, startsWith(
                    "etter: AnnotatedElementHolder(FIELD"));
        } else if (expect.startsWith("etter: picocli.CommandLine.Model.MethodBinding")) {
            assertThat(actual, startsWith(
                    "etter: AnnotatedElementHolder(METHOD"));
        } else if (expect.startsWith("etter: picocli.CommandLine$Model$PicocliInvocationHandler$ProxyBinding")) {
            assertThat(actual, startsWith(
                    "etter: AnnotatedElementHolder(METHOD"));
        } else if (expect.startsWith("etter: picocli.CommandLine.Model.ObjectBinding")) {
            assertThat(actual, startsWith(
                    "etter: AnnotatedElementHolder(PARAMETER"));
        } else {
            assertEquals("Not implemented yet", expect, actual);
        }
    }
}
