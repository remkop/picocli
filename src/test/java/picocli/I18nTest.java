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

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.ArgSpec;
import picocli.CommandLine.Model.OptionSpec;
import picocli.CommandLine.Model.PositionalParamSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.Assert.*;

/**
 * Tests internationalization (i18n) and localization (l12n)-related functionality.
 */
public class I18nTest {

    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");

    @Test
    public void testSuperclassWithResourceBundle() {
        String expected = String.format("" +
                "This is my app. There are other apps like it but this one is mine.%n" +
                "header first line from bundle%n" +
                "header second line from bundle%n" +
                "BundleUsage: ln [OPTION]... [-T] TARGET LINK_NAME   (1st form)%n" +
                "  or:  ln [OPTION]... TARGET                  (2nd form)%n" +
                "  or:  ln [OPTION]... TARGET... DIRECTORY     (3rd form)%n" +
                "Description from bundle:%n" +
                "bundle line 0%n" +
                "bundle line 1%n" +
                "bundle line 2%n" +
                "%n" +
                "Positional parameters from bundle:%n" +
                "      <param0>    parameter 0 description[0] from bundle%n" +
                "                  parameter 0 description[1] from bundle%n" +
                "      <param1>    super param1 description%n" +
                "%n" +
                "Options from bundle:%n" +
                "  -h, --help      Help option description from bundle.%n" +
                "  -V, --version   Version option description from bundle.%n" +
                "  -x, --xxx=<x>   X option description[0] from bundle.%n" +
                "                  X option description[1] from bundle.%n" +
                "  -y, --yyy=<y>   super yyy description 1%n" +
                "                  super yyy description 2%n" +
                "  -z, --zzz=<z>   Z option description overwritten from bundle%n" +
                "%n" +
                "Commands from bundle:%n" +
                "  help  header first line from bundle%n" +
                "Powered by picocli from bundle%n" +
                "footer from bundle%n");
        assertEquals(expected, new CommandLine(new I18nSuperclass()).getUsageMessage());
    }

    @Test
    public void testSubclassInheritsSuperResourceBundle() {
        String expected = String.format("" +
                "This is my app. There are other apps like it but this one is mine.%n" +
                "header first line from bundle%n" +
                "header second line from bundle%n" +
                "BundleUsage: ln [OPTION]... [-T] TARGET LINK_NAME   (1st form)%n" +
                "  or:  ln [OPTION]... TARGET                  (2nd form)%n" +
                "  or:  ln [OPTION]... TARGET... DIRECTORY     (3rd form)%n" +
                "Description from bundle:%n" +
                "bundle line 0%n" +
                "bundle line 1%n" +
                "bundle line 2%n" +
                "%n" +
                "Positional parameters from bundle:%n" +
                "      <param0>    parameter 0 description[0] from bundle%n" +
                "                  parameter 0 description[1] from bundle%n" +
                "      <param1>    super param1 description%n" +
                "      <param2>    sub%n" +
                "      <param3>    orig sub param1 description%n" +
                "%n" +
                "Options from bundle:%n" +
                "  -a, --aaa=<a>%n" +
                "  -b, --bbb=<b>   orig sub bbb description 1%n" +
                "                  orig sub bbb description 2%n" +
                "  -c, --ccc=<c>   orig sub ccc description%n" +
                "  -h, --help      Help option description from bundle.%n" +
                "  -V, --version   Version option description from bundle.%n" +
                "  -x, --xxx=<x>   X option description[0] from bundle.%n" +
                "                  X option description[1] from bundle.%n" +
                "  -y, --yyy=<y>   super yyy description 1%n" +
                "                  super yyy description 2%n" +
                "  -z, --zzz=<z>   Z option description overwritten from bundle%n" +
                "%n" +
                "Commands from bundle:%n" +
                "  help  header first line from bundle%n" +
                "Powered by picocli from bundle%n" +
                "footer from bundle%n");
        assertEquals(expected, new CommandLine(new I18nSubclass()).getUsageMessage());
    }

