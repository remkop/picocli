package picocli;

import org.junit.Test;
import picocli.CommandLine.Model.CommandSpec;

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
}
