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
import picocli.CommandLine.Model.OptionSpec;
import picocli.CommandLine.Model.PositionalParamSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.codegen.aot.graalvm.ReflectionConfigGenerator;
import picocli.codegen.util.OutputFileMixin;
import picocli.codegen.util.Util;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class ManPageGenerator {
    static final IStyle BOLD = new IStyle() {
        public String on()  { return "*"; }
        public String off() { return "*"; }
    };
    static final IStyle ITALIC = new IStyle() {
        public String on()  { return "_"; }
        public String off() { return "_"; }
    };
    static final ColorScheme COLOR_SCHEME = new ColorScheme.Builder(CommandLine.Help.Ansi.ON).
            commands(BOLD).options(BOLD).optionParams(ITALIC).parameters(ITALIC).build();

    @Command(name = "gen-manpage",
            description = {"Generates an AsciiDoc file in the manpage format. " +
                    "The generated AsciiDoc file can be passed to asciidoc with the `--backend manpage` option " +
                    "to convert it to a groff man page.",
                    "See https://asciidoctor.org/docs/user-manual/#man-pages"},
            mixinStandardHelpOptions = true,
            version = "picocli-codegen ${COMMAND-NAME} " + CommandLine.VERSION)
    private static class App implements Callable<Integer> {

        @Parameters(arity = "1..*", description = "One or more classes to generate man pages for.")
        Class<?>[] classes = new Class<?>[0];

        @Option(names = {"-c", "--factory"}, description = "Optionally specify the fully qualified class name of the custom factory to use to instantiate the command class. " +
                "When omitted, the default picocli factory is used.")
        String factoryClass;

        @CommandLine.Mixin
        OutputFileMixin outputFile = new OutputFileMixin();

        public Integer call() throws Exception {
            List<CommandSpec> specs = Util.getCommandSpecs(factoryClass, classes);
            String result = ReflectionConfigGenerator.generateReflectionConfig(specs.toArray(new CommandSpec[0]));
            outputFile.write(result);
            return 0;
        }

    }

    public static void main(String[] args) {
        System.exit(new CommandLine(new App()).execute(args));
    }

    public static void generateManPage(PrintWriter pw, CommandSpec... specs) {
        for (CommandSpec spec : specs) {
            generateSingleManPage(pw, spec);
        }
    }

    private static void generateSingleManPage(PrintWriter pw, CommandSpec spec) {
        genHeader(pw, spec);
        genOptions(pw, spec);
        genPositionals(pw, spec);
        genExitStatus(pw, spec);
        genFooter(pw, spec);
    }

    static void genHeader(PrintWriter pw, CommandSpec spec) {
        pw.printf("= %s(1)%n", spec.qualifiedName("-")); // command name (lower case)
        pw.printf(":doctype: manpage%n");
        //pw.printf(":authors: %s%n", spec.userObject()); // author
        pw.printf(":revnumber: %s%n", versionString(spec)); // version
        pw.printf(":manmanual: %s%n", manualTitle(spec));
        pw.printf(":mansource: %s%n", versionString(spec)); // spec.qualifiedName("-").toUpperCase()
        pw.printf(":man-linkstyle: pass:[blue R < >]%n");
        pw.println();

        pw.printf("== Name%n%n");
        pw.printf("%s - %s%n", spec.qualifiedName("-"), headerDescriptionString(spec)); // name and description
        pw.println();

        pw.printf("== Synopsis%n%n");
        pw.printf("%s%n", synopsisString(spec));

        pw.printf("== Description%n%n");
        pw.printf("%s%n", join("%n", spec.usageMessage().description())); // description
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
        return join("%n", headerDescription);
    }

    private static String synopsisString(CommandSpec spec) {
        return spec.commandLine().getHelp().synopsis(0).replace(spec.qualifiedName(), "*" + spec.qualifiedName() + "*");
    }

    static void genOptions(PrintWriter pw, CommandSpec spec) {
        if (spec.options().isEmpty()) {
            return;
        }
        pw.printf("== Options%n");
        pw.println();

        IOptionRenderer optionRenderer = spec.commandLine().getHelp().createDefaultOptionRenderer();
        IParamLabelRenderer paramLabelRenderer = spec.commandLine().getHelp().createDefaultParamLabelRenderer();
        for (OptionSpec option : spec.options()) {
            Text[][] rows = optionRenderer.render(option, paramLabelRenderer, COLOR_SCHEME);
            pw.printf("%s::%n", replaceAll(join(", ", rows[0][1], rows[0][3]), Style.reset.off(), ""));
            pw.printf("  %s%n", replaceAll(rows[0][4].toString(), Style.reset.off(), ""));
            for (int i = 1; i < rows.length; i++) {
                pw.printf("  %s%n", replaceAll(rows[i][4].toString(), Style.reset.off(), ""));
            }
            pw.println();
        }
    }

    static void genPositionals(PrintWriter pw, CommandSpec spec) {
        if (spec.positionalParameters().isEmpty()) {
            return;
        }
        pw.printf("== Arguments%n");
        pw.println();

        IParameterRenderer parameterRenderer = spec.commandLine().getHelp().createDefaultParameterRenderer();
        IParamLabelRenderer paramLabelRenderer = spec.commandLine().getHelp().createDefaultParamLabelRenderer();
        for (PositionalParamSpec positional : spec.positionalParameters()) {
            Text[][] rows = parameterRenderer.render(positional, paramLabelRenderer, COLOR_SCHEME);
            pw.printf("%s::%n", replaceAll(join(", ", rows[0][1], rows[0][3]), Style.reset.off(), ""));
            pw.printf("  %s%n", replaceAll(rows[0][4].toString(), Style.reset.off(), ""));
            for (int i = 1; i < rows.length; i++) {
                pw.printf("  %s%n", replaceAll(rows[i][4].toString(), Style.reset.off(), ""));
            }
            pw.println();
        }
    }

    static void genExitStatus(PrintWriter pw, CommandSpec spec) {
        if (spec.usageMessage().exitCodeList().isEmpty()) {
            return;
        }
        pw.printf("== Exit status%n");
        pw.println();

        for (Map.Entry<String, String> entry : spec.usageMessage().exitCodeList().entrySet()) {
            pw.printf("*%s*::%n", entry.getKey().trim());
            pw.printf("  %s%n", entry.getValue());
            pw.println();
        }
    }

    static void genFooter(PrintWriter pw, CommandSpec spec) {
        if (spec.usageMessage().footerHeading().length() == 0 || spec.usageMessage().footer().length == 0) {
            return;
        }
        String heading = spec.usageMessage().footerHeading();
        heading = heading.length() == 0 ? "Footer" : heading.replaceAll("%n", " ");
        pw.printf("== %s%n", heading);
        pw.println();

        for (String line : spec.usageMessage().footer()) {
            pw.printf("%s%n", line);
            pw.println();
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
}