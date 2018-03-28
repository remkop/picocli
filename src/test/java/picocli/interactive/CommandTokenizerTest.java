package picocli.interactive;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

/**
 * Tests CommandTokenizer to make sure it works. 
 */
public class CommandTokenizerTest {
	
	static final CommandTokenizer tokenizer = new CommandTokenizer();
	
	/**
	 * Just a basic test. 
	 */
	@Test
	public static void testBasic() {
		String[] expected = new String[]{"a", "b", "c"};
		String[] actual = tokenizer.parse("a b c");
		assertArrayEquals(expected, actual);
	}
	
	/**
	 * Tests that escaping stuff works. 
	 */
	@Test
	public static void testEscapes() {
		String[] expected = new String[]{"a", "b c"};
		String[] actual = tokenizer.parse("a b\\ c");
		assertArrayEquals(expected, actual);
	}
	
	/**
	 * Tests if quoting works. 
	 */
	@Test
	public static void testQuotes() {
		String[] expected = new String[]{"a", "\"b 'c", "d e"};
		String[] actual = tokenizer.parse("a \"\\\"b 'c\" 'd e'");
		assertArrayEquals(expected, actual);
	}
	
	/**
	 * Tests if commenting works. 
	 */
	@Test
	public static void testComments() {
		String[] expected = new String[]{"a"};
		String[] actual = tokenizer.parse("a #b c");
		assertArrayEquals(expected, actual);
	}
	
}
