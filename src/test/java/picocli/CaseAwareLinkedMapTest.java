package picocli;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;
import picocli.CommandLine.Model.CaseAwareLinkedMap;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.junit.Assert.*;

public class CaseAwareLinkedMapTest {

    // allows tests to set any kind of properties they like, without having to individually roll them back
    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");

    @Test
    public void testDefaultCaseSensitivity() {
        assertFalse(new CaseAwareLinkedMap<String, String>().isCaseInsensitive());
    }

    @Test
    public void testDefaultLocale() {
        assertEquals(Locale.ENGLISH, new CaseAwareLinkedMap<String, String>().getLocale());
    }

    @Test
    public void testCopyConstructor() {
        CaseAwareLinkedMap<String, String> map = new CaseAwareLinkedMap<String, String>();
        map.put("foo", "bar");
        map.put("FOO", "BAR");
        CaseAwareLinkedMap<String, String> copy = new CaseAwareLinkedMap<String, String>(map);
        assertFalse(copy.isCaseInsensitive());
        assertEquals(Locale.ENGLISH, copy.getLocale());
        assertEquals(2, copy.size());
        assertEquals("bar", copy.get("foo"));
        assertEquals("BAR", copy.get("FOO"));
    }

    @Test
    public void testCaseSensitiveAddDuplicateElement() {
        CaseAwareLinkedMap<String, String> map = new CaseAwareLinkedMap<String, String>();
        map.setCaseInsensitive(false);
        map.put("key", "value");
        assertEquals(1, map.size());
        assertTrue(map.containsKey("key"));
        assertFalse(map.containsKey("KEY"));
        assertTrue(map.containsValue("value"));
        Object replacedValue = map.put("Key", "VALUE");
        assertNull(replacedValue);
        assertEquals(2, map.size());
        assertTrue(map.containsKey("key"));
        assertTrue(map.containsKey("Key"));
        assertFalse(map.containsKey("KEY"));
        assertTrue(map.containsValue("value"));
        replacedValue = map.put("key", "VALUE");
        assertEquals("value", replacedValue);
        assertEquals(2, map.size());
        assertTrue(map.containsKey("key"));
        assertTrue(map.containsKey("Key"));
        assertFalse(map.containsKey("KEY"));
        assertFalse(map.containsValue("value"));
        assertTrue(map.containsValue("VALUE"));
    }

    @Test
    public void testToggleCaseInsensitiveNoDuplicateElement() {
        CaseAwareLinkedMap<String, String> map = new CaseAwareLinkedMap<String, String>();
        map.setCaseInsensitive(false);
        map.put("key", "value");
        map.put("kee", "value");
        assertTrue(map.containsKey("key"));
        assertFalse(map.containsKey("KEY"));
        assertEquals(2, map.size());
        map.setCaseInsensitive(true);
        assertTrue(map.containsKey("key"));
        assertTrue(map.containsKey("KEY"));
        assertEquals(2, map.size());
    }

    @Test
    public void testCaseSensitiveRemoveElement() {
        CaseAwareLinkedMap<String, String> map = new CaseAwareLinkedMap<String, String>();
        map.setCaseInsensitive(false);
        map.put("key", "value");
        assertEquals(1, map.size());
        Object removedValue = map.remove("KEY");
        assertNull(removedValue);
        assertEquals(1, map.size());
        removedValue = map.remove("key");
        assertEquals("value", removedValue);
        assertEquals(0, map.size());
    }

    @Test
    public void testCaseInsensitiveAddDuplicateElement() {
        CaseAwareLinkedMap<String, String> map = new CaseAwareLinkedMap<String, String>();
        map.setCaseInsensitive(true);
        map.put("key", "value");
        assertEquals(1, map.size());
        Object replacedValue = map.put("Key", "VALUE");
        assertEquals("value", replacedValue);
        assertEquals(1, map.size());
    }

