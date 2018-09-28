/*
   Copyright 2017 Remko Popma

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package picocli;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import picocli.CommandLine.Command;
import picocli.CommandLine.Model.PositionalParamSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Model.ArgSpec;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.OptionSpec;
import picocli.CommandLine.Range;

import static java.lang.String.*;
import static java.util.Locale.ENGLISH;

/**
 * Stand-alone tool that generates bash auto-complete scripts for picocli-based command line applications.
 */
public class BashCompletionAutoComplete {
    private BashCompletionAutoComplete() { }

    /**
     * Generates a bash completion script for the specified command class.
     * @param args command line options. Specify at least the {@code commandLineFQCN} mandatory parameter, which is
     *      the fully qualified class name of the annotated {@code @Command} class to generate a completion script for.
     *      Other parameters are optional. Specify {@code -h} to see details on the available options.
     */
    public static void main(String... args) { CommandLine.run(new App(), System.err, args); }

    /**
     * CLI command class for generating completion script.
     */
    @Command(name = "picocli.BashCompletionAutoComplete", sortOptions = false,
            description = "Generates a bash completion script for the specified command class.")
    private static class App implements Runnable {

        @Parameters(arity = "1", description = "Fully qualified class name of the annotated " +
                "@Command class to generate a completion script for.")
        String commandLineFQCN;

        @Option(names = {"-n", "--name"}, description = "Name of the command to create a completion script for. " +
                "When omitted, the annotated class @Command 'name' attribute is used. " +
                "If no @Command 'name' attribute exists, '<CLASS-SIMPLE-NAME>' (in lower-case) is used.")
        String commandName;

        @Option(names = {"-o", "--completionScript"},
                description = "Path of the completion script file to generate. " +
                        "When omitted, a file named '<commandName>_completion' " +
                        "is generated in the current directory.")
        File autoCompleteScript;

        @Option(names = {"-w", "--writeCommandScript"},
                description = "Write a '<commandName>' sample command script to the same directory " +
                        "as the completion script.")
        boolean writeCommandScript;

        @Option(names = {"-f", "--force"}, description = "Overwrite existing script files.")
        boolean overwriteIfExists;

        @Option(names = { "-h", "--help"}, usageHelp = true, description = "Display this help message and quit.")
        boolean usageHelpRequested;

        public void run() {
            try {
                Class<?> cls = Class.forName(commandLineFQCN);
                Object instance = CommandLine.defaultFactory().create(cls);
                CommandLine commandLine = new CommandLine(instance);

                if (commandName == null) {
                    commandName = commandLine.getCommandName();
                    if (CommandLine.Help.DEFAULT_COMMAND_NAME.equals(commandName)) {
                        commandName = cls.getSimpleName().toLowerCase();
                    }
                }
                if (autoCompleteScript == null) {
                    autoCompleteScript = new File(commandName + "_completion");
                }
                File commandScript = null;
                if (writeCommandScript) {
                    commandScript = new File(autoCompleteScript.getAbsoluteFile().getParentFile(), commandName);
                }
                if (commandScript != null && !overwriteIfExists && checkExists(commandScript)) { return; }
                if (!overwriteIfExists && checkExists(autoCompleteScript)) { return; }

                bash(commandName, autoCompleteScript, commandScript, commandLine);

            } catch (Exception ex) {
                ex.printStackTrace();
                CommandLine.usage(new App(), System.err);
            }
        }

        private boolean checkExists(final File file) {
            if (file.exists()) {
                System.err.println(file.getAbsolutePath() + " exists. Specify -f to overwrite.");
                CommandLine.usage(this, System.err);
                return true;
            }
            return false;
        }
    }

    private static interface Function<T, V> {
        V apply(T t);
    }

