package picocli.codegen.aot.graalvm;

import org.junit.Test;
import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import static org.junit.Assert.*;

public class DynamicProxyConfigGeneratorTest {

    @Test
    public void testMainStdOut() throws IOException {
        PrintStream old = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        try {
            DynamicProxyConfigGenerator.main(ExampleInterface.class.getName());
        } finally {
            System.setOut(old);
        }
        String expected = read("/example-interface-proxy.json");
        expected = expected.replace("\r\n", "\n");
        expected = expected.replace("\n", System.getProperty("line.separator"));

        assertEquals(expected, baos.toString());
    }

    @Test
    public void testMainOutputFile() throws IOException {
        File file = File.createTempFile("picocli-codegen", ".json");

        DynamicProxyConfigGenerator.main("--output", file.getAbsolutePath(), ExampleInterface.class.getName());

        String expected = read("/example-interface-proxy.json");
        expected = expected.replace("\r\n", "\n");
        expected = expected.replace("\n", System.getProperty("line.separator"));

        String actual = readAndClose(new FileInputStream(file));
        file.delete();

        assertEquals(expected, actual);
    }

    @Test
    public void testMultipleInterfaces() {
        String[] interfaceNames = {
                "com.example.Interface1,com.example.Interface2",
                "com.other.Interface1,com.other.Interface2",
        };
        String actual = DynamicProxyConfigGenerator.generateProxyConfig(new CommandSpec[0], interfaceNames);

        String expected = String.format("" +
                "[%n" +
                "  [\"com.example.Interface1\", \"com.example.Interface2\"],%n" +
                "  [\"com.other.Interface1\", \"com.other.Interface2\"]%n" +
                "]%n");
        assertEquals(expected, actual);
    }

    private String read(String resource) throws IOException {
        return readAndClose(getClass().getResourceAsStream(resource));
    }

    private String readAndClose(InputStream in) throws IOException {
        try {
            byte[] buff = new byte[15000];
            int size = in.read(buff);
            return new String(buff, 0, size);
        } finally {
            in.close();
        }
    }
}