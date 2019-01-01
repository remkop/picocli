package picocli;

import java.lang.reflect.Field;
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

    public static Object interpreter(CommandLine cmd) throws Exception {
        Field field = CommandLine.class.getDeclaredField("interpreter");
        field.setAccessible(true);
        return field.get(cmd);
    }
}
