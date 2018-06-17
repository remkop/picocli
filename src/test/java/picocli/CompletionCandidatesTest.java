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
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.OptionSpec;
import picocli.CommandLine.Model.PositionalParamSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static org.junit.Assert.*;

/**
 * Tests valid values-related functionality.
 */
public class CompletionCandidatesTest {
    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");

    @Rule
    public final SystemErrRule systemErrRule = new SystemErrRule().enableLog().muteForSuccessfulTests();

    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule().enableLog().muteForSuccessfulTests();

    static class MyAbcdCandidates extends ArrayList<String> {
        MyAbcdCandidates() { super(Arrays.asList("A", "B", "C", "D")); }
    }
    private static List<String> extract(Iterable<String> generator) {
        List<String> result = new ArrayList<String>();
        for (String e : generator) {
            result.add(e);
        }
        return result;
    }
    @Test
    public void testCompletionCandidatesClass_forOption() {
        class App {
            @Option(names = "-x", completionCandidates = MyAbcdCandidates.class)
            String x;
        }
        App app = new App();
        CommandLine cmd = new CommandLine(app);
        assertEquals(MyAbcdCandidates.class, cmd.getCommandSpec().findOption("x").completionCandidates().getClass());
    }
    @Test
    public void testCompletionCandidatesClass_forParameters() {
        class App {
            @Parameters(completionCandidates = MyAbcdCandidates.class)
            String x;
        }
        App app = new App();
        CommandLine cmd = new CommandLine(app);
        assertEquals(MyAbcdCandidates.class, cmd.getCommandSpec().positionalParameters().get(0).completionCandidates().getClass());
    }
    @Test
    public void testCompletionCandidatesValues_forOption() {
        class App {
            @Option(names = "-x", completionCandidates = MyAbcdCandidates.class)
            String x;
        }
        CommandLine cmd = new CommandLine(new App());
        assertEquals(Arrays.asList("A", "B", "C", "D"), extract(cmd.getCommandSpec().findOption("x").completionCandidates()));
    }
    @Test
    public void testCompletionCandidatesValues_forParameters() {
        class App {
            @Parameters(completionCandidates = MyAbcdCandidates.class)
            String x;
        }
        CommandLine cmd = new CommandLine(new App());
        assertEquals(Arrays.asList("A", "B", "C", "D"), extract(cmd.getCommandSpec().positionalParameters().get(0).completionCandidates()));
    }
    @Test
    public void testCompletionCandidatesValues_forOptionSpec() {
        CommandSpec spec = CommandSpec.create();
        spec.add(OptionSpec.builder("-x").completionCandidates(Arrays.asList("x", "y", "z")).build());
        assertEquals(Arrays.asList("x", "y", "z"), extract(spec.findOption("x").completionCandidates()));
    }
    @Test
    public void testCompletionCandidatesValues_forPositionalParamSpec() {
        CommandSpec spec = CommandSpec.create();
        spec.add(PositionalParamSpec.builder().completionCandidates(Arrays.asList("x", "y", "z")).build());
        assertEquals(Arrays.asList("x", "y", "z"), extract(spec.positionalParameters().get(0).completionCandidates()));
    }
}
