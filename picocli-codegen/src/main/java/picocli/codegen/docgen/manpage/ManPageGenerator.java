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
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.IOrdered;
import picocli.CommandLine.Model.OptionSpec;
import picocli.CommandLine.Model.PositionalParamSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.codegen.util.Util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static java.lang.String.format;

public class ManPageGenerator {
    static final int CUSTOMIZABLE_MAN_PAGE_FILE_EXISTS = 4;

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

    private static Map<String, IStyle> createMarkupMap() {
        Map<String, IStyle> result = new HashMap<String, IStyle>();
        result.put(Style.bold.name(), BOLD);
        result.put(Style.italic.name(), ITALIC);
        result.put(Style.underline.name(), ITALIC);
        result.put(Style.reverse.name(), HIGHLIGHT);
        return result;
    }

    @Command(name = "gen-manpage",
            description = {"Generates an AsciiDoc file in the manpage format. " +
                    "The generated AsciiDoc file can be passed to asciidoc with the `--backend manpage` option " +
                    "to convert it to a groff man page.",
                    "See https://asciidoctor.org/docs/user-manual/#man-pages"},
            mixinStandardHelpOptions = true, sortOptions = false, usageHelpAutoWidth = true, usageHelpWidth = 100,
            version = "picocli-codegen ${COMMAND-NAME} " + CommandLine.VERSION)
    private static class App implements Callable<Integer> {

        @Parameters(arity = "1..*", description = "One or more classes to generate man pages for.")
        Class<?>[] classes = new Class<?>[0];

        @Option(names = {"-d", "--outdir"}, defaultValue = ".", paramLabel = "<dir>",
                description = {"Output directory to write the generated AsciiDoc files to. " +
                        "If not specified, files are written to the current directory.",
                        "To convert these AsciiDoc files to manpage files, execute " +
                                "`asciidoctor --backend=manpage --source-dir=THISDIR --destination-dir=ELSEWHERE` " +
                                "with this directory as the source-dir and some other directory as the destination."
                })
        File directory;

        @Option(names = {"--customizable-pages-dir"}, paramLabel = "<dir>",
                description = {
                        "Optional directory to write customizable man pages. " +
                                "If specified, additional AsciiDoc files are generated here that have no content " +
                                "other than `include` directives that pull in the contents " +
                                "of a generated manpage AsciiDoc file in the `--outdir` directory.",
                        "These customizable man pages are intended to be generated once, and afterwards " +
                                "be manually updated and maintained. The resulting man page will be a mixture of " +
                                "generated and manually edited text.",
                        "To convert these AsciiDoc files to manpage files, execute " +
                                "`asciidoctor --backend=manpage --source-dir=THISDIR --destination-dir=ELSEWHERE` " +
                                "with this directory as the source-dir and some other directory as the destination."
                }
        )
        File customizablePagesDirectory;

        @Option(names = {"-c", "--factory"}, description = "Optionally specify the fully qualified class name of the custom factory to use to instantiate the command class. " +
                "If omitted, the default picocli factory is used.")
        String factoryClass;

        public Integer call() throws Exception {
            List<CommandSpec> specs = Util.getCommandSpecs(factoryClass, classes);
            return generateManPage(directory, customizablePagesDirectory, specs.toArray(new CommandSpec[0]));
        }
    }

    public static void main(String[] args) {
        System.exit(new CommandLine(new App()).execute(args));
    }

    public static int generateManPage(File directory,
                                       File customizablePagesDirectory,
                                       CommandSpec... specs) throws IOException {
        for (CommandSpec spec : specs) {
            int result = generateSingleManPage(directory, customizablePagesDirectory, spec);
            if (result != CommandLine.ExitCode.OK) {
                return result;
            }

            // recursively create man pages for subcommands
            for (CommandLine sub : spec.subcommands().values()) {
                result = generateManPage(directory, customizablePagesDirectory, sub.getCommandSpec());
                if (result != CommandLine.ExitCode.OK) {
                    return result;
                }
            }
        }
        return CommandLine.ExitCode.OK;
    }

