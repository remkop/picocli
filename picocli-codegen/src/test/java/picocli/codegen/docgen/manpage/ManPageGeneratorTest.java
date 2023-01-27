package picocli.codegen.docgen.manpage;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.codegen.util.Assert;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static org.junit.Assert.*;

public class ManPageGeneratorTest {
    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");

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
        ManPageGenerator.writeSingleManPage(pw, new CommandLine(new MyApp()).getCommandSpec());
        pw.flush();

        String expected = read("/myapp.manpage.adoc");
        expected = expected.replace("\r\n", "\n");
        expected = expected.replace("\n", System.getProperty("line.separator"));
        assertEquals(expected, sw.toString());
    }

    static class CsvOptions {
        @Option(names = {"-e", "--encoding"}, defaultValue = "Shift_JIS", order = 2,
                description = "(CSV/TSV-only) Character encoding of the file to import. Default: ${DEFAULT-VALUE}")
        Charset charset;

        @Option(names = {"-C", "--column"}, order = 3, paramLabel = "<file-column>=<db-column>", required = true,
                description = {"(CSV/TSV-only) Key-value pair specifying the column mapping between the import file column name and the destination table column name."})
        Map<String, String> columnMapping;

        @Option(names = {"-W", "--column-value"}, order = 4, paramLabel = "<db-column>=<value>",
                description = {"(CSV/TSV-only) Key-value pair specifying the destination table column name and the value to set it to."})
        Map<String, String> columnValues = new LinkedHashMap<String, String>();

        @Option(names = {"--indexed"}, order = 5, description = "(CSV/TSV-only) If true, use indexed access in the file, so specify the (1-based) file column index instead of the file column name.")
        boolean indexed;

        @Option(names = {"--no-header"}, negatable = true, defaultValue = "true",
                description = "(CSV/TSV-only) By default, or if `--header` is specified, the first line of the file is a list of the column names. " +
                        "If `--no-header` is specified, the first line of the file is data (and indexed access is used).")
        boolean header;

        @Parameters(description = "Extra CSV file.")
        File extraFile;

    }
    enum Format { CSV, TSV }

    @Test
    public void testImport() throws IOException {

        @Command(name = "import", version = {"import 2.3", "ignored line 1", "ignored line 2"},
                description = "Imports data from a file into the infra inventory db.",
                optionListHeading = "%nOptions%n", parameterListHeading = "Positional Arguments%n",
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

            @ArgGroup(validate = false, heading = "%nCSV/TSV-only Options%n")
            CsvOptions csvOptions;

            @Option(names = {"--dry-run"},
                    description = "Don't actually add the row(s), just show if they exist and/or will be ignored..")
            boolean dryRun;

            @Option(names = "-n", description = {"Number-of-iterations limit as:  `-n number`",
                    "Specifies the maximum number of iterations, or frames, top " +
                            "should produce before ending."})
            int number;

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
        ManPageGenerator.writeSingleManPage(pw, new CommandLine(new ImportCommand()).getCommandSpec());
        pw.flush();

        String expected = read("/import.manpage.txt.adoc");
        expected = expected.replace("\r\n", "\n");
        expected = expected.replace("\n", System.getProperty("line.separator"));
        assertEquals(expected, sw.toString());
    }

    @Test
    public void testHidden() throws IOException {

        @Command(name = "a-sub", mixinStandardHelpOptions = true, description = "A sub command")
        class ASubCommand {
            @Option(names = "input-a")
            String inputA;
        }

        @Command(name = "hidden-sub", mixinStandardHelpOptions = true, hidden = true)
        class HiddenSubCommand {
            @Option(names = "input-b")
            String inputB;
        }

        @Command(name = "testHidden", mixinStandardHelpOptions = true,
                version = {
                        "Versioned Command 1.0",
                        "Picocli " + picocli.CommandLine.VERSION,
                        "JVM: ${java.version} (${java.vendor} ${java.vm.name} ${java.vm.version})",
                        "OS: ${os.name} ${os.version} ${os.arch}"},
                description = "This app does great things.",
                subcommands = { ASubCommand.class, HiddenSubCommand.class }

        )
        class MyApp {
            @Option(names = {"-o", "--output"}, description = "Output location full path.")
            File outputFolder;

            @Option(names = {"--hidden-test"}, hidden = true)
            File hidden;

            @Parameters(hidden = true)
            List<String> values;
        }

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw); //System.out, true
        ManPageGenerator.writeSingleManPage(pw, new CommandLine(new MyApp()).getCommandSpec());
        pw.flush();

        String expected = read("/testHidden.manpage.adoc");
        expected = expected.replace("\r\n", "\n");
        expected = expected.replace("\n", System.getProperty("line.separator"));
        assertEquals(expected, sw.toString());
    }

    @Test
    public void testHiddenOptions() throws IOException {

        @Command(name = "testHiddenOptions",
                version = {
                        "Versioned Command 1.0",
                        "Picocli " + picocli.CommandLine.VERSION,
                        "JVM: ${java.version} (${java.vendor} ${java.vm.name} ${java.vm.version})",
                        "OS: ${os.name} ${os.version} ${os.arch}"},
                description = "This app does great things."
        )
        class MyApp {
            @Option(names = {"-o", "--output"}, hidden = true, description = "Output location full path.")
            File outputFolder;

            @Option(names = {"--hidden-test"}, hidden = true)
            File hidden;

            @Parameters(hidden = true)
            List<String> values;
        }

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw); //System.out, true
        ManPageGenerator.writeSingleManPage(pw, new CommandLine(new MyApp()).getCommandSpec());
        pw.flush();

        String expected = read("/testHiddenOptions.manpage.adoc");
        expected = expected.replace("\r\n", "\n");
        expected = expected.replace("\n", System.getProperty("line.separator"));
        assertEquals(expected, sw.toString());
    }

