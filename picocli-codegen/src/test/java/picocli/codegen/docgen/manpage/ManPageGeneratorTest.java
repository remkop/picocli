package picocli.codegen.docgen.manpage;

import org.junit.Test;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import static org.junit.Assert.*;

public class ManPageGeneratorTest {

    @Test
    public void main() {
    }

    @Test
    public void generateManPage() throws IOException {
        @CommandLine.Command(name = "myapp", mixinStandardHelpOptions = true,
                version = {
                        "Versioned Command 1.0",
                        "Picocli " + picocli.CommandLine.VERSION,
                        "JVM: ${java.version} (${java.vendor} ${java.vm.name} ${java.vm.version})",
                        "OS: ${os.name} ${os.version} ${os.arch}"},
                description = "This app does great things.",
                exitCodeList = {
                        " 0:Successful program execution.",
                        "64:Invalid input: an unknown option or invalid parameter was specified.",
                        "70:Execution exception: an exception occurred while executing the business logic."},
                footerHeading = "Examples",
                footer = {
                        "This is the first line.",
                        "This is the second line.",
                        "This is a very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very veryvery very very very very very very very very very very very very very very very long line."
                }
        )
        class MyApp {
            @CommandLine.Option(names = {"-o", "--output"}, description = "Output location full path.")
            File outputFolder;

            @CommandLine.Parameters(split = ",", description = "Some comma-separated values.")
            List<String> values;
        }

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw); //System.out, true
        ManPageGenerator.generateSingleManPage(pw, new CommandLine(new MyApp()).getCommandSpec());
        pw.flush();

        assertEquals(read("/myapp.manpage.adoc"), sw.toString());
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