    private static int generateSingleManPage(File directory,
                                              File customizablePagesDirectory,
                                              CommandSpec spec) throws IOException {

        if (customizablePagesDirectory != null && customizablePagesDirectory.equals(directory)) {
            System.err.println("gen-manpage: Error: output directory must differ from customizable man pages directory.");
            System.err.println("Try 'gen-manpage --help' for more information.");
            return CommandLine.ExitCode.USAGE;
        }

        FileWriter writer = null;
        PrintWriter pw = null;
        try {
            if (!mkdirs(directory))                  {return CommandLine.ExitCode.SOFTWARE;}
            if (!mkdirs(customizablePagesDirectory)) {return CommandLine.ExitCode.SOFTWARE;}

            writer = new FileWriter(new File(directory, makeFileName(spec)));
            pw = new PrintWriter(writer);
            generateSingleManPage(pw, spec);
            Util.closeSilently(pw);
            Util.closeSilently(writer);

            if (customizablePagesDirectory != null) {
                File customizablePage = new File(directory, makeFileName(spec));
                if (customizablePage.exists()) {
                    System.err.printf("gen-manpage: Error: customizable man page %s already exists.%n", customizablePage);
                    System.err.println("Try 'gen-manpage --help' for more information.");
                    return CUSTOMIZABLE_MAN_PAGE_FILE_EXISTS;
                }
                writer = new FileWriter(customizablePage);
                pw = new PrintWriter(writer);
                generateCustomizableManPage(pw, directory, spec);
            }
        } finally {
            Util.closeSilently(pw);
            Util.closeSilently(writer);
        }
        return CommandLine.ExitCode.OK;
    }

    private static boolean mkdirs(File directory) {
        if (directory != null && !directory.exists() && !directory.mkdirs()) {
            System.err.println("Unable to mkdirs for " + directory.getAbsolutePath());
            return false;
        }
        return true;
    }

    private static String makeFileName(CommandSpec spec) {
        String result = spec.qualifiedName("-") + ".adoc";
        return result.replaceAll("\\s", "_");
    }

    static void generateCustomizableManPage(PrintWriter pw, File directory, CommandSpec spec) {
        pw.printf(":includedir: %s%n", directory.getAbsolutePath());
        pw.printf("//include::{includedir}/%s[tag=picocli-generated-manpage]%n", makeFileName(spec));

        List<String> tags = Arrays.asList("header", "name-section", "synopsis",
                "description", "arguments", "options", "commands", "exit-status", "footer");
        for (String tag : tags) {
            pw.println(); // ensure that the include directives are separated with a newline
            pw.printf("include::{includedir}/%s[tag=picocli-generated-man-%s]%n",
                    makeFileName(spec), tag);
        }
    }

    public static void generateSingleManPage(PrintWriter pw, CommandSpec spec) {
        spec.commandLine().setColorScheme(COLOR_SCHEME);

        pw.printf("// tag::picocli-generated-manpage[]%n");
        genHeader(pw, spec);
        genOptions(pw, spec);
        genPositionals(pw, spec);
        genCommands(pw, spec);
        genExitStatus(pw, spec);
        genFooter(pw, spec);
        pw.printf("// end::picocli-generated-manpage[]%n");
    }

    static void genHeader(PrintWriter pw, CommandSpec spec) {
        pw.printf("// tag::picocli-generated-man-header[]%n");
        pw.printf(":doctype: manpage%n");
        //pw.printf(":authors: %s%n", spec.userObject()); // author
        pw.printf(":revnumber: %s%n", versionString(spec)); // version
        pw.printf(":manmanual: %s%n", manualTitle(spec));
        pw.printf(":mansource: %s%n", versionString(spec)); // spec.qualifiedName("-").toUpperCase()
        pw.printf(":man-linkstyle: pass:[blue R < >]%n");
        pw.printf("= %s(1)%n", spec.qualifiedName("-")); // command name (lower case)
        pw.printf("// end::picocli-generated-man-header[]%n");
        pw.println();

        pw.printf("// tag::picocli-generated-man-name-section[]%n");
        pw.printf("== Name%n%n");
        pw.printf("%s - %s%n", spec.qualifiedName("-"), headerDescriptionString(spec)); // name and description
        pw.printf("// end::picocli-generated-man-name-section[]%n");
        pw.println();

        pw.printf("// tag::picocli-generated-man-synopsis[]%n");
        pw.printf("== Synopsis%n%n");
        pw.printf("%s", synopsisString(spec));
        pw.printf("// end::picocli-generated-man-synopsis[]%n");
        pw.println();

        pw.printf("// tag::picocli-generated-man-description[]%n");
        pw.printf("== Description%n%n");
        pw.printf("%s%n", join("%n", (Object[]) spec.usageMessage().description())); // description
        pw.printf("// end::picocli-generated-man-description[]%n");
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
        String[] headerDescription = spec.usageMessage().header();
        if (headerDescription == null || headerDescription.length == 0 || headerDescription[0] == null || headerDescription[0].length() == 0) {
            headerDescription = spec.usageMessage().description();
        }
        return join("%n", (Object[]) headerDescription);
    }

