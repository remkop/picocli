/*
   Copyright 2017 Remko Popma

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package picocli;

import picocli.CommandLine.Help;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.Help.Ansi.Text;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class TestUtil {

    /**
     * Set the trace level to one of "DEBUG", "INFO", "WARN", "OFF".
     * @param level one of "DEBUG", "INFO", "WARN", "OFF".
     * @deprecated use {@link #setTraceLevel(CommandLine.TraceLevel)} instead
     */
    public static void setTraceLevel(String level) {
        System.setProperty("picocli.trace", level);
    }

    /**
     * Set the trace level to one of "DEBUG", "INFO", "WARN", "OFF".
     * @param level the trace level to set.
     */
    public static void setTraceLevel(CommandLine.TraceLevel level) {
        System.setProperty("picocli.trace", String.valueOf(level));
    }

    public static String usageString(Object annotatedObject, Ansi ansi) {
        return usageString(new CommandLine(annotatedObject), ansi);
    }

    public static String usageString(CommandLine commandLine, Ansi ansi) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        commandLine.usage(new PrintStream(baos, true), ansi);
        String result = baos.toString();
        assertEquals(result, commandLine.getUsageMessage(ansi));

        if (ansi == Ansi.AUTO) {
            baos.reset();
            commandLine.usage(new PrintStream(baos, true));
            assertEquals(result, baos.toString());
            assertEquals(result, commandLine.getUsageMessage());
        } else if (ansi == Ansi.ON) {
            baos.reset();
            commandLine.usage(new PrintStream(baos, true), Help.defaultColorScheme(Ansi.ON));
            assertEquals(result, baos.toString());
            assertEquals(result, commandLine.getUsageMessage(Help.defaultColorScheme(Ansi.ON)));
        }
        return result;
    }

    public static String versionString(CommandLine commandLine, Ansi ansi, Object... params) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        commandLine.printVersionHelp(new PrintStream(baos, true), ansi, params);
        String result = baos.toString();
        return result;
    }

    public static Text[] textArray(Help help, String... str) {
        return textArray(help.ansi(), str);
    }

    public static Text[] textArray(Ansi ansi, String... str) {
        Text[] result = new Text[str.length];
        for (int i = 0; i < str.length; i++) {
            result[i] = str[i] == null ? Ansi.EMPTY_TEXT : ansi.new Text(str[i]);
        }
        return result;
    }

    public static Field field(Class<?> cls, String fieldName) throws NoSuchFieldException {
        return cls.getDeclaredField(fieldName);
    }
    public static Field[] fields(Class<?> cls, String... fieldNames) throws NoSuchFieldException {
        Field[] result = new Field[fieldNames.length];
        for (int i = 0; i < fieldNames.length; i++) {
            result[i] = cls.getDeclaredField(fieldNames[i]);
        }
        return result;
    }
    public static CommandLine.Model.OptionSpec option(Object obj, String fieldName) throws Exception {
        return option(obj, fieldName, CommandLine.defaultFactory());
    }
    public static CommandLine.Model.OptionSpec option(Object obj, String fieldName, CommandLine.IFactory factory) throws Exception {
        return CommandLine.Model.OptionSpec.builder(CommandLine.Model.TypedMember.createIfAnnotated(obj.getClass().getDeclaredField(fieldName), new CommandLine.Model.ObjectScope(obj)), factory).build();
    }
    public static CommandLine.Model.OptionSpec[] options(Object obj, String... fieldNames) throws Exception {
        CommandLine.Model.OptionSpec[] result = new CommandLine.Model.OptionSpec[fieldNames.length];
        for (int i = 0; i < fieldNames.length; i++) {
            result[i] = option(obj, fieldNames[i]);
        }
        return result;
    }
    // gives access to package-protected method {@code versionString}
    public static String versionString() {
        return CommandLine.versionString();
    }

    /**
     * Returns the original String with some text stripped out:
     *
     * <pre>
     * "[picocli DEBUG] (ANSI is disabled by default: TTY=false, isXTERM=false, hasOSTYPE=false, isWindows=true, JansiConsoleInstalled=false)" +
     * </pre>
     *
     * @param original the text to process
     * @return
     */
    public static String stripAnsiTrace(String original) {
        String result = original;
        do {
            original = result;
            //result = stripAnsiTraceOnce(original, "(ANSI is disabled by default:", ")", "(ANSI is disabled ...");
            result = stripAnsiTraceOnce(original, "(ANSI is disabled by default:", "CLICOLOR_FORCE=", ")", "(ANSI is disabled ...");
            result = stripAnsiTraceOnce(result, "Creating CommandSpec for object", null, " of class", "Creating CommandSpec... for object");
            result = stripAnsiTraceOnce(result, " on ", null, System.getProperty("line.separator"), "");
        } while (result != original);
        return result;
    }

    private static String stripAnsiTraceOnce(String original, String prefix, String prefix2, String suffix, String replacement) {
        int pos = original.indexOf(prefix);
        if (pos > 0) {
            int endPos = pos;
            if (prefix2 != null) { // this allows us to skip over any intermediate closing brackets ')' in the "ANSI is disabled by default" line (https://github.com/remkop/picocli/issues/1103#issuecomment-640204473)
                int pos2 = original.indexOf(prefix2, pos);
                if (pos2 > 0) {
                    endPos = pos2;
                }
            }
            int to = original.indexOf(suffix, endPos);
            return original.substring(0, pos) + replacement + original.substring(to);
        }
        return original;
    }

    public static <T> Set<T> setOf(T... elements) {
        return new HashSet<T>(Arrays.asList(elements));
    }

    public static <K, V> Map<K, V> mapOf(K k1, V v1) {
        Map<K, V> result = new LinkedHashMap<K, V>();
        result.put(k1, v1);
        return result;
    }

    public static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2) {
        Map<K, V> result = new LinkedHashMap<K, V>();
        result.put(k1, v1);
        result.put(k2, v2);
        return result;
    }

    public static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2, K k3, V v3) {
        Map<K, V> result = new LinkedHashMap<K, V>();
        result.put(k1, v1);
        result.put(k2, v2);
        result.put(k3, v3);
        return result;
    }

    public static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
        Map<K, V> result = new LinkedHashMap<K, V>();
        result.put(k1, v1);
        result.put(k2, v2);
        result.put(k3, v3);
        result.put(k4, v4);
        return result;
    }

    public static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
        Map<K, V> result = new LinkedHashMap<K, V>();
        result.put(k1, v1);
        result.put(k2, v2);
        result.put(k3, v3);
        result.put(k4, v4);
        result.put(k5, v5);
        return result;
    }

    @SuppressWarnings("unchecked")
    public static Object interpreter(CommandLine cmd) throws Exception {
        Field field = CommandLine.class.getDeclaredField("interpreter");
        field.setAccessible(true);
        Object interpreter =  field.get(cmd);

        Class c = Class.forName("picocli.CommandLine$Interpreter");
        Method clear = c.getDeclaredMethod("clear");
        clear.setAccessible(true);
        clear.invoke(interpreter); // initializes the interpreter instance

        Field parseResultField = c.getDeclaredField("parseResultBuilder");
        parseResultField.setAccessible(true);
        Field nowProcessing = CommandLine.ParseResult.Builder.class.getDeclaredField("nowProcessing");
        nowProcessing.setAccessible(true);
        Object parseResult = parseResultField.get(interpreter);
        nowProcessing.set(parseResult, new ArrayList<Object>());

        return interpreter;
    }

    public static String stripHashcodes(String original) {
        StringBuilder result = new StringBuilder();
        int pos = original.indexOf('@');
        int start = 0;
        while (pos >= 0) {
            result.append(original.substring(start, pos + 1));
            start = pos + 1;
            while (Character.isJavaIdentifierPart(original.charAt(start))) { start++; }
            pos = original.indexOf('@', start);
        }
        if (start >= 0) {
            result.append(original.substring(start));
        }
        return result.toString();
    }

    public static boolean equals(Object obj1, Object obj2) { return obj1 == null ? obj2 == null : obj1.equals(obj2); }
    public static int hashCode(Object obj) {return obj == null ? 0 : obj.hashCode(); }
    public static int hashCode(boolean bool) {return bool ? 1 : 0; }
}
