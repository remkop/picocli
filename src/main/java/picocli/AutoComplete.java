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
import java.io.PrintWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import picocli.CommandLine.*;
import picocli.CommandLine.Model.PositionalParamSpec;
import picocli.CommandLine.Model.ArgSpec;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.OptionSpec;

import static java.lang.String.*;
import static picocli.CommandLine.Model.UsageMessageSpec.keyValuesMap;

/**
 * Stand-alone tool that generates bash auto-complete scripts for picocli-based command line applications.
 */
public class AutoComplete {
    /** Normal exit code of this application ({@value}). */
    public static final int EXIT_CODE_SUCCESS = 0;
    /** Exit code of this application when the specified command line arguments are invalid ({@value}). */
    public static final int EXIT_CODE_INVALID_INPUT = 1;
    /** Exit code of this application when the specified command script exists ({@value}). */
    public static final int EXIT_CODE_COMMAND_SCRIPT_EXISTS = 2;
    /** Exit code of this application when the specified completion script exists ({@value}). */
    public static final int EXIT_CODE_COMPLETION_SCRIPT_EXISTS = 3;
    /** Exit code of this application when an exception was encountered during operation ({@value}). */
    public static final int EXIT_CODE_EXECUTION_ERROR = 4;

    private AutoComplete() { }

    /**
     * Generates a bash completion script for the specified command class.
     * @param args command line options. Specify at least the {@code commandLineFQCN} mandatory parameter, which is
     *      the fully qualified class name of the annotated {@code @Command} class to generate a completion script for.
     *      Other parameters are optional. Specify {@code -h} to see details on the available options.
     */
    public static void main(String... args) {
        IExecutionExceptionHandler errorHandler = new IExecutionExceptionHandler() {
            public int handleExecutionException(Exception ex, CommandLine commandLine, ParseResult parseResult) {
                ex.printStackTrace();
                commandLine.usage(commandLine.getErr());
                return EXIT_CODE_EXECUTION_ERROR;
            }
        };
        int exitCode = new CommandLine(new App())
                .setExecutionExceptionHandler(errorHandler)
                .execute(args);
        if ((exitCode == EXIT_CODE_SUCCESS && exitOnSuccess()) || (exitCode != EXIT_CODE_SUCCESS && exitOnError())) {
            System.exit(exitCode);
        }
    }

    private static boolean exitOnSuccess() {
        return syspropDefinedAndNotFalse("picocli.autocomplete.systemExitOnSuccess");
    }

    private static boolean exitOnError() {
        return syspropDefinedAndNotFalse("picocli.autocomplete.systemExitOnError");
    }

    private static boolean syspropDefinedAndNotFalse(String key) {
        String value = System.getProperty(key);
        return value != null && !"false".equalsIgnoreCase(value);
    }

    /**
     * CLI command class for generating completion script.
     */
    @Command(name = "picocli.AutoComplete", mixinStandardHelpOptions = true,
            version = "picocli.AutoComplete " + CommandLine.VERSION, sortOptions = false,
            description = "Generates a bash completion script for the specified command class.",
            footerHeading = "%n@|bold System Properties:|@%n",
            footer = {"Set the following system properties to control the exit code of this program:",
                    " \"@|yellow picocli.autocomplete.systemExitOnSuccess|@\" - call `System.exit(0)` when",
                    "                                              execution completes normally",
                    " \"@|yellow picocli.autocomplete.systemExitOnError|@\"   - call `System.exit(ERROR_CODE)`",
                    "                                              when an error occurs",
                    "If these system properties are not defined or have value \"false\", this program completes without terminating the JVM."
            },
            exitCodeListHeading = "%nExit Codes:%n",
            exitCodeList = {
                    "0:Successful program execution",
                    "1:Usage error: user input for the command was incorrect, " +
                            "e.g., the wrong number of arguments, a bad flag, " +
                            "a bad syntax in a parameter, etc.",
                    "2:The specified command script exists (Specify --force to overwrite).",
                    "3:The specified completion script exists (Specify --force to overwrite).",
                    "4:An exception occurred while generating the completion script."
            },
            exitCodeOnInvalidInput = EXIT_CODE_INVALID_INPUT,
            exitCodeOnExecutionException = EXIT_CODE_EXECUTION_ERROR)
    private static class App implements Callable<Integer> {

