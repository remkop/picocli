package picocli.codegen.docgen.manpage;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Ansi.IStyle;
import picocli.CommandLine.Help.Ansi.Style;
import picocli.CommandLine.Help.Ansi.Text;
import picocli.CommandLine.Help.ColorScheme;
import picocli.CommandLine.Help.IOptionRenderer;
import picocli.CommandLine.Help.IParamLabelRenderer;
import picocli.CommandLine.Help.IParameterRenderer;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Model.ArgGroupSpec;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.IOrdered;
import picocli.CommandLine.Model.OptionSpec;
import picocli.CommandLine.Model.PositionalParamSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;
import picocli.codegen.util.Assert;
import picocli.codegen.util.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.Callable;

import static java.lang.String.format;

/**
 * Generates AsciiDoc files in a special format that can be converted to HTML, PDF and Unix Man pages.
 * <p>
 *  This class can be used as a subcommand, in which case it generates man pages for all
 *  non-hidden commands in the hierarchy from the top-level command down,
 *  or it can be executed as a stand-alone tool, in which case the user needs to specify
 *  the {@code @Command}-annotated classes to generate man pages for.
 * </p>
 */
@Command(name = "gen-manpage",
        version = "${COMMAND-FULL-NAME} " + CommandLine.VERSION,
        helpCommand = true, // don't validate required options and positional parameters of the parent command
        showAtFileInUsageHelp = true,
        mixinStandardHelpOptions = true,
        sortOptions = false,
        usageHelpAutoWidth = true,
        usageHelpWidth = 100,
        description = {"Generates man pages for all commands in the specified directory."},
        //exitCodeListHeading = "%nExit Codes (if enabled with `--exit`)%n",
        //exitCodeList = {
        //        "0:Successful program execution.",
        //        "1:A runtime exception occurred while generating man pages.",
        //        "2:Usage error: user input for the command was incorrect, " +
        //                "e.g., the wrong number of arguments, a bad flag, " +
        //                "a bad syntax in a parameter, etc.",
        //        "4:A template file exists in the template directory. (Remove the `--template-dir` option or use `--force` to overwrite.)"
        //},
        footerHeading = "%nConverting to Man Page Format%n%n",
        footer = {"Use the `asciidoctor` tool to convert the generated AsciiDoc files to man pages in roff format:",
                "",
                "`asciidoctor --backend=manpage --source-dir=SOURCE_DIR --destination-dir=DESTINATION *.adoc`",
                "",
                "Point the SOURCE_DIR to either the `--outdir` directory or the `--template-dir` directory. Use some other directory as the DESTINATION.",
                "See https://asciidoctor.org/docs/user-manual/#man-pages",
                "See http://man7.org/linux/man-pages/man7/roff.7.html",
        }
)
public class ManPageGenerator implements Callable<Integer> {
    static final int EXIT_CODE_TEMPLATE_EXISTS = 4;

    static final IStyle BOLD = new IStyle() {
        public String on()  { return "*"; }
        public String off() { return "*"; }
    };
    static final IStyle ITALIC = new IStyle() {
        public String on()  { return "_"; }
        public String off() { return "_"; }
    };
    static final IStyle HIGHLIGHT = new IStyle() {
        public String on() { return "#"; }
        public String off() { return "#"; }
    };
    static final ColorScheme COLOR_SCHEME = new ColorScheme.Builder(CommandLine.Help.Ansi.ON).
            commands(BOLD).options(BOLD).optionParams(ITALIC).parameters(ITALIC).customMarkupMap(createMarkupMap()).build();

    @Mixin Config config;
    @Spec CommandSpec spec;

    /**
     * Invokes {@link #generateManPage(Config, CommandLine.Model.CommandSpec...)} to generate man pages for
     * all non-hidden commands in the hierarchy from the top-level command down.
     * This method is only called when this class is used as a subcommand.
     * @return an exit code indicating success or failure, as follows:
     *         <ul>
     *         <li>0: Successful program execution.</li>
     *         <li>1: A runtime exception occurred while generating man pages.</li>
     *         <li>2: Usage error: user input for the command was incorrect,
     *                e.g., the wrong number of arguments, a bad flag,
     *                a bad syntax in a parameter, etc.</li>
     *         <li>4: A template file exists in the template directory. (Remove the `--template-dir` option or use `--force` to overwrite.)</li>
     *         </ul>
     * @throws IOException if a problem occurred writing files.
     */
    public Integer call() throws IOException {
        return generateManPage(config, spec.root());
    }

