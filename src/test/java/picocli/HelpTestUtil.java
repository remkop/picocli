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

import picocli.CommandLine.Help;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.Help.Ansi.Text;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertEquals;

public class HelpTestUtil {

    public static void setTraceLevel(String level) {
        System.setProperty("picocli.trace", level);
    }

    public static String usageString(Object annotatedObject, Ansi ansi) {
        return usageString(new CommandLine(annotatedObject), ansi);
    }

    public static String usageString(CommandLine commandLine, Ansi ansi) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        commandLine.usage(new PrintStream(baos, true), ansi);
        String result = baos.toString();
        assertEquals(result, commandLine.getUsageMessage(ansi));

        if (ansi == Ansi.AUTO) {
            baos.reset();
            commandLine.usage(new PrintStream(baos, true));
            assertEquals(result, baos.toString());
            assertEquals(result, commandLine.getUsageMessage());
        } else if (ansi == Ansi.ON) {
            baos.reset();
            commandLine.usage(new PrintStream(baos, true), Help.defaultColorScheme(Ansi.ON));
            assertEquals(result, baos.toString());
            assertEquals(result, commandLine.getUsageMessage(Help.defaultColorScheme(Ansi.ON)));
        }
        return result;
    }

    public static String versionString(CommandLine commandLine, Ansi ansi, Object... params) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        commandLine.printVersionHelp(new PrintStream(baos, true), ansi, params);
        String result = baos.toString();
        return result;
    }

    public static Text[] textArray(Help help, String... str) {
        return textArray(help.ansi(), str);
    }

    public static Text[] textArray(Ansi ansi, String... str) {
        Text[] result = new Text[str.length];
        for (int i = 0; i < str.length; i++) {
            result[i] = str[i] == null ? Ansi.EMPTY_TEXT : ansi.new Text(str[i]);
        }
        return result;
    }
}
