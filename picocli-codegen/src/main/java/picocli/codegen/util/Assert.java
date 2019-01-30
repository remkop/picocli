package picocli.codegen.util;

/**
 * Utility class providing some defensive coding convenience methods.
 */
public final class Assert {
    /**
     * Throws a NullPointerException if the specified object is null.
     * @param object the object to verify
     * @param description error message
     * @param <T> type of the object to check
     * @return the verified object
     */
    public static <T> T notNull(T object, String description) {
        if (object == null) {
            throw new NullPointerException(description);
        }
        return object;
    }
    public static boolean equals(Object obj1, Object obj2) { return obj1 == null ? obj2 == null : obj1.equals(obj2); }
    public static int hashCode(Object obj) {return obj == null ? 0 : obj.hashCode(); }
    public static int hashCode(boolean bool) {return bool ? 1 : 0; }

    private Assert() {} // private constructor: never instantiate
}