    @Test
    public void testSubclassBundleOverridesSuperBundle() {
        String expected = String.format("" +
                "Header heading from subbundle%n" +
                "header line from subbundle%n" +
                "BundleSubUsage: i18n-sub2 [-hV] [-a=<a>] [-b=<b>] [-c=<c>] [-x=<x>] [-y=<y>]%n" +
                "                          [-z=<z>] <param0> <param1> <param2> <param3> [COMMAND]%n" +
                "orig sub2 desc heading%n" +
                "subbundle line 0%n" +
                "subbundle line 1%n" +
                "subbundle line 2%n" +
                "super param list heading%n" +
                "      <param0>    parameter 0 description[0] from subbundle%n" +
                "                  parameter 0 description[1] from subbundle%n" +
                "      <param1>    super param1 description%n" +
                "      <param2>    parameter with descriptionKey line 1%n" +
                "                  parameter with descriptionKey line 2%n" +
                "      <param3>    orig sub2 param1 description%n" +
                "super option list heading%n" +
                "  -a, --aaa=<a>%n" +
                "  -b, --bbb=<b>   orig sub2 bbb description 1%n" +
                "                  orig sub2 bbb description 2%n" +
                "  -c, --ccc=<c>   orig sub2 ccc description%n" +
                "  -h, --help      Help option description from subbundle.%n" +
                "  -V, --version   Version option description from subbundle.%n" +
                "  -x, --xxx=<x>   X option description[0] from subbundle.%n" +
                "                  X option description[1] from subbundle.%n" +
                "  -y, --yyy=<y>   super yyy description 1%n" +
                "                  super yyy description 2%n" +
                "  -z, --zzz=<z>   Z option description overwritten from subbundle%n" +
                "super command list heading%n" +
                // not "header line from subbundle":
                // help command is inherited from superclass, initialized with resource bundle from superclass
                "  help  header first line from bundle%n" +
                "sub footer heading from subbundle%n" +
                "sub footer from subbundle%n");
        CommandLine cmd = new CommandLine(new I18nSubclass2());
        assertEquals(expected, cmd.getUsageMessage());
    }

    @Test
    public void testParentCommandWithSharedResourceBundle() {
        String expected = String.format("" +
                "i18n-top header heading%n" +
                "Shared header first line%n" +
                "Shared header second line%n" +
                "Usage: i18n-top [-hV] [-x=<x>] [-y=<y>] [-z=<z>] <param0> <param1> [COMMAND]%n" +
                "i18n-top description heading:%n" +
                "Shared description 0%n" +
                "Shared description 1%n" +
                "Shared description 2%n" +
                "top param list heading%n" +
                "      <param0>    param0 desc for i18n-top%n" +
                "      <param1>    shared param1 desc line 0%n" +
                "                  shared param1 desc line 1%n" +
                "top option list heading%n" +
                "  -h, --help      Shared help option description.%n" +
                "  -V, --version   Print version information and exit.%n" +
                "  -x, --xxx=<x>   X option description for i18n-top%n" +
                "  -y, --yyy=<y>   top yyy description 1%n" +
                "                  top yyy description 2%n" +
                "  -z, --zzz=<z>   top zzz description%n" +
                "top command list heading%n" +
                "  help      i18n-top HELP command header%n" +
                "  i18n-sub  i18n-sub header (only one line)%n" +
                "Shared footer heading%n" +
                "footer for i18n-top%n");
        Locale original = Locale.getDefault();
        Locale.setDefault(Locale.ENGLISH);
        try {
            assertEquals(expected, new CommandLine(new I18nCommand()).getUsageMessage());
        } finally {
            Locale.setDefault(original);
        }
    }

