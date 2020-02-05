package picocli.codegen.docgen.manpage;

import org.junit.Ignore;
import org.junit.Test;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static org.junit.Assert.*;

public class ManPageGeneratorTest {

    @Test
    public void main() {
    }

    @Test
    public void generateManPage() throws IOException {
        @Command(name = "myapp", mixinStandardHelpOptions = true,
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
            @Option(names = {"-o", "--output"}, description = "Output location full path.")
            File outputFolder;

            @Parameters(split = ",", description = "Some comma-separated values.")
            List<String> values;
        }

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw); //System.out, true
        ManPageGenerator.generateSingleManPage(pw, new CommandLine(new MyApp()).getCommandSpec());
        pw.flush();

        String expected = read("/myapp.manpage.adoc");
        expected = expected.replace("\r\n", "\n");
        expected = expected.replace("\n", System.getProperty("line.separator"));
        assertEquals(expected, sw.toString());
    }

    enum Format { CSV, TSV }

    @Test
    public void testImport() throws IOException {
        @Command(name = "import", version = {"import 2.3", "ignored line 1", "ignored line 2"},
                description = "Imports data from a file into the infra inventory db.",
                footerHeading = "%nExample:%n",
                footer = {
                        "# This imports all rows from the IP_Allocation_v1.20.csv file into the `${table.hosts}` table.",
                        "@|bold ${COMMAND-FULL-NAME} -v src/test/resources/IP_Allocation_v1.20.csv -Chostname=hostname " +
                                "-Chw_type=server_type -Cenv=class -Cdeviceid=${column.deviceid} -Cenv=env -Cteam=team -COS=os " +
                                "-Cremarks=description -Crack=rack -Clocation=datacenter -Cmgmt_ip=management_ip -Cfront_ip=front_ip " +
                                "-Cilo_ip=ilo_ip -Capp=application|@",
                        "",
                        "# This imports all rows from the network.csv file into the `network` table.",
                        "@|bold ${COMMAND-FULL-NAME} -v --table=network src/test/resources/network.csv -Cdesc=purpose -CDC=datacenter -Csubnet=subnet -Cgateway=gateway -Cvlanid=network|@"

                })
        class ImportCommand {

            @Option(names = {"-o", "--format"}, defaultValue = "CSV", order = 1,
                    description = "File format. Valid values: ${COMPLETION-CANDIDATES}. Default: ${DEFAULT-VALUE}")
            Format format;

            @Parameters(description = "The file to import.")
            File file;

            @Option(names = {"-e", "--encoding"}, defaultValue = "Shift_JIS", order = 2,
                    description = "Character encoding of the file to import. Default: ${DEFAULT-VALUE}")
            Charset charset;

            @Option(names = {"-C", "--column"}, order = 3, paramLabel = "<file-column>=<db-column>", required = true,
                    description = {"Key-value pair specifying the column mapping between the import file column name and the destination table column name."})
            Map<String, String> columnMapping;

            @Option(names = {"-W", "--column-value"}, order = 4, paramLabel = "<db-column>=<value>",
                    description = {"Key-value pair specifying the destination table column name and the value to set it to."})
            Map<String, String> columnValues = new LinkedHashMap<String, String>();

            @Option(names = {"--indexed"}, order = 5, description = "If true, use indexed access in the file, so specify the (1-based) file column index instead of the file column name.")
            boolean indexed;

            @Option(names = {"--no-header"}, negatable = true, defaultValue = "true",
                    description = "By default, or if `--header` is specified, the first line of the file is a list of the column names. " +
                            "If `--no-header` is specified, the first line of the file is data (and indexed access is used).")
            boolean header;

            @Option(names = {"-n", "--dry-run"},
                    description = "Don't actually add the row(s), just show if they exist and/or will be ignored..")
            boolean dryRun;

            @Option(names = {"-t", "--table"}, paramLabel = "<tableName>", order = 51,
                    description = {"Name of the table that the CRUD operations apply to. Default: ${table.hosts}."})
            public void setTableName(String tableName) {
            }

            @Option(names = {"-v", "--verbose"}, order = 50,
                    description = {
                            "Specify multiple -v options to increase verbosity.",
                            "For example, `-v -v -v` or `-vvv`"})
            public void setVerbosity(boolean[] verbosity) {
            }
        }
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw); //System.out, true
        ManPageGenerator.generateSingleManPage(pw, new CommandLine(new ImportCommand()).getCommandSpec());
        pw.flush();

        String expected = read("/import.manpage.txt");
        expected = expected.replace("\r\n", "\n");
        expected = expected.replace("\n", System.getProperty("line.separator"));
        assertEquals(expected, sw.toString());
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