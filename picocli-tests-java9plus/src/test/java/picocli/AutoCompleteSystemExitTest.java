package picocli;


import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledForJreRange;
import org.junit.jupiter.api.condition.JRE;
import picocli.CommandLine.Help.Ansi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.github.stefanbirkner.systemlambda.SystemLambda.catchSystemExit;
import static com.github.stefanbirkner.systemlambda.SystemLambda.restoreSystemProperties;
import static com.github.stefanbirkner.systemlambda.SystemLambda.tapSystemErr;
import static com.github.stefanbirkner.systemlambda.SystemLambda.tapSystemOut;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AutoCompleteSystemExitTest {

    private static final String AUTO_COMPLETE_APP_USAGE = String.format("" +
        "Usage: picocli.AutoComplete [-fhVw] [-c=<factoryClass>] [-n=<commandName>]%n" +
        "                            [-o=<autoCompleteScript>] [@<filename>...]%n" +
        "                            <commandLineFQCN>%n" +
        "Generates a bash completion script for the specified command class.%n" +
        "      [@<filename>...]       One or more argument files containing options.%n" +
        "      <commandLineFQCN>      Fully qualified class name of the annotated%n" +
        "                               `@Command` class to generate a completion script%n" +
        "                               for.%n" +
        "  -c, --factory=<factoryClass>%n" +
        "                             Optionally specify the fully qualified class name%n" +
        "                               of the custom factory to use to instantiate the%n" +
        "                               command class. When omitted, the default picocli%n" +
        "                               factory is used.%n" +
        "  -n, --name=<commandName>   Optionally specify the name of the command to%n" +
        "                               create a completion script for. When omitted,%n" +
        "                               the annotated class `@Command(name = \"...\")`%n" +
        "                               attribute is used. If no `@Command(name = ...)`%n" +
        "                               attribute exists, '<CLASS-SIMPLE-NAME>' (in%n" +
        "                               lower-case) is used.%n" +
        "  -o, --completionScript=<autoCompleteScript>%n" +
        "                             Optionally specify the path of the completion%n" +
        "                               script file to generate. When omitted, a file%n" +
        "                               named '<commandName>_completion' is generated in%n" +
        "                               the current directory.%n" +
        "  -w, --writeCommandScript   Write a '<commandName>' sample command script to%n" +
        "                               the same directory as the completion script.%n" +
        "  -f, --force                Overwrite existing script files.%n" +
        "  -h, --help                 Show this help message and exit.%n" +
        "  -V, --version              Print version information and exit.%n" +
        "%n" +
        "Exit Codes:%n" +
        "  0   Successful program execution%n" +
        "  1   Usage error: user input for the command was incorrect, e.g., the wrong%n" +
        "        number of arguments, a bad flag, a bad syntax in a parameter, etc.%n" +
        "  2   The specified command script exists (Specify `--force` to overwrite).%n" +
        "  3   The specified completion script exists (Specify `--force` to overwrite).%n" +
        "  4   An exception occurred while generating the completion script.%n" +
        "%n" +
        "System Properties:%n" +
        "Set the following system properties to control the exit code of this program:%n" +
        "%n" +
        "* `\"picocli.autocomplete.systemExitOnSuccess\"`%n" +
        "   call `System.exit(0)` when execution completes normally.%n" +
        "* `\"picocli.autocomplete.systemExitOnError\"`%n" +
        "   call `System.exit(ERROR_CODE)` when an error occurs.%n" +
        "%n" +
        "If these system properties are not defined or have value \"false\", this program%n" +
        "completes without terminating the JVM.%n" +
        "%n" +
        "Example%n" +
        "-------%n" +
        "  java -cp \"myapp.jar;picocli-%s.jar\" \\%n" +
        "              picocli.AutoComplete my.pkg.MyClass%n",
        CommandLine.VERSION);

    private String expectedCompletionScriptForAutoCompleteApp() {
        return String.format("" +
                "#!/usr/bin/env bash\n" +
                "#\n" +
                "# picocli.AutoComplete Bash Completion\n" +
                "# =======================\n" +
                "#\n" +
                "# Bash completion support for the `picocli.AutoComplete` command,\n" +
                "# generated by [picocli](https://picocli.info/) version %s.\n" +
                "#\n" +
                "# Installation\n" +
                "# ------------\n" +
                "#\n" +
                "# 1. Source all completion scripts in your .bash_profile\n" +
                "#\n" +
                "#   cd $YOUR_APP_HOME/bin\n" +
                "#   for f in $(find . -name \"*_completion\"); do line=\". $(pwd)/$f\"; grep \"$line\" ~/.bash_profile || echo \"$line\" >> ~/.bash_profile; done\n" +
                "#\n" +
                "# 2. Open a new bash console, and type `picocli.AutoComplete [TAB][TAB]`\n" +
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
                "# 'picocli.AutoComplete (..)'. By reading entered command line parameters,\n" +
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
                "  # Only initialize completions module once to avoid unregistering existing completions.\n" +
                "  if ! type compdef > /dev/null; then\n" +
                "    autoload -U +X compinit && compinit\n" +
                "  fi\n" +
                "  autoload -U +X bashcompinit && bashcompinit\n" +
                "fi\n" +
                "\n" +
                "# CompWordsContainsArray takes an array and then checks\n" +
                "# if all elements of this array are in the global COMP_WORDS array.\n" +
                "#\n" +
                "# Returns zero (no error) if all elements of the array are in the COMP_WORDS array,\n" +
                "# otherwise returns 1 (error).\n" +
                "function CompWordsContainsArray() {\n" +
                "  declare -a localArray\n" +
                "  localArray=(\"$@\")\n" +
                "  local findme\n" +
                "  for findme in \"${localArray[@]}\"; do\n" +
                "    if ElementNotInCompWords \"$findme\"; then return 1; fi\n" +
                "  done\n" +
                "  return 0\n" +
                "}\n" +
                "function ElementNotInCompWords() {\n" +
                "  local findme=\"$1\"\n" +
                "  local element\n" +
                "  for element in \"${COMP_WORDS[@]}\"; do\n" +
                "    if [[ \"$findme\" = \"$element\" ]]; then return 1; fi\n" +
                "  done\n" +
                "  return 0\n" +
                "}\n" +
                "\n" +
                "# The `currentPositionalIndex` function calculates the index of the current positional parameter.\n" +
                "#\n" +
                "# currentPositionalIndex takes three parameters:\n" +
                "# the command name,\n" +
                "# a space-separated string with the names of options that take a parameter, and\n" +
                "# a space-separated string with the names of boolean options (that don't take any params).\n" +
                "# When done, this function echos the current positional index to std_out.\n" +
                "#\n" +
                "# Example usage:\n" +
                "# local currIndex=$(currentPositionalIndex \"mysubcommand\" \"$ARG_OPTS\" \"$FLAG_OPTS\")\n" +
                "function currentPositionalIndex() {\n" +
                "  local commandName=\"$1\"\n" +
                "  local optionsWithArgs=\"$2\"\n" +
                "  local booleanOptions=\"$3\"\n" +
                "  local previousWord\n" +
                "  local result=0\n" +
                "\n" +
                "  for i in $(seq $((COMP_CWORD - 1)) -1 0); do\n" +
                "    previousWord=${COMP_WORDS[i]}\n" +
                "    if [ \"${previousWord}\" = \"$commandName\" ]; then\n" +
                "      break\n" +
                "    fi\n" +
                "    if [[ \"${optionsWithArgs}\" =~ ${previousWord} ]]; then\n" +
                "      ((result-=2)) # Arg option and its value not counted as positional param\n" +
                "    elif [[ \"${booleanOptions}\" =~ ${previousWord} ]]; then\n" +
                "      ((result-=1)) # Flag option itself not counted as positional param\n" +
                "    fi\n" +
                "    ((result++))\n" +
                "  done\n" +
                "  echo \"$result\"\n" +
                "}\n" +
                "\n" +
                "# compReplyArray generates a list of completion suggestions based on an array, ensuring all values are properly escaped.\n" +
                "#\n" +
                "# compReplyArray takes a single parameter: the array of options to be displayed\n" +
                "#\n" +
                "# The output is echoed to std_out, one option per line.\n" +
                "#\n" +
                "# Example usage:\n" +
                "# local options=(\"foo\", \"bar\", \"baz\")\n" +
                "# local IFS=$'\\n'\n" +
                "# COMPREPLY=($(compReplyArray \"${options[@]}\"))\n" +
                "function compReplyArray() {\n" +
                "  declare -a options\n" +
                "  options=(\"$@\")\n" +
                "  local curr_word=${COMP_WORDS[COMP_CWORD]}\n" +
                "  local i\n" +
                "  local quoted\n" +
                "  local optionList=()\n" +
                "\n" +
                "  for (( i=0; i<${#options[@]}; i++ )); do\n" +
                "    # Double escape, since we want escaped values, but compgen -W expands the argument\n" +
                "    printf -v quoted %%q \"${options[i]}\"\n" +
                "    quoted=\\'${quoted//\\'/\\'\\\\\\'\\'}\\'\n" +
                "\n" +
                "    optionList[i]=$quoted\n" +
                "  done\n" +
                "\n" +
                "  # We also have to add another round of escaping to $curr_word.\n" +
                "  curr_word=${curr_word//\\\\/\\\\\\\\}\n" +
                "  curr_word=${curr_word//\\'/\\\\\\'}\n" +
                "\n" +
                "  # Actually generate completions.\n" +
                "  local IFS=$'\\n'\n" +
                "  echo -e \"$(compgen -W \"${optionList[*]}\" -- \"$curr_word\")\"\n" +
                "}\n" +
                "\n" +
                "# Bash completion entry point function.\n" +
                "# _complete_picocli.AutoComplete finds which commands and subcommands have been specified\n" +
                "# on the command line and delegates to the appropriate function\n" +
                "# to generate possible options and subcommands for the last specified subcommand.\n" +
                "function _complete_picocli.AutoComplete() {\n" +
                "  # Edge case: if command line has no space after subcommand, then don't assume this subcommand is selected (remkop/picocli#1468).\n" +
                "\n" +
                "  # Find the longest sequence of subcommands and call the bash function for that subcommand.\n" +
                "\n" +
                "\n" +
                "  # No subcommands were specified; generate completions for the top-level command.\n" +
                "  _picocli_picocli.AutoComplete; return $?;\n" +
                "}\n" +
                "\n" +
                "# Generates completions for the options and subcommands of the `picocli.AutoComplete` command.\n" +
                "function _picocli_picocli.AutoComplete() {\n" +
                "  # Get completion data\n" +
                "  local curr_word=${COMP_WORDS[COMP_CWORD]}\n" +
                "  local prev_word=${COMP_WORDS[COMP_CWORD-1]}\n" +
                "\n" +
                "  local commands=\"\"\n" +
                "  local flag_opts=\"-w --writeCommandScript -f --force -h --help -V --version\"\n" +
                "  local arg_opts=\"-c --factory -n --name -o --completionScript\"\n" +
                "\n" +
                "  type compopt &>/dev/null && compopt +o default\n" +
                "\n" +
                "  case ${prev_word} in\n" +
                "    -c|--factory)\n" +
                "      return\n" +
                "      ;;\n" +
                "    -n|--name)\n" +
                "      return\n" +
                "      ;;\n" +
                "    -o|--completionScript)\n" +
                "      local IFS=$'\\n'\n" +
                "      type compopt &>/dev/null && compopt -o filenames\n" +
                "      COMPREPLY=( $( compgen -f -- \"${curr_word}\" ) ) # files\n" +
                "      return $?\n" +
                "      ;;\n" +
                "  esac\n" +
                "\n" +
                "  if [[ \"${curr_word}\" == -* ]]; then\n" +
                "    COMPREPLY=( $(compgen -W \"${flag_opts} ${arg_opts}\" -- \"${curr_word}\") )\n" +
                "  else\n" +
                "    local positionals=\"\"\n" +
                "    local IFS=$'\\n'\n" +
                "    COMPREPLY=( $(compgen -W \"${commands// /$'\\n'}${IFS}${positionals}\" -- \"${curr_word}\") )\n" +
                "  fi\n" +
                "}\n" +
                "\n" +
                "# Define a completion specification (a compspec) for the\n" +
                "# `picocli.AutoComplete`, `picocli.AutoComplete.sh`, and `picocli.AutoComplete.bash` commands.\n" +
                "# Uses the bash `complete` builtin (see [6]) to specify that shell function\n" +
                "# `_complete_picocli.AutoComplete` is responsible for generating possible completions for the\n" +
                "# current word on the command line.\n" +
                "# The `-o default` option means that if the function generated no matches, the\n" +
                "# default Bash completions and the Readline default filename completions are performed.\n" +
                "complete -F _complete_picocli.AutoComplete -o default picocli.AutoComplete picocli.AutoComplete.sh picocli.AutoComplete.bash\n",
            CommandLine.VERSION);
    }

    public static byte[] readBytes(File f) throws IOException {
        int pos = 0;
        int len = 0;
        byte[] buffer = new byte[(int) f.length()];
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(f);
            while ((len = fis.read(buffer, pos, buffer.length - pos)) > 0) {
                pos += len;
            }
            return buffer;
        } finally {
            fis.close();
        }
    }

    private String expectedCompletionScriptForNonDefault() {
        return String.format("" +
                "#!/usr/bin/env bash\n" +
                "#\n" +
                "# nondefault Bash Completion\n" +
                "# =======================\n" +
                "#\n" +
                "# Bash completion support for the `nondefault` command,\n" +
                "# generated by [picocli](https://picocli.info/) version %s.\n" +
                "#\n" +
                "# Installation\n" +
                "# ------------\n" +
                "#\n" +
                "# 1. Source all completion scripts in your .bash_profile\n" +
                "#\n" +
                "#   cd $YOUR_APP_HOME/bin\n" +
                "#   for f in $(find . -name \"*_completion\"); do line=\". $(pwd)/$f\"; grep \"$line\" ~/.bash_profile || echo \"$line\" >> ~/.bash_profile; done\n" +
                "#\n" +
                "# 2. Open a new bash console, and type `nondefault [TAB][TAB]`\n" +
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
                "# 'nondefault (..)'. By reading entered command line parameters,\n" +
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
                "  # Only initialize completions module once to avoid unregistering existing completions.\n" +
                "  if ! type compdef > /dev/null; then\n" +
                "    autoload -U +X compinit && compinit\n" +
                "  fi\n" +
                "  autoload -U +X bashcompinit && bashcompinit\n" +
                "fi\n" +
                "\n" +
                "# CompWordsContainsArray takes an array and then checks\n" +
                "# if all elements of this array are in the global COMP_WORDS array.\n" +
                "#\n" +
                "# Returns zero (no error) if all elements of the array are in the COMP_WORDS array,\n" +
                "# otherwise returns 1 (error).\n" +
                "function CompWordsContainsArray() {\n" +
                "  declare -a localArray\n" +
                "  localArray=(\"$@\")\n" +
                "  local findme\n" +
                "  for findme in \"${localArray[@]}\"; do\n" +
                "    if ElementNotInCompWords \"$findme\"; then return 1; fi\n" +
                "  done\n" +
                "  return 0\n" +
                "}\n" +
                "function ElementNotInCompWords() {\n" +
                "  local findme=\"$1\"\n" +
                "  local element\n" +
                "  for element in \"${COMP_WORDS[@]}\"; do\n" +
                "    if [[ \"$findme\" = \"$element\" ]]; then return 1; fi\n" +
                "  done\n" +
                "  return 0\n" +
                "}\n" +
                "\n" +
                "# The `currentPositionalIndex` function calculates the index of the current positional parameter.\n" +
                "#\n" +
                "# currentPositionalIndex takes three parameters:\n" +
                "# the command name,\n" +
                "# a space-separated string with the names of options that take a parameter, and\n" +
                "# a space-separated string with the names of boolean options (that don't take any params).\n" +
                "# When done, this function echos the current positional index to std_out.\n" +
                "#\n" +
                "# Example usage:\n" +
                "# local currIndex=$(currentPositionalIndex \"mysubcommand\" \"$ARG_OPTS\" \"$FLAG_OPTS\")\n" +
                "function currentPositionalIndex() {\n" +
                "  local commandName=\"$1\"\n" +
                "  local optionsWithArgs=\"$2\"\n" +
                "  local booleanOptions=\"$3\"\n" +
                "  local previousWord\n" +
                "  local result=0\n" +
                "\n" +
                "  for i in $(seq $((COMP_CWORD - 1)) -1 0); do\n" +
                "    previousWord=${COMP_WORDS[i]}\n" +
                "    if [ \"${previousWord}\" = \"$commandName\" ]; then\n" +
                "      break\n" +
                "    fi\n" +
                "    if [[ \"${optionsWithArgs}\" =~ ${previousWord} ]]; then\n" +
                "      ((result-=2)) # Arg option and its value not counted as positional param\n" +
                "    elif [[ \"${booleanOptions}\" =~ ${previousWord} ]]; then\n" +
                "      ((result-=1)) # Flag option itself not counted as positional param\n" +
                "    fi\n" +
                "    ((result++))\n" +
                "  done\n" +
                "  echo \"$result\"\n" +
                "}\n" +
                "\n" +
                "# compReplyArray generates a list of completion suggestions based on an array, ensuring all values are properly escaped.\n" +
                "#\n" +
                "# compReplyArray takes a single parameter: the array of options to be displayed\n" +
                "#\n" +
                "# The output is echoed to std_out, one option per line.\n" +
                "#\n" +
                "# Example usage:\n" +
                "# local options=(\"foo\", \"bar\", \"baz\")\n" +
                "# local IFS=$'\\n'\n" +
                "# COMPREPLY=($(compReplyArray \"${options[@]}\"))\n" +
                "function compReplyArray() {\n" +
                "  declare -a options\n" +
                "  options=(\"$@\")\n" +
                "  local curr_word=${COMP_WORDS[COMP_CWORD]}\n" +
                "  local i\n" +
                "  local quoted\n" +
                "  local optionList=()\n" +
                "\n" +
                "  for (( i=0; i<${#options[@]}; i++ )); do\n" +
                "    # Double escape, since we want escaped values, but compgen -W expands the argument\n" +
                "    printf -v quoted %%q \"${options[i]}\"\n" +
                "    quoted=\\'${quoted//\\'/\\'\\\\\\'\\'}\\'\n" +
                "\n" +
                "    optionList[i]=$quoted\n" +
                "  done\n" +
                "\n" +
                "  # We also have to add another round of escaping to $curr_word.\n" +
                "  curr_word=${curr_word//\\\\/\\\\\\\\}\n" +
                "  curr_word=${curr_word//\\'/\\\\\\'}\n" +
                "\n" +
                "  # Actually generate completions.\n" +
                "  local IFS=$'\\n'\n" +
                "  echo -e \"$(compgen -W \"${optionList[*]}\" -- \"$curr_word\")\"\n" +
                "}\n" +
                "\n" +
                "# Bash completion entry point function.\n" +
                "# _complete_nondefault finds which commands and subcommands have been specified\n" +
                "# on the command line and delegates to the appropriate function\n" +
                "# to generate possible options and subcommands for the last specified subcommand.\n" +
                "function _complete_nondefault() {\n" +
                "  # Edge case: if command line has no space after subcommand, then don't assume this subcommand is selected (remkop/picocli#1468).\n" +
                "\n" +
                "  # Find the longest sequence of subcommands and call the bash function for that subcommand.\n" +
                "\n" +
                "\n" +
                "  # No subcommands were specified; generate completions for the top-level command.\n" +
                "  _picocli_nondefault; return $?;\n" +
                "}\n" +
                "\n" +
                "# Generates completions for the options and subcommands of the `nondefault` command.\n" +
                "function _picocli_nondefault() {\n" +
                "  # Get completion data\n" +
                "  local curr_word=${COMP_WORDS[COMP_CWORD]}\n" +
                "  local prev_word=${COMP_WORDS[COMP_CWORD-1]}\n" +
                "\n" +
                "  local commands=\"\"\n" +
                "  local flag_opts=\"\"\n" +
                "  local arg_opts=\"-t --timeout\"\n" +
                "\n" +
                "  type compopt &>/dev/null && compopt +o default\n" +
                "\n" +
                "  case ${prev_word} in\n" +
                "    -t|--timeout)\n" +
                "      return\n" +
                "      ;;\n" +
                "  esac\n" +
                "\n" +
                "  if [[ \"${curr_word}\" == -* ]]; then\n" +
                "    COMPREPLY=( $(compgen -W \"${flag_opts} ${arg_opts}\" -- \"${curr_word}\") )\n" +
                "  else\n" +
                "    local positionals=\"\"\n" +
                "    local IFS=$'\\n'\n" +
                "    COMPREPLY=( $(compgen -W \"${commands// /$'\\n'}${IFS}${positionals}\" -- \"${curr_word}\") )\n" +
                "  fi\n" +
                "}\n" +
                "\n" +
                "# Define a completion specification (a compspec) for the\n" +
                "# `nondefault`, `nondefault.sh`, and `nondefault.bash` commands.\n" +
                "# Uses the bash `complete` builtin (see [6]) to specify that shell function\n" +
                "# `_complete_nondefault` is responsible for generating possible completions for the\n" +
                "# current word on the command line.\n" +
                "# The `-o default` option means that if the function generated no matches, the\n" +
                "# default Bash completions and the Readline default filename completions are performed.\n" +
                "complete -F _complete_nondefault -o default nondefault nondefault.sh nondefault.bash\n",
            CommandLine.VERSION);
    }

    @BeforeAll
    static void disableAnsi() {
        // Clear the globally cached jansiInstalled value that might
        // have been set in a previous test to force the
        // Ansi#isJansiConsoleInstalled method to recalculate
        // the cached value.
        Ansi.jansiInstalled = null;
    }

    @Test
    @DisabledForJreRange(min = JRE.JAVA_18, max = JRE.JAVA_21, disabledReason = "UnsupportedOperationException in SystemLambda.withSecurityManager")
    public void testAutoCompleteAppHelp() throws Exception {
        String[][] argsList = new String[][] {
            {"-h"},
            {"--help"},
        };
        for (final String[] args : argsList) {
            int statusCode = catchSystemExit(() -> {
                restoreSystemProperties(() -> {
                    System.setProperty("picocli.autocomplete.systemExitOnSuccess", "YES");
                    String out = tapSystemOut(() -> {
                        AutoComplete.main(args);
                    });
                    assertEquals(AUTO_COMPLETE_APP_USAGE, out, args[0]);
                });
            });
            assertEquals(AutoComplete.EXIT_CODE_SUCCESS, statusCode, args[0]);
        }
    }

    @Test
    public void testAutoCompleteAppHelp_NoSystemExit() throws Exception {
        String[][] argsList = new String[][] {
            {"-h"},
            {"--help"},
        };
        restoreSystemProperties(() -> {
            System.setProperty("picocli.autocomplete.systemExitOnSuccess", "false");
            for (final String[] args : argsList) {
                String out = tapSystemOut(() -> {
                    AutoComplete.main(args);
                });
                assertEquals(AUTO_COMPLETE_APP_USAGE, out, args[0]);
            }
        });
    }

    @Test
    @DisabledForJreRange(min = JRE.JAVA_18, max = JRE.JAVA_21, disabledReason = "UnsupportedOperationException in SystemLambda.withSecurityManager")
    public void testAutoCompleteRequiresCommandLineFQCN() throws Exception {
        int statusCode = catchSystemExit(() -> {
            restoreSystemProperties(() -> {
                System.setProperty("picocli.autocomplete.systemExitOnError", "true");
                String err = tapSystemErr(() -> {
                    AutoComplete.main();
                });
                String expected = String.format("Missing required parameter: '<commandLineFQCN>'%n") + AUTO_COMPLETE_APP_USAGE;
                assertEquals(expected, err);
            });
        });
        assertEquals(AutoComplete.EXIT_CODE_INVALID_INPUT, statusCode);
    }

    @Test
    public void testAutoCompleteRequiresCommandLineFQCN_NoSystemExit() throws Exception {
        String text = tapSystemErr(() -> {
            AutoComplete.main();
        });
        String expected = String.format("Missing required parameter: '<commandLineFQCN>'%n") + AUTO_COMPLETE_APP_USAGE;
        assertEquals(expected, text);
    }

    @Test
    @DisabledForJreRange(min = JRE.JAVA_18, max = JRE.JAVA_21, disabledReason = "UnsupportedOperationException in SystemLambda.withSecurityManager")
    public void testAutoCompleteAppCannotInstantiate() throws Exception {
        @CommandLine.Command(name = "test")
        class TestApp {
            public TestApp(String noDefaultConstructor) { throw new RuntimeException();}
        }

        int statusCode = catchSystemExit(() -> {
            restoreSystemProperties(() -> {
                System.setProperty("picocli.autocomplete.systemExitOnSuccess", "false");
                System.setProperty("picocli.autocomplete.systemExitOnError", "YES");
                String err = tapSystemErr(() -> {
                    AutoComplete.main(TestApp.class.getName());
                });
                assertTrue(err.startsWith("java.lang.NoSuchMethodException: picocli.AutoCompleteSystemExitTest$1TestApp.<init>()"));
                assertTrue(err.contains(AUTO_COMPLETE_APP_USAGE));
            });
        });
        assertEquals(AutoComplete.EXIT_CODE_EXECUTION_ERROR, statusCode);
    }

    @Test
    public void testAutoCompleteAppCannotInstantiate_NoSystemExit() throws Exception {
        @CommandLine.Command(name = "test")
        class TestApp {
            public TestApp(String noDefaultConstructor) { throw new RuntimeException();}
        }
        String err = tapSystemErr(() -> {
            AutoComplete.main(TestApp.class.getName());
        });
        assertTrue(err.startsWith("java.lang.NoSuchMethodException: picocli.AutoCompleteSystemExitTest$2TestApp.<init>()"));
        assertTrue(err.contains(AUTO_COMPLETE_APP_USAGE));
    }

    @Test
    @DisabledForJreRange(min = JRE.JAVA_18, max = JRE.JAVA_21, disabledReason = "UnsupportedOperationException in SystemLambda.withSecurityManager")
    public void testAutoCompleteAppCompletionScriptFileWillNotOverwrite() throws Exception {
        File dir = new File(System.getProperty("java.io.tmpdir"));
        final File completionScript = new File(dir, "App_completion");
        if (completionScript.exists()) {
            assertTrue(completionScript.delete());}
        completionScript.deleteOnExit();

        // create the file
        FileOutputStream fous = new FileOutputStream(completionScript, false);
        fous.close();

        int statusCode = catchSystemExit(() -> {
            restoreSystemProperties(() -> {
                System.setProperty("picocli.autocomplete.systemExitOnError", "");
                String err = tapSystemErr(() -> {
                    AutoComplete.main(String.format("-o=%s", completionScript.getAbsolutePath()), "picocli.AutoComplete$App");
                });
                String expected = String.format("" +
                    "ERROR: picocli.AutoComplete: %s exists. Specify --force to overwrite.%n" +
                    "%s", completionScript.getAbsolutePath(), AUTO_COMPLETE_APP_USAGE);
                assertTrue(err.startsWith(expected));
            });
        });
        assertEquals(AutoComplete.EXIT_CODE_COMPLETION_SCRIPT_EXISTS, statusCode);
    }

    @Test
    public void testAutoCompleteAppCompletionScriptFileWillNotOverwrite_NoSystemExit() throws Exception {
        File dir = new File(System.getProperty("java.io.tmpdir"));
        final File completionScript = new File(dir, "App_completion");
        if (completionScript.exists()) {
            assertTrue(completionScript.delete());}
        completionScript.deleteOnExit();

        // create the file
        FileOutputStream fous = new FileOutputStream(completionScript, false);
        fous.close();

        String err = tapSystemErr(() -> {
            AutoComplete.main(String.format("-o=%s", completionScript.getAbsolutePath()), "picocli.AutoComplete$App");
        });
        String expected = String.format("" +
            "ERROR: picocli.AutoComplete: %s exists. Specify --force to overwrite.%n" +
            "%s", completionScript.getAbsolutePath(), AUTO_COMPLETE_APP_USAGE);
        assertTrue(err.startsWith(expected));
    }

    @Test
    @DisabledForJreRange(min = JRE.JAVA_18, max = JRE.JAVA_21, disabledReason = "UnsupportedOperationException in SystemLambda.withSecurityManager")
    public void testAutoCompleteAppCommandScriptFileWillNotOverwrite() throws Exception {
        File dir = new File(System.getProperty("java.io.tmpdir"));
        final File commandScript = new File(dir, "picocli.AutoComplete");
        if (commandScript.exists()) {
            assertTrue(commandScript.delete());}
        commandScript.deleteOnExit();

        // create the file
        FileOutputStream fous = new FileOutputStream(commandScript, false);
        fous.close();

        File completionScript = new File(dir, commandScript.getName() + "_completion");

        int statusCode = catchSystemExit(() -> {
            restoreSystemProperties(() -> {
                System.setProperty("picocli.autocomplete.systemExitOnError", "true");
                String err = tapSystemErr(() -> {
                    AutoComplete.main("--writeCommandScript", String.format("-o=%s", completionScript.getAbsolutePath()), "picocli.AutoComplete$App");
                });
                String expected = String.format("" +
                    "ERROR: picocli.AutoComplete: %s exists. Specify --force to overwrite.%n" +
                    "%s", commandScript.getAbsolutePath(), AUTO_COMPLETE_APP_USAGE);
                assertTrue(err.startsWith(expected));
            });
        });
        assertEquals(AutoComplete.EXIT_CODE_COMMAND_SCRIPT_EXISTS, statusCode);
    }

    @Test
    public void testAutoCompleteAppCommandScriptFileWillNotOverwrite_NoSystemExit() throws Exception {
        File dir = new File(System.getProperty("java.io.tmpdir"));
        final File commandScript = new File(dir, "picocli.AutoComplete");
        if (commandScript.exists()) {
            assertTrue(commandScript.delete());}
        commandScript.deleteOnExit();

        // create the file
        FileOutputStream fous = new FileOutputStream(commandScript, false);
        fous.close();

        File completionScript = new File(dir, commandScript.getName() + "_completion");

        String err = tapSystemErr(() -> {
            AutoComplete.main("--writeCommandScript", String.format("-o=%s", completionScript.getAbsolutePath()), "picocli.AutoComplete$App");
        });
        String expected = String.format("" +
            "ERROR: picocli.AutoComplete: %s exists. Specify --force to overwrite.%n" +
            "%s", commandScript.getAbsolutePath(), AUTO_COMPLETE_APP_USAGE);
        assertTrue(err.startsWith(expected));
    }

    @Test
    @DisabledForJreRange(min = JRE.JAVA_18, max = JRE.JAVA_21, disabledReason = "UnsupportedOperationException in SystemLambda.withSecurityManager")
    public void testAutoCompleteAppCommandScriptFileWillOverwriteIfRequested() throws Exception {
        File dir = new File(System.getProperty("java.io.tmpdir"));
        final File commandScript = new File(dir, "picocli.AutoComplete");
        if (commandScript.exists()) {
            assertTrue(commandScript.delete());}
        commandScript.deleteOnExit();

        // create the file
        FileOutputStream fous = new FileOutputStream(commandScript, false);
        fous.close();
        assertEquals(0, commandScript.length());

        File completionScript = new File(dir, commandScript.getName() + "_completion");

        int statusCode = catchSystemExit(() -> {
            restoreSystemProperties(() -> {
                System.setProperty("picocli.autocomplete.systemExitOnSuccess", "true");
                String err = tapSystemErr(() -> {
                    AutoComplete.main("--writeCommandScript", "--force", String.format("-o=%s", completionScript.getAbsolutePath()), "picocli.AutoComplete$App");
                });
                assertEquals("", err);
                assertNotEquals(0, commandScript.length());
                assertTrue(commandScript.delete());
            });
        });
        assertEquals(AutoComplete.EXIT_CODE_SUCCESS, statusCode);
    }

    @Test
    @DisabledForJreRange(min = JRE.JAVA_18, max = JRE.JAVA_21, disabledReason = "UnsupportedOperationException in SystemLambda.withSecurityManager")
    public void testAutoCompleteAppBothScriptFilesForceOverwrite() throws Exception {
        File dir = new File(System.getProperty("java.io.tmpdir"));
        final File commandScript = new File(dir, "picocli.AutoComplete");
        if (commandScript.exists()) {
            assertTrue(commandScript.delete());}
        commandScript.deleteOnExit();

        // create the file
        FileOutputStream fous1 = new FileOutputStream(commandScript, false);
        fous1.close();

        final File completionScript = new File(dir, commandScript.getName() + "_completion");
        if (completionScript.exists()) {
            assertTrue(completionScript.delete());}
        completionScript.deleteOnExit();

        // create the file
        FileOutputStream fous2 = new FileOutputStream(completionScript, false);
        fous2.close();

        int statusCode = catchSystemExit(() -> {
            restoreSystemProperties(() -> {
                System.setProperty("picocli.autocomplete.systemExitOnSuccess", "true");
                String err = tapSystemErr(() -> {
                    AutoComplete.main("--force", "--writeCommandScript", String.format("-o=%s", completionScript.getAbsolutePath()), "picocli.AutoComplete$App");
                });
                assertEquals("", err);
                byte[] command = readBytes(commandScript);
                assertEquals(("" +
                    "#!/usr/bin/env bash\n" +
                    "\n" +
                    "LIBS=path/to/libs\n" +
                    "CP=\"${LIBS}/myApp.jar\"\n" +
                    "java -cp \"${CP}\" 'picocli.AutoComplete$App' $@"), new String(command, "UTF8"));

                byte[] completion = readBytes(completionScript);

                String expected = expectedCompletionScriptForAutoCompleteApp();
                assertEquals(expected, new String(completion, "UTF8"));

                assertTrue(commandScript.delete());
                assertTrue(completionScript.delete());
            });
        });
        assertEquals(AutoComplete.EXIT_CODE_SUCCESS, statusCode);
    }

    @Test
    public void testAutoCompleteAppBothScriptFilesForceOverwrite_NoSystemExit() throws Exception {
        File dir = new File(System.getProperty("java.io.tmpdir"));
        final File commandScript = new File(dir, "picocli.AutoComplete");
        if (commandScript.exists()) {
            assertTrue(commandScript.delete());}
        commandScript.deleteOnExit();

        // create the file
        FileOutputStream fous1 = new FileOutputStream(commandScript, false);
        fous1.close();

        final File completionScript = new File(dir, commandScript.getName() + "_completion");
        if (completionScript.exists()) {
            assertTrue(completionScript.delete());}
        completionScript.deleteOnExit();

        // create the file
        FileOutputStream fous2 = new FileOutputStream(completionScript, false);
        fous2.close();

        AutoComplete.main("--force", "--writeCommandScript", String.format("-o=%s", completionScript.getAbsolutePath()), "picocli.AutoComplete$App");

        byte[] command = readBytes(commandScript);
        assertEquals(("" +
            "#!/usr/bin/env bash\n" +
            "\n" +
            "LIBS=path/to/libs\n" +
            "CP=\"${LIBS}/myApp.jar\"\n" +
            "java -cp \"${CP}\" 'picocli.AutoComplete$App' $@"), new String(command, "UTF8"));

        byte[] completion = readBytes(completionScript);

        String expected = expectedCompletionScriptForAutoCompleteApp();
        assertEquals(expected, new String(completion, "UTF8"));
    }

    @Test
    @DisabledForJreRange(min = JRE.JAVA_18, max = JRE.JAVA_21, disabledReason = "UnsupportedOperationException in SystemLambda.withSecurityManager")
    public void testAutoCompleteAppGeneratesScriptNameBasedOnCommandName() throws Exception {

        final String commandName = "bestCommandEver";
        final File completionScript = new File(commandName + "_completion");
        if (completionScript.exists()) {
            assertTrue(completionScript.delete());}
        completionScript.deleteOnExit();

        int statusCode = catchSystemExit(() -> {
            restoreSystemProperties(() -> {
                System.setProperty("picocli.autocomplete.systemExitOnSuccess", "YES");
                String err = tapSystemErr(() -> {
                    AutoComplete.main(String.format("--name=%s", commandName), "picocli.AutoComplete$App");
                });
                assertEquals("", err);
                byte[] completion = readBytes(completionScript);
                assertTrue(completionScript.delete());

                String expected = expectedCompletionScriptForAutoCompleteApp().replaceAll("picocli\\.AutoComplete", commandName);
                assertEquals(expected, new String(completion, "UTF8"));
            });
        });
        assertEquals(AutoComplete.EXIT_CODE_SUCCESS, statusCode);
    }

    @Test
    public void testAutoCompleteAppGeneratesScriptNameBasedOnCommandName_NoSystemExit() throws Exception {

        final String commandName = "bestCommandEver";
        final File completionScript = new File(commandName + "_completion");
        if (completionScript.exists()) {
            assertTrue(completionScript.delete());}
        completionScript.deleteOnExit();

        AutoComplete.main(String.format("--name=%s", commandName), "picocli.AutoComplete$App");

        byte[] completion = readBytes(completionScript);
        assertTrue(completionScript.delete());

        String expected = expectedCompletionScriptForAutoCompleteApp().replaceAll("picocli\\.AutoComplete", commandName);
        assertEquals(expected, new String(completion, "UTF8"));
    }

    public static class NonDefaultCommand {
        @CommandLine.Option(names = {"-t", "--timeout"}) private long timeout;
        public NonDefaultCommand(int i) {}
    }
    public static class MyFactory implements CommandLine.IFactory {
        @SuppressWarnings("unchecked")
        public <K> K create(Class<K> cls) {
            return (K) new NonDefaultCommand(123);
        }
    }

    @Test
    @DisabledForJreRange(min = JRE.JAVA_18, max = JRE.JAVA_21, disabledReason = "UnsupportedOperationException in SystemLambda.withSecurityManager")
    public void testAutoCompleteAppUsesCustomFactory() throws Exception {

        final String commandName = "nondefault";
        final File completionScript = new File(commandName + "_completion");
        if (completionScript.exists()) {
            assertTrue(completionScript.delete());}
        completionScript.deleteOnExit();

        int statusCode = catchSystemExit(() -> {
            restoreSystemProperties(() -> {
                System.setProperty("picocli.autocomplete.systemExitOnSuccess", "true");
                String err = tapSystemErr(() -> {
                    AutoComplete.main(String.format("--factory=%s", AutoCompleteSystemExitTest.MyFactory.class.getName()),
                        String.format("--name=%s", commandName),
                        AutoCompleteSystemExitTest.NonDefaultCommand.class.getName());
                });
                assertEquals("", err);
                byte[] completion = readBytes(completionScript);
                assertTrue(completionScript.delete());

                String expected = expectedCompletionScriptForNonDefault().replaceAll("picocli\\.AutoComplete", commandName);
                assertEquals(expected, new String(completion, "UTF8"));
            });
        });
        assertEquals(AutoComplete.EXIT_CODE_SUCCESS, statusCode);
    }

    @CommandLine.Command
    private static class PrivateCommandClass { }
    //Support generating autocompletion scripts for non-public @Command classes #306
    @Test
    @DisabledForJreRange(min = JRE.JAVA_18, max = JRE.JAVA_21, disabledReason = "UnsupportedOperationException in SystemLambda.withSecurityManager")
    public void test306_SupportGeneratingAutocompletionScriptForNonPublicCommandClasses() throws Exception {
        File dir = new File(System.getProperty("java.io.tmpdir"));
        final File completionScript = new File(dir, "App_completion");
        if (completionScript.exists()) {
            assertTrue(completionScript.delete());}
        completionScript.deleteOnExit();

        int statusCode = catchSystemExit(() -> {
            restoreSystemProperties(() -> {
                System.setProperty("picocli.autocomplete.systemExitOnSuccess", "");
                String out = tapSystemOut(() -> {
                    String err = tapSystemErr(() -> {
                        AutoComplete.main(String.format("-o=%s", completionScript.getAbsolutePath()), PrivateCommandClass.class.getName());
                    });
                    assertEquals("", err);
                });
                assertEquals("", out);

                completionScript.delete();
            });
        });
        assertEquals(AutoComplete.EXIT_CODE_SUCCESS, statusCode);
    }

}
