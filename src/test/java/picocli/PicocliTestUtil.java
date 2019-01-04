package picocli;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class PicocliTestUtil {
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
            result = stripAnsiTraceOnce(original);
        } while (result != original);
        return result;
    }

    private static String stripAnsiTraceOnce(String original) {
        String prefix = "(ANSI is disabled by default: isatty=";
        int pos = original.indexOf(prefix);
        if (pos > 0) {
            int to = original.indexOf(")", pos);
            return original.substring(0, pos + 1) + original.substring(to);
        }
        return original;
    }

    public static <T> Set<T> setOf(T... elements) {
        Set<T> result = new HashSet<T>();
        for (T t : elements) { result.add(t); }
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

        Field parseResultField = c.getDeclaredField("parseResult");
        parseResultField.setAccessible(true);
        Field nowProcessing = CommandLine.ParseResult.Builder.class.getDeclaredField("nowProcessing");
        nowProcessing.setAccessible(true);
        Object parseResult = parseResultField.get(interpreter);
        nowProcessing.set(parseResult, new ArrayList<Object>());

        return interpreter;
    }
}