        @Parameters(arity = "1", description = "Fully qualified class name of the annotated " +
                "@Command class to generate a completion script for.")
        String commandLineFQCN;

        @Option(names = {"-c", "--factory"}, description = "Optionally specify the fully qualified class name of the custom factory to use to instantiate the command class. " +
                "When omitted, the default picocli factory is used.")
        String factoryClass;

        @Option(names = {"-n", "--name"}, description = "Optionally specify the name of the command to create a completion script for. " +
                "When omitted, the annotated class @Command 'name' attribute is used. " +
                "If no @Command 'name' attribute exists, '<CLASS-SIMPLE-NAME>' (in lower-case) is used.")
        String commandName;

        @Option(names = {"-o", "--completionScript"},
                description = "Optionally specify the path of the completion script file to generate. " +
                        "When omitted, a file named '<commandName>_completion' " +
                        "is generated in the current directory.")
        File autoCompleteScript;

        @Option(names = {"-w", "--writeCommandScript"},
                description = "Write a '<commandName>' sample command script to the same directory " +
                        "as the completion script.")
        boolean writeCommandScript;

        @Option(names = {"-f", "--force"}, description = "Overwrite existing script files.")
        boolean overwriteIfExists;

        @Spec CommandSpec spec;

        public Integer call() throws Exception {
            IFactory factory = CommandLine.defaultFactory();
            if (factoryClass != null) {
                factory = (IFactory) factory.create(Class.forName(factoryClass));
            }
            Class<?> cls = Class.forName(commandLineFQCN);
            Object instance = factory.create(cls);
            CommandLine commandLine = new CommandLine(instance, factory);

            if (commandName == null) {
                commandName = commandLine.getCommandName(); //new CommandLine.Help(commandLine.commandDescriptor).commandName;
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
            if (commandScript != null && !overwriteIfExists && checkExists(commandScript)) {
                return EXIT_CODE_COMMAND_SCRIPT_EXISTS;
            }
            if (!overwriteIfExists && checkExists(autoCompleteScript)) {
                return EXIT_CODE_COMPLETION_SCRIPT_EXISTS;
            }

            AutoComplete.bash(commandName, autoCompleteScript, commandScript, commandLine);
            return EXIT_CODE_SUCCESS;
        }

        private boolean checkExists(final File file) {
            if (file.exists()) {
                PrintWriter err = spec.commandLine().getErr();
                err.printf("ERROR: picocli.AutoComplete: %s exists. Specify --force to overwrite.%n", file.getAbsolutePath());
                spec.commandLine().usage(err);
                return true;
            }
            return false;
        }
    }

    /**
     * Command that generates a Bash/ZSH completion script for its parent command (which should be the top-level command).
     * <p>
     * This class can be used as a subcommand for the top-level command in your application.
     * Users can then install completion for the top-level command by running the following command:
     * </p><pre>
     * source &lt;(top-level-command generate-completion)
     * </pre>
     * @since 4.1
     */
    @Command(name = "generate-completion", version = "generate-completion " + CommandLine.VERSION,
            mixinStandardHelpOptions = true,
            description = {
                "Generate bash/zsh completion script for ${PARENT-COMMAND-NAME}.",
                "Run the following command to give `${PARENT-COMMAND-NAME}` TAB completion in the current shell:",
                "",
                "source <(${PARENT-COMMAND-NAME} ${COMMAND-NAME})",
                ""},
            optionListHeading = "Options:%n",
            helpCommand = true
    )
    public static class GenerateCompletion implements Runnable {

        @Spec CommandLine.Model.CommandSpec spec;

