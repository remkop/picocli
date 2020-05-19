package picocli;

import org.junit.Test;
import java.util.*;

import static org.junit.Assert.*;
import static picocli.AbbreviationMatcher.*;

public class AbbreviationMatcherTest {
    private Set<String> createSet() {
        Set<String> result = new LinkedHashSet<String>();
        result.add("kebab-case-extra");
        result.add("kebab-case-extra-extra");
        result.add("kebab-case");
        result.add("kc"); // alias
        result.add("very-long-kebab-case");
        result.add("camelCase");
        result.add("veryLongCamelCase");
        return result;
    }

    @Test
    public void testPrefixMatch() {
        Set<String> set = createSet();

        assertEquals("kebab-case", match(set, "kebab-case", false));
        assertEquals("kebab-case-extra", match(set, "kebab-case-extra", false));
        assertEquals("very-long-kebab-case", match(set, "very-long-kebab-case", false));
        assertEquals("very-long-kebab-case", match(set, "v-l-k-c", false));
        assertEquals("very-long-kebab-case", match(set, "vLKC", false));
        assertEquals("camelCase", match(set, "camelCase", false));
        assertEquals("camelCase", match(set, "cC", false));
        assertEquals("camelCase", match(set, "c-c", false));
        assertEquals("camelCase", match(set, "camC", false));
        assertEquals("veryLongCamelCase", match(set, "veryLongCamelCase", false));
        assertEquals("veryLongCamelCase", match(set, "vLCC", false));
        assertEquals("veryLongCamelCase", match(set, "v-l-c-c", false));

        try {
            match(set, "vLC", false);
            fail("Expected exception");
        } catch (IllegalArgumentException ex) {
            assertEquals("vLC is not unique: it matches 'very-long-kebab-case', 'veryLongCamelCase'", ex.getMessage());
        }
        try {
            match(set, "k-c", false);
            fail("Expected exception");
        } catch (IllegalArgumentException ex) {
            assertEquals("k-c is not unique: it matches 'kebab-case-extra', 'kebab-case-extra-extra', 'kebab-case'", ex.getMessage());
        }
        try {
            match(set, "kC", false);
            fail("Expected exception");
        } catch (IllegalArgumentException ex) {
            assertEquals("kC is not unique: it matches 'kebab-case-extra', 'kebab-case-extra-extra', 'kebab-case'", ex.getMessage());
        }
        try {
            match(set, "keb-ca", false);
            fail("Expected exception");
        } catch (IllegalArgumentException ex) {
            assertEquals("keb-ca is not unique: it matches 'kebab-case-extra', 'kebab-case-extra-extra', 'kebab-case'", ex.getMessage());
        }
    }

    @Test
    public void testSplitIntoChunks() {
        assertEquals(Arrays.asList("k", "C"), splitIntoChunks("kC", false));
        assertEquals(Arrays.asList("k", "C"), splitIntoChunks("k-c", false));
        assertEquals(Arrays.asList("kebab", "Case"), splitIntoChunks("kebab-case", false));
        assertEquals(Arrays.asList("very", "Long", "Kebab", "Case"), splitIntoChunks("very-long-kebab-case", false));
        assertEquals(Arrays.asList("camel", "Case"), splitIntoChunks("camelCase", false));
        assertEquals(Arrays.asList("very", "Long", "Camel", "Case"), splitIntoChunks("veryLongCamelCase", false));
    }
}