    @Test
    public void testParentCommandWithSharedResourceBundle_ja() {
        String expected = String.format("" +
                "i18n-top \u30d8\u30c3\u30c0\u30fc\u898b\u51fa\u3057%n" +
                "\u5171\u901a\u30d8\u30c3\u30c0\u30fc\uff11\u884c\u76ee%n" +
                "\u5171\u901a\u30d8\u30c3\u30c0\u30fc\uff12\u884c\u76ee%n" +
                "Usage: i18n-top [-hV] [-x=<x>] [-y=<y>] [-z=<z>] <param0> <param1> [COMMAND]%n" +
                "i18n-top \u8aac\u660e\u898b\u51fa\u3057:%n" +
                "\u5171\u901a\u8aac\u660e0%n" +
                "\u5171\u901a\u8aac\u660e1%n" +
                "\u5171\u901a\u8aac\u660e2%n" +
                "top param list heading%n" +
                "      <param0>    i18n-top\u7528 param0 \u8aac\u660e%n" +
                "      <param1>    \u5171\u901a param1 \u8aac\u660e\uff11\u884c\u76ee%n" +
                "                  \u5171\u901a param1 \u8aac\u660e\uff12\u884c\u76ee%n" +
                "top option list heading%n" +
                "  -h, --help      \u5171\u901a help \u30aa\u30d7\u30b7\u30e7\u30f3\u8aac\u660e.%n" +
                "  -V, --version   Print version information and exit.%n" +
                "  -x, --xxx=<x>   i18n-top\u7528 X \u30aa\u30d7\u30b7\u30e7\u30f3\u8aac\u660e%n" +
                "  -y, --yyy=<y>   top yyy description 1%n" +
                "                  top yyy description 2%n" +
                "  -z, --zzz=<z>   top zzz description%n" +
                "top command list heading%n" +
                "  help      i18n-top\u7528 HELP \u30b3\u30de\u30f3\u30c9\u30d8\u30c3\u30c0\u30fc%n" +
                "  i18n-sub  i18n-sub \u30d8\u30c3\u30c0\u30fc\uff08\u4e00\u884c\u306e\u307f\uff09%n" +
                "\u5171\u901a\u30d5\u30c3\u30bf\u30fc\u898b\u51fa\u3057%n" +
                "i18n-top\u7528\u30d5\u30c3\u30bf\u30fc%n");
        Locale original = Locale.getDefault();
        Locale.setDefault(Locale.JAPAN);
        try {
            assertEquals(expected, new CommandLine(new I18nCommand()).getUsageMessage());
        } finally {
            Locale.setDefault(original);
        }
    }

    @Test
    public void testSubcommandUsesParentBundle() {
        String expected = String.format("" +
                "i18n-sub header heading%n" +
                "i18n-sub header (only one line)%n" +
                "Usage: i18n-top i18n-sub [-hV] [-x=<x>] [-y=<y>] [-z=<z>] <param0> <param1>%n" +
                "                         [COMMAND]%n" +
                "i18n-sub description heading:%n" +
                "Shared description 0%n" +
                "Shared description 1%n" +
                "Shared description 2%n" +
                "subcmd param list heading%n" +
                "      <param0>    param0 desc for i18n-sub%n" +
                "      <param1>    shared param1 desc line 0%n" +
                "                  shared param1 desc line 1%n" +
                "subcmd option list heading%n" +
                "  -h, --help      Shared help option description.%n" +
                "  -V, --version   Special version for i18n-sub.%n" +
                "  -x, --xxx=<x>   X option description for i18n-sub%n" +
                "  -y, --yyy=<y>   subcmd yyy description 1%n" +
                "                  subcmd yyy description 2%n" +
                "  -z, --zzz=<z>   subcmd zzz description%n" +
                "subcmd command list heading%n" +
                "  help  i18n-sub HELP command header%n" +
                "Shared footer heading%n" +
                "footer for i18n-sub%n");
        Locale original = Locale.getDefault();
        Locale.setDefault(Locale.ENGLISH);
        try {
            CommandLine top = new CommandLine(new I18nCommand());
            CommandLine sub = top.getSubcommands().get("i18n-sub");
            assertEquals(expected, sub.getUsageMessage());
        } finally {
            Locale.setDefault(original);
        }
    }

    @Test
    public void testGetResourceBundle_nullIfNotSpecified() {
        @Command class Noop {}
        CommandLine cmd = new CommandLine(new Noop());
        assertNull(cmd.getResourceBundle());
    }