        public void run() {
            String script = AutoComplete.bash(
                    spec.parent().name(),
                    spec.parent().commandLine());
            // not PrintWriter.println: scripts with Windows line separators fail in strange ways!
            spec.commandLine().getOut().print(script);
            spec.commandLine().getOut().print('\n');
            spec.commandLine().getOut().flush();
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
            } else if (Character.isSpaceChar(c)) {
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
    private static class BooleanArgFilter implements Predicate<ArgSpec> {
        public boolean test(ArgSpec f) {
            return f.type() == Boolean.TYPE || f.type() == Boolean.class;
        }
    }
    private static <T> Predicate<T> negate(final Predicate<T> original) {
        return new Predicate<T>() {
            public boolean test(T t) {
                return !original.test(t);
            }
        };
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
        CommandDescriptor(String functionName, String commandName) {
            this.functionName = functionName;
            this.commandName = commandName;
        }
        public int hashCode() { return functionName.hashCode() * 37 + commandName.hashCode(); }
        public boolean equals(Object obj) {
            if (!(obj instanceof CommandDescriptor)) { return false; }
            if (obj == this) { return true; }
            CommandDescriptor other = (CommandDescriptor) obj;
            return other.functionName.equals(functionName) && other.commandName.equals(commandName);
        }
    }

    private static final String HEADER = "" +
            "#!/usr/bin/env bash\n" +
            "#\n" +
            "# %1$s Bash Completion\n" +
            "# =======================\n" +
            "#\n" +
            "# Bash completion support for the `%1$s` command,\n" +
            "# generated by [picocli](http://picocli.info/) version %2$s.\n" +
            "#\n" +
            "# Installation\n" +
            "# ------------\n" +
            "#\n" +
            "# 1. Source all completion scripts in your .bash_profile\n" +
            "#\n" +
            "#   cd $YOUR_APP_HOME/bin\n" +
            "#   for f in $(find . -name \"*_completion\"); do line=\". $(pwd)/$f\"; grep \"$line\" ~/.bash_profile || echo \"$line\" >> ~/.bash_profile; done\n" +
            "#\n" +
            "# 2. Open a new bash console, and type `%1$s [TAB][TAB]`\n" +
            "#\n" +
            "# 1a. Alternatively, if you have [bash-completion](https://github.com/scop/bash-completion) installed:\n" +
            "#     Place this file in a `bash-completion.d` folder:\n" +
            "#\n" +
            "#   * /etc/bash-completion.d\n" +
            "#   * /usr/local/etc/bash-completion.d\n" +
            "#   * ~/bash-completion.d\n" +
            "#\n" +
            "# Documentation\n" +
            "# -------------\n" +
            "# The script is called by bash whenever [TAB] or [TAB][TAB] is pressed after\n" +
            "# '%1$s (..)'. By reading entered command line parameters,\n" +
            "# it determines possible bash completions and writes them to the COMPREPLY variable.\n" +
            "# Bash then completes the user input if only one entry is listed in the variable or\n" +
            "# shows the options if more than one is listed in COMPREPLY.\n" +
            "#\n" +
            "# References\n" +
            "# ----------\n" +
            "# [1] http://stackoverflow.com/a/12495480/1440785\n" +
            "# [2] http://tiswww.case.edu/php/chet/bash/FAQ\n" +
            "# [3] https://www.gnu.org/software/bash/manual/html_node/The-Shopt-Builtin.html\n" +
            "# [4] http://zsh.sourceforge.net/Doc/Release/Options.html#index-COMPLETE_005fALIASES\n" +
            "# [5] https://stackoverflow.com/questions/17042057/bash-check-element-in-array-for-elements-in-another-array/17042655#17042655\n" +
            "# [6] https://www.gnu.org/software/bash/manual/html_node/Programmable-Completion.html#Programmable-Completion\n" +
            "# [7] https://stackoverflow.com/questions/3249432/can-a-bash-tab-completion-script-be-used-in-zsh/27853970#27853970\n" +
            "#\n" +
            "\n" +
            "if [ -n \"$BASH_VERSION\" ]; then\n" +
            "  # Enable programmable completion facilities when using bash (see [3])\n" +
            "  shopt -s progcomp\n" +
            "elif [ -n \"$ZSH_VERSION\" ]; then\n" +
            "  # Make alias a distinct command for completion purposes when using zsh (see [4])\n" +
            "  setopt COMPLETE_ALIASES\n" +
            "  alias compopt=complete\n" +
            "\n" +
            "  # Enable bash completion in zsh (see [7])\n" +
            "  autoload -U +X compinit && compinit\n" +
            "  autoload -U +X bashcompinit && bashcompinit\n" +
            "fi\n" +
            "\n" +
            "# ArrContains takes two arguments, both of which are the name of arrays.\n" +
            "# It creates a temporary hash from lArr1 and then checks if all elements of lArr2\n" +
            "# are in the hashtable.\n" +
            "#\n" +
            "# Returns zero (no error) if all elements of the 2nd array are in the 1st array,\n" +
            "# otherwise returns 1 (error).\n" +
            "#\n" +
            "# Modified from [5]\n" +
            "function ArrContains() {\n" +
            "  local lArr1 lArr2\n" +
            "  declare -A tmp\n" +
            "  eval lArr1=(\"\\\"\\${$1[@]}\\\"\")\n" +
            "  eval lArr2=(\"\\\"\\${$2[@]}\\\"\")\n" +
            "  for i in \"${lArr1[@]}\";{ [ -n \"$i\" ] && ((++tmp[$i]));}\n" +
            "  for i in \"${lArr2[@]}\";{ [ -n \"$i\" ] && [ -z \"${tmp[$i]}\" ] && return 1;}\n" +
            "  return 0\n" +
            "}\n" +
            "\n";

    private static final String FOOTER = "" +
            "\n" +
            "# Define a completion specification (a compspec) for the\n" +
            "# `%1$s`, `%1$s.sh`, and `%1$s.bash` commands.\n" +
            "# Uses the bash `complete` builtin (see [6]) to specify that shell function\n" +
            "# `_complete_%1$s` is responsible for generating possible completions for the\n" +
            "# current word on the command line.\n" +
            "# The `-o default` option means that if the function generated no matches, the\n" +
            "# default Bash completions and the Readline default filename completions are performed.\n" +
            "complete -F _complete_%1$s -o default %1$s %1$s.sh %1$s.bash\n";

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
                        "#!/usr/bin/env bash\n" +
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
        if (scriptName == null)  { throw new NullPointerException("scriptName"); }
        if (commandLine == null) { throw new NullPointerException("commandLine"); }
        StringBuilder result = new StringBuilder();
        result.append(format(HEADER, scriptName, CommandLine.VERSION));

        Map<CommandDescriptor, CommandLine> function2command = new LinkedHashMap<CommandDescriptor, CommandLine>();
        result.append(generateEntryPointFunction(scriptName, commandLine, function2command));

        for (Map.Entry<CommandDescriptor, CommandLine> functionSpec : function2command.entrySet()) {
            CommandDescriptor descriptor = functionSpec.getKey();
            result.append(generateFunctionForCommand(descriptor.functionName, descriptor.commandName, functionSpec.getValue()));
        }
        result.append(format(FOOTER, scriptName));
        return result.toString();
    }

