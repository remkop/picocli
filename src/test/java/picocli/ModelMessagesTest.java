package picocli;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.Messages;

import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.Vector;

import static org.junit.Assert.*;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;

public class ModelMessagesTest {

    // allows tests to set any kind of properties they like, without having to individually roll them back
    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");

    @Test
    public void testMessagesCopyNull() {
        assertNull(Messages.copy(CommandSpec.create(), null));
    }

    @Test
    public void testMessagesCopyNonNull() {
        Messages orig = new Messages(CommandSpec.create(), (ResourceBundle) null);
        Messages copy = Messages.copy(CommandSpec.create(), orig);
        assertNull(copy.resourceBundle());
    }

    @Test
    public void testMessagesCommandSpec() {
        CommandSpec spec = CommandSpec.create();
        Messages orig = new Messages(spec, (ResourceBundle) null);
        assertSame(spec, orig.commandSpec());
    }

    @Test
    public void testMessagesEmpty() {
        assertTrue(Messages.empty((Messages) null));
        assertTrue(Messages.empty(new Messages(CommandSpec.create(), (ResourceBundle) null)));
    }

    @Test
    public void testMessagesIsEmpty() {
        assertTrue(new Messages(CommandSpec.create(), (ResourceBundle) null).isEmpty());
    }

    @Test
    public void testMessagesIsEmptyForEmptyResourceBundle() {
        ResourceBundle rb = new ResourceBundle() {
            protected Object handleGetObject(String key) { return null; }
            public Enumeration<String> getKeys() {
                return new Vector<String>().elements();
            }
        };
        assertTrue(new Messages(CommandSpec.create(), rb).isEmpty());
    }

    @Test
    public void testMessagesGetStringNullKey() {
        String def = "abc";
        assertSame(def, new Messages(CommandSpec.create(), (ResourceBundle) null).getString(null, def));
        assertSame(def, new Messages(CommandSpec.create(), (ResourceBundle) null).getString("help", def));

        ResourceBundle rb = ResourceBundle.getBundle("picocli.SharedMessages");
        assertSame(def, new Messages(CommandSpec.create(), rb).getString(null, def));

        assertNotEquals(def, new Messages(CommandSpec.create(), rb).getString("help", def));
    }

    @Test
    public void testMessagesGetStringArrayNullKey() {
        String[] def = {"abc"};
        assertSame(def, new Messages(CommandSpec.create(), (ResourceBundle) null).getStringArray(null, def));
        assertSame(def, new Messages(CommandSpec.create(), (ResourceBundle) null).getStringArray("help", def));

        ResourceBundle rb = ResourceBundle.getBundle("picocli.SharedMessages");
        assertSame(def, new Messages(CommandSpec.create(), rb).getStringArray(null, def));

        assertNotEquals(def, new Messages(CommandSpec.create(), rb).getStringArray("usage.description", def));
    }
}
