package picocli.examples.env_var;

import picocli.CommandLine;
import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.Option;

import java.util.Arrays;

/**
 * Demonstrates the use of variables in default values.
 *
 * Demonstrated how to interpret the mere existence of
 * an environment variable or system property as a flag being "true".
 */
public class App {
    @Option(names = "--flag1", defaultValue = "${sys:MYVAR}", converter = MyBooleanConverter.class)
    boolean flag1;

    @Option(names = "--flag2", defaultValue = "${env:PATH}", converter = MyBooleanConverter.class)
    boolean flag2;

    @Option(names = "--flag3", defaultValue = "${env:NO_SUCH_VAR_HOPEFULLY}", converter = MyBooleanConverter.class)
    boolean flag3;

    /**
     * Custom boolean converter that returns {@code false} if:
     * <ul>
     *   <li>the variable does not exist</li>
     *   <li>the variable exists with value 'N', 'NO', 'FALSE' (case-insensitive)</li>
     * </ul>
     * The converter returns {@code true} if:
     * <ul>
     *   <li>the variable exists but is blank (e.g. {@code `java -DMYVAR App`})</li>
     *   <li>the variable exists with any value except 'N', 'NO', 'FALSE' (case-insensitive)</li>
     * </ul>
     */
    static class MyBooleanConverter implements ITypeConverter<Boolean> {
        public Boolean convert(String value) {
            return value == null || !Arrays.asList("n", "no", "false").contains(value.toLowerCase());
        }
    }

    public static void main(String[] args) {

        // prints false
        System.clearProperty("MYVAR");
        System.out.printf("Var not specified,              result=%s%n", CommandLine.populateCommand(new App()).flag1);

        // prints true
        System.setProperty("MYVAR","");
        System.out.printf("Var specified without value,    result=%s%n", CommandLine.populateCommand(new App()).flag1);

        // prints false
        System.setProperty("MYVAR","N");
        System.out.printf("Var specified with false value, result=%s%n", CommandLine.populateCommand(new App()).flag1);

        // prints true
        System.setProperty("MYVAR","yes");
        System.out.printf("Var specified with other value, result=%s%n", CommandLine.populateCommand(new App()).flag1);

        // prints true
        System.out.printf("Env var with non-FALSE value,   result=%s%n", CommandLine.populateCommand(new App()).flag2);

        // prints false
        System.out.printf("Env var that does not exist,    result=%s%n", CommandLine.populateCommand(new App()).flag3);
    }
}