    private static String generateEntryPointFunction(String scriptName,
                                                     CommandLine commandLine,
                                                     Map<CommandDescriptor, CommandLine> function2command) {
        String HEADER = "" +
                "# Bash completion entry point function.\n" +
                "# _complete_%1$s finds which commands and subcommands have been specified\n" +
                "# on the command line and delegates to the appropriate function\n" +
                "# to generate possible options and subcommands for the last specified subcommand.\n" +
                "function _complete_%1$s() {\n" +
//                "  CMDS1=(%1$s gettingstarted)\n" +
//                "  CMDS2=(%1$s tool)\n" +
//                "  CMDS3=(%1$s tool sub1)\n" +
//                "  CMDS4=(%1$s tool sub2)\n" +
//                "\n" +
//                "  ArrContains COMP_WORDS CMDS4 && { _picocli_basic_tool_sub2; return $?; }\n" +
//                "  ArrContains COMP_WORDS CMDS3 && { _picocli_basic_tool_sub1; return $?; }\n" +
//                "  ArrContains COMP_WORDS CMDS2 && { _picocli_basic_tool; return $?; }\n" +
//                "  ArrContains COMP_WORDS CMDS1 && { _picocli_basic_gettingstarted; return $?; }\n" +
//                "  _picocli_%1$s; return $?;\n" +
//                "}\n" +
//                "\n" +
//                "complete -F _complete_%1$s %1$s\n" +
//                "\n";
                "";
        String FOOTER = "\n" +
                "  # No subcommands were specified; generate completions for the top-level command.\n" +
                "  _picocli_%1$s; return $?;\n" +
                "}\n";

        StringBuilder buff = new StringBuilder(1024);
        buff.append(format(HEADER, scriptName));

        List<String> predecessors = new ArrayList<String>();
        List<String> functionCallsToArrContains = new ArrayList<String>();

        function2command.put(new CommandDescriptor("_picocli_" + scriptName, scriptName), commandLine);
        generateFunctionCallsToArrContains(scriptName, predecessors, commandLine, buff, functionCallsToArrContains, function2command);

        buff.append("\n");
        Collections.reverse(functionCallsToArrContains);
        for (String func : functionCallsToArrContains) {
            buff.append(func);
        }
        buff.append(format(FOOTER, scriptName));
        return buff.toString();
    }