    private static String synopsisString(CommandSpec spec) {
        String synopsis = spec.commandLine().getHelp().synopsis(0);
        return replaceAll(synopsis, Style.reset.off(), "");
    }

    static void genOptions(PrintWriter pw, CommandSpec spec) {
        if (spec.options().isEmpty()) {
            return;
        }
        pw.printf("// tag::picocli-generated-man-options[]%n");
        pw.printf("== Options%n");

        IOptionRenderer optionRenderer = spec.commandLine().getHelp().createDefaultOptionRenderer();
        IParamLabelRenderer valueLabelRenderer = spec.commandLine().getHelp().createDefaultParamLabelRenderer();

        Comparator<OptionSpec> optionSort = spec.usageMessage().sortOptions()
                ? new SortByShortestOptionNameAlphabetically()
                : createOrderComparatorIfNecessary(spec.options());

        List<OptionSpec> options = new ArrayList<OptionSpec>(spec.options()); // options are stored in order of declaration
        if (optionSort != null) {
            Collections.sort(options, optionSort); // default: sort options ABC
        }
//        List<ArgGroupSpec> groups = optionListGroups();
//        for (ArgGroupSpec group : groups) { options.removeAll(group.options()); }
//
//        StringBuilder sb = new StringBuilder();
//        layout.addOptions(options, valueLabelRenderer);
//        sb.append(layout.toString());
//
//        Collections.sort(groups, new SortByOrder<ArgGroupSpec>());
//        for (ArgGroupSpec group : groups) {
//            sb.append(createHeading(group.heading()));
//
//            CommandLine.Help.Layout groupLayout = createDefaultLayout();
//            groupLayout.addPositionalParameters(group.positionalParameters(), valueLabelRenderer);
//            List<OptionSpec> groupOptions = new ArrayList<OptionSpec>(group.options());
//            if (optionSort != null) {
//                Collections.sort(groupOptions, optionSort);
//            }
//            groupLayout.addOptions(groupOptions, valueLabelRenderer);
//            sb.append(groupLayout);
//        }
//        return sb.toString();

        for (OptionSpec option : options) {
            pw.println();
            Text[][] rows = optionRenderer.render(option, valueLabelRenderer, COLOR_SCHEME);
            pw.printf("%s::%n", replaceAll(join(", ", rows[0][1], rows[0][3]), Style.reset.off(), ""));
            pw.printf("  %s%n", replaceAll(rows[0][4].toString(), Style.reset.off(), ""));
            for (int i = 1; i < rows.length; i++) {
                pw.printf("+%n%s%n", replaceAll(rows[i][4].toString(), Style.reset.off(), ""));
            }
        }
        pw.printf("// end::picocli-generated-man-options[]%n");
        pw.println();
    }

    static void genPositionals(PrintWriter pw, CommandSpec spec) {
        if (spec.positionalParameters().isEmpty()) {
            return;
        }
        pw.printf("// tag::picocli-generated-man-arguments[]%n");
        pw.printf("== Arguments%n");

        IParameterRenderer parameterRenderer = spec.commandLine().getHelp().createDefaultParameterRenderer();
        IParamLabelRenderer paramLabelRenderer = spec.commandLine().getHelp().createDefaultParamLabelRenderer();
        for (PositionalParamSpec positional : spec.positionalParameters()) {
            pw.println();
            Text[][] rows = parameterRenderer.render(positional, paramLabelRenderer, COLOR_SCHEME);
            pw.printf("%s::%n", replaceAll(join(", ", rows[0][1], rows[0][3]), Style.reset.off(), ""));
            pw.printf("  %s%n", replaceAll(rows[0][4].toString(), Style.reset.off(), ""));
            for (int i = 1; i < rows.length; i++) {
                pw.printf("+%n%s%n", replaceAll(rows[i][4].toString(), Style.reset.off(), ""));
            }
        }
        pw.printf("// end::picocli-generated-man-arguments[]%n");
        pw.println();
    }

