package picocli.annotation.processing.tests;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.ArgGroupSpec;
import picocli.CommandLine.Model.ArgSpec;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.OptionSpec;
import picocli.CommandLine.Model.ParserSpec;
import picocli.CommandLine.Model.PositionalParamSpec;
import picocli.CommandLine.Model.UnmatchedArgsBinding;
import picocli.CommandLine.Model.UsageMessageSpec;
import picocli.CommandLine.Parameters;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Dumps a {@code CommandSpec} in YAML format.
 */
public class CommandSpecYamlPrinter {

    public static void main(String... args) {
        new CommandLine(new App()).execute(args);
    }

    static void print(Object userObject) {
        print(CommandSpec.forAnnotatedObject(userObject));
    }

    static void print(CommandSpec spec) {
        StringWriter sw = new StringWriter();
        new CommandSpecYamlPrinter().print(spec, new PrintWriter(sw));
        System.out.println(sw);
    }

    public void print(CommandSpec spec, PrintWriter pw) {
        pw.println("---");
        printCommandSpec(spec, "CommandSpec:", pw, "  ", "  ");
    }
    private void printCommandSpec(CommandSpec spec, String label, PrintWriter pw,
                                  String initialIndent, String indent) {
        pw.printf("%s%n", label);
        pw.printf("%sname: '%s'%n", initialIndent, spec.name());
        pw.printf("%saliases: %s%n", indent, Arrays.toString(spec.aliases()));
        pw.printf("%suserObject: %s%n", indent, spec.userObject());
        pw.printf("%shelpCommand: %s%n", indent, spec.helpCommand());
        pw.printf("%sdefaultValueProvider: %s%n", indent, spec.defaultValueProvider());
        pw.printf("%sversionProvider: %s%n", indent, spec.versionProvider());
        pw.printf("%sversion: %s%n", indent, Arrays.toString(spec.version()));

        printGroupList(spec.argGroups(), pw, indent);
        List<OptionSpec> options = new ArrayList<OptionSpec>(spec.options());
        Collections.sort(options, new Comparator<OptionSpec>() {
            public int compare(OptionSpec o1, OptionSpec o2) {
                return o1.shortestName().compareTo(o2.shortestName());
            }
        });
        printOptionList(options, pw, indent);
        printPositionalList(spec.positionalParameters(), pw, indent);
        printUnmatchedArgsBindingList(spec.unmatchedArgsBindings(), pw, indent);
        printMixinList(spec.mixins(), pw, indent);

        printUsageMessage(spec.usageMessage(), pw, indent);
        printParser(spec.parser(), pw, indent);
        printResourceBundle(spec.resourceBundle(), pw, indent);

        printSubcommandList(spec.subcommands(), pw, indent);
    }

    private void printResourceBundle(ResourceBundle resourceBundle, PrintWriter pw, String indent) {
        if (resourceBundle == null) {
            return;
        }
        pw.printf("%sResourceBundle:%n", indent);
        indent += "  ";
        ArrayList<String> keys = Collections.list(resourceBundle.getKeys());
        Collections.sort(keys);
        for (String key : keys) {
            pw.printf("%s%s: '%s'%n", indent, key, resourceBundle.getString(key));
        }
    }

    private void printParser(ParserSpec parser, PrintWriter pw, String indent) {
        pw.printf("%sParserSpec:%n", indent);
        indent += "  ";
        pw.printf("%sseparator: '%s'%n", indent, parser.separator());
        pw.printf("%sendOfOptionsDelimiter: '%s'%n", indent, parser.endOfOptionsDelimiter());
        pw.printf("%sexpandAtFiles: %s%n", indent, parser.expandAtFiles());
        pw.printf("%satFileCommentChar: '%s'%n", indent, parser.atFileCommentChar());
        pw.printf("%soverwrittenOptionsAllowed: %s%n", indent, parser.overwrittenOptionsAllowed());
        pw.printf("%sunmatchedArgumentsAllowed: %s%n", indent, parser.unmatchedArgumentsAllowed());
        pw.printf("%sunmatchedOptionsArePositionalParams: %s%n", indent, parser.unmatchedOptionsArePositionalParams());
        pw.printf("%sstopAtUnmatched: %s%n", indent, parser.stopAtUnmatched());
        pw.printf("%sstopAtPositional: %s%n", indent, parser.stopAtPositional());
        pw.printf("%sposixClusteredShortOptionsAllowed: %s%n", indent, parser.posixClusteredShortOptionsAllowed());
        pw.printf("%saritySatisfiedByAttachedOptionParam: %s%n", indent, parser.aritySatisfiedByAttachedOptionParam());
        pw.printf("%scaseInsensitiveEnumValuesAllowed: %s%n", indent, parser.caseInsensitiveEnumValuesAllowed());
        pw.printf("%scollectErrors: %s%n", indent, parser.collectErrors());
        pw.printf("%slimitSplit: %s%n", indent, parser.limitSplit());
        pw.printf("%stoggleBooleanFlags: %s%n", indent, parser.toggleBooleanFlags());
    }

