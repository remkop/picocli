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
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.ArgSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import static org.junit.Assert.*;
import static picocli.TestUtil.stripAnsiTrace;
import static picocli.TestUtil.stripHashcodes;

/**
 * Tests internationalization (i18n) and localization (l12n)-related functionality.
 */
public class I18nTest {

    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");

    @Rule
    public final RestoreSystemProperties restore = new RestoreSystemProperties();

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
                "Exit Codes:%n" +
                "This exit code description comes from top bundle%n" +
                "   0   (top bundle) Normal termination (notice leading space)%n" +
                "  64   (top bundle) Invalid input%n" +
                "  70   (top bundle) internal error%n" +
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
                "Exit Codes:%n" +
                "This exit code description comes from top bundle%n" +
                "   0   (top bundle) Normal termination (notice leading space)%n" +
                "  64   (top bundle) Invalid input%n" +
                "  70   (top bundle) internal error%n" +
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
                "super exit code list heading%n" +
                "  000   super exit code 1%n" +
                "  111   super exit code 2%n" +
                "sub footer heading from subbundle%n" +
                "sub footer from subbundle%n");
        System.setProperty("picocli.trace", "DEBUG");
        CommandLine cmd = new CommandLine(new I18nSubclass2());
        assertEquals(expected, cmd.getUsageMessage());
    }

    @Test
    public void testParentCommandWithSharedResourceBundle() {
        String expected = String.format("" +
                "i18n-top header heading%n" +
                "Shared header first line%n" +
                "Shared header second line%n" +
                "Usage: i18n-top [-hV] [--optionWithDescriptionFromParent] [-x=<x>] [-y=<y>]%n" +
                "                [-z=<z>] <param0> <param1> [COMMAND]%n" +
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
                "      --optionWithDescriptionFromParent%n" +
                "                  This description should be visible in both parent and sub%n" +
                "                    commands%n" +
                "  -V, --version   Print version information and exit.%n" +
                "  -x, --xxx=<x>   X option description for i18n-top%n" +
                "  -y, --yyy=<y>   top yyy description 1%n" +
                "                  top yyy description 2%n" +
                "  -z, --zzz=<z>   top zzz description%n" +
                "top command list heading%n" +
                "  help      i18n-top HELP command header%n" +
                "  i18n-sub  i18n-sub header (only one line)%n" +
                "  sub2      Shared header first line%n" +
                "Shared Exit Codes Heading%n" +
                "These exit codes are blah blah etc.%n" +
                "  00   (From shared bundle) Normal termination%n" +
                "  64   (From shared bundle)%n" +
                "       Multiline!%n" +
                "       Invalid input%n" +
                "  70   (From shared bundle) Internal error%n" +
                "Shared footer heading%n" +
                "footer for i18n-top%n");
        Locale original = Locale.getDefault();
        Locale.setDefault(Locale.ENGLISH);
        try {
            String msg = new CommandLine(new I18nCommand()).getUsageMessage();
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
                "Usage: i18n-top [-hV] [--optionWithDescriptionFromParent] [-x=<x>] [-y=<y>]%n" +
                "                [-z=<z>] <param0> <param1> [COMMAND]%n" +
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
                "      --optionWithDescriptionFromParent%n" +
                "                  \u3053\u306e\u8aac\u660e\u306f\u3001\u89aa\u30b3\u30de\u30f3\u30c9\u3068\u30b5\u30d6\u30b3\u30de\u30f3\u30c9\u306e\u4e21\u65b9\u306b\u8868\u793a\u3055\u308c\u308b\u5fc5\u8981\u304c\u3042%n" +
                "                    \u308a\u307e\u3059%n" +
                "  -V, --version   Print version information and exit.%n" +
                "  -x, --xxx=<x>   i18n-top\u7528 X \u30aa\u30d7\u30b7\u30e7\u30f3\u8aac\u660e%n" +
                "  -y, --yyy=<y>   top yyy description 1%n" +
                "                  top yyy description 2%n" +
                "  -z, --zzz=<z>   top zzz description%n" +
                "top command list heading%n" +
                "  help      i18n-top\u7528 HELP \u30b3\u30de\u30f3\u30c9\u30d8\u30c3\u30c0\u30fc%n" +
                "  i18n-sub  i18n-sub \u30d8\u30c3\u30c0\u30fc\uff08\u4e00\u884c\u306e\u307f\uff09%n" +
                "  sub2      \u5171\u901a\u30d8\u30c3\u30c0\u30fc\uff11\u884c\u76ee%n" +
                "\u7d42\u4e86\u30b9\u30c6\u30fc\u30bf\u30b9%n" +
                "\u3053\u308c\u3089\u306e\u7d42\u4e86\u30b9\u30c6\u30fc\u30bf\u30b9\u306f\u7b49\u3005%n" +
                "  00   (\u5171\u901a\u30d0\u30f3\u30c9\u30eb\u304b\u3089) \u6b63\u5e38\u7d42\u4e86%n" +
                "  64   (\u5171\u901a\u30d0\u30f3\u30c9\u30eb\u304b\u3089)%n" +
                "       \u8907\u6570\u884c!%n" +
                "       \u7121\u52b9\u306e\u5165\u529b%n" +
                "  70   (\u5171\u901a\u30d0\u30f3\u30c9\u30eb\u304b\u3089) \u5185\u90e8\u30a8\u30e9\u30fc%n" +
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
                "Usage: i18n-top i18n-sub [-hV] [--optionWithDescriptionFromParent] [-x=<x>]%n" +
                "                         [-y=<y>] [-z=<z>] <param0> <param1> [COMMAND]%n" +
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
                "      --optionWithDescriptionFromParent%n" +
                "                  This description should be visible in both parent and sub%n" +
                "                    commands%n" +
                "  -V, --version   Special version for i18n-sub.%n" +
                "  -x, --xxx=<x>   X option description for i18n-sub%n" +
                "  -y, --yyy=<y>   subcmd yyy description 1%n" +
                "                  subcmd yyy description 2%n" +
                "  -z, --zzz=<z>   subcmd zzz description%n" +
                "subcmd command list heading%n" +
                "  help  i18n-sub HELP command header%n" +
                "Shared Exit Codes Heading%n" +
                "These exit codes are blah blah etc.%n" +
                "  00   (From shared bundle) Normal termination%n" +
                "  64   (From shared bundle)%n" +
                "       Multiline!%n" +
                "       Invalid input%n" +
                "  70   (From shared bundle) Internal error%n" +
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
    public void testSubcommandAndParentCommandResourceBundlePrioritization() {
        String expected = String.format("" +
            "my name is sub2%n" +
            "Shared header first line%n" +
            "Shared header second line%n" +
            "Usage: i18n-top sub2 [-hV] [--optionWithDescriptionFromParent] [-a=<a>]%n" +
            "                     [-q=<q>] [-x=<x>] [COMMAND]%n" +
            "my parent is i18n-top, Shared description 0%n" +
            "Shared description 1%n" +
            "Shared description 2%n" +
            "  -a, --aaa=<a>   lorem ipsum%n" +
            "  -h, --help      Shared help option description.%n" +
            "      --optionWithDescriptionFromParent%n" +
            "                  This description should be visible in both parent and sub%n" +
            "                    commands%n" +
            "  -q=<q>          This is the correct description for q%n" +
            "  -V, --version   Print version information and exit.%n" +
            "  -x=<x>          shared X option description%n" +
            "Commands:%n" +
            "  help  Shared header first line%n" +
            "Shared Exit Codes Heading%n" +
            "These exit codes are blah blah etc.%n" +
            "  00   (From shared bundle) Normal termination%n" +
            "  64   (From shared bundle)%n" +
            "       Multiline!%n" +
            "       Invalid input%n" +
            "  70   (From shared bundle) Internal error%n" +
            "Shared footer heading%n" +
            "lorem ipsum%n");
        Locale original = Locale.getDefault();
        Locale.setDefault(Locale.ENGLISH);
        try {
            CommandLine top = new CommandLine(new I18nCommand());
            CommandLine sub = top.getSubcommands().get("sub2");
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
    public void testExitCodeMapFromResourceBundle() {
        @Command class Noop {}
        CommandLine cmd = new CommandLine(new Noop());
        Locale original = Locale.getDefault();
        Locale.setDefault(Locale.ENGLISH);
        try {
            ResourceBundle rb = ResourceBundle.getBundle("picocli.SharedMessages");
            cmd.setResourceBundle(rb);
            CommandLine.Model.UsageMessageSpec usageMessageSpec = cmd.getCommandSpec().usageMessage();

            assertEquals("Shared Exit Codes Heading%nThese exit codes are blah blah etc.%n", usageMessageSpec.exitCodeListHeading());

            Map<String, String> exitCodes = new LinkedHashMap<String, String>();
            exitCodes.put("00", "(From shared bundle) Normal termination");
            exitCodes.put("64", "(From shared bundle)%nMultiline!%nInvalid input");
            exitCodes.put("70", "(From shared bundle) Internal error");
            assertEquals(exitCodes, usageMessageSpec.exitCodeList());
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
                "Usage: git commit [-m=<arg0>] [--squash=<commit>] [<file>...]%n" +
                "      [<file>...]%n" +
                        "  -m, --message=<arg0>%n" +
                "      --squash=<commit>%n");
        assertEquals(commitUsage, git.getSubcommands().get("commit").getUsageMessage());

        String pushUsage = String.format("" +
                "Usage: git push [-f] [--tags] <repository>%n" +
                "      <repository>%n" +
                "  -f, --force%n" +
                "      --tags%n");
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
                "Usage: git commit [-m=<arg0>] [--squash=<commit>] [<file>...]%n" +
                "Record changes to the repository%n" +
                "      [<file>...]         The files to commit.%n" +
                "  -m, --message=<arg0>    Use the given <msg> as the commit message.%n" +
                "      --squash=<commit>   Construct a commit message for use with rebase%n" +
                "                            --autosquash.%n");
        assertEquals(commitUsage, git.getSubcommands().get("commit").getUsageMessage());

        String pushUsage = String.format("" +
                "Usage: git push [-f] [--tags] <repository>%n" +
                "Update remote refs along with associated objects%n" +
                "      <repository>   The \"remote\" repository that is destination of a push%n" +
                "                       operation.%n" +
                "  -f, --force        Disable checks.%n" +
                "      --tags         All refs under refs/tags are pushed.%n");
        assertEquals(pushUsage, git.getSubcommands().get("push").getUsageMessage());
    }

    @Test
    public void testLocalizeBuiltInHelp_Shared() {
        String expected = String.format("" +
                "Shared header heading%n" +
                "i18n-sub HELP command header%n" +
                "%n" +
                "Usage: i18n-top i18n-sub help [-h] [--optionWithDescriptionFromParent] [COMMAND]%n" +
                "Shared description 0%n" +
                "Shared description 1%n" +
                "Shared description 2%n" +
                "      [COMMAND]   Shared description of COMMAND parameter of built-in help%n" +
                "                    subcommand%n" +
                "  -h, --help      Shared description of --help option of built-in help%n" +
                "                    subcommand%n" +
                "      --optionWithDescriptionFromParent%n" +
                "                  This description should be visible in both parent and sub%n" +
                "                    commands%n" +
                "Shared Exit Codes Heading%n" +
                "These exit codes are blah blah etc.%n" +
                "  00   (From shared bundle) Normal termination%n" +
                "  64   (From shared bundle)%n" +
                "       Multiline!%n" +
                "       Invalid input%n" +
                "  70   (From shared bundle) Internal error%n" +
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
                "Usage: i18n-top help [-h] [--optionWithDescriptionFromParent] [COMMAND]%n" +
                "Shared description 0%n" +
                "Shared description 1%n" +
                "Shared description 2%n" +
                "      [COMMAND]   Specialized description of COMMAND parameter of i18-top help%n" +
                "                    subcommand%n" +
                "  -h, --help      Specialized description of --help option of i18-top help%n" +
                "                    subcommand%n" +
                "      --optionWithDescriptionFromParent%n" +
                "                  This description should be visible in both parent and sub%n" +
                "                    commands%n" +
                "Shared Exit Codes Heading%n" +
                "These exit codes are blah blah etc.%n" +
                "  00   (From shared bundle) Normal termination%n" +
                "  64   (From shared bundle)%n" +
                "       Multiline!%n" +
                "       Invalid input%n" +
                "  70   (From shared bundle) Internal error%n" +
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

    @Test
    public void testIssue670NoScrambledCharactersOnJava9() {
        try {
            Class.forName("java.lang.Runtime$Version"); // introduced in Java 9
        } catch (ClassNotFoundException e) {
            return;
        }
        @Command(name = "tests", aliases = {"t"}, resourceBundle = "picocli.i18n.SG_cli")
        class App {
            @Option(names = {"-a", "--auth"}, descriptionKey = "auth.desc") boolean auth;
            @Option(names = {"-u", "--upload"}, descriptionKey = "upload.desc") String upload;
        }

        Locale original = Locale.getDefault();
        Locale.setDefault(Locale.FRENCH);
        String expected = String.format(Locale.FRENCH, "" +
                "Usage: tests [-a] [-u=<upload>]%n" +
                "  -a, --auth              V\u00e9rifie si l'utilisateur est connecte%n" +
                "  -u, --upload=<upload>   Tester le t\u00e9l\u00e9versement de fichiers.%n" +
                "                          Attend un chemin complet de fichier.%n");
        try {
            assertEquals(new CommandLine(new App()).getUsageMessage(), expected, new CommandLine(new App()).getUsageMessage());
        } finally {
            Locale.setDefault(original);
        }
    }

    @Test
    public void testResourceBundleBaseNameGetter() {
        @Command(resourceBundle = "picocli.i18n.SG_cli")
        class App { }
        assertEquals("picocli.i18n.SG_cli", new CommandLine(new App()).getCommandSpec().resourceBundleBaseName());
    }

    @Test
    public void testResourceBundleBaseNameSetter() {
        @Command
        class App {}

        CommandLine cmd = new CommandLine(new App());
        cmd.getCommandSpec().resourceBundleBaseName("picocli.i18n.SG_cli");

        assertEquals("picocli.i18n.SG_cli", cmd.getCommandSpec().resourceBundleBaseName());
    }

    @Test
    public void testTracingWithResourceBundle() {
        TestUtil.setTraceLevel(CommandLine.TraceLevel.DEBUG);
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        PrintStream oldErr = System.err;

        I18nCommand userObject = new I18nCommand();
        try {
            System.setErr(new PrintStream(err));
            CommandLine cl = new CommandLine(userObject);
        } finally {
            System.setErr(oldErr);
        }

        String expected = String.format("" +
                "[picocli DEBUG] Creating CommandSpec for picocli.I18nCommand@6bcae9 with factory picocli.CommandLine$DefaultFactory%n" +
                "[picocli DEBUG] Messages: Loading ResourceBundle[base=picocli.SharedMessages]...%n" +
                "[picocli DEBUG] Created Messages from resourceBundle[base=picocli.SharedMessages] for command 'i18n-top' (command 'i18n-top' (user object: picocli.I18nCommand@6bcae9))%n" +
                "[picocli DEBUG] Creating CommandSpec for class picocli.CommandLine$HelpCommand with factory picocli.CommandLine$DefaultFactory%n" +
                "[picocli DEBUG] Adding subcommand 'help' to 'i18n-top'%n" +
                "[picocli DEBUG] Created Messages from resourceBundle[base=picocli.SharedMessages] for command 'help' (command 'help' (user object: class picocli.CommandLine$HelpCommand))%n" +
                "[picocli DEBUG] Creating CommandSpec for class picocli.I18nSubcommand with factory picocli.CommandLine$DefaultFactory%n" +
                "[picocli DEBUG] Creating CommandSpec for class picocli.CommandLine$HelpCommand with factory picocli.CommandLine$DefaultFactory%n" +
                "[picocli DEBUG] Adding subcommand 'help' to 'i18n-sub'%n" +
                "[picocli DEBUG] Creating CommandSpec for picocli.CommandLine$AutoHelpMixin@7db40fd5 with factory picocli.CommandLine$DefaultFactory%n" +
                "[picocli DEBUG] Adding subcommand 'i18n-sub' to 'i18n-top'%n" +
                "[picocli DEBUG] Created Messages from resourceBundle[base=picocli.SharedMessages] for command 'i18n-sub' (command 'i18n-sub' (user object: class picocli.I18nSubcommand))%n" +
                "[picocli DEBUG] Created Messages from resourceBundle[base=picocli.SharedMessages] for command 'help' (command 'help' (user object: class picocli.CommandLine$HelpCommand))%n" +
                "[picocli DEBUG] Creating CommandSpec for class picocli.I18nSubcommand2 with factory picocli.CommandLine$DefaultFactory%n" +
                "[picocli DEBUG] Messages: Loading ResourceBundle[base=picocli.ResourceBundlePropagationTest]...%n" +
                "[picocli DEBUG] Created Messages from resourceBundle[base=picocli.ResourceBundlePropagationTest] for command 'sub2' (command 'sub2' (user object: class picocli.I18nSubcommand2))%n" +
                "[picocli DEBUG] Creating CommandSpec for class picocli.CommandLine$HelpCommand with factory picocli.CommandLine$DefaultFactory%n" +
                "[picocli DEBUG] Adding subcommand 'help' to 'sub2'%n" +
                "[picocli DEBUG] Created Messages from resourceBundle[base=picocli.ResourceBundlePropagationTest] for command 'help' (command 'help' (user object: class picocli.CommandLine$HelpCommand))%n" +
                "[picocli DEBUG] Creating CommandSpec for picocli.CommandLine$AutoHelpMixin@ with factory picocli.CommandLine$DefaultFactory%n" +
                "[picocli DEBUG] Adding subcommand 'sub2' to 'i18n-top'%n" +
                "[picocli DEBUG] Creating CommandSpec for picocli.CommandLine$AutoHelpMixin@34c53688 with factory picocli.CommandLine$DefaultFactory%n" +
                "");
        assertEquals(stripAnsiTrace(stripHashcodes(expected)), stripAnsiTrace(stripHashcodes(err.toString())));
    }
}
