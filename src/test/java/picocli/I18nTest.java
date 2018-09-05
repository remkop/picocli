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
                "      <param2>    sub2%n" +
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
        assertEquals(expected, new CommandLine(new I18nCommand()).getUsageMessage());
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
        CommandLine top = new CommandLine(new I18nCommand());
        CommandLine sub = top.getSubcommands().get("i18n-sub");
        assertEquals(expected, sub.getUsageMessage());
    }

}