    /**
     * Drops all characters that are not valid for bash function and identifier names.
     */
    private static class Bashify implements Function<CharSequence, String> {
        public String apply(CharSequence value) {
            return bashify(value);
        }
    }
    private static String bashify(CharSequence value) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);

            if (Character.isLetterOrDigit(c) || c == '_') {
                builder.append(c);
            } else if (Character.isSpaceChar(c) || c == '-') {
                builder.append('_');
            }
        }

        return builder.toString();
    }

    private static class NullFunction implements Function<CharSequence, String> {
        public String apply(CharSequence value) { return value.toString(); }
    }

    private static interface Predicate<T> {
        boolean test(T t);
    }
    private static class EnumArgFilter implements Predicate<ArgSpec> {
        public boolean test(ArgSpec f) {
            return f.type().isEnum();
        }
    }
    private static class HasCompletions implements Predicate<ArgSpec> {
        public boolean test(ArgSpec f) {
            return f.completionCandidates() != null;
        }
    }
    private static <K, T extends K> List<T> filter(List<T> list, Predicate<K> filter) {
        List<T> result = new ArrayList<T>();
        for (T t : list) { if (filter.test(t)) { result.add(t); } }
        return result;
    }
    /** Package-private for tests; consider this class private. */
    static class CommandDescriptor {
        final String functionName;
        final String commandName;
        final int depth;
        CommandDescriptor(String functionName, String commandName, int depth) {
            this.functionName = functionName;
            this.commandName = commandName;
            this.depth = depth;
        }
        public int hashCode() { return functionName.hashCode() * 37 + commandName.hashCode(); }
        public boolean equals(Object obj) {
            if (!(obj instanceof CommandDescriptor)) { return false; }
            if (obj == this) { return true; }
            CommandDescriptor other = (CommandDescriptor) obj;
            return other.functionName.equals(functionName) && other.commandName.equals(commandName);
        }
    }

    /**
     * Generates source code for an autocompletion bash script for the specified picocli-based application,
     * and writes this script to the specified {@code out} file, and optionally writes an invocation script
     * to the specified {@code command} file.
     * @param scriptName the name of the command to generate a bash autocompletion script for
     * @param commandLine the {@code CommandLine} instance for the command line application
     * @param out the file to write the autocompletion bash script source code to
     * @param command the file to write a helper script to that invokes the command, or {@code null} if no helper script file should be written
     * @throws IOException if a problem occurred writing to the specified files
     */
    public static void bash(String scriptName, File out, File command, CommandLine commandLine) throws IOException {
        String autoCompleteScript = bash(scriptName, commandLine);
        Writer completionWriter = null;
        Writer scriptWriter = null;
        try {
            completionWriter = new FileWriter(out);
            completionWriter.write(autoCompleteScript);

            if (command != null) {
                scriptWriter = new FileWriter(command);
                scriptWriter.write("" +
                        "#!/usr/bin/bash\n" +
                        "\n" +
                        "LIBS=path/to/libs\n" +
                        "CP=\"${LIBS}/myApp.jar\"\n" +
                        "java -cp \"${CP}\" '" + commandLine.getCommand().getClass().getName() + "' $@");
            }
        } finally {
            if (completionWriter != null) { completionWriter.close(); }
            if (scriptWriter != null)     { scriptWriter.close(); }
        }
    }

    /**
     * Generates and returns the source code for an autocompletion bash script for the specified picocli-based application.
     * @param scriptName the name of the command to generate a bash autocompletion script for
     * @param commandLine the {@code CommandLine} instance for the command line application
     * @return source code for an autocompletion bash script
     */
    public static String bash(String scriptName, CommandLine commandLine) {
        StringBuilder buff = new StringBuilder(1024);

        final String DESCRIPTION = "# " + scriptName + " completion";
        final int WIDTH = 79 - DESCRIPTION.length();
        final String TYPE =  "-*- shell-script -*-";
        final String FORMAT = "%s%" + (WIDTH > 0 ? WIDTH + "s" : "s");
        final String HEADER = String.format(FORMAT, DESCRIPTION, TYPE);

        final String FOOTER = "" +
                " &&\n" +
                "complete -F _%s %s\n" +
                "\n" +
                "# ex: filetype=sh\n";

        if (scriptName == null)  { throw new NullPointerException("scriptName"); }
        if (commandLine == null) { throw new NullPointerException("commandLine"); }

        buff.append(HEADER);

        Map<CommandDescriptor, CommandLine> function2command = new LinkedHashMap<CommandDescriptor, CommandLine>();
        buff.append(generateEntryPointFunction(scriptName, commandLine, function2command));

        for (Map.Entry<CommandDescriptor, CommandLine> functionSpec : function2command.entrySet()) {
            CommandDescriptor descriptor = functionSpec.getKey();
            buff.append(generateFunctionForCommand(bashify(descriptor.functionName), descriptor.commandName, functionSpec.getValue(), descriptor.depth));
        }

        buff.append(format(FOOTER, bashify(scriptName), scriptName));

        return buff.toString();
    }

    private static String generateEntryPointFunction(String scriptName,
                                                     CommandLine commandLine,
                                                     Map<CommandDescriptor, CommandLine> function2command) {
        StringBuilder buff = new StringBuilder(1024);

        List<String> predecessors = new ArrayList<String>();
        List<String> functionCallsToArrContains = new ArrayList<String>();

        function2command.put(new CommandDescriptor("_" + scriptName, scriptName, 0), commandLine);
        generateFunctionCallsToArrContains(scriptName, predecessors, commandLine, buff, functionCallsToArrContains, function2command, 1);

        return "";
    }

    private static void generateFunctionCallsToArrContains(String scriptName, List<String> predecessors,
            CommandLine commandLine, StringBuilder buff, List<String> functionCalls,
            Map<CommandDescriptor, CommandLine> function2command, int depth) {
        // breadth-first: generate command lists and function calls for predecessors +
        // each subcommand
        for (Map.Entry<String, CommandLine> entry : commandLine.getSubcommands().entrySet()) {
            String functionName = "_" + scriptName + "_"
                    + concat("_", predecessors, entry.getKey(), new Bashify());
            function2command.put(new CommandDescriptor(functionName, entry.getKey(), depth), entry.getValue());
        }

        // then recursively do the same for all nested subcommands
        for (Map.Entry<String, CommandLine> entry : commandLine.getSubcommands().entrySet()) {
            predecessors.add(entry.getKey());
            generateFunctionCallsToArrContains(scriptName, predecessors, entry.getValue(), buff, functionCalls,
                    function2command, depth + predecessors.size());
            predecessors.remove(predecessors.size() - 1);
        }
    }

    private static String concat(String infix, String... values) {
        return concat(infix, Arrays.asList(values));
    }
    private static String concat(String infix, List<String> values) {
        return concat(infix, values, null, new NullFunction());
    }
    private static <V, T extends V> String concat(String infix, List<T> values, T lastValue, Function<V, String> normalize) {
        StringBuilder sb = new StringBuilder();
        for (T val : values) {
            if (sb.length() > 0) { sb.append(infix); }
            sb.append(normalize.apply(val));
        }
        if (lastValue == null) { return sb.toString(); }
        if (sb.length() > 0) { sb.append(infix); }
        return sb.append(normalize.apply(lastValue)).toString();
    }

    private static String generateFunctionForCommand(String functionName, String commandName, CommandLine commandLine, int depth) {
        // FIXME: Don't generate commands and options variables if empty
        String HEADER = "\n" +
                "\n" +
                "%s()\n" +
                "{\n" +
                "    local cur prev words cword split\n" +
                "    _init_completion -s -n " + commandLine.getSeparator() + " || return\n" +
                "\n" +
                "    local commands options\n" +
                "    commands=\'%s\'\n" +
                "    options=\'%s\'\n";

        CommandSpec commandSpec = commandLine.getCommandSpec();
        List<PositionalParamSpec> enumPositionalParams = filter(commandSpec.positionalParameters(), new EnumArgFilter());

        String FOOTER = "" +
                "\n" +
                "    if [[ \"$cur\" == -* ]]; then\n" +
                "        COMPREPLY=( $(compgen -W \'${options}\' -- \"$cur\" ) )\n" +
                "        [[ $COMPREPLY == *" + commandLine.getSeparator() + " ]] && compopt -o nospace\n" +
                "    else\n" +
                         generatePositionalParametersConditionals(commandLine, enumPositionalParams, depth) +
                "    fi\n" +
                "}";

        String commands = concat(" ", new ArrayList<String>(commandLine.getSubcommands().keySet())).trim();

        StringBuilder buff = new StringBuilder(1024);
        String optionNames = optionNames(commandSpec.options(), commandLine.getSeparator());
        buff.append(format(HEADER, functionName, commands, optionNames));

        List<OptionSpec> optionsWithCompletionCandidates = filter(commandSpec.options(), new HasCompletions());

        for (OptionSpec f : optionsWithCompletionCandidates) {
            generateCompetionCandidates(buff, f);
        }

        List<PositionalParamSpec> positionalParametersWithCompletionCandidates = filter(commandSpec.positionalParameters(), new HasCompletions());

        for (PositionalParamSpec f : positionalParametersWithCompletionCandidates) {
            generateCompetionCandidates(buff, f);
        }

        buff.append(generateOptionsSwitch(commandSpec.options()));

        buff.append(generateSubcommandsSwitch(commandLine, functionName));

        buff.append(format(FOOTER));

        return buff.toString();
    }

    private static Object generateSubcommandsSwitch(CommandLine commandLine, String functionName) {
        StringBuilder buff = new StringBuilder(1024);

        Set<String> subcommands = commandLine.getSubcommands().keySet();

        if (subcommands.isEmpty()) {
            return buff.toString();
        }

        buff.append("\n");
        buff.append("    local arg\n");
        buff.append("    _get_first_arg\n");
        buff.append("\n");
        buff.append("    case $arg in\n");
        for (String subcommand : subcommands) {
            buff.append("        ");
            buff.append(subcommand);
            buff.append(")\n");
            buff.append("            ");
            buff.append(functionName);
            buff.append("_");
            buff.append(subcommand);
            buff.append("\n");
            buff.append("            return\n");
            buff.append("            ;;\n");
        }
        buff.append("    esac\n");

        return buff.toString();
    }

    private static void generateCompetionCandidates(StringBuilder buff, ArgSpec f) {
        if (f instanceof OptionSpec) {
            buff.append("\n");
            buff.append(format("    local %s_option_args\n", bashify(f.paramLabel().toLowerCase(ENGLISH))));
            buff.append(format("    %s_option_args=\"%s\"\n",
                    bashify(f.paramLabel().toLowerCase(ENGLISH)),
                    concat(" ", extract(f.completionCandidates())).trim()));
        } else if (f instanceof PositionalParamSpec) {
            buff.append("\n");
            buff.append(format("    local %s_parameter_args\n", bashify(f.paramLabel().toLowerCase(ENGLISH))));
            buff.append(format("    %s_parameter_args=\"%s\"\n",
                    bashify(f.paramLabel().toLowerCase(ENGLISH)),
                    concat(" ", extract(f.completionCandidates())).trim()));
        }
    }
    private static List<String> extract(Iterable<String> generator) {
        List<String> result = new ArrayList<String>();
        for (String e : generator) {
            result.add(e);
        }
        return result;
    }

    private static String generateOptionsSwitch(List<OptionSpec> argOptions) {
        String outerCases = generateOptionsCases(argOptions);

        if (outerCases.length() == 0) {
            return "";
        }

        StringBuilder buff = new StringBuilder(1024);

        buff.append("\n");
        buff.append("    case $prev in\n");
        buff.append(outerCases);
        buff.append("    esac\n");
        buff.append("\n");
        buff.append("    $split = && return\n");

        return buff.toString();
    }

    private static boolean isDirectory(ArgSpec f) {
        if (f instanceof OptionSpec) {
            for (String name : ((OptionSpec) f).names()) {
                String lname = name.toLowerCase(ENGLISH);

                if (lname.contains("dir")) {
                    return true;
                }
            }
        } else if (f instanceof PositionalParamSpec) {
            String pname = ((PositionalParamSpec) f).paramLabel().toLowerCase(ENGLISH);

            if (pname.contains("dir")) {
                return true;
            }
        }

        return false;
    }

    private static String generateOptionsCases(List<OptionSpec> argOptionFields) {
        StringBuilder buff = new StringBuilder(1024);

        for (OptionSpec option : argOptionFields) {
            if (option.completionCandidates() != null) {
                buff.append(format("        %s)\n", concat("|", option.names())));
                buff.append(format("            COMPREPLY=( $( compgen -W \'${%s_option_args}\' -- \"$cur\" ) )\n", bashify(option.paramLabel().toLowerCase(ENGLISH))));
                buff.append("            return\n");
                buff.append("            ;;\n");
            } else if (option.type().equals(File.class) || "java.nio.file.Path".equals(option.type().getName())) {
                buff.append(format("        %s)\n", concat("|", option.names())));
                if (isDirectory(option)) {
                    buff.append("            _filedir -d\n");
                } else {
                    buff.append("            _filedir\n");
                }
                buff.append("            return\n");
                buff.append("            ;;\n");
            } else if (option.type().equals(InetAddress.class)) {
                buff.append(format("        %s)\n", concat("|", option.names())));
                buff.append("            _known_hosts_real \"$cur\"\n");
                buff.append("            return\n");
                buff.append("            ;;\n");
            } else if (option.type().equals(NetworkInterface.class)) {
                buff.append(format("        %s)\n", concat("|", option.names())));
                buff.append("            _available_interfaces\n");
                buff.append("            return\n");
                buff.append("            ;;\n");
            } else {
                buff.append(format("        %s)\n", concat("|", option.names())));
                buff.append("            return\n");
                buff.append("            ;;\n");
            }
        }

        return buff.toString();
    }

    private static String optionNames(List<OptionSpec> options, String separator) {
        List<String> result = new ArrayList<String>();
        for (OptionSpec option : options) {
            if (separator == null) {
                result.addAll(Arrays.asList(option.names()));
            } else {
                String[] names = option.names();
                List<String> optionsList = new ArrayList<String>(names.length);
                for (String name : names) {
                    if (name.equals(option.longestName())) {
                        if (name.startsWith("--") && option.type() != Boolean.TYPE && option.type() != Boolean.class) {
                            optionsList.add(name + separator);
                        } else {
                            optionsList.add(name);
                        }
                    }
                }
                result.addAll(optionsList);
            }
        }
        return concat(" ", result , "", new NullFunction()).trim();
    }

    private static String generatePositionalParametersConditionals(CommandLine commandLine, List<PositionalParamSpec> enumPositionalParams, int depth) {
        CommandSpec commandSpec = commandLine.getCommandSpec();
        List<PositionalParamSpec> positionalParameters = commandSpec.positionalParameters();
        StringBuilder buff = new StringBuilder(1024);

        String separator = commandLine.getSeparator();

        buff.append("        local args\n");
        buff.append("        _count_args");
        buff.append(" ");
        buff.append(separator);
        buff.append("\n");

        if (!commandLine.getSubcommands().keySet().isEmpty()) {
            buff.append("\n");
            buff.append("        if (( $args == 1 )); then\n");
            buff.append("            COMPREPLY=( $(compgen -W \'${commands}\' -- \"$cur\" ) )\n");
            buff.append("        fi\n");
        }

        if (positionalParameters.isEmpty()) {
            return buff.toString();
        }

        int positionalParametersSize = positionalParameters.size();

        for (int i = 0; i < positionalParametersSize; i++) {
            PositionalParamSpec positionalParameter = positionalParameters.get(i);
            int index = i + 1 + depth;

            Range arity = positionalParameter.arity();

            buff.append("\n");
            if (arity.max == 1) {
                buff.append("        if (( $args == ");
                buff.append(index);
            } else {
                buff.append("        if (( $args >= ");
                buff.append(index);

                if (arity.max < Integer.MAX_VALUE) {
                    buff.append(" && $args <= ");
                    buff.append(index + arity.max - 1);
                }
            }

            buff.append(" )); then\n");

            Class<?> type = positionalParameter.auxiliaryTypes() != null ? positionalParameter.auxiliaryTypes()[0] : positionalParameter.type();

            if (enumPositionalParams.contains(positionalParameter) || positionalParameter.completionCandidates() != null) {
                buff.append("            COMPREPLY=( $(compgen -W \"${");
                buff.append(bashify(positionalParameter.paramLabel().toLowerCase(ENGLISH)));
                buff.append("_parameter_args}\" -- \"$cur\" ) )\n");
            } else if (type.equals(File.class) || "java.nio.file.Path".equals(type.getName())) {
                if (isDirectory(positionalParameter)) {
                    buff.append("            _filedir -d\n");
                } else {
                    buff.append("            _filedir\n");
                }
            } else if (type.equals(InetAddress.class)) {
                buff.append("            _known_hosts_real \"$cur\"\n");
            } else if (type.equals(NetworkInterface.class)) {
                buff.append("            _available_interfaces\n");
            }

            buff.append("            return\n");
            buff.append("        fi\n");
        }

        return buff.toString();
    }
}
