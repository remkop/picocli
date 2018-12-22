package picocli;

import org.junit.Test;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.Messages;

import java.util.ResourceBundle;

import static org.junit.Assert.*;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;

public class ModelMessagesTest {

    @Test
    public void testMessagesCopyNull() {
        assertNull(Messages.copy(CommandSpec.create(), null));
    }

    @Test
    public void testMessagesCopyNonNull() {
        Messages orig = new Messages(CommandSpec.create(), null);
        Messages copy = Messages.copy(CommandSpec.create(), orig);
        assertNull(copy.resourceBundle());
    }

    @Test
    public void testMessagesCommandSpec() {
        CommandSpec spec = CommandSpec.create();
        Messages orig = new Messages(spec, null);
        assertSame(spec, orig.commandSpec());
    }

    @Test
    public void testMessagesEmpty() {
        assertTrue(Messages.empty((Messages) null));
        assertTrue(Messages.empty(new Messages(CommandSpec.create(), null)));
    }

    @Test
    public void testMessagesGetStringNullKey() {
        String def = "abc";
        assertSame(def, new Messages(CommandSpec.create(), null).getString(null, def));
        assertSame(def, new Messages(CommandSpec.create(), null).getString("help", def));

        ResourceBundle rb = ResourceBundle.getBundle("picocli.SharedMessages");
        assertSame(def, new Messages(CommandSpec.create(), rb).getString(null, def));

        assertNotEquals(def, new Messages(CommandSpec.create(), rb).getString("help", def));
    }

    @Test
    public void testMessagesGetStringArrayNullKey() {
        String[] def = {"abc"};
        assertSame(def, new Messages(CommandSpec.create(), null).getStringArray(null, def));
        assertSame(def, new Messages(CommandSpec.create(), null).getStringArray("help", def));

        ResourceBundle rb = ResourceBundle.getBundle("picocli.SharedMessages");
        assertSame(def, new Messages(CommandSpec.create(), rb).getStringArray(null, def));

        assertNotEquals(def, new Messages(CommandSpec.create(), rb).getStringArray("usage.description", def));
    }
}