    private static Map<String, IStyle> createMarkupMap() {
        Map<String, IStyle> result = new HashMap<String, IStyle>();
        result.put(Style.bold.name(), BOLD);
        result.put(Style.italic.name(), ITALIC);
        result.put(Style.underline.name(), ITALIC);
        result.put(Style.reverse.name(), HIGHLIGHT);
        return result;
    }

    static class Config {
        @Option(names = {"-d", "--outdir"}, defaultValue = ".", paramLabel = "<outdir>",
                description = {"Output directory to write the generated AsciiDoc files to. " +
                        "If not specified, files are written to the current directory."})
        File directory;

        @Option(names = {"-t", "--template-dir"}, paramLabel = "<template-dir>",
                description = {
                        "Optional directory to write customizable man page template files. " +
                                "If specified, an additional \"template\" file is created here for each " +
                                "generated manpage AsciiDoc file. ",
                        "Each template file contains `include` directives that import content " +
                                "from the corresponding generated manpage AsciiDoc file in the `--outdir` directory. " +
                                "Text can be added after each include to customize the resulting man page. " +
                                "The resulting man page will be a mixture of generated and manually edited text.",
                        "These customizable templates are intended to be generated once, and afterwards " +
                                "be manually updated and maintained."})
        File templatesDirectory;

        @Option(names = {"-v", "--verbose"},
                description = {
                        "Specify multiple -v options to increase verbosity.",
                        "For example, `-v -v -v` or `-vvv`"})
        boolean[] verbosity = new boolean[0];

        @Option(names = {"-f", "--force"}, negatable = true,
                description = { "Overwrite existing man page templates. " +
                        "The default is `--no-force`, meaning processing is aborted and the process exits " +
                        "with status code 4 if a man page template file already exists."})
        boolean force;

        private void verbose(String message, Object... params) {
            if (verbosity.length > 0) {
                System.err.printf(message, params);
            }
        }

        private void verboseDetailed(String message, Object... params) {
            if (verbosity.length > 1) {
                System.err.printf(message, params);
            }
        }
    }

    @Command(name = "gen-manpage",
            version = "picocli-codegen ${COMMAND-NAME} " + CommandLine.VERSION, showAtFileInUsageHelp = true,
            mixinStandardHelpOptions = true, sortOptions = false, usageHelpAutoWidth = true, usageHelpWidth = 100,
            description = {"Generates one or more AsciiDoc files with doctype 'manpage' in the specified directory."},
            exitCodeListHeading = "%nExit Codes (if enabled with `--exit`)%n",
            exitCodeList = {
                    "0:Successful program execution.",
                    "1:A runtime exception occurred while generating man pages.",
                    "2:Usage error: user input for the command was incorrect, " +
                            "e.g., the wrong number of arguments, a bad flag, " +
                            "a bad syntax in a parameter, etc.",
                    "4:A template file exists in the template directory. (Remove the `--template-dir` option or use `--force` to overwrite.)"
            },
            footerHeading = "%nConverting to Man Page Format%n%n",
            footer = {"Use the `asciidoctor` tool to convert the generated AsciiDoc files to man pages in roff format:",
                    "",
                    "`asciidoctor --backend=manpage --source-dir=SOURCE_DIR --destination-dir=DESTINATION *.adoc`",
                    "",
                    "Point the SOURCE_DIR to either the `--outdir` directory or the `--template-dir` directory. Use some other directory as the DESTINATION.",
                    "See https://asciidoctor.org/docs/user-manual/#man-pages",
                    "See http://man7.org/linux/man-pages/man7/roff.7.html",
                    "",
                    "In order to generate localized man pages, set the target locale by specifying the user.language, user.country, and user.variant system properties.",
                    "The generated usage help will then contain information retrieved from the resource bundle based on the user locale.",
                    "",
                    "Example",
                    "-------",
                    "  java -Duser.language=de -cp \"myapp.jar;picocli-4.7.6-SNAPSHOT.jar;picocli-codegen-4.7.6-SNAPSHOT.jar\" " +
                            "picocli.codegen.docgen.manpage.ManPageGenerator my.pkg.MyClass"
            }
    )
    private static class App implements Callable<Integer> {

