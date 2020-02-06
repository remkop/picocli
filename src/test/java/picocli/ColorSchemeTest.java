package picocli;

import org.junit.Test;
import picocli.CommandLine.Help;
import picocli.CommandLine.Help.ColorScheme;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class ColorSchemeTest {
    @Test
    public void testEquals() {
        ColorScheme defaultScheme = Help.defaultColorScheme(Help.Ansi.AUTO);
        ColorScheme expect = new ColorScheme.Builder()
                .commands(Help.Ansi.Style.bold)
                .options(Help.Ansi.Style.fg_yellow)
                .parameters(Help.Ansi.Style.fg_yellow)
                .optionParams(Help.Ansi.Style.italic).build();
        assertEquals(expect, defaultScheme);
    }

    @Test
    public void testEqualsOther() {
        ColorScheme defaultScheme = Help.defaultColorScheme(Help.Ansi.AUTO);
        assertNotEquals("blah", defaultScheme);
    }

    @Test
    public void testHashCode() {
        ColorScheme defaultScheme = Help.defaultColorScheme(Help.Ansi.AUTO);
        ColorScheme expect = new ColorScheme.Builder()
                .commands(Help.Ansi.Style.bold)
                .options(Help.Ansi.Style.fg_yellow)
                .parameters(Help.Ansi.Style.fg_yellow)
                .optionParams(Help.Ansi.Style.italic).build();
        assertEquals(expect.hashCode(), defaultScheme.hashCode());
    }

    @Test
    public void testToString() {
        ColorScheme defaultScheme = Help.defaultColorScheme(Help.Ansi.AUTO);
        assertEquals("ColorScheme[ansi=AUTO, commands=[bold], optionStyles=[fg_yellow], parameterStyles=[fg_yellow], optionParamStyles=[italic], customMarkupMap=null]", defaultScheme.toString());
    }
    @Test
    public void testColorScheme_customMarkupMapEmptyByDefault() {
        ColorScheme colorScheme = new ColorScheme.Builder().build();
        assertEquals(Collections.emptyMap(), colorScheme.customMarkupMap());
    }

    static final Help.Ansi.IStyle BOLD = new Help.Ansi.IStyle() {
        public String on() { return "*"; }
        public String off() { return "*"; }
    };
    static final Help.Ansi.IStyle ITALIC = new Help.Ansi.IStyle() {
        public String on() { return "_"; }
        public String off() { return "_"; }
    };
    static final Help.Ansi.IStyle UNDERLINE = new Help.Ansi.IStyle() {
        public String on() { return "#"; }
        public String off() { return "#"; }
    };
    @Test
    public void testColorScheme_customMarkupMapSet() {
        Map<String, Help.Ansi.IStyle> map = new HashMap<String, Help.Ansi.IStyle>();
        map.put("bold", BOLD);
        ColorScheme colorScheme = new ColorScheme.Builder().customMarkupMap(map).build();
        assertEquals(map, colorScheme.customMarkupMap());
        assertNotSame(map, colorScheme.customMarkupMap());

        try {
            colorScheme.customMarkupMap().put("not allowed", Help.Ansi.Style.reset);
            fail("Expected exception");
        } catch (UnsupportedOperationException ok) {
        }
    }

    @Test
    public void testColorScheme_parse() {
        Map<String, Help.Ansi.IStyle> map = new HashMap<String, Help.Ansi.IStyle>();
        map.put("bold", BOLD);
        map.put("italic", ITALIC);
        map.put("underline", UNDERLINE);
        ColorScheme colorScheme = new ColorScheme.Builder().customMarkupMap(map).build();

        Help.Ansi.IStyle[] parsed = colorScheme.parse("italic,bold");
        assertArrayEquals(new Help.Ansi.IStyle[] {ITALIC, BOLD}, parsed);
    }

    @Test
    public void testColorScheme_parseNotFound() {
        Map<String, Help.Ansi.IStyle> map = new HashMap<String, Help.Ansi.IStyle>();
        map.put("bold", BOLD);
        map.put("italic", ITALIC);
        map.put("underline", UNDERLINE);
        ColorScheme colorScheme = new ColorScheme.Builder().customMarkupMap(map).build();

        Help.Ansi.IStyle[] parsed = colorScheme.parse("x,y");
        assertArrayEquals(new Help.Ansi.IStyle[0], parsed);
    }

    @Test
    public void testColorScheme_resetStyleWithoutCustomMap() {
        ColorScheme colorScheme = new ColorScheme.Builder().build();
        assertSame(Help.Ansi.Style.reset, colorScheme.resetStyle());
    }

    @Test
    public void testColorScheme_resetStyleInCustomMap() {
        Map<String, Help.Ansi.IStyle> map = new HashMap<String, Help.Ansi.IStyle>();
        map.put("reset", UNDERLINE);
        ColorScheme colorScheme = new ColorScheme.Builder().customMarkupMap(map).build();
        assertSame(UNDERLINE, colorScheme.resetStyle());
    }

    @Test
    public void testColorScheme_resetStyleNotInCustomMap() {
        Map<String, Help.Ansi.IStyle> map = new HashMap<String, Help.Ansi.IStyle>();
        map.put("underline", UNDERLINE);
        ColorScheme colorScheme = new ColorScheme.Builder().customMarkupMap(map).build();
        assertNotSame(UNDERLINE, colorScheme.resetStyle());
        assertEquals("", colorScheme.resetStyle().on());
        assertEquals("", colorScheme.resetStyle().off());
        assertEquals("picocli.CommandLine$Help$ColorScheme$1", colorScheme.resetStyle().getClass().getName());
    }

    @Test
    public void testColorScheme_stringAnsiOff() {
        Map<String, Help.Ansi.IStyle> map = new HashMap<String, Help.Ansi.IStyle>();
        map.put("underline", UNDERLINE);
        ColorScheme colorScheme = new ColorScheme.Builder().ansi(Help.Ansi.OFF).customMarkupMap(map).build();

        assertEquals("Hello UNDERLINE ME please.", colorScheme.string("Hello @|underline UNDERLINE ME|@ please."));
    }

    @Test
    public void testColorScheme_stringAnsiOn() {
        Map<String, Help.Ansi.IStyle> map = new HashMap<String, Help.Ansi.IStyle>();
        map.put("underline", UNDERLINE);
        ColorScheme colorScheme = new ColorScheme.Builder().ansi(Help.Ansi.ON).customMarkupMap(map).build();

        assertEquals("Hello #UNDERLINE ME# please.", colorScheme.string("Hello @|underline UNDERLINE ME|@ please."));
    }

    @Test
    public void testColorSchemeBuilder_customMarkupMapInitiallyNull() {
        ColorScheme.Builder builder = new ColorScheme.Builder();
        assertNull(builder.customMarkupMap());
    }

    @Test
    public void testColorSchemeBuilder_customMarkupMapSet() {
        Map<String, Help.Ansi.IStyle> map = new HashMap<String, Help.Ansi.IStyle>();
        map.put("bold", BOLD);
        ColorScheme.Builder builder = new ColorScheme.Builder().customMarkupMap(map);
        assertEquals(map, builder.customMarkupMap());
        assertSame(map, builder.customMarkupMap());
    }

    @Test
    public void testColorSchemeBuilder_copyWhenCustomMarkupMapSet() {
        Map<String, Help.Ansi.IStyle> map = new HashMap<String, Help.Ansi.IStyle>();
        map.put("bold", BOLD);
        ColorScheme original = new ColorScheme.Builder().customMarkupMap(map).build();

        ColorScheme.Builder builder = new ColorScheme.Builder(original);
        assertEquals(original.customMarkupMap(), builder.customMarkupMap());
        assertNotSame(original.customMarkupMap(), builder.customMarkupMap());
        assertNotSame(map, builder.customMarkupMap());
    }
}
