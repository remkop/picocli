package picocli.codegen.aot.graalvm;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertEquals;

public class ReflectionConfigGeneratorTest {
    @Test
    public void testMain() throws IOException {
        PrintStream old = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        try {
            ReflectionConfigGenerator.main(Example.class.getName());
        } finally {
            System.setOut(old);
        }
        String expected = read("/example-reflect.json");
        expected.replace("\r\n", "\n");
        expected.replace("\n", System.getProperty("line.separator"));

        assertEquals(expected, baos.toString());
    }

    private String read(String resource) throws IOException {
        InputStream in = null;
        try {
            in = getClass().getResourceAsStream(resource);
            byte[] buff = new byte[15000];
            int size = in.read(buff);
            return new String(buff, 0, size);
        } finally {
            in.close();
        }
    }
}
