package picocli;

import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.*;

public class LinkedCaseAwareMapTest {
    private Class<?> getLinkedCaseAwareMapClass() throws ClassNotFoundException {
        return Class.forName("picocli.CommandLine$Model$LinkedCaseAwareMap");
    }

    private Object constructMap() {
        try {
            Class<?> clazz = getLinkedCaseAwareMapClass();
            return clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Object get(Object map, Object key) {
        try {
            Class<?> clazz = getLinkedCaseAwareMapClass();
            Method get = clazz.getMethod("get", Object.class);
            return get.invoke(map, key);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Object put(Object map, Object key, Object value) {
        try {
            Class<?> clazz = getLinkedCaseAwareMapClass();
            Method put = clazz.getMethod("put", Object.class, Object.class);
            return put.invoke(map, key, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Object remove(Object map, Object key) {
        try {
            Class<?> clazz = getLinkedCaseAwareMapClass();
            Method remove = clazz.getMethod("remove", Object.class);
            return remove.invoke(map, key);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isCaseInsensitive(Object map) {
        try {
            Class<?> clazz = getLinkedCaseAwareMapClass();
            Method isCaseInsensitive = clazz.getMethod("isCaseInsensitive");
            return (Boolean) isCaseInsensitive.invoke(map);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setCaseInsensitive(Object map, boolean caseInsensitive) {
        try {
            Class<?> clazz = getLinkedCaseAwareMapClass();
            Method setCaseInsensitive = clazz.getMethod("setCaseInsensitive", boolean.class);
            setCaseInsensitive.invoke(map, caseInsensitive);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private int size(Object map) {
        try {
            Class<?> clazz = getLinkedCaseAwareMapClass();
            Method size = clazz.getMethod("size");
            return (Integer) size.invoke(map);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testDefaultCaseSensitivity() {
        assertFalse(isCaseInsensitive(constructMap()));
    }

    @Test
    public void testCaseSensitiveAddDuplicateElement() {
        Object map = constructMap();
        setCaseInsensitive(map, false);
        put(map, "key", "value");
        assertEquals(1, size(map));
        Object replacedValue = put(map, "Key", "VALUE");
        assertNull(replacedValue);
        assertEquals(2, size(map));
        replacedValue = put(map, "key", "VALUE");
        assertEquals("value", replacedValue);
        assertEquals(2, size(map));
    }

    @Test
    public void testToggleCaseInsensitiveNoDuplicateElement() {
        Object map = constructMap();
        setCaseInsensitive(map, false);
        put(map, "key", "value");
        put(map, "kee", "value");
        assertEquals(2, size(map));
        setCaseInsensitive(map, true);
        assertEquals(2, size(map));
    }

    @Test
    public void testCaseSensitiveRemoveElement() {
        Object map = constructMap();
        setCaseInsensitive(map, false);
        put(map, "key", "value");
        assertEquals(1, size(map));
        Object removedValue = remove(map, "KEY");
        assertNull(removedValue);
        assertEquals(1, size(map));
        removedValue = remove(map, "key");
        assertEquals("value", removedValue);
        assertEquals(0, size(map));
    }

    @Test
    public void testCaseInsensitiveAddDuplicateElement() {
        Object map = constructMap();
        setCaseInsensitive(map, true);
        put(map, "key", "value");
        assertEquals(1, size(map));
        Object replacedValue = put(map, "Key", "VALUE");
        assertEquals("value", replacedValue);
        assertEquals(1, size(map));
    }

    @Test
    public void testToggleCaseInsensitiveDuplicateElement() {
        Object map = constructMap();
        put(map, "key", "value");
        put(map, "Key", "value");
        assertEquals(2, size(map));
        try {
            setCaseInsensitive(map, true);
            fail("Expected exception");
        } catch (Exception ex) {
            assertEquals("Duplicated keys: key and Key", ex.getCause().getCause().getMessage());
        }
    }

    @Test
    public void testCaseInsensitiveRemoveElement() {
        Object map = constructMap();
        setCaseInsensitive(map, true);
        put(map, "key", "value");
        assertEquals(1, size(map));
        Object removedValue = remove(map, "KEY");
        assertEquals("value", removedValue);
        assertEquals(0, size(map));
    }

    @Test
    public void testNullKeys() {
        Object map = constructMap();
        setCaseInsensitive(map, false);
        assertNull(get(map, null));

        put(map, null, "value");
        assertEquals(1, size(map));
        assertEquals("value", get(map, null));
        setCaseInsensitive(map, true);
        assertEquals(1, size(map));
        assertEquals("value", get(map, null));

        assertEquals("value", put(map, null, "value2"));
        assertEquals(1, size(map));
        assertEquals("value2", get(map, null));

        assertEquals("value2", remove(map, null));
        assertEquals(0, size(map));
        assertNull(get(map, null));
    }
}