    private void printUsageMessage(UsageMessageSpec usageMessage, PrintWriter pw, String indent) {
        pw.printf("%sUsageMessageSpec:%n", indent);
        indent += "  ";
        pw.printf("%swidth: %s%n", indent, usageMessage.width());
        pw.printf("%sabbreviateSynopsis: %s%n", indent, usageMessage.abbreviateSynopsis());
        pw.printf("%shidden: %s%n", indent, usageMessage.hidden());
        pw.printf("%sshowDefaultValues: %s%n", indent, usageMessage.showDefaultValues());
        pw.printf("%ssortOptions: %s%n", indent, usageMessage.sortOptions());
        pw.printf("%srequiredOptionMarker: '%s'%n", indent, usageMessage.requiredOptionMarker());
        pw.printf("%sheaderHeading: '%s'%n", indent, usageMessage.headerHeading());
        pw.printf("%sheader: %s%n", indent, Arrays.toString(usageMessage.header()));
        pw.printf("%ssynopsisHeading: '%s'%n", indent, usageMessage.synopsisHeading());
        pw.printf("%scustomSynopsis: %s%n", indent, Arrays.toString(usageMessage.customSynopsis()));
        pw.printf("%sdescriptionHeading: '%s'%n", indent, usageMessage.descriptionHeading());
        pw.printf("%sdescription: %s%n", indent, Arrays.toString(usageMessage.description()));
        pw.printf("%sparameterListHeading: '%s'%n", indent, usageMessage.parameterListHeading());
        pw.printf("%soptionListHeading: '%s'%n", indent, usageMessage.optionListHeading());
        pw.printf("%scommandListHeading: '%s'%n", indent, usageMessage.commandListHeading());
        pw.printf("%sfooterHeading: '%s'%n", indent, usageMessage.footerHeading());
        pw.printf("%sfooter: %s%n", indent, Arrays.toString(usageMessage.footer()));
    }


    private void printUnmatchedArgsBindingList(List<UnmatchedArgsBinding> unmatchedArgsBindings, PrintWriter pw, String indent) {
        pw.printf("%sUnmatchedArgsBindings:", indent);
        pw.println(unmatchedArgsBindings.isEmpty() ? " []" : "");
        for (UnmatchedArgsBinding unmatched : unmatchedArgsBindings) {
            pw.printf("%sgetter: %s%n", indent + "- ", unmatched.getter());
            pw.printf("%ssetter: %s%n", indent + "  ", unmatched.setter());
        }
    }

    private void printMixinList(Map<String, CommandSpec> mixins, PrintWriter pw, String indent) {
        pw.printf("%sMixins:", indent);
        pw.println(mixins.isEmpty() ? " []" : "");
        for (Map.Entry<String, CommandSpec> entry : mixins.entrySet()) {
            printCommandSpec(entry.getValue(), indent + "# " + entry.getKey(), pw, indent + "- ", indent + "  ");
        }
    }

    private void printSubcommandList(Map<String, CommandLine> subcommands, PrintWriter pw, String indent) {
        pw.printf("%sSubcommands:", indent);
        pw.println(subcommands.isEmpty() ? " []" : "");
        for (Map.Entry<String, CommandLine> entry : subcommands.entrySet()) {
            printCommandSpec(entry.getValue().getCommandSpec(),
                    indent + "# " + entry.getKey(), pw, indent + "- ", indent + "  ");
        }
    }

    private void printGroupList(List<ArgGroupSpec> argGroups, PrintWriter pw, String indent) {
        pw.printf("%sArgGroups:", indent);
        pw.println(argGroups.isEmpty() ? " []" : "");
        for (ArgGroupSpec group : argGroups) {
            printGroup(group, pw, indent);
        }
    }