        @Parameters(arity = "1..*", description = "One or more command classes to generate man pages for.")
        Class<?>[] classes = new Class<?>[0];

        @Mixin Config config;

        @Option(names = {"-c", "--factory"}, description = "Optionally specify the fully qualified class name of the custom factory to use to instantiate the command class. " +
                "If omitted, the default picocli factory is used.")
        String factoryClass;

        @Option(names = "--exit", negatable = true,
                description = "Specify `--exit` if you want the application to call `System.exit` when finished. " +
                "By default, `System.exit` is not called.")
        boolean exit;

        public Integer call() throws Exception {
            List<CommandSpec> specs = Util.getCommandSpecs(factoryClass, classes);
            return generateManPage(config, specs.toArray(new CommandSpec[0]));
        }
    }

    /**
     * Invokes {@link #generateManPage(Config, CommandLine.Model.CommandSpec...)} to generate man pages for
     * the user-specified {@code @Command}-annotated classes.
     * <p>
     *     If the {@code --exit} option is specified, {@code System.exit} is invoked
     *     afterwards with an exit code as follows:
     * </p>
     * <ul>
     * <li>0: Successful program execution.</li>
     * <li>1: A runtime exception occurred while generating man pages.</li>
     * <li>2: Usage error: user input for the command was incorrect,
     *        e.g., the wrong number of arguments, a bad flag,
     *        a bad syntax in a parameter, etc.</li>
     * <li>4: A template file exists in the template directory. (Remove the `--template-dir` option or use `--force` to overwrite.)</li>
     * </ul>
     * @param args command line arguments to be parsed. Must include the classes to
     *             generate man pages for.
     */
    public static void main(String[] args) {
        App app = new App();
        int exitCode = new CommandLine(app).execute(args);
        if (app.exit) {
            System.exit(exitCode);
        }
    }

    /**
     * Generates AsciiDoc files for the specified classes to the specified output directory,
     * optionally also generating template files in the {@code customizablePagesDirectory} directory.
     * @param outdir Output directory to write the generated AsciiDoc files to.
     * @param customizablePagesDirectory Optional directory to write customizable man page template files.
     *                                 If non-{@code null}, an additional "template" file is created here for each
     *                                 generated manpage AsciiDoc file.
     * @param verbosity the length of this array determines verbosity during processing
     * @param overwriteCustomizablePages Overwrite existing man page templates.
     *                         The default is false, meaning processing is aborted and the process exits
     *                         with status code 4 if a man page template file already exists.
     * @param specs the Commands to generate AsciiDoc man pages for
     * @return the exit code
     * @throws IOException if a problem occurred writing to the file system
     */
    public static int generateManPage(File outdir,
                                      File customizablePagesDirectory,
                                      boolean[] verbosity,
                                      boolean overwriteCustomizablePages,
                                      CommandSpec... specs) throws IOException {
        Config config = new Config();
        config.directory = outdir;
        config.templatesDirectory = customizablePagesDirectory;
        config.verbosity = verbosity;
        config.force = overwriteCustomizablePages;

        return generateManPage(config, specs);
    }

    static int generateManPage(Config config, CommandSpec... specs) throws IOException {
        Assert.notNull(config, "config");
        Assert.notNull(config.directory, "output directory");
        Assert.notNull(config.verbosity, "verbosity array");

        if (config.templatesDirectory != null && config.templatesDirectory.equals(config.directory)) {
            System.err.println("gen-manpage: Error: output directory must differ from the templates directory.");
            System.err.println("Try 'gen-manpage --help' for more information.");
            return CommandLine.ExitCode.USAGE;
        }

        traceAllSpecs(specs, config);

        for (CommandSpec spec : specs) {
            int result = generateSingleManPage(config, spec);
            if (result != CommandLine.ExitCode.OK) {
                return result;
            }

            Set<CommandSpec> done = new HashSet<CommandSpec>();

            // recursively create man pages for subcommands
            for (CommandLine sub : spec.subcommands().values()) {
                CommandSpec subSpec = sub.getCommandSpec();
                if (done.contains(subSpec) || subSpec.usageMessage().hidden()) {continue;}
                done.add(subSpec);
                result = generateManPage(config, subSpec);
                if (result != CommandLine.ExitCode.OK) {
                    return result;
                }
            }
        }
        return CommandLine.ExitCode.OK;
    }

