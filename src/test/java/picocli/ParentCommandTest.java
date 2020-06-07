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
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import static org.junit.Assert.*;

import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;
import picocli.CommandLine.Command;
import picocli.CommandLine.InitializationException;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

public class ParentCommandTest {

    // allows tests to set any kind of properties they like, without having to individually roll them back
    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");

    @Command(name = "top", subcommands = Sub.class)
    static class Top implements Runnable {
        @Option(names = {"-d", "--directory"}, description = "this option applies to all subcommands")
        private File baseDirectory;
        int result;
        public void run() { }
    }

    @Command(name = "sub")
    static class Sub implements Runnable {
        @ParentCommand private Top parent;
        @Parameters private int count;
        private int result;

        public void run() {
            int multiplier = parent == null ? 0 : parent.baseDirectory.toString().length();
            result = multiplier * count;
            parent.result = result;
        }
    }
    @SuppressWarnings("deprecation")
    @Test
    public void testParentInjectedOnParseWhenConfiguredAsSubcommand() {
        List<CommandLine> result = new CommandLine(new Top()).parse("-d/tmp/blah", "sub", "3");
        Top top = result.get(0).getCommand();
        assertEquals(new File("/tmp/blah"), top.baseDirectory);
        Sub sub = result.get(1).getCommand();
        assertEquals(3, sub.count);
        assertSame(top, sub.parent);
    }
    @Test
    public void testParentInjectedOnRunWhenConfiguredAsSubcommand() {
        Top top = new Top();
        new CommandLine(top).execute("-d/tmp/blah", "sub", "3");
        assertEquals(new File("/tmp/blah"), top.baseDirectory);
        assertEquals(3 * "/tmp/blah".length(), top.result);
    }
    @SuppressWarnings("deprecation")
    @Test
    public void testParentNotInjectedWhenConfiguredAsTopLevelCommand() {
        List<CommandLine> result = new CommandLine(new Sub()).parse("3");
        Sub sub = result.get(0).getCommand();
        assertNull(sub.parent);
        assertEquals(3, sub.count);
        assertEquals(0, sub.result);
    }
    @SuppressWarnings("deprecation")
    @Test
    public void testParentInjectedWhenAddedAsSubcommand() {
        class Top1 {
            @Option(names = {"-d", "--directory"}, description = "this option applies to all subcommands")
            private File baseDirectory;
        }
        class Sub1 {
            @ParentCommand private Top1 parent;
            @Parameters private int count;
        }
        List<CommandLine> result = new CommandLine(new Top1())
                .addSubcommand("sub1", new Sub1())
                .parse("-d/tmp/blah", "sub1", "3");
        Top1 top = result.get(0).getCommand();
        assertEquals(new File("/tmp/blah"), top.baseDirectory);
        Sub1 sub = result.get(1).getCommand();
        assertEquals(3, sub.count);
        assertSame(top, sub.parent);
    }
    @Test
    public void testInitializationExceptionForTypeMismatch() {
        class Top1 {
            @Option(names = {"-d", "--directory"}, description = "this option applies to all subcommands")
            private File baseDirectory;
        }
        class Sub1 {
            @ParentCommand private String parent;
            @Parameters private int count;
        }
        Top1 top1 = new Top1();
        Sub1 sub1 = new Sub1();
        try {
            new CommandLine(top1).addSubcommand("sub1", sub1);
            fail("expected failure");
        } catch (InitializationException ex) {
            String prefix = "Unable to initialize @ParentCommand field: picocli.CommandLine$PicocliException";
            if (prefix.equals(ex.getMessage())) { return; }
            String expected = prefix + ": Could not set value for field private java.lang.String " + sub1.getClass().getName() + ".parent to " + top1;
            assertEquals(expected, ex.getMessage());
        }
    }
}