    @Test
    public void testGetResourceBundle_notNullIfSpecified() {
        Locale original = Locale.getDefault();
        Locale.setDefault(Locale.ENGLISH);
        try {
            CommandLine cmd = new CommandLine(new I18nCommand());
            ResourceBundle rb = cmd.getResourceBundle();
            assertNotNull(rb);
            assertEquals("Shared footer heading%n", rb.getString("usage.footerHeading"));
        } finally {
            Locale.setDefault(original);
        }
    }

    @Test
    public void testSetResourceBundle_canBeObtainedWithGet() {
        @Command class Noop {}
        CommandLine cmd = new CommandLine(new Noop());
        assertNull(cmd.getResourceBundle());

        ResourceBundle rb = ResourceBundle.getBundle("picocli.SharedMessages");
        cmd.setResourceBundle(rb);
        assertSame(rb, cmd.getResourceBundle());
    }

    @Test
    public void testSetResourceBundle_descriptionsFromBundle() {
        @Command class Noop {}
        CommandLine cmd = new CommandLine(new Noop());
        assertNull(cmd.getResourceBundle());
        assertArrayEquals(new String[0], cmd.getCommandSpec().usageMessage().header());

        Locale original = Locale.getDefault();
        Locale.setDefault(Locale.ENGLISH);
        try {
            ResourceBundle rb = ResourceBundle.getBundle("picocli.SharedMessages");
            cmd.setResourceBundle(rb);
            assertArrayEquals(new String[]{"Shared header first line", "Shared header second line"}, cmd.getCommandSpec().usageMessage().header());
        } finally {
            Locale.setDefault(original);
        }
    }

    @Test
    public void testSetResourceBundle_overwritesSubcommandBundle() {
        Locale original = Locale.getDefault();
        Locale.setDefault(Locale.ENGLISH);
        try {
            // init
            CommandLine top = new CommandLine(new I18nCommand());
            CommandLine sub = top.getSubcommands().get("i18n-sub");
            CommandLine help = top.getSubcommands().get("i18n-sub");
            ResourceBundle orig = top.getResourceBundle();
            assertSame(orig, sub.getResourceBundle());
            assertArgsHaveBundle(orig, sub.getCommandSpec().options());
            assertArgsHaveBundle(orig, sub.getCommandSpec().positionalParameters());

            assertSame(orig, help.getResourceBundle());
            assertArgsHaveBundle(orig, help.getCommandSpec().options());
            assertArgsHaveBundle(orig, help.getCommandSpec().positionalParameters());

            ResourceBundle update = ResourceBundle.getBundle("picocli.I18nSuperclass_Messages");
            assertNotSame(update, orig);
            assertNotEquals(orig.getString("usage.header"), update.getString("usage.header"));

            // exercise SUT
            top.setResourceBundle(update);

            // verify: command and subcommands modified
            assertSame(update, top.getResourceBundle());
            assertArgsHaveBundle(update, top.getCommandSpec().options());
            assertArgsHaveBundle(update, top.getCommandSpec().positionalParameters());

            assertSame(update, sub.getResourceBundle());
            assertArgsHaveBundle(update, sub.getCommandSpec().options());
            assertArgsHaveBundle(update, sub.getCommandSpec().positionalParameters());

            assertSame(update, help.getResourceBundle());
            assertArgsHaveBundle(update, help.getCommandSpec().options());
            assertArgsHaveBundle(update, help.getCommandSpec().positionalParameters());

        } finally {
            Locale.setDefault(original);
        }
    }

