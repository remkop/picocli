package picocli.examples.stdin;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.contrib.java.lang.system.TextFromStandardInputStream;

import java.net.URL;

import static org.junit.Assert.*;

public class PrintFirstLineTest {

    @Rule
    public final SystemOutRule outRule = new SystemOutRule().enableLog().muteForSuccessfulTests();

    @Rule
    public final TextFromStandardInputStream systemInMock = TextFromStandardInputStream.emptyStandardInputStream();

    @Test
    public void testMain() {
        URL resource = PrintFirstLineTest.class.getResource("/PrintFirstLineTest.txt");
        String path = resource.getPath();
        PrintFirstLine.main(path);
        assertEquals("file line 1", outRule.getLog().trim());
    }

    @Test
    public void testStandardInput() {
        systemInMock.provideLines("stdin line1", "stdin line2", "stdin line3");
        PrintFirstLine.main("-");
        assertEquals("stdin line1", outRule.getLog().trim());
    }
}
