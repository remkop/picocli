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

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.contrib.java.lang.system.SystemOutRule;

import static org.junit.Assert.*;
import static picocli.HelpTestUtil.usageString;

/**
 * Tests internationalization (i18n) and localization (l12n)-related functionality.
 */
public class I18nTest {

    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");

    @Rule
    public final SystemErrRule systemErrRule = new SystemErrRule().enableLog().muteForSuccessfulTests();

    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule().enableLog().muteForSuccessfulTests();

    @Test
    public void testLocalizedCommandWithResourceBundle() {
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
                "      <param1>    orig param1 description%n" +
                "%n" +
                "Options from bundle:%n" +
                "  -h, --help      Help option description from bundle.%n" +
                "  -V, --version   Version option description from bundle.%n" +
                "  -x, --xxx=<x>   X option description[0] from bundle.%n" +
                "                  X option description[1] from bundle.%n" +
                "  -y, --yyy=<y>   orig yyy description 1%n" +
                "                  orig yyy description 2%n" +
                "  -z, --zzz=<z>   Z option description overwritten from bundle%n" +
                "%n" +
                "Commands from bundle:%n" +
                "  help  Displays help information about the specified command%n" +
                "Powered by picocli from bundle%n" +
                "footer from bundle%n");
        assertEquals(expected, new CommandLine(new I18nBean()).getUsageMessage());
    }

    @Test
    public void testLocalizedSubclassedCommand() {
        String expected = String.format("" +
                "orig sub header heading%n" +
                "header first line from bundle%n" +
                "header second line from bundle%n" +
                "BundleUsage: ln [OPTION]... [-T] TARGET LINK_NAME   (1st form)%n" +
                "  or:  ln [OPTION]... TARGET                  (2nd form)%n" +
                "  or:  ln [OPTION]... TARGET... DIRECTORY     (3rd form)%n" +
                "orig sub desc heading:%n" +
                "orig sub desc 1%n" +
                "orig sub desc 2%n" +
                "%n" +
                "Positional parameters from bundle:%n" +
                "      <param0>    parameter 0 description[0] from bundle%n" +
                "                  parameter 0 description[1] from bundle%n" +
                "      <param1>    orig param1 description%n" +
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
                "  -y, --yyy=<y>   orig yyy description 1%n" +
                "                  orig yyy description 2%n" +
                "  -z, --zzz=<z>   Z option description overwritten from bundle%n" +
                "%n" +
                "Commands from bundle:%n" +
                "  help  Displays help information about the specified command%n" +
                "Powered by picocli from bundle%n" +
                "footer from bundle%n");
        assertEquals(expected, new CommandLine(new I18nSubclassBean()).getUsageMessage());
    }

    @Test
    public void testLocalizedSubclassedCommandWithResourceBundle() {
        String expected = String.format("" +
                "sub sub sub.%n" +
                "header second line from subbundle%n" +
                "BundleSubUsage: ln [OPTION]... [-T] TARGET LINK_NAME   (1st form)%n" +
                "  or:  ln [OPTION]... TARGET                  (2nd form)%n" +
                "  or:  ln [OPTION]... TARGET... DIRECTORY     (3rd form)%n" +
                "orig sub2 desc heading%n" +
                "subbundle line 0%n" +
                "subbundle line 1%n" +
                "subbundle line 2%n" +
                "%n" +
                "Positional parameters from bundle:%n" +
                "      <param0>    parameter 0 description[0] from bundle%n" + // superclass option: ignores subclass bundle
                "                  parameter 0 description[1] from bundle%n" + // superclass option: ignores subclass bundle
                "      <param1>    orig param1 description%n" +
                "      <param2>    sub2%n" +
                "      <param3>    orig sub2 param1 description%n" +
                "%n" +
                "Options from bundle:%n" +
                "  -a, --aaa=<a>%n" +
                "  -b, --bbb=<b>   orig sub2 bbb description 1%n" +
                "                  orig sub2 bbb description 2%n" +
                "  -c, --ccc=<c>   orig sub2 ccc description%n" +
                "  -h, --help      Help option description from subbundle.%n" +
                "  -V, --version   Version option description from subbundle.%n" +
                "  -x, --xxx=<x>   X option description[0] from bundle.%n" + // superclass option: ignores subclass bundle
                "                  X option description[1] from bundle.%n" + // superclass option: ignores subclass bundle
                "  -y, --yyy=<y>   orig yyy description 1%n" +
                "                  orig yyy description 2%n" +
                "  -z, --zzz=<z>   Z option description overwritten from bundle%n" + // superclass option: ignores subclass bundle
                "%n" +
                "Commands from bundle:%n" +
                "  help  Displays help information about the specified command%n" +
                "sub footer heading%n" +
                "sub footer%n");
        assertEquals(expected, new CommandLine(new I18nSubclassBean2()).getUsageMessage());
    }
}
