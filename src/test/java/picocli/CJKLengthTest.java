
package picocli;

import static org.junit.Assert.assertEquals;
import static picocli.CommandLine.Help.Ansi;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;

public class CJKLengthTest {

    // allows tests to set any kind of properties they like, without having to individually roll them back
    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");

    @Test
    public void testCJKLengths() {
      testLength("abc", 3);
      // some double width Hiragana characters
      testLength("平仮名", 6);

      // a supplementary code point character (has a high and low code point values)
      testLength("𝑓", 1);
    }

    private void testLength(String of, int expectedLength) {
      Ansi.Text text = Ansi.OFF.text(of);
      int cjkWidth = text.getCJKAdjustedLength();
      assertEquals(String.format("Expected '%s' to have width %d but is %d", of, expectedLength, cjkWidth),
          expectedLength, cjkWidth);
    }

}
