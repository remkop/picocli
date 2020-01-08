package picocli.codegen.aot.graalvm;

import org.junit.Test;
import picocli.CommandLine.Model.CommandSpec;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import static org.junit.Assert.*;

public class ResourceConfigGeneratorTest {

    @Test
    public void testMainStdOut() throws IOException {
        PrintStream old = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        try {
            ResourceConfigGenerator.main("--bundle=some.extra.bundle", "--pattern=^ExtraPattern$", Example.class.getName());
        } finally {
            System.setOut(old);
        }
        String expected = read("/example-resource.json");
        expected = expected.replace("\r\n", "\n");
        expected = expected.replace("\n", System.getProperty("line.separator"));

        assertEquals(expected, baos.toString());
    }

    @Test
    public void testMainOutputFile() throws IOException {
        File file = File.createTempFile("picocli-codegen", ".json");

        ResourceConfigGenerator.main("--output", file.getAbsolutePath(),
                "--bundle=some.extra.bundle", "--pattern=^ExtraPattern$", Example.class.getName());

        String expected = read("/example-resource.json");
        expected = expected.replace("\r\n", "\n");
        expected = expected.replace("\n", System.getProperty("line.separator"));

        String actual = readAndClose(new FileInputStream(file));
        file.delete();

        assertEquals(expected, actual);
    }

    @Test
    public void testMultipleBundlesAndResources() {
        String[] bundles = {
                "com.example.extra.bundle1",
                "com.example.extra.bundle2",
        };
        String[] resources = {
                "ABCD",
                ".*\\.json",
        };
        String actual = ResourceConfigGenerator.generateResourceConfig(new CommandSpec[0], bundles, resources);

        String expected = String.format("" +
                "{%n" +
                "  \"bundles\" : [%n" +
                "    {\"name\" : \"com.example.extra.bundle1\"},%n" +
                "    {\"name\" : \"com.example.extra.bundle2\"}%n" +
                "  ],%n" +
                "  \"resources\" : [%n" +
                "    {\"pattern\" : \"ABCD\"},%n" +
                "    {\"pattern\" : \".*\\.json\"}%n" +
                "  ]%n" +
                "}%n");
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