package picocli;

import org.junit.Ignore;
import org.junit.Test;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;

import static org.junit.Assert.*;

public class NegatableOptionTest {

    @Test
    public void testRegex() {
        CommandLine.RegexTransformer transformer = new CommandLine.RegexTransformer()
                .addPattern("^-(\\w)$", "+$1", "±$1") // TBD include short option transforms by default?
                .addPattern("^+(\\w)$", "-$1", "±$1") // (same: transform +x to -x)
                .addPattern("^--(\\w(-|\\w)*)$", "--no-$1", "--[no-]$1")
                .addPattern("^--no-(\\w(-|\\w)*)$", "--$1", "--[no-]$1")
                .addPattern("^(-|--)(\\w*:)\\+(\\w(-|\\w)*)$", "$1$2-$3", "$1$2±$3")
                .addPattern("^(-|--)(\\w*:)\\-(\\w(-|\\w)*)$", "$1$2+$3", "$1$2±$3")
                ;

        CommandSpec dummy = CommandSpec.create();
        assertEquals("-X:-option", transformer.makeNegative("-X:+option", dummy));
        assertEquals("-X:±option", transformer.makeSynopsis("-X:+option", dummy));
        assertEquals("-X:+option", transformer.makeNegative("-X:-option", dummy));
        assertEquals("-X:±option", transformer.makeSynopsis("-X:-option", dummy));

        assertEquals("+x", transformer.makeNegative("-x", dummy));
        assertEquals("±x", transformer.makeSynopsis("-x", dummy));

        assertEquals("--no-verbose", transformer.makeNegative("--verbose", dummy));
        assertEquals("--[no-]verbose", transformer.makeSynopsis("--verbose", dummy));
    }

    @Ignore
    @Test
    public void testUsage() {
        class App {
            @Option(names = "-a",                     negatable = true, description = "...") boolean a;
            @Option(names = "--long",                 negatable = true, description = "...") boolean longWithoutNo;
            @Option(names = {"-b", "--long-b"},       negatable = true, description = "...") boolean shortAndLong;
            @Option(names = "--no-verbose",           negatable = true, description = "...") boolean longInitiallyNo;
            @Option(names = {"-n", "--no-verbose-b"}, negatable = true, description = "...") boolean shortAndLongInitiallyNo;
            @Option(names = {"-c", "--X:-java1"},     negatable = true, description = "...", defaultValue = "true") boolean javaStyle1;
            @Option(names = {      "-X:-java2"},      negatable = true, description = "...", defaultValue = "true") boolean javaStyle2;
            @Option(names = {      "-XX:-java3"},     negatable = true, description = "...", defaultValue = "true") boolean javaStyle3;
            @Option(names = {"-f", "--X:+java4"},     negatable = true, description = "...", defaultValue = "true") boolean javaStyle4;
            @Option(names = {"-g", "-X:+java5"},      negatable = true, description = "...", defaultValue = "true") boolean javaStyle5;
            @Option(names = {"-h", "-XX:+java6"},     negatable = true, description = "...", defaultValue = "true") boolean javaStyle6;
        }

        String expected = String.format("" +
                "Usage: <main class> [-abcfghn] [--[no-]long] [--[no-]verbose] [-X:±java2] [-XX:±java3]%n" +
                "  -a                     ...%n" +
                "  -b, --[no-]long-b      ...%n" +
                "  -c, --X:±java1         ...%n" +
                "  -f, --X:±java4         ...%n" +
                "  -g, -X:±java5          ...%n" +
                "  -h, -XX:±java6         ...%n" +
                "      --[no-]long        ...%n" +
                "  -n, --[no-]verbose-b   ...%n" +
                "      --[no-]verbose     ...%n" +
                "      -X:±java2          ...%n" +
                "      -XX:±java3         ...%n");
        String actual = new CommandLine(new App()).getUsageMessage(Ansi.OFF);
        assertEquals(expected, actual);
    }

}