    private static void traceAllSpecs(CommandSpec[] specs, Config config) {
        List<String> all = new ArrayList<String>();
        for (CommandSpec spec: specs) {
            Object obj = spec.userObject();
            if (obj == null) {
                all.add(spec.name() + " (no user object)");
            } else if (obj instanceof Method) {
                all.add(spec.name() + " (" + ((Method) obj).toGenericString() + ")");
            } else {
                all.add(obj.getClass().getName());
            }
        }
        config.verbose("Generating man pages for %s and all subcommands%n", all);
    }

    private static int generateSingleManPage(Config config, CommandSpec spec) throws IOException {
        if (!mkdirs(config, config.directory)) {
            return CommandLine.ExitCode.SOFTWARE;
        }
        File manpage = new File(config.directory, makeFileName(spec));
        config.verbose("Generating man page %s%n", manpage);

        generateSingleManPage(spec, manpage);

        return generateCustomizableTemplate(config, spec);
    }

    private static boolean mkdirs(Config config, File directory) {
        if (directory != null && !directory.exists()) {
            config.verboseDetailed("Creating directory %s%n", directory);

            if (!directory.mkdirs()) {
                System.err.println("Unable to mkdirs for " + directory.getAbsolutePath());
                return false;
            }
        }
        return true;
    }

    private static String makeFileName(CommandSpec spec) {
        return (spec.qualifiedName("-") + ".adoc")
                .replaceAll("\\s", "_")
                .replace("<main_class>", "main_class");
    }

