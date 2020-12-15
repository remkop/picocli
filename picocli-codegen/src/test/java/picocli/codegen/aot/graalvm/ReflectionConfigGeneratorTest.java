package picocli.codegen.aot.graalvm;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertEquals;

public class ReflectionConfigGeneratorTest {
    @Test
    public void testMainStdOut() throws IOException {
        PrintStream old = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        try {
            ReflectionConfigGenerator.main(Example.class.getName());
        } finally {
            System.setOut(old);
        }
        String expected = read("/example-reflect.json");
        expected = expected.replace("\r\n", "\n");
        expected = expected.replace("\n", System.getProperty("line.separator"));

        assertEquals(expected, baos.toString());
    }

    @Test
    public void testMainOutputFile() throws IOException {
        File file = File.createTempFile("picocli-codegen", ".json");

        ReflectionConfigGenerator.main("--output", file.getAbsolutePath(), Example.class.getName());

        String expected = read("/example-reflect.json");
        expected = expected.replace("\r\n", "\n");
        expected = expected.replace("\n", System.getProperty("line.separator"));

        String actual = readAndClose(new FileInputStream(file));
        file.delete();

        assertEquals(expected, actual);
    }

    @Test
    public void testMainLocalOutputFile() throws IOException {
        File file = File.createTempFile("picocli-codegen-example-interface-reflect", ".json");

        ReflectionConfigGenerator.main("--output", file.getAbsolutePath(), ExampleInterface.class.getName());

        String expected = read("/example-interface-reflect.json");
        expected = expected.replace("\r\n", "\n");
        expected = expected.replace("\n", System.getProperty("line.separator"));

        String actual = readAndClose(new FileInputStream(file));
        file.delete();

        assertEquals(expected, actual);
    }

    @Test
    public void testIssue930NonDefaultConstructor() {
        PrintStream old = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        try {
            ReflectionConfigGenerator.main("--factory", Issue930Factory.class.getName(), Issue930Example.class.getName());
        } finally {
            System.setOut(old);
        }
        String expected = String.format("" +
                "[%n" +
                "  {%n" +
                "    \"name\" : \"picocli.codegen.aot.graalvm.Issue930Example\",%n" +
                "    \"allDeclaredConstructors\" : true,%n" +
                "    \"allPublicConstructors\" : true,%n" +
                "    \"allDeclaredMethods\" : true,%n" +
                "    \"allPublicMethods\" : true%n" +
                "  }%n" +
                "]%n");

        assertEquals(expected, baos.toString());
    }

    @Test
    public void testIssue622FieldsFromAbstractSuperclass() throws IOException {
        File file = File.createTempFile("picocli-codegen", ".json");

        ReflectionConfigGenerator.main("--output", file.getAbsolutePath(), Issue622App.class.getName());

        String expected = read("/issue622-reflect.json");
        expected = expected.replace("\r\n", "\n");
        expected = expected.replace("\n", System.getProperty("line.separator"));

        String actual = readAndClose(new FileInputStream(file));
        file.delete();

        assertEquals(expected, actual);
    }

    @Test
    public void testIssue1274() throws IOException {
        File file = File.createTempFile("picocli-codegen", ".json");

        ReflectionConfigGenerator.main("--output", file.getAbsolutePath(), Issue1274Command.class.getName());

        String expected = read("/issue1274-reflect.json");
        expected = expected.replace("\r\n", "\n");
        expected = expected.replace("\n", System.getProperty("line.separator"));

        String actual = readAndClose(new FileInputStream(file));
        file.delete();

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