    static void genCommands(PrintWriter pw, CommandSpec spec) {
        if (spec.subcommands().isEmpty()) {
            return;
        }
        pw.printf("// tag::picocli-generated-man-commands[]%n");
        pw.printf("== Commands%n");

        for (CommandLine.Help subHelp : spec.commandLine().getHelp().subcommands().values()) {
            pw.println();

            Text namesText = subHelp.commandNamesText(", ");
            String names = replaceAll(namesText.toString(), Style.reset.off(), "");
            pw.printf("%s::%n", names);

            CommandLine.Model.UsageMessageSpec usage = subHelp.commandSpec().usageMessage();
            String header = !empty(usage.header())
                    ? usage.header()[0]
                    : (!empty(usage.description()) ? usage.description()[0] : "");
            Text[] lines = COLOR_SCHEME.ansi().new Text(format(header), COLOR_SCHEME).splitLines();

            pw.printf("  %s%n", replaceAll(lines[0].toString(), Style.reset.off(), ""));
            for (int i = 1; i < lines.length; i++) {
                pw.printf("+%n%s%n", replaceAll(lines[i].toString(), Style.reset.off(), ""));
            }
        }
        pw.printf("// end::picocli-generated-man-commands[]%n");
        pw.println();
    }

    static void genExitStatus(PrintWriter pw, CommandSpec spec) {
        if (spec.usageMessage().exitCodeList().isEmpty()) {
            return;
        }
        pw.printf("// tag::picocli-generated-man-exit-status[]%n");
        pw.printf("== Exit status%n");

        for (Map.Entry<String, String> entry : spec.usageMessage().exitCodeList().entrySet()) {
            pw.println();
            pw.printf("*%s*::%n", COLOR_SCHEME.ansi().new Text(entry.getKey().trim(), COLOR_SCHEME));
            pw.printf("  %s%n", COLOR_SCHEME.ansi().new Text(entry.getValue(), COLOR_SCHEME));
        }
        pw.printf("// end::picocli-generated-man-exit-status[]%n");
        pw.println();
    }

    static void genFooter(PrintWriter pw, CommandSpec spec) {
        if (spec.usageMessage().footerHeading().length() == 0 || spec.usageMessage().footer().length == 0) {
            return;
        }
        String heading = spec.usageMessage().footerHeading();
        if (heading.endsWith("%n")) { heading = heading.substring(0, heading.length() - 2); }
        heading = heading.length() == 0 ? "Footer" : heading.replaceAll("%n", " ");
        pw.printf("// tag::picocli-generated-man-footer[]%n");
        pw.printf("== %s%n", COLOR_SCHEME.ansi().new Text(heading, COLOR_SCHEME));
        pw.println();

        boolean hardbreaks = true;
        for (String line : spec.usageMessage().footer()) {

            if (hardbreaks) {
                pw.println("[%hardbreaks]"); // preserve line breaks
                hardbreaks = false;
            }
            String renderedLine = COLOR_SCHEME.ansi().new Text(format(line), COLOR_SCHEME).toString();
            if (renderedLine.startsWith("# ")) {
                renderedLine = "pass:c[# ]" + renderedLine.substring(2);
            }
            pw.printf("%s%n", renderedLine);
            if (line.trim().length() == 0) {
                hardbreaks = true;
            }
        }
        pw.printf("// end::picocli-generated-man-footer[]%n");
        pw.println();
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

    private static String replaceAll(String str, String find, String replacement) {
        String result = str.replace(find, replacement);
        while (!result.equals(str)) {
            str = result;
            result = str.replace(find, replacement);
        }
        return result;
    }
    private static boolean empty(Object[] array) { return array == null || array.length == 0; }
    static String stripPrefix(String prefixed) {
        for (int i = 0; i < prefixed.length(); i++) {
            if (Character.isJavaIdentifierPart(prefixed.charAt(i))) { return prefixed.substring(i); }
        }
        return prefixed;
    }
}
