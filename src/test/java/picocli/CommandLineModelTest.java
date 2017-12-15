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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import org.junit.Test;

import picocli.CommandLine.CommandSpec;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.OptionSpec;

import static org.junit.Assert.assertEquals;

public class CommandLineModelTest {
    private static String usageString(Object annotatedObject, Ansi ansi) throws
            UnsupportedEncodingException {
        return usageString(new CommandLine(annotatedObject), ansi);
    }
    private static String usageString(CommandLine commandLine, Ansi ansi) throws UnsupportedEncodingException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        commandLine.usage(new PrintStream(baos, true, "UTF8"), ansi);
        String result = baos.toString("UTF8");

        if (ansi == Ansi.AUTO) {
            baos.reset();
            commandLine.usage(new PrintStream(baos, true, "UTF8"));
            assertEquals(result, baos.toString("UTF8"));
        } else if (ansi == Ansi.ON) {
            baos.reset();
            commandLine.usage(new PrintStream(baos, true, "UTF8"), CommandLine.Help.defaultColorScheme(Ansi.ON));
            assertEquals(result, baos.toString("UTF8"));
        }
        return result;
    }

    @Test
    public void testEmptyModelHelp() throws Exception {
        CommandSpec spec = new CommandSpec(null);
        CommandLine commandLine = new CommandLine(spec);
        String actual = usageString(commandLine, Ansi.OFF);
        assertEquals(String.format("Usage: <main class>%n"), actual);
    }

    @Test
    public void testEmptyModelParse() throws Exception {
        System.setProperty("picocli.trace", "OFF");
        CommandSpec spec = new CommandSpec(null);
        CommandLine commandLine = new CommandLine(spec);
        commandLine.setUnmatchedArgumentsAllowed(true);
        commandLine.parse("-p", "123", "abc");
        assertEquals(Arrays.asList("-p", "123", "abc"), commandLine.getUnmatchedArguments());
    }

    @Test
    public void testModelHelp() throws Exception {
        CommandSpec spec = new CommandSpec(null);
        spec.add(new OptionSpec().names("-h", "--help").usageHelp(true).description("show help and exit"));
        spec.add(new OptionSpec().names("-V", "--version").usageHelp(true).description("show help and exit"));
        spec.add(new OptionSpec().names("-c", "--count").paramLabel("COUNT").arity("1").type(int.class).description("number of times to execute"));
        CommandLine commandLine = new CommandLine(spec);
        String actual = usageString(commandLine, Ansi.OFF);
        String expected = String.format("" +
                "Usage: <main class> [-h] [-V] [-c=COUNT]%n" +
                "  -c, --count=COUNT           number of times to execute%n" +
                "  -h, --help                  show help and exit%n" +
                "  -V, --version               show help and exit%n");
        assertEquals(expected, actual);
    }

    @Test
    public void testModelParse() throws Exception {
        CommandSpec spec = new CommandSpec(null);
        spec.add(new OptionSpec().names("-h", "--help").usageHelp(true).description("show help and exit"));
        spec.add(new OptionSpec().names("-V", "--version").usageHelp(true).description("show help and exit"));
        spec.add(new OptionSpec().names("-c", "--count").paramLabel("COUNT").arity("1").type(int.class).description("number of times to execute"));
        CommandLine commandLine = new CommandLine(spec);
        commandLine.parse("-c", "33");
        assertEquals(33, spec.optionsMap().get("-c").getValue());
    }
}
