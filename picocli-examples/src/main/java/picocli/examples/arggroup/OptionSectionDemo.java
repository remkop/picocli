package picocli.examples.arggroup;

import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * <p>
 * The example below uses groups to define options sections in the usage help.
 * When a group has a non-null {@code heading} (or {@code headingKey}),
 * the options in the group are given the specified heading in the usage help message.
 * </p><p>
 * The {@code headingKey} attribute can be used to get the heading text from the command's resource bundle.
 * </p><p>
 * In this example, the groups are non-validating ({@code validate = false}),
 * so the grouping is for display purposes only.
 * </p><p>
 * This prints the following usage help message:
 * </p>
 * <pre>{@code
 * Usage: sectiondemo [-a=<a>] [-b=<b>] [-c=<c>] [-x=<x>] [-y=<y>] [-z=<z>]
 * Section demo
 * This is the first section
 *   -a=<a>    Option A
 *   -b=<b>    Option B
 *   -c=<c>    Option C
 * This is the second section
 *   -x=<x>    Option X
 *   -y=<y>    Option Y
 *   -z=<z>    Option X
 * }</pre>
 */
@Command(name = "sectiondemo", description = "Section demo")
public class OptionSectionDemo {

    @ArgGroup(validate = false, heading = "This is the first section%n")
    Section1 section1;

    static class Section1 {
        @Option(names = "-a", description = "Option A") int a;
        @Option(names = "-b", description = "Option B") int b;
        @Option(names = "-c", description = "Option C") int c;
    }

    @ArgGroup(validate = false, heading = "This is the second section%n")
    Section2 section2;

    static class Section2 {
        @Option(names = "-x", description = "Option X") int x;
        @Option(names = "-y", description = "Option Y") int y;
        @Option(names = "-z", description = "Option X") int z;
    }

    public static void main(String[] args) {
        new CommandLine(new OptionSectionDemo()).usage(System.out);
    }
}
