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

import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import picocli.CommandLine.ParseResult;

import static org.junit.Assert.*;
import static picocli.CommandLine.*;
import static picocli.HelpTestUtil.setTraceLevel;

public class ParseResultTest {
    @Test
    public void testBasicUsage() {
        class App {
            @Option(names = {"-t", "-ttt"}) boolean boolVal;
            @Option(names = {"-i", "-int"}) int intVal;
            @Parameters String[] positional;
        }
        ParseResult result = new CommandLine(new App()).parseArgs("-t", "-i", "1", "a", "b");
        assertEquals(Arrays.asList("-t", "-i", "1", "a", "b"), result.originalArgs());

        assertTrue(result.unmatched().isEmpty());
        assertFalse(result.hasSubcommand());
        assertFalse(result.isUsageHelpRequested());
        assertFalse(result.isVersionHelpRequested());

        assertTrue(result.hasOption("-ttt"));
        assertTrue(result.hasOption("-t"));
        assertTrue(result.hasOption("-i"));
        assertTrue(result.hasOption("-int"));
        assertFalse(result.hasOption("-unknown"));

        assertTrue(result.hasPositional(0));
        assertTrue(result.hasPositional(1));
    }
    @Test
    public void testMultipleOverlappingPositionals() {
        class App {
            @Parameters String[] all;
            @Parameters(index = "0..1") String[] zeroOne;
            @Parameters(index = "1..*") String[] oneAndUp;
        }
        String[] args = {"a", "b", "c", "d", "e"};
        ParseResult result = new CommandLine(new App()).parseArgs(args);
        assertEquals(Arrays.asList(args), result.originalArgs());

        assertTrue(result.unmatched().isEmpty());
        assertFalse(result.hasSubcommand());
        assertFalse(result.isUsageHelpRequested());
        assertFalse(result.isVersionHelpRequested());

        assertEquals(Collections.emptyList(), result.options());
        assertEquals(3, result.positionalParams().size());
        assertEquals(Range.valueOf("0..1"), result.positionalParams().get(0).index());
        assertEquals(Range.valueOf("0..*"), result.positionalParams().get(1).index());
        assertEquals(Range.valueOf("1..*"), result.positionalParams().get(2).index());

        assertArrayEquals(args, (String[]) result.positionalParams().get(1).getValue());
        assertArrayEquals(new String[]{"a", "b"}, (String[]) result.positionalParams().get(0).getValue());
        assertArrayEquals(new String[]{"b", "c", "d", "e"}, (String[]) result.positionalParams().get(2).getValue());

        for (int i = 0; i < args.length; i++) {
            assertTrue(result.hasPositional(i));
            assertEquals(args[i], result.positionalValue(i));
        }
        assertFalse(result.hasPositional(args.length));
    }

    @Test
    public void testOriginalArgsForSubcommands() {
        class App {
            @Option(names = "-x") String x;
        }
        class Sub {
            @Parameters String[] all;
        }
        CommandLine cmd = new CommandLine(new App());
        cmd.addSubcommand("sub", new Sub());
        ParseResult parseResult = cmd.parseArgs("-x", "xval", "sub", "1", "2", "3");
        assertEquals(Arrays.asList("-x", "xval", "sub", "1", "2", "3"), parseResult.originalArgs());

        assertTrue(parseResult.hasOption("-x"));
        assertEquals("xval", parseResult.optionValue("-x"));
        assertEquals("xval", parseResult.typedOptionValue("-x"));
        assertFalse(parseResult.hasPositional(0));

        assertTrue(parseResult.hasSubcommand());
        ParseResult subResult = parseResult.subcommand();
        assertEquals(Arrays.asList("-x", "xval", "sub", "1", "2", "3"), subResult.originalArgs()); // TODO is this okay?
        assertEquals("1", subResult.positionalValue(0));
        assertEquals("2", subResult.positionalValue(1));
        assertEquals("3", subResult.positionalValue(2));
    }
}
