package picocli;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;
import picocli.CommandLine.Help;
import picocli.CommandLine.Help.ColorScheme;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class ColorSchemeTest {

    // allows tests to set any kind of properties they like, without having to individually roll them back
    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Test
    public void testEquals() {
        ColorScheme defaultScheme = Help.defaultColorScheme(Help.Ansi.AUTO);
        ColorScheme expect = new ColorScheme.Builder()
                .commands(Help.Ansi.Style.bold)
                .options(Help.Ansi.Style.fg_yellow)
                .parameters(Help.Ansi.Style.fg_yellow)
                .optionParams(Help.Ansi.Style.italic)
                .errors(Help.Ansi.Style.fg_red, Help.Ansi.Style.bold)
                .stackTraces(Help.Ansi.Style.italic)
                .build();
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
                .optionParams(Help.Ansi.Style.italic)
                .errors(Help.Ansi.Style.fg_red, Help.Ansi.Style.bold)
                .stackTraces(Help.Ansi.Style.italic).build();
        assertEquals(expect.hashCode(), defaultScheme.hashCode());
    }

    @Test
    public void testToString() {
        ColorScheme defaultScheme = Help.defaultColorScheme(Help.Ansi.AUTO);
        assertEquals("ColorScheme[ansi=AUTO, commands=[bold], optionStyles=[fg_yellow], parameterStyles=[fg_yellow], optionParamStyles=[italic], errorStyles=[fg_red, bold], stackTraceStyles=[italic], customMarkupMap=null]", defaultScheme.toString());
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
    public void testColorScheme_singleException() throws UnsupportedEncodingException {
        @CommandLine.Command
        class App implements Runnable{
            public void run() {
                throw new RuntimeException("This is a runtime exception");
            }
        }

        PrintStream originalErr = System.err;
        ByteArrayOutputStream baos = new ByteArrayOutputStream(2500);
        System.setErr(new PrintStream(baos));

        final String PROPERTY = "picocli.ansi";
        String old = System.getProperty(PROPERTY);
        System.setProperty(PROPERTY, "true");

        CommandLine commandLine = new CommandLine(new App());
        commandLine.execute(new String[0]);
        ColorScheme colorScheme = commandLine.getColorScheme();

        System.setErr(originalErr);

        String actual = new String(baos.toByteArray(), "UTF8");
        String[] outputs = actual.split(System.getProperty("line.separator"));

        final String exceptionMessage = "java.lang.RuntimeException: This is a runtime exception";
        final String stackTraceFirstLine = "\tat picocli.ColorSchemeTest$1App.run";
        final String stackTraceANSIStart = Help.Ansi.Style.on(colorScheme.stackTraceStyles().toArray(new Help.Ansi.Style[0]));
        final String stackTraceFirstLineWithANSIStart = stackTraceANSIStart + stackTraceFirstLine;

        assertEquals(colorScheme.errorText(exceptionMessage).toString(), outputs[0]);
        assertEquals(stackTraceFirstLineWithANSIStart, outputs[1].substring(0, stackTraceFirstLineWithANSIStart.length()));

        if (old == null) {
            System.clearProperty(PROPERTY);
        } else {
            System.setProperty(PROPERTY, old);
        }
    }

    @Test
    public void testColorScheme_nestedException() throws UnsupportedEncodingException {
        @CommandLine.Command
        class App implements Runnable{
            public void run() {
                Exception e1 = new RuntimeException("Root Cause");
                Exception e2 = new RuntimeException("Inner Exception", e1);
                throw new RuntimeException("Outer Exception", e2);
            }
        }

        PrintStream originalErr = System.err;
        ByteArrayOutputStream baos = new ByteArrayOutputStream(2500);
        System.setErr(new PrintStream(baos));

        final String PROPERTY = "picocli.ansi";
        String old = System.getProperty(PROPERTY);
        System.setProperty(PROPERTY, "true");

        CommandLine commandLine = new CommandLine(new App());
        commandLine.execute(new String[0]);
        ColorScheme colorScheme = commandLine.getColorScheme();

        System.setErr(originalErr);

        String actual = new String(baos.toByteArray(), "UTF8");
        String[] outputs = actual.split(System.getProperty("line.separator"));

        final String[] exceptionMessages = new String[]{"java.lang.RuntimeException: Outer Exception",
                "Caused by: java.lang.RuntimeException: Inner Exception",
                "Caused by: java.lang.RuntimeException: Root Cause"};

        final String exceptionANSIStart = Help.Ansi.Style.on(colorScheme.errorStyles().toArray(new Help.Ansi.Style[0]));

        int foundExceptionMessageCount = 0;
        for (String line : outputs) {
            if (line.startsWith(exceptionANSIStart + exceptionMessages[foundExceptionMessageCount])) {
                foundExceptionMessageCount++;

                if (foundExceptionMessageCount == 3) {
                    break;
                }
            }
        }

        assertEquals(3, foundExceptionMessageCount);

        if (old == null) {
            System.clearProperty(PROPERTY);
        } else {
            System.setProperty(PROPERTY, old);
        }
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
