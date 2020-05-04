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

    private boolean isCaseConvertible(Class<?> clazzToTest) {
        try {
            Class<?> clazz = getLinkedCaseAwareMapClass();
            Method isCaseInsensitive = clazz.getDeclaredMethod("isCaseConvertible", Class.class);
            isCaseInsensitive.setAccessible(true);
            return (Boolean) isCaseInsensitive.invoke(null, clazzToTest);
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

    private Object getCaseSensitiveKey(Object map, Object caseInsensitiveKey) {
        try {
            Class<?> clazz = getLinkedCaseAwareMapClass();
            Method getCaseSensitiveKey = clazz.getMethod("getCaseSensitiveKey", Object.class);
            return getCaseSensitiveKey.invoke(map, caseInsensitiveKey);
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

    private boolean containsKey(Object map, Object key) {
        try {
            Class<?> clazz = getLinkedCaseAwareMapClass();
            Method containsKey = clazz.getMethod("containsKey", Object.class);
            return (Boolean) containsKey.invoke(map, key);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean containsValue(Object map, Object value) {
        try {
            Class<?> clazz = getLinkedCaseAwareMapClass();
            Method containsValue = clazz.getMethod("containsValue", Object.class);
            return (Boolean) containsValue.invoke(map, value);
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

    private void clear(Object map) {
        try {
            Class<?> clazz = getLinkedCaseAwareMapClass();
            Method clear = clazz.getMethod("clear");
            clear.invoke(map);
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
        assertTrue(containsKey(map, "key"));
        assertFalse(containsKey(map, "KEY"));
        assertTrue(containsValue(map, "value"));
        Object replacedValue = put(map, "Key", "VALUE");
        assertNull(replacedValue);
        assertEquals(2, size(map));
        assertTrue(containsKey(map, "key"));
        assertTrue(containsKey(map, "Key"));
        assertFalse(containsKey(map, "KEY"));
        assertTrue(containsValue(map, "value"));
        replacedValue = put(map, "key", "VALUE");
        assertEquals("value", replacedValue);
        assertEquals(2, size(map));
        assertTrue(containsKey(map, "key"));
        assertTrue(containsKey(map, "Key"));
        assertFalse(containsKey(map, "KEY"));
        assertFalse(containsValue(map, "value"));
        assertTrue(containsValue(map, "VALUE"));
    }

    @Test
    public void testToggleCaseInsensitiveNoDuplicateElement() {
        Object map = constructMap();
        setCaseInsensitive(map, false);
        put(map, "key", "value");
        put(map, "kee", "value");
        assertTrue(containsKey(map, "key"));
        assertFalse(containsKey(map, "KEY"));
        assertEquals(2, size(map));
        setCaseInsensitive(map, true);
        assertTrue(containsKey(map, "key"));
        assertTrue(containsKey(map, "KEY"));
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
        assertFalse(containsKey(map, null));
        assertNull(getCaseSensitiveKey(map, null));

        put(map, null, "value");
        assertTrue(containsKey(map, null));
        assertTrue(containsValue(map, "value"));
        assertEquals(1, size(map));
        assertEquals("value", get(map, null));
        assertNull(getCaseSensitiveKey(map, null));
        setCaseInsensitive(map, true);
        assertEquals(1, size(map));
        assertEquals("value", get(map, null));
        assertTrue(containsKey(map, null));
        assertTrue(containsValue(map, "value"));
        assertNull(getCaseSensitiveKey(map, null));

        assertEquals("value", put(map, null, "value2"));
        assertTrue(containsKey(map, null));
        assertFalse(containsValue(map, "value"));
        assertTrue(containsValue(map, "value2"));
        assertEquals(1, size(map));
        assertEquals("value2", get(map, null));

        assertEquals("value2", remove(map, null));
        assertEquals(0, size(map));
        assertNull(get(map, null));
        assertFalse(containsKey(map, null));
        assertFalse(containsKey(map, "value2"));
        assertNull(getCaseSensitiveKey(map, null));
    }

    @Test
    public void testClearMap() {
        Object map = constructMap();

        clear(map);
        setCaseInsensitive(map, false);
        put(map, "key", "value");
        clear(map);
        assertEquals(0, size(map));
        assertNull(get(map, "key"));
        assertFalse(containsKey(map, "key"));

        clear(map);
        setCaseInsensitive(map, true);
        put(map, "key", "value");
        clear(map);
        assertEquals(0, size(map));
        assertNull(get(map, "key"));
        assertFalse(containsKey(map, "key"));
    }

    @Test
    public void testNonExistentKey() {
        Object map = constructMap();

        setCaseInsensitive(map, false);
        assertNull(get(map, "key"));
        assertNull(put(map, "key2", "value"));
        assertNull(remove(map, "key"));

        clear(map);

        setCaseInsensitive(map, true);
        assertNull(get(map, "key"));
        assertNull(put(map, "key2", "value"));
        assertNull(remove(map, "key"));
    }

    @Test
    public void testInconvertibleClass() {
        assertTrue(isCaseConvertible(String.class));
        assertTrue(isCaseConvertible(Character.class));
        assertFalse(isCaseConvertible(Object.class));

        Object map = constructMap();
        Object dummy = new Object();
        setCaseInsensitive(map, false);
        assertNull(get(map, dummy));
        assertFalse(containsKey(map, dummy));

        setCaseInsensitive(map, true);
        assertNull(get(map, dummy));
        assertFalse(containsKey(map, dummy));

        try {
            getCaseSensitiveKey(map, dummy);
            fail("Expected exception");
        } catch (Exception ex) {
            assertEquals("Unsupported case-conversion for class class java.lang.Object", ex.getCause().getCause().getMessage());
        }
    }
}