    private static void generateFunctionCallsToArrContains(String scriptName,
                                                           List<String> predecessors,
                                                           CommandLine commandLine,
                                                           StringBuilder buff,
                                                           List<String> functionCalls,
                                                           Map<CommandDescriptor, CommandLine> function2command) {

        // breadth-first: generate command lists and function calls for predecessors + each subcommand
        for (Map.Entry<String, CommandLine> entry : commandLine.getSubcommands().entrySet()) {
            int count = functionCalls.size();
            String functionName = "_picocli_" + scriptName + "_" + concat("_", predecessors, entry.getKey(), new Bashify());
            functionCalls.add(format("  ArrContains COMP_WORDS CMDS%2$d && { %1$s; return $?; }\n", functionName, count));
            buff.append(      format("  CMDS%2$d=(%1$s)\n", concat(" ", predecessors, entry.getKey(), new NullFunction()), count));

            // remember the function name and associated subcommand so we can easily generate a function later
            function2command.put(new CommandDescriptor(functionName, entry.getKey()), entry.getValue());
        }

        // then recursively do the same for all nested subcommands
        for (Map.Entry<String, CommandLine> entry : commandLine.getSubcommands().entrySet()) {
            predecessors.add(entry.getKey());
            generateFunctionCallsToArrContains(scriptName, predecessors, entry.getValue(), buff, functionCalls, function2command);
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

    private static String generateFunctionForCommand(String functionName, String commandName, CommandLine commandLine) {
        String HEADER = "" +
                "\n" +
                "# Generates completions for the options and subcommands of the `%s` %scommand.\n" +
                "function %s() {\n" +
                "  # Get completion data\n" +
                "  CURR_WORD=${COMP_WORDS[COMP_CWORD]}\n" +
                "  PREV_WORD=${COMP_WORDS[COMP_CWORD-1]}\n" +
                "\n" +
                "  COMMANDS=\"%s\"\n" +  // COMMANDS="gettingstarted tool"
                "  FLAG_OPTS=\"%s\"\n" + // FLAG_OPTS="--verbose -V -x --extract -t --list"
                "  ARG_OPTS=\"%s\"\n";   // ARG_OPTS="--host --option --file -f -u --timeUnit"

        String FOOTER = "" +
                "\n" +
                "  if [[ \"${CURR_WORD}\" == -* ]]; then\n" +
                "    COMPREPLY=( $(compgen -W \"${FLAG_OPTS} ${ARG_OPTS}\" -- ${CURR_WORD}) )\n" +
                "  else\n" +
                "    COMPREPLY=( $(compgen -W \"${COMMANDS}\" -- ${CURR_WORD}) )\n" +
                "  fi\n" +
                "}\n";

        // Get the fields annotated with @Option and @Parameters for the specified CommandLine.
        CommandSpec commandSpec = commandLine.getCommandSpec();

        // Build a list of "flag" options that take no parameters and "arg" options that do take parameters, and subcommands.
        String flagOptionNames = optionNames(filter(commandSpec.options(), new BooleanArgFilter()));
        List<OptionSpec> argOptionFields = filter(commandSpec.options(), negate(new BooleanArgFilter()));
        String argOptionNames = optionNames(argOptionFields);
        String commands = concat(" ", new ArrayList<String>(commandLine.getSubcommands().keySet())).trim();

        // Generate the header: the function declaration, CURR_WORD, PREV_WORD and COMMANDS, FLAG_OPTS and ARG_OPTS.
        StringBuilder buff = new StringBuilder(1024);
        String sub = functionName.equals("_picocli_" + commandName) ? "" : "sub";
        buff.append(format(HEADER, commandName, sub, functionName, commands, flagOptionNames, argOptionNames));

        // Generate completion lists for options with a known set of valid values (including java enums)
        for (OptionSpec f : commandSpec.options()) {
            if (f.completionCandidates() != null) {
                generateCompletionCandidates(buff, f);
            }
        }
        // TODO generate completion lists for other option types:
        // Charset, Currency, Locale, TimeZone, ByteOrder,
        // javax.crypto.Cipher, javax.crypto.KeyGenerator, javax.crypto.Mac, javax.crypto.SecretKeyFactory
        // java.security.AlgorithmParameterGenerator, java.security.AlgorithmParameters, java.security.KeyFactory, java.security.KeyPairGenerator, java.security.KeyStore, java.security.MessageDigest, java.security.Signature
        // sql.Types?

        // Now generate the "case" switches for the options whose arguments we can generate completions for
        buff.append(generateOptionsSwitch(argOptionFields));

        // Generate the footer: a default COMPREPLY to fall back to, and the function closing brace.
        buff.append(format(FOOTER));
        return buff.toString();
    }

    private static void generateCompletionCandidates(StringBuilder buff, OptionSpec f) {
        buff.append(format("  %s_OPTION_ARGS=\"%s\" # %s values\n",
                bashify(f.paramLabel()),
                concat(" ", extract(f.completionCandidates())).trim(),
                f.longestName()));
    }
    private static List<String> extract(Iterable<String> generator) {
        List<String> result = new ArrayList<String>();
        for (String e : generator) {
            result.add(e);
        }
        return result;
    }

    private static String generateOptionsSwitch(List<OptionSpec> argOptions) {
        String optionsCases = generateOptionsCases(argOptions, "", "${CURR_WORD}");

        if (optionsCases.length() == 0) {
            return "";
        }

        StringBuilder buff = new StringBuilder(1024);
        buff.append("\n");
        buff.append("  compopt +o default\n");
        buff.append("\n");
        buff.append("  case ${PREV_WORD} in\n");
        buff.append(optionsCases);
        buff.append("  esac\n");
        return buff.toString();
    }

    private static String generateOptionsCases(List<OptionSpec> argOptionFields, String indent, String currWord) {
        StringBuilder buff = new StringBuilder(1024);
        for (OptionSpec option : argOptionFields) {
            if (option.completionCandidates() != null) {
                buff.append(format("%s    %s)\n", indent, concat("|", option.names()))); // "    -u|--timeUnit)\n"
                buff.append(format("%s      COMPREPLY=( $( compgen -W \"${%s_OPTION_ARGS}\" -- %s ) )\n", indent, bashify(option.paramLabel()), currWord));
                buff.append(format("%s      return $?\n", indent));
                buff.append(format("%s      ;;\n", indent));
            } else if (option.type().equals(File.class) || "java.nio.file.Path".equals(option.type().getName())) {
                buff.append(format("%s    %s)\n", indent, concat("|", option.names()))); // "    -f|--file)\n"
                buff.append(format("%s      compopt -o filenames\n", indent));
                buff.append(format("%s      COMPREPLY=( $( compgen -f -- %s ) ) # files\n", indent, currWord));
                buff.append(format("%s      return $?\n", indent));
                buff.append(format("%s      ;;\n", indent));
            } else if (option.type().equals(InetAddress.class)) {
                buff.append(format("%s    %s)\n", indent, concat("|", option.names()))); // "    -h|--host)\n"
                buff.append(format("%s      compopt -o filenames\n", indent));
                buff.append(format("%s      COMPREPLY=( $( compgen -A hostname -- %s ) )\n", indent, currWord));
                buff.append(format("%s      return $?\n", indent));
                buff.append(format("%s      ;;\n", indent));
            } else {
                buff.append(format("%s    %s)\n", indent, concat("|", option.names()))); // no completions available
                buff.append(format("%s      return\n", indent));
                buff.append(format("%s      ;;\n", indent));
            }
        }
        return buff.toString();
    }

    private static String optionNames(List<OptionSpec> options) {
        List<String> result = new ArrayList<String>();
        for (OptionSpec option : options) {
            result.addAll(Arrays.asList(option.names()));
        }
        return concat(" ", result, "", new NullFunction()).trim();
    }

    public static int complete(CommandSpec spec, String[] args, int argIndex, int positionInArg, int cursor, List<CharSequence> candidates) {
        if (spec == null)       { throw new NullPointerException("spec is null"); }
        if (args == null)       { throw new NullPointerException("args is null"); }
        if (candidates == null) { throw new NullPointerException("candidates list is null"); }
        if (argIndex == args.length) {
            String[] copy = new String[args.length + 1];
            System.arraycopy(args, 0, copy, 0, args.length);
            args = copy;
            args[argIndex] = "";
        }
        if (argIndex < 0      || argIndex >= args.length)                 { throw new IllegalArgumentException("Invalid argIndex " + argIndex + ": args array only has " + args.length + " elements."); }
        if (positionInArg < 0 || positionInArg > args[argIndex].length()) { throw new IllegalArgumentException("Invalid positionInArg " + positionInArg + ": args[" + argIndex + "] (" + args[argIndex] + ") only has " + args[argIndex].length() + " characters."); }

        String currentArg = args[argIndex];
        boolean reset = spec.parser().collectErrors();
        try {
            String committedPrefix = currentArg.substring(0, positionInArg);

            spec.parser().collectErrors(true);
            CommandLine parser = new CommandLine(spec);
            ParseResult parseResult = parser.parseArgs(args);
            if (argIndex >= parseResult.tentativeMatch.size()) {
                Object startPoint = findCompletionStartPoint(parseResult);
                addCandidatesForArgsFollowing(startPoint, candidates);
            } else {
                Object obj = parseResult.tentativeMatch.get(argIndex);
                if (obj instanceof CommandSpec) { // subcommand
                    addCandidatesForArgsFollowing(((CommandSpec) obj).parent(), candidates);

                } else if (obj instanceof OptionSpec) { // option
                    int sep = currentArg.indexOf(spec.parser().separator());
                    if (sep < 0 || positionInArg < sep) { // no '=' or cursor before '='
                        addCandidatesForArgsFollowing(findCommandFor((OptionSpec) obj, spec), candidates);
                    } else {
                        addCandidatesForArgsFollowing((OptionSpec) obj, candidates);

                        int sepLength = spec.parser().separator().length();
                        if (positionInArg < sep + sepLength) {
                            int posInSeparator = positionInArg - sep;
                            String prefix = spec.parser().separator().substring(posInSeparator);
                            for (int i = 0; i < candidates.size(); i++) {
                                candidates.set(i, prefix + candidates.get(i));
                            }
                            committedPrefix = currentArg.substring(sep, positionInArg);
                        } else {
                            committedPrefix = currentArg.substring(sep + sepLength, positionInArg);
                        }
                    }

                } else if (obj instanceof PositionalParamSpec) { // positional
                    //addCandidatesForArgsFollowing(obj, candidates);
                    addCandidatesForArgsFollowing(findCommandFor((PositionalParamSpec) obj, spec), candidates);

                } else {
                    int i = argIndex - 1;
                    while (i > 0 && !isPicocliModelObject(parseResult.tentativeMatch.get(i))) {i--;}
                    if (i < 0) { return -1; }
                    addCandidatesForArgsFollowing(parseResult.tentativeMatch.get(i), candidates);
                }
            }
            filterAndTrimMatchingPrefix(committedPrefix, candidates);
            return candidates.isEmpty() ? -1 : cursor;
        } finally {
            spec.parser().collectErrors(reset);
        }
    }
    private static Object findCompletionStartPoint(ParseResult parseResult) {
        List<Object> tentativeMatches = parseResult.tentativeMatch;
        for (int i = 1; i <= tentativeMatches.size(); i++) {
            Object found = tentativeMatches.get(tentativeMatches.size() - i);
            if (found instanceof CommandSpec) {
                return found;
            }
            if (found instanceof ArgSpec) {
                CommandLine.Range arity = ((ArgSpec) found).arity();
                if (i < arity.min()) {
                    return found; // not all parameters have been supplied yet
                } else {
                    return findCommandFor((ArgSpec) found, parseResult.commandSpec());
                }
            }
        }
        return parseResult.commandSpec();
    }

    private static CommandSpec findCommandFor(ArgSpec arg, CommandSpec cmd) {
        return (arg instanceof OptionSpec) ? findCommandFor((OptionSpec) arg, cmd) : findCommandFor((PositionalParamSpec) arg, cmd);
    }
    private static CommandSpec findCommandFor(OptionSpec option, CommandSpec commandSpec) {
        for (OptionSpec defined : commandSpec.options()) {
            if (defined == option) { return commandSpec; }
        }
        for (CommandLine sub : commandSpec.subcommands().values()) {
            CommandSpec result = findCommandFor(option, sub.getCommandSpec());
            if (result != null) { return result; }
        }
        return null;
    }
    private static CommandSpec findCommandFor(PositionalParamSpec positional, CommandSpec commandSpec) {
        for (PositionalParamSpec defined : commandSpec.positionalParameters()) {
            if (defined == positional) { return commandSpec; }
        }
        for (CommandLine sub : commandSpec.subcommands().values()) {
            CommandSpec result = findCommandFor(positional, sub.getCommandSpec());
            if (result != null) { return result; }
        }
        return null;
    }
    private static boolean isPicocliModelObject(Object obj) {
        return obj instanceof CommandSpec || obj instanceof OptionSpec || obj instanceof PositionalParamSpec;
    }

    private static void filterAndTrimMatchingPrefix(String prefix, List<CharSequence> candidates) {
        List<CharSequence> replace = new ArrayList<CharSequence>();
        for (CharSequence seq : candidates) {
            if (seq.toString().startsWith(prefix)) {
                replace.add(seq.subSequence(prefix.length(), seq.length()));
            }
        }
        candidates.clear();
        candidates.addAll(replace);
    }
    private static void addCandidatesForArgsFollowing(Object obj, List<CharSequence> candidates) {
        if (obj == null) { return; }
        if (obj instanceof CommandSpec) {
            addCandidatesForArgsFollowing((CommandSpec) obj, candidates);
        } else if (obj instanceof OptionSpec) {
            addCandidatesForArgsFollowing((OptionSpec) obj, candidates);
        } else if (obj instanceof PositionalParamSpec) {
            addCandidatesForArgsFollowing((PositionalParamSpec) obj, candidates);
        }
    }
    private static void addCandidatesForArgsFollowing(CommandSpec commandSpec, List<CharSequence> candidates) {
        if (commandSpec == null) { return; }
        for (Map.Entry<String, CommandLine> entry : commandSpec.subcommands().entrySet()) {
            candidates.add(entry.getKey());
            candidates.addAll(Arrays.asList(entry.getValue().getCommandSpec().aliases()));
        }
        candidates.addAll(commandSpec.optionsMap().keySet());
        for (PositionalParamSpec positional : commandSpec.positionalParameters()) {
            addCandidatesForArgsFollowing(positional, candidates);
        }
    }
    private static void addCandidatesForArgsFollowing(OptionSpec optionSpec, List<CharSequence> candidates) {
        if (optionSpec != null) {
            addCompletionCandidates(optionSpec.completionCandidates(), candidates);
        }
    }
    private static void addCandidatesForArgsFollowing(PositionalParamSpec positionalSpec, List<CharSequence> candidates) {
        if (positionalSpec != null) {
            addCompletionCandidates(positionalSpec.completionCandidates(), candidates);
        }
    }
    private static void addCompletionCandidates(Iterable<String> completionCandidates, List<CharSequence> candidates) {
        if (completionCandidates != null) {
            for (String candidate : completionCandidates) { candidates.add(candidate); }
        }
    }
}