    @Test
    public void testCommandSpecSetResourceBundle_doesNotOverwriteSubcommandBundle() {
        Locale original = Locale.getDefault();
        Locale.setDefault(Locale.ENGLISH);
        try {
            // init
            CommandLine top = new CommandLine(new I18nCommand());
            CommandLine sub = top.getSubcommands().get("i18n-sub");
            CommandLine help = top.getSubcommands().get("i18n-sub");
            ResourceBundle orig = top.getResourceBundle();

            ResourceBundle update = ResourceBundle.getBundle("picocli.I18nSuperclass_Messages");
            assertNotSame(update, orig);

            // exercise SUT
            top.getCommandSpec().resourceBundle(update);

            // verify: command was modified
            assertSame(update, top.getResourceBundle());
            assertSame(update, top.getCommandSpec().resourceBundle());
            assertArgsHaveBundle(update, top.getCommandSpec().options());
            assertArgsHaveBundle(update, top.getCommandSpec().positionalParameters());

            // verify: subcommands not modified
            assertSame(orig, sub.getResourceBundle());
            assertArgsHaveBundle(orig, sub.getCommandSpec().options());
            assertArgsHaveBundle(orig, sub.getCommandSpec().positionalParameters());

            assertSame(orig, help.getResourceBundle());
            assertArgsHaveBundle(orig, help.getCommandSpec().options());
            assertArgsHaveBundle(orig, help.getCommandSpec().positionalParameters());

        } finally {
            Locale.setDefault(original);
        }
    }

    private void assertArgsHaveBundle(ResourceBundle orig, List<? extends ArgSpec> args) {
        assertFalse("args should not be empty", args.isEmpty());
        for (ArgSpec arg : args) {
            assertNotNull("Messages for " + arg.toString(), arg.messages());
            assertSame(arg.toString(), orig, arg.messages().resourceBundle());
        }
    }

    @Command(name = "git", mixinStandardHelpOptions = true, version = "picocli-3.6.0")
    static class Git {
        @Option(names = "--git-dir", descriptionKey = "GITDIR")
        File path;

        @Command
        void clone(@Option(names = {"-l", "--local"}) boolean local,
                   @Option(names = "-q") boolean quiet,
                   @Option(names = "-v") boolean verbose,
                   @Option(names = {"-b", "--branch"}) String branch,
                   @Parameters(paramLabel = "<repository>") String repo) {
            // ... implement business logic
        }

        @Command
        void commit(@Option(names = {"-m", "--message"}) String commitMessage,
                    @Option(names = "--squash", paramLabel = "<commit>") String squash,
                    @Parameters(paramLabel = "<file>") File[] files) {
            // ... implement business logic
        }

        @Command
        void push(@Option(names = {"-f", "--force"}) boolean force,
                  @Option(names = "--tags") boolean tags,
                  @Parameters(paramLabel = "<repository>") String repo) {
            // ... implement business logic
        }
    }

    @Test
    public void testCommandMethodWithoutResources() {

        CommandLine git = new CommandLine(new Git());
        String gitUsage = String.format("" +
                "Usage: git [-hV] [--git-dir=<path>] [COMMAND]%n" +
                "      --git-dir=<path>%n" +
                "  -h, --help             Show this help message and exit.%n" +
                "  -V, --version          Print version information and exit.%n" +
                "Commands:%n" +
                "  clone%n" +
                "  commit%n" +
                "  push%n");
        assertEquals(gitUsage, git.getUsageMessage());

        String cloneUsage = String.format("" +
                "Usage: git clone [-lqv] [-b=<arg3>] <repository>%n" +
                "      <repository>%n" +
                "  -b, --branch=<arg3>%n" +
                "  -l, --local%n" +
                "  -q%n" +
                "  -v%n");
        assertEquals(cloneUsage, git.getSubcommands().get("clone").getUsageMessage());

        String commitUsage = String.format("" +
                "Usage: git commit [--squash=<commit>] [-m=<arg0>] [<file>...]%n" +
                "      [<file>...]%n" +
                "      --squash=<commit>%n" +
                "  -m, --message=<arg0>%n");
        assertEquals(commitUsage, git.getSubcommands().get("commit").getUsageMessage());

        String pushUsage = String.format("" +
                "Usage: git push [-f] [--tags] <repository>%n" +
                "      <repository>%n" +
                "      --tags%n" +
                "  -f, --force%n");
        assertEquals(pushUsage, git.getSubcommands().get("push").getUsageMessage());
    }