    @Test
    public void testToggleCaseInsensitiveDuplicateElement() {
        CaseAwareLinkedMap<String, String> map = new CaseAwareLinkedMap<String, String>();
        map.put("key", "value");
        map.put("Key", "value");
        assertEquals(2, map.size());
        try {
            map.setCaseInsensitive(true);
            fail("Expected exception");
        } catch (CommandLine.DuplicateNameException ex) {
            assertEquals("Duplicated keys: key and Key", ex.getMessage());
        }
    }

    @Test
    public void testCaseInsensitiveRemoveElement() {
        CaseAwareLinkedMap<String, String> map = new CaseAwareLinkedMap<String, String>();
        map.setCaseInsensitive(true);
        map.put("key", "value");
        assertEquals(1, map.size());
        Object removedValue = map.remove("KEY");
        assertEquals("value", removedValue);
        assertEquals(0, map.size());
    }

    @Test
    public void testNullKeys() {
        CaseAwareLinkedMap<String, String> map = new CaseAwareLinkedMap<String, String>();
        map.setCaseInsensitive(false);
        assertNull(map.get(null));
        assertFalse(map.containsKey(null));
        assertNull(map.getCaseSensitiveKey(null));

        map.put(null, "value");
        assertTrue(map.containsKey(null));
        assertTrue(map.containsValue("value"));
        assertEquals(1, map.size());
        assertEquals("value", map.get(null));
        assertNull(map.getCaseSensitiveKey(null));
        map.setCaseInsensitive(true);
        assertEquals(1, map.size());
        assertEquals("value", map.get(null));
        assertTrue(map.containsKey(null));
        assertTrue(map.containsValue("value"));
        assertNull(map.getCaseSensitiveKey(null));

        assertEquals("value", map.put(null, "value2"));
        assertTrue(map.containsKey(null));
        assertFalse(map.containsValue("value"));
        assertTrue(map.containsValue("value2"));
        assertEquals(1, map.size());
        assertEquals("value2", map.get(null));

        assertEquals("value2", map.remove(null));
        assertEquals(0, map.size());
        assertNull(map.get(null));
        assertFalse(map.containsKey(null));
        assertFalse(map.containsKey("value2"));
        assertNull(map.getCaseSensitiveKey(null));
    }

    @Test
    public void testClearMap() {
        CaseAwareLinkedMap<String, String> map = new CaseAwareLinkedMap<String, String>();

        map.clear();
        map.setCaseInsensitive(false);
        map.put("key", "value");
        map.clear();
        assertEquals(0, map.size());
        assertNull(map.get("key"));
        assertFalse(map.containsKey("key"));

        map.clear();
        map.setCaseInsensitive(true);
        map.put("key", "value");
        map.clear();
        assertEquals(0, map.size());
        assertNull(map.get("key"));
        assertFalse(map.containsKey("key"));
    }

    @Test
    public void testNonExistentKey() {
        CaseAwareLinkedMap<String, String> map = new CaseAwareLinkedMap<String, String>();

        map.setCaseInsensitive(false);
        assertNull(map.get("key"));
        assertNull(map.put("key2", "value"));
        assertNull(map.remove("key"));

        map.clear();

        map.setCaseInsensitive(true);
        assertNull(map.get("key"));
        assertNull(map.put("key2", "value"));
        assertNull(map.remove("key"));
    }

    @Test
    public void testInconvertibleClass() {
        assertTrue (CaseAwareLinkedMap.isCaseConvertible(String.class));
        assertTrue (CaseAwareLinkedMap.isCaseConvertible(Character.class));
        assertFalse(CaseAwareLinkedMap.isCaseConvertible(Object.class));

        CaseAwareLinkedMap<Object, String> map = new CaseAwareLinkedMap<Object, String>();
        Object dummy = new Object();
        map.setCaseInsensitive(false);
        assertNull(map.get(dummy));
        assertFalse(map.containsKey(dummy));

        map.setCaseInsensitive(true);
        assertNull(map.get(dummy));
        assertFalse(map.containsKey(dummy));

        try {
            map.getCaseSensitiveKey(dummy);
            fail("Expected exception");
        } catch (UnsupportedOperationException ex) {
            assertEquals("Unsupported case-conversion for key class java.lang.Object", ex.getMessage());
        }
    }
}