    private static void generateSingleManPage(CommandSpec spec, File manpage) throws IOException {
        OutputStreamWriter writer = null;
        PrintWriter pw = null;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(manpage), "UTF-8");
            pw = new PrintWriter(writer);
            writeSingleManPage(pw, spec);
        } finally {
            Util.closeSilently(pw);
            Util.closeSilently(writer);
        }
    }

    private static int generateCustomizableTemplate(Config config, CommandSpec spec) throws IOException {
        if (config.templatesDirectory == null) {
            return CommandLine.ExitCode.OK;
        }
        if (!mkdirs(config, config.templatesDirectory)) {
            return CommandLine.ExitCode.SOFTWARE;
        }

        File templateFile = new File(config.templatesDirectory, makeFileName(spec));
        if (templateFile.exists()) {
            if (config.force) {
                config.verbose("Overwriting existing man page template file %s...%n", templateFile);
            } else {
                System.err.printf("gen-manpage: ERROR: cannot generate man page template file %s: it already exists. " +
                        "Remove the --template-dir option or use --force to overwrite.%n", templateFile);
                System.err.println("Try 'gen-manpage --help' for more information.");
                return EXIT_CODE_TEMPLATE_EXISTS;
            }
        } else {
            config.verbose("Generating customizable man page template %s%n", templateFile);
        }

        FileWriter writer = null;
        PrintWriter pw = null;
        try {
            writer = new FileWriter(templateFile);
            pw = new PrintWriter(writer);
            writeCustomizableManPageTemplate(pw, config.directory, spec);
        } finally {
            Util.closeSilently(pw);
            Util.closeSilently(writer);
        }
        return CommandLine.ExitCode.OK;
    }

    static void writeCustomizableManPageTemplate(PrintWriter pw, File includeDir, CommandSpec spec) {
        pw.printf(":includedir: %s%n", includeDir.getAbsolutePath().replace('\\', '/'));
        pw.printf("//include::{includedir}/%s[tag=picocli-generated-full-manpage]%n", makeFileName(spec));

        List<String> tags = Arrays.asList("header", "name", "synopsis",
                "description", "options", "arguments", "commands", "exit-status", "footer");
        for (String tag : tags) {
            pw.println(); // ensure that the include directives are separated with a newline
            pw.printf("include::{includedir}/%s[tag=picocli-generated-man-section-%s]%n",
                    makeFileName(spec), tag);
        }
    }

    public static void writeSingleManPage(PrintWriter pw, CommandSpec spec) {
        spec.commandLine().setColorScheme(COLOR_SCHEME);

        pw.printf("// tag::picocli-generated-full-manpage[]%n");
        genHeader(pw, spec);
        genOptions(pw, spec);
        genPositionalArgs(pw, spec);
        genCommands(pw, spec);
        genExitStatus(pw, spec);
        genFooter(pw, spec);
        pw.printf("// end::picocli-generated-full-manpage[]%n");
    }

    static void genHeader(PrintWriter pw, CommandSpec spec) {
        pw.printf("// tag::picocli-generated-man-section-header[]%n");
        pw.printf(":doctype: manpage%n");
        //pw.printf(":authors: %s%n", spec.userObject()); // author
        pw.printf(":revnumber: %s%n", versionString(spec)); // version
        pw.printf(":manmanual: %s%n", manualTitle(spec));
        pw.printf(":mansource: %s%n", versionString(spec)); // spec.qualifiedName("-").toUpperCase()
        pw.printf(":man-linkstyle: pass:[blue R < >]%n");
        pw.printf("= %s(1)%n", spec.qualifiedName("-")); // command name (lower case)
        pw.println();
        pw.printf("// end::picocli-generated-man-section-header[]%n");
        pw.println();

        pw.printf("// tag::picocli-generated-man-section-name[]%n");
        pw.printf("== Name%n%n");
        pw.printf("%s - %s%n", spec.qualifiedName("-"), headerDescriptionString(spec)); // name and description
        pw.println();
        pw.printf("// end::picocli-generated-man-section-name[]%n");
        pw.println();

        pw.printf("// tag::picocli-generated-man-section-synopsis[]%n");
        pw.printf("== Synopsis%n%n");
        pw.printf("%s", spec.commandLine().getHelp().synopsis(0));
        pw.println();
        pw.printf("// end::picocli-generated-man-section-synopsis[]%n");
        pw.println();

        pw.printf("// tag::picocli-generated-man-section-description[]%n");
        pw.printf("== Description%n%n");
        pw.printf("%s%n", format(COLOR_SCHEME.text(join("%n", (Object[]) spec.usageMessage().description())).toString())); // description
        pw.println();
        pw.printf("// end::picocli-generated-man-section-description[]%n");
        pw.println();
    }

    private static String versionString(CommandSpec spec) {
        return spec.version().length == 0 ? "" : spec.version()[0].replaceAll(":", " ");
    }

    private static String manualTitle(CommandSpec spec) {
        CommandSpec parent = spec;
        while (parent.parent() != null) {
            parent = parent.parent();
        }
        String name = parent.name();
        return Character.toUpperCase(name.charAt(0)) + name.substring(1) + " Manual";
    }

    private static String headerDescriptionString(CommandSpec spec) {
        String result = null;
        String[] headerDescription = spec.usageMessage().header();
        if (headerDescription == null || headerDescription.length == 0 || headerDescription[0] == null || headerDescription[0].length() == 0) {
            // if the command does not have a header, use only the first line from the description:
            // the other lines will be shown in the DESCRIPTION section of the man page
            result = firstElement(spec.usageMessage().description());
        } else {
            // if the command header has multiple lines, we display all of them in the NAME section
            result = join("%n", (Object[]) headerDescription);
        }
        return format(COLOR_SCHEME.text(result).toString()); // convert any embedded %n strings to newlines
    }

    static void genOptions(PrintWriter pw, CommandSpec spec) {
        List<OptionSpec> options = new ArrayList<OptionSpec>(spec.options()); // options are stored in order of declaration

        // remove hidden options
        for (Iterator<OptionSpec> iter = options.iterator(); iter.hasNext();) {
            if (iter.next().hidden()) { iter.remove(); }
        }

        IOptionRenderer optionRenderer = spec.commandLine().getHelp().createDefaultOptionRenderer();
        IParamLabelRenderer paramLabelRenderer = spec.commandLine().getHelp().createDefaultParamLabelRenderer();
        IParameterRenderer parameterRenderer = spec.commandLine().getHelp().createDefaultParameterRenderer();

        List<ArgGroupSpec> groups = optionListGroups(spec);
        for (ArgGroupSpec group : groups) { options.removeAll(group.allOptionsNested()); }
        
        Comparator<OptionSpec> optionSort = spec.usageMessage().sortOptions()
                ? new SortByShortestOptionNameAlphabetically()
                : createOrderComparatorIfNecessary(spec.options());
        
        pw.printf("// tag::picocli-generated-man-section-options[]%n");
        if (!options.isEmpty()) {
            pw.printf("== Options%n");

            if (optionSort != null) {
                Collections.sort(options, optionSort); // default: sort options ABC
            }
            for (OptionSpec option : options) {
                writeOption(pw, optionRenderer, paramLabelRenderer, option);
            }
    
            if (spec.usageMessage().showEndOfOptionsDelimiterInUsageHelp()) {
                CommandLine cmd = new CommandLine(spec).setColorScheme(COLOR_SCHEME);
                CommandLine.Help help = cmd.getHelp();
                writeEndOfOptions(pw, optionRenderer, paramLabelRenderer, help.END_OF_OPTIONS_OPTION);
            }
        }

        // now create a custom option section for each arg group that has a heading
        Collections.sort(groups, new SortByOrder<ArgGroupSpec>());
        for (ArgGroupSpec group : groups) {
            pw.println();
            String heading = makeHeading(group.heading(), "Options Group");
            pw.printf("== %s%n", COLOR_SCHEME.text(heading));

            for (PositionalParamSpec positional : group.allPositionalParametersNested()) {
                if (!positional.hidden()) {
                    writePositional(pw, positional, parameterRenderer, paramLabelRenderer);
                }
            }
            List<OptionSpec> groupOptions = new ArrayList<OptionSpec>(group.allOptionsNested());
            if (optionSort != null) {
                Collections.sort(groupOptions, optionSort);
            }
            for (OptionSpec option : groupOptions) {
                writeOption(pw, optionRenderer, paramLabelRenderer, option);
            }
        }
        pw.println();
        pw.printf("// end::picocli-generated-man-section-options[]%n");
        pw.println();
    }


    /** Returns the list of {@code ArgGroupSpec}s with a non-{@code null} heading. */
    private static List<ArgGroupSpec> optionListGroups(CommandSpec commandSpec) {
        List<ArgGroupSpec> result = new ArrayList<ArgGroupSpec>();
        optionListGroups(commandSpec.argGroups(), result);
        return result;
    }
    private static void optionListGroups(List<ArgGroupSpec> groups, List<ArgGroupSpec> result) {
        for (ArgGroupSpec group : groups) {
            optionListGroups(group.subgroups(), result);
            if (group.heading() != null) { result.add(group); }
        }
    }

    private static void writeOption(PrintWriter pw, IOptionRenderer optionRenderer, IParamLabelRenderer paramLabelRenderer, OptionSpec option) {
        pw.println();
        Text[][] rows = optionRenderer.render(option, paramLabelRenderer, COLOR_SCHEME);
        pw.printf("%s::%n", join(", ", rows[0][1], rows[0][3]));
        pw.printf("  %s%n", rows[0][4]);
        for (int i = 1; i < rows.length; i++) {
            pw.printf("+%n%s%n", rows[i][4]);
        }
    }

    private static void writePositional(PrintWriter pw, PositionalParamSpec positional, IParameterRenderer parameterRenderer, IParamLabelRenderer paramLabelRenderer) {
        pw.println();
        Text[][] rows = parameterRenderer.render(positional, paramLabelRenderer, COLOR_SCHEME);
        pw.printf("%s::%n", join(", ", rows[0][1], rows[0][3]));
        pw.printf("  %s%n", rows[0][4]);
        for (int i = 1; i < rows.length; i++) {
            pw.printf("+%n%s%n", rows[i][4]);
        }
    }

    /** Write the end of options. */
    private static void writeEndOfOptions(PrintWriter pw, IOptionRenderer optionRenderer, IParamLabelRenderer paramLabelRenderer, OptionSpec option) {
        pw.println();
        Text[][] rows = optionRenderer.render(option, paramLabelRenderer, COLOR_SCHEME);
        pw.printf("%s::%n", join("", rows[0][1], rows[0][3]));
        String description = String.valueOf(rows[0][4]);
        // ignore "${picocli.endofoptions.description:-" and "}"
        pw.printf("  %s%n",  description.substring(36,description.length()-1));
    }

    static void genPositionalArgs(PrintWriter pw, CommandSpec spec) {
        List<PositionalParamSpec> positionals = new ArrayList<PositionalParamSpec>(spec.positionalParameters());
        // remove hidden params
        for (Iterator<PositionalParamSpec> iter = positionals.iterator(); iter.hasNext();) {
            if (iter.next().hidden()) { iter.remove(); }
        }
        // positional parameters that are part of a group
        // are shown in the custom option section for that group
        List<ArgGroupSpec> groups = optionListGroups(spec);
        for (ArgGroupSpec group : groups) { positionals.removeAll(group.positionalParameters()); }

        if (positionals.isEmpty() && !spec.usageMessage().showAtFileInUsageHelp()) {
            pw.printf("// tag::picocli-generated-man-section-arguments[]%n");
            pw.printf("// end::picocli-generated-man-section-arguments[]%n");
            pw.println();
            return;
        }
        pw.printf("// tag::picocli-generated-man-section-arguments[]%n");
        pw.printf("== Arguments%n");

        IParameterRenderer parameterRenderer = spec.commandLine().getHelp().createDefaultParameterRenderer();
        IParamLabelRenderer paramLabelRenderer = spec.commandLine().getHelp().createDefaultParamLabelRenderer();

        if (spec.usageMessage().showAtFileInUsageHelp()) {
            CommandLine cmd = new CommandLine(spec).setColorScheme(COLOR_SCHEME);
            CommandLine.Help help = cmd.getHelp();
            writePositional(pw, help.AT_FILE_POSITIONAL_PARAM, parameterRenderer, paramLabelRenderer);
        }

        for (PositionalParamSpec positional : positionals) {
            writePositional(pw, positional, parameterRenderer, paramLabelRenderer);
        }
        pw.println();
        pw.printf("// end::picocli-generated-man-section-arguments[]%n");
        pw.println();
    }

    static void genCommands(PrintWriter pw, CommandSpec spec) {

        // remove hidden subcommands before tags are added
        Map<String, CommandLine> subCommands = new LinkedHashMap<String, CommandLine>(spec.subcommands());
        for (Iterator<Map.Entry<String, CommandLine>> iter = subCommands.entrySet().iterator(); iter.hasNext();) {
            if (iter.next().getValue().getCommandSpec().usageMessage().hidden()) {
                iter.remove();
            }
        }

        if (spec.subcommands().isEmpty()) {
            pw.printf("// tag::picocli-generated-man-section-commands[]%n");
            pw.printf("// end::picocli-generated-man-section-commands[]%n");
            pw.println();
            return;
        }
        pw.printf("// tag::picocli-generated-man-section-commands[]%n");
        pw.printf("== Commands%n");

        for (CommandLine.Help subHelp : spec.commandLine().getHelp().subcommands().values()) {
            pw.println();

            Text namesText = subHelp.commandNamesText(", ");
            String names = namesText.toString();
            String xrefname = makeFileName(subHelp.commandSpec());
            pw.printf("xref:%s[%s]::%n", xrefname, names);

            CommandLine.Model.UsageMessageSpec usage = subHelp.commandSpec().usageMessage();
            String header = !empty(usage.header())
                    ? usage.header()[0]
                    : (!empty(usage.description()) ? usage.description()[0] : "");
            Text[] lines = COLOR_SCHEME.text(format(header)).splitLines();

            pw.printf("  %s%n", lines[0].toString());
            for (int i = 1; i < lines.length; i++) {
                pw.printf("+%n%s%n", lines[i].toString());
            }
        }
        pw.println();
        pw.printf("// end::picocli-generated-man-section-commands[]%n");
        pw.println();
    }

    static void genExitStatus(PrintWriter pw, CommandSpec spec) {
        if (spec.usageMessage().exitCodeList().isEmpty()) {
            pw.printf("// tag::picocli-generated-man-section-exit-status[]%n");
            pw.printf("// end::picocli-generated-man-section-exit-status[]%n");
            pw.println();
            return;
        }
        String heading = makeHeading(spec.usageMessage().exitCodeListHeading(), "Exit status");
        pw.printf("// tag::picocli-generated-man-section-exit-status[]%n");
        //pw.printf("== Exit status%n");
        pw.printf("== %s%n", COLOR_SCHEME.text(heading));



        for (Map.Entry<String, String> entry : spec.usageMessage().exitCodeList().entrySet()) {
            pw.println();
            pw.printf("*%s*::%n", COLOR_SCHEME.text(entry.getKey().trim()));
            pw.printf("  %s%n", COLOR_SCHEME.text(entry.getValue()));
        }
        pw.println();
        pw.printf("// end::picocli-generated-man-section-exit-status[]%n");
        pw.println();
    }

    static void genFooter(PrintWriter pw, CommandSpec spec) {
        if (spec.usageMessage().footerHeading().length() == 0 || spec.usageMessage().footer().length == 0) {
            pw.printf("// tag::picocli-generated-man-section-footer[]%n");
            pw.printf("// end::picocli-generated-man-section-footer[]%n");
            pw.println();
            return;
        }
        String heading = makeHeading(spec.usageMessage().footerHeading(), "Footer");
        pw.printf("// tag::picocli-generated-man-section-footer[]%n");
        pw.printf("== %s%n", COLOR_SCHEME.text(heading));
        pw.println();

        boolean hardbreaks = true;
        for (String line : spec.usageMessage().footer()) {

            if (hardbreaks) {
                pw.println("[%hardbreaks]"); // preserve line breaks
                hardbreaks = false;
            }
            String renderedLine = COLOR_SCHEME.text(format(line)).toString();

            // Lines that start with "# " may be intended as shell comments,
            // but are rendered as AsciiDoc headers (equivalent to "= ...").
            // We use a passthrough to prevent substitution. (TODO Should this be customizable?)
            // See https://asciidoctor.org/docs/user-manual/#passthroughs
            if (renderedLine.startsWith("# ")) {
                renderedLine = "pass:c[# ]" + renderedLine.substring(2);
            }
            pw.printf("%s%n", renderedLine);
            if (line.trim().length() == 0) {
                hardbreaks = true;
            }
        }
        pw.println();
        pw.printf("// end::picocli-generated-man-section-footer[]%n");
        pw.println();
    }

    private static String makeHeading(String heading, String defaultIfEmpty) {
        if (heading.endsWith("%n")) { heading = heading.substring(0, heading.length() - 2); }
        heading = heading.trim().length() == 0 ? defaultIfEmpty : heading.replaceAll("%n", " ");
        return heading;
    }

    private static Comparator<OptionSpec> createOrderComparatorIfNecessary(List<OptionSpec> options) {
        for (OptionSpec option : options) {
            if (option.order() != -1/*OptionSpec.DEFAULT_ORDER*/) {
                return new SortByOrder<OptionSpec>();
            }
        }
        return null;
    }
    static class SortByOrder<T extends IOrdered> implements Comparator<T> {
        public int compare(T o1, T o2) {
            return Integer.signum(o1.order() - o2.order());
        }
    }
    /** Sorts short strings before longer strings. */
    static class ShortestFirst implements Comparator<String> {
        public int compare(String o1, String o2) {
            return o1.length() - o2.length();
        }
        /** Sorts the specified array of Strings shortest-first and returns it. */
        public static String[] sort(String[] names) {
            Arrays.sort(names, new ShortestFirst());
            return names;
        }
        /** Sorts the specified array of Strings longest-first and returns it. */
        public static String[] longestFirst(String[] names) {
            Arrays.sort(names, Collections.reverseOrder(new ShortestFirst()));
            return names;
        }
    }
    /** Sorts {@code OptionSpec} instances by their name in case-insensitive alphabetic order. If an option has
     * multiple names, the shortest name is used for the sorting. Help options follow non-help options. */
    static class SortByShortestOptionNameAlphabetically implements Comparator<OptionSpec> {
        @SuppressWarnings("deprecation")
        public int compare(OptionSpec o1, OptionSpec o2) {
            if (o1 == null) { return 1; } else if (o2 == null) { return -1; } // options before params
            String[] names1 = ShortestFirst.sort(o1.names());
            String[] names2 = ShortestFirst.sort(o2.names());
            String s1 = stripPrefix(names1[0]);
            String s2 = stripPrefix(names2[0]);
            int result = s1.toUpperCase().compareTo(s2.toUpperCase()); // case insensitive sort
            result = result == 0 ? -s1.compareTo(s2) : result; // lower case before upper case
            return o1.help() == o2.help() ? result : o2.help() ? -1 : 1; // help options come last
        }
    }

    private static String join(String sep, Object... lines) {
        StringBuilder sb = new StringBuilder();
        for (Object line : lines) {
            if (sb.length() > 0) { sb.append(sep); }
            sb.append(line);
        }
        return sb.toString();
    }

    private static String firstElement(String[] elements) {
        if (elements == null || elements.length ==0) {
            return "";
        }
        return elements[0];
    }

    private static boolean empty(Object[] array) { return array == null || array.length == 0; }

    static String stripPrefix(String prefixed) {
        for (int i = 0; i < prefixed.length(); i++) {
            if (Character.isJavaIdentifierPart(prefixed.charAt(i))) { return prefixed.substring(i); }
        }
        return prefixed;
    }
}