//    @Ignore // test case for https://github.com/remkop/picocli/issues/1077
    @Test
    public void testEndOfOptions() throws IOException {

        @Command(name = "testEndOfOptions", mixinStandardHelpOptions = true,
                showEndOfOptionsDelimiterInUsageHelp = true,
                version = { "Versioned Command 1.0"},
                description = "This app does great things."
        )
        class MyApp {
            @Option(names = {"-o", "--output"}, description = "Output location full path.")
            File outputFolder;

            @Parameters(description = "Some values")
            List<String> values;
        }

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw); //System.out, true
        ManPageGenerator.writeSingleManPage(pw, new CommandLine(new MyApp()).getCommandSpec());
        pw.flush();

        String expected = read("/testEndOfOptions.manpage.adoc");
        expected = expected.replace("\r\n", "\n");
        expected = expected.replace("\n", System.getProperty("line.separator"));
        assertEquals(expected, sw.toString());
    }

    private String read(String resource) throws IOException {
        URL url = ManPageGenerator.class.getResource(resource);
        Assert.notNull(url, "resource '" + resource + "'");
        return readAndClose(url.openStream());
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

    @Test
    public void testEndOfOptionsWithoutOptions() throws IOException {

        @Command(name = "testEndOfOptionsWithoutOptions", showEndOfOptionsDelimiterInUsageHelp = true,
                version = { "Versioned Command 1.0"},
                description = "This app does great things.")
        class MyApp {
        }

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw); //System.out, true
        ManPageGenerator.writeSingleManPage(pw, new CommandLine(new MyApp()).getCommandSpec());
        pw.flush();


        String expected = read("/testEndOfOptionsWithoutOptions.manpage.adoc");
        expected = expected.replace("\r\n", "\n");
        expected = expected.replace("\n", System.getProperty("line.separator"));
        assertEquals(expected, sw.toString());
    }

    @Command(name = "top-level-command", description = "example top-level command",
            mixinStandardHelpOptions = true)
    static class Top {
        @Option(names = {"-x", "--long-option"}, description = "Some example option")
        int x;

        @Command(description = "Example hidden subcommand", hidden = true, mixinStandardHelpOptions = true)
        void hidden() {
        }

        @Command(description = "Example visible subcommand", mixinStandardHelpOptions = true)
        void visible() {
        }

        @Command(description = "Example subcommand",
                mixinStandardHelpOptions = true,
                subcommands = ManPageGenerator.class)
        void subcommand() {
        }
    }

    @Test
    public void testManPageGeneratorAsSubcommandHelp() {
        StringWriter sw = new StringWriter();
        new CommandLine(new Top()).setOut(new PrintWriter(sw, true))
                .execute("subcommand", "gen-manpage", "--help");
        String expected = String.format("" +
                "Usage: top-level-command subcommand gen-manpage [-fhVv] [-d=<outdir>] [-t=<template-dir>]%n" +
                "                                                [@<filename>...]%n" +
                "Generates man pages for all commands in the specified directory.%n" +
                "      [@<filename>...]    One or more argument files containing options.%n" +
                "  -d, --outdir=<outdir>   Output directory to write the generated AsciiDoc files to. If not%n" +
                "                            specified, files are written to the current directory.%n" +
                "  -t, --template-dir=<template-dir>%n" +
                "                          Optional directory to write customizable man page template files. If%n" +
                "                            specified, an additional \"template\" file is created here for each%n" +
                "                            generated manpage AsciiDoc file.%n" +
                "                          Each template file contains `include` directives that import content from%n" +
                "                            the corresponding generated manpage AsciiDoc file in the `--outdir`%n" +
                "                            directory. Text can be added after each include to customize the%n" +
                "                            resulting man page. The resulting man page will be a mixture of%n" +
                "                            generated and manually edited text.%n" +
                "                          These customizable templates are intended to be generated once, and%n" +
                "                            afterwards be manually updated and maintained.%n" +
                "  -v, --verbose           Specify multiple -v options to increase verbosity.%n" +
                "                          For example, `-v -v -v` or `-vvv`%n" +
                "  -f, --[no-]force        Overwrite existing man page templates. The default is `--no-force`,%n" +
                "                            meaning processing is aborted and the process exits with status code 4%n" +
                "                            if a man page template file already exists.%n" +
                "  -h, --help              Show this help message and exit.%n" +
                "  -V, --version           Print version information and exit.%n" +
                "%n" +
                "Converting to Man Page Format%n" +
                "%n" +
                "Use the `asciidoctor` tool to convert the generated AsciiDoc files to man pages in roff format:%n" +
                "%n" +
                "`asciidoctor --backend=manpage --source-dir=SOURCE_DIR --destination-dir=DESTINATION *.adoc`%n" +
                "%n" +
                "Point the SOURCE_DIR to either the `--outdir` directory or the `--template-dir` directory. Use some%n" +
                "other directory as the DESTINATION.%n" +
                "See https://asciidoctor.org/docs/user-manual/#man-pages%n" +
                "See http://man7.org/linux/man-pages/man7/roff.7.html%n");
        assertEquals(expected, sw.toString());
    }

    @Test
    public void testManPageGeneratorAsSubcommandParentHelp() {
        StringWriter sw = new StringWriter();
        new CommandLine(new Top()).setOut(new PrintWriter(sw, true))
                .execute("subcommand", "--help");
        String expected = String.format("" +
                "Usage: top-level-command subcommand [-hV] [COMMAND]%n" +
                "Example subcommand%n" +
                "  -h, --help      Show this help message and exit.%n" +
                "  -V, --version   Print version information and exit.%n" +
                "Commands:%n" +
                "  gen-manpage  Generates man pages for all commands in the specified directory.%n");
        assertEquals(expected, sw.toString());
    }

    @Test
    public void testManPageGeneratorAsSubcommand() throws IOException {
        File outdir = new File(System.getProperty("java.io.tmpdir"), "manpage" + System.currentTimeMillis());
        outdir.mkdir();
        File templateDir = new File(outdir, "templates");
        int exitCode = new CommandLine(new Top())
                .execute("subcommand", "gen-manpage", /*"-vv",*/ "--outdir=" + outdir, "--template-dir=" + templateDir);
        try {
            assertEquals(0, exitCode);

            //System.out.println(Arrays.asList(templateDir.listFiles()));
            //System.out.println(Arrays.asList(outdir.listFiles()));

            String[] files = new String[] {
                    "top-level-command.adoc", //
                    "top-level-command-visible.adoc", //
                    "top-level-command-subcommand.adoc", //
                    "top-level-command-subcommand-gen-manpage.adoc"
            };

            for (String f : files) {
                String expected = read("/manpagegenerator/templates/" + f);
                String actual = readAndClose(new FileInputStream(new File(templateDir, f)));

                expected = expected.replace("$OUTDIR", outdir.getAbsolutePath().replace('\\', '/')).replace("\r\n", "\n").replace("\n", System.getProperty("line.separator"));
                assertEquals("/manpagegenerator/templates/" + f, expected, actual);
            }

            for (String f : files) {
                String expected = read("/manpagegenerator/" + f);
                String actual = readAndClose(new FileInputStream(new File(outdir, f)));

                expected = expected.replace("$VERSION", CommandLine.VERSION).replace("\r\n", "\n").replace("\n", System.getProperty("line.separator"));
                assertEquals("/manpagegenerator/" + f, expected, actual);
            }

        } finally {
            for (File f : templateDir.listFiles()) {
                f.delete();
            }
            try {
                assertTrue(templateDir.getAbsolutePath(), templateDir.delete());
            } finally {
                for (File f : outdir.listFiles()) {
                    f.delete();
                }
                assertTrue(outdir.getAbsolutePath(), outdir.delete());
            }
        }
    }

    @Command(subcommands = { ManPageGenerator.class})
    static class Nameless implements Callable<Integer> {

         @Parameters(index = "0")
         private File file;

        public static void main(String... args) throws Exception {
            CommandLine cmd = new CommandLine(new Nameless());
            System.exit(cmd.execute(args));
        }

        @Override
        public Integer call() throws Exception {
            return 0;
        }
    }

    @Test
    public void testNamelessCommand() throws IOException {
        File outdir = new File(System.getProperty("java.io.tmpdir"), "manpage" + System.currentTimeMillis());
        outdir.mkdir();
        File templateDir = new File(outdir, "templates");
        int exitCode = new CommandLine(new Nameless())
                .execute("gen-manpage", /*"-vv",*/ "--outdir=" + outdir, "--template-dir=" + templateDir);
        try {
            assertEquals(0, exitCode);

            //System.out.println(Arrays.asList(templateDir.listFiles()));
            //System.out.println(Arrays.asList(outdir.listFiles()));

            String[] files = new String[] {
                    "main_class.adoc", //
                    "main_class-gen-manpage.adoc"
            };

            for (String f : files) {
                String expected = read("/manpagegenerator/templates/" + f);
                String actual = readAndClose(new FileInputStream(new File(templateDir, f)));

                expected = expected.replace("$OUTDIR", outdir.getAbsolutePath().replace('\\', '/')).replace("\r\n", "\n").replace("\n", System.getProperty("line.separator"));
                assertEquals("/manpagegenerator/templates/" + f, expected, actual);
            }

            for (String f : files) {
                String expected = read("/manpagegenerator/" + f);
                String actual = readAndClose(new FileInputStream(new File(outdir, f)));

                expected = expected.replace("$VERSION", CommandLine.VERSION).replace("\r\n", "\n").replace("\n", System.getProperty("line.separator"));
                assertEquals("/manpagegenerator/" + f, expected, actual);
            }

        } finally {
            for (File f : templateDir.listFiles()) {
                f.delete();
            }
            try {
                assertTrue(templateDir.getAbsolutePath(), templateDir.delete());
            } finally {
                for (File f : outdir.listFiles()) {
                    f.delete();
                }
                assertTrue(outdir.getAbsolutePath(), outdir.delete());
            }
        }
    }

}
