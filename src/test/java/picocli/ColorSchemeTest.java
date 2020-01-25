package picocli;

import org.junit.Test;
import picocli.CommandLine.Help.ColorScheme;

import static org.junit.Assert.*;

public class ColorSchemeTest {
    @Test
    public void testEquals() {
        ColorScheme defaultScheme = CommandLine.Help.defaultColorScheme(CommandLine.Help.Ansi.AUTO);
        ColorScheme expect = new ColorScheme.Builder()
                .commands(CommandLine.Help.Ansi.Style.bold)
                .options(CommandLine.Help.Ansi.Style.fg_yellow)
                .parameters(CommandLine.Help.Ansi.Style.fg_yellow)
                .optionParams(CommandLine.Help.Ansi.Style.italic).build();
        assertEquals(expect, defaultScheme);
    }

    @Test
    public void testEqualsOther() {
        ColorScheme defaultScheme = CommandLine.Help.defaultColorScheme(CommandLine.Help.Ansi.AUTO);
        assertNotEquals("blah", defaultScheme);
    }

    @Test
    public void testHashCode() {
        ColorScheme defaultScheme = CommandLine.Help.defaultColorScheme(CommandLine.Help.Ansi.AUTO);
        ColorScheme expect = new ColorScheme.Builder()
                .commands(CommandLine.Help.Ansi.Style.bold)
                .options(CommandLine.Help.Ansi.Style.fg_yellow)
                .parameters(CommandLine.Help.Ansi.Style.fg_yellow)
                .optionParams(CommandLine.Help.Ansi.Style.italic).build();
        assertEquals(expect.hashCode(), defaultScheme.hashCode());
    }

    @Test
    public void testToString() {
        ColorScheme defaultScheme = CommandLine.Help.defaultColorScheme(CommandLine.Help.Ansi.AUTO);
        assertEquals("ColorScheme[ansi=AUTO, commands=[bold], optionStyles=[fg_yellow], parameterStyles=[fg_yellow], optionParamStyles=[italic]]", defaultScheme.toString());
    }
}