    @Test
    public void testCommandMethodWithResources() {

        CommandLine git = new CommandLine(new Git());
        git.setResourceBundle(ResourceBundle.getBundle("picocli.command-method-demo"));

        String gitUsage = String.format("" +
                "Usage: git [-hV] [--git-dir=<path>] [COMMAND]%n" +
                "Version control system%n" +
                "      --git-dir=<path>   Set the path to the repository%n" +
                "  -h, --help             Show this help message and exit.%n" +
                "  -V, --version          Print version information and exit.%n" +
                "Commands:%n" +
                "  clone   Clone a repository into a new directory%n" +
                "  commit  Record changes to the repository%n" +
                "  push    Update remote refs along with associated objects%n");
        assertEquals(gitUsage, git.getUsageMessage());

        String cloneUsage = String.format("" +
                "Usage: git clone [-lqv] [-b=<arg3>] <repository>%n" +
                "Clone a repository into a new directory%n" +
                "      <repository>      The (possibly remote) repository to clone from.%n" +
                "  -b, --branch=<arg3>   Point to <name> branch instead of HEAD.%n" +
                "  -l, --local           Bypass the normal \"Git aware\" transport mechanism.%n" +
                "  -q                    Operate quietly.%n" +
                "  -v                    Run verbosely.%n");
        assertEquals(cloneUsage, git.getSubcommands().get("clone").getUsageMessage());

        String commitUsage = String.format("" +
                "Usage: git commit [--squash=<commit>] [-m=<arg0>] [<file>...]%n" +
                "Record changes to the repository%n" +
                "      [<file>...]         The files to commit.%n" +
                "      --squash=<commit>   Construct a commit message for use with rebase%n" +
                "                            --autosquash.%n" +
                "  -m, --message=<arg0>    Use the given <msg> as the commit message.%n");
        assertEquals(commitUsage, git.getSubcommands().get("commit").getUsageMessage());

        String pushUsage = String.format("" +
                "Usage: git push [-f] [--tags] <repository>%n" +
                "Update remote refs along with associated objects%n" +
                "      <repository>   The \"remote\" repository that is destination of a push operation.%n" +
                "      --tags         All refs under refs/tags are pushed.%n" +
                "  -f, --force        Disable checks.%n");
        assertEquals(pushUsage, git.getSubcommands().get("push").getUsageMessage());
    }

    @Test
    public void testLocalizeBuiltInHelp_Shared() {
        String expected = String.format("" +
                "Shared header heading%n" +
                "i18n-sub HELP command header%n" +
                "%n" +
                "Usage: i18n-top i18n-sub help [-h] [COMMAND...]%n" +
                "Shared description 0%n" +
                "Shared description 1%n" +
                "Shared description 2%n" +
                "      [COMMAND...]   Shared description of COMMAND parameter of built-in help%n" +
                "                       subcommand%n" +
                "  -h, --help         Shared description of --help option of built-in help subcommand%n" +
                "Shared footer heading%n" +
                "Shared footer%n");

        Locale original = Locale.getDefault();
        Locale.setDefault(Locale.ENGLISH);
        try {
            assertEquals(expected, new CommandLine(new I18nCommand())
                    .getSubcommands().get("i18n-sub")
                    .getSubcommands().get("help").getUsageMessage());
        } finally {
            Locale.setDefault(original);
        }
    }

    @Test
    public void testLocalizeBuiltInHelp_Specialized() {
        String expected = String.format("" +
                "Shared header heading%n" +
                "i18n-top HELP command header%n" +
                "%n" +
                "Usage: i18n-top help [-h] [COMMAND...]%n" +
                "Shared description 0%n" +
                "Shared description 1%n" +
                "Shared description 2%n" +
                "      [COMMAND...]   Specialized description of COMMAND parameter of i18-top help%n" +
                "                       subcommand%n" +
                "  -h, --help         Specialized description of --help option of i18-top help%n" +
                "                       subcommand%n" +
                "Shared footer heading%n" +
                "Shared footer%n");

        Locale original = Locale.getDefault();
        Locale.setDefault(Locale.ENGLISH);
        try {
            assertEquals(expected, new CommandLine(new I18nCommand()).getSubcommands().get("help").getUsageMessage());
        } finally {
            Locale.setDefault(original);
        }
    }
}