    private void printGroup(ArgGroupSpec group, PrintWriter pw, String indent) {
        pw.printf("%ssynopsis: '%s'%n", indent + "- ", group.synopsis());
        indent += "  ";
        pw.printf("%smultiplicity: %s%n", indent, group.multiplicity());
        pw.printf("%sexclusive: %s%n", indent, group.exclusive());
        pw.printf("%sheading: %s%n", indent, group.heading());
        pw.printf("%sheadingKey: %s%n", indent, group.headingKey());
        pw.printf("%ssubgroupCount: %s%n", indent, group.subgroups().size());
        pw.printf("%sargCount: %s%n", indent, group.args().size());
    }

    private void printOptionList(List<OptionSpec> options, PrintWriter pw, String indent) {
        pw.printf("%sOptions:", indent);
        pw.println(options.isEmpty() ? " []" : "");
        for (OptionSpec option : options) {
            printOption(option, pw, indent);
        }
    }
    private void printOption(OptionSpec option, PrintWriter pw, String indent) {
        pw.printf("%snames: %s%n", indent + "- ", Arrays.toString(option.names()));
        indent += "  ";
        pw.printf("%susageHelp: %s%n", indent, option.usageHelp());
        pw.printf("%sversionHelp: %s%n", indent, option.versionHelp());
        printArg(option, pw, indent);
    }

    private void printPositionalList(List<PositionalParamSpec> positionals, PrintWriter pw, String indent) {
        pw.printf("%sPositionalParams:", indent);
        pw.println(positionals.isEmpty() ? " []" : "");
        for (PositionalParamSpec positional : positionals) {
            printPositional(positional, pw, indent);
        }
    }
    private void printPositional(PositionalParamSpec positional, PrintWriter pw, String indent) {
        pw.printf("%sindex: %s%n", indent + "- ", positional.index());
        indent += "  ";
        printArg(positional, pw, indent);
    }
    private void printArg(ArgSpec arg, PrintWriter pw, String indent) {
        pw.printf("%sdescription: %s%n", indent, Arrays.toString(arg.description()));
        pw.printf("%sdescriptionKey: '%s'%n", indent, arg.descriptionKey());
        pw.printf("%stypeInfo: %s%n", indent, arg.typeInfo());
        pw.printf("%sarity: %s%n", indent, arg.arity());
        pw.printf("%ssplitRegex: '%s'%n", indent, arg.splitRegex());
        pw.printf("%sinteractive: %s%n", indent, arg.interactive());
        pw.printf("%srequired: %s%n", indent, arg.required());
        pw.printf("%shidden: %s%n", indent, arg.hidden());
        pw.printf("%shideParamSyntax: %s%n", indent, arg.hideParamSyntax());
        pw.printf("%sdefaultValue: '%s'%n", indent, arg.defaultValue());
        pw.printf("%sshowDefaultValue: %s%n", indent, arg.showDefaultValue());
        pw.printf("%shasInitialValue: %s%n", indent, arg.hasInitialValue());
        pw.printf("%sinitialValue: '%s'%n", indent, arg.initialValue());
        pw.printf("%sparamLabel: '%s'%n", indent, arg.paramLabel());
        pw.printf("%sconverters: %s%n", indent, Arrays.toString(arg.converters()));
        pw.printf("%scompletionCandidates: %s%n", indent, iter(arg.completionCandidates()));
        pw.printf("%sgetter: %s%n", indent, arg.getter());
        pw.printf("%ssetter: %s%n", indent, arg.setter());
    }

    private String iter(Iterable<String> iterable) {
        // cannot list actual completion candidates: class cannot be instantiated at compile time
        return String.valueOf(iterable);
    }

    @Command(name = "CommandSpecYamlPrinter", mixinStandardHelpOptions = true,
            description = "Prints details of a CommandSpec")
    private static class App implements Runnable {

        @Parameters(arity = "1..*")
        Class<?>[] classes = new Class[0];

        // @Override (requires Java 6)
        public void run() {
            for (Class<?> cls : classes) {
                StringWriter sw = new StringWriter();
                new CommandSpecYamlPrinter().print(CommandSpec.forAnnotatedObject(cls), new PrintWriter(sw));
                System.out.println(sw);
            }
        }
    }
}
