package picocli;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.ArgSpec;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Unmatched;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class ParameterPreprocessorTest {

    // allows tests to set any kind of properties they like, without having to individually roll them back
    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");

    @Command(name = "edit")
    static class Edit {
        @Parameters(index = "0", description = "The file to edit.")
        File file;

        @Unmatched List<String> unmatched;

        enum Editor { defaultEditor, eclipse, idea, netbeans }

        @Option(names = {"-o", "--open"}, arity = "0..1", preprocessor = Edit.MyPreprocessor.class,
                description = {
                        "Optionally specify the editor to use; if omitted the default editor is used. ",
                        "Example: edit --open=idea FILE opens IntelliJ IDEA (notice the '=' separator)",
                        "         edit --open FILE opens the specified file in the default editor"
                })
        Editor editor = Editor.defaultEditor;

        static class MyPreprocessor implements CommandLine.IParameterPreprocessor {
            public boolean preprocess(Stack<String> args, CommandSpec commandSpec, ArgSpec argSpec, Map<String, Object> info) {
                // we need to decide whether the next arg is the file to edit or the name of the editor to use...
                if (" ".equals(info.get("separator"))) { // parameter was not attached to option
                    args.push(Editor.defaultEditor.name()); // act as if the user specified --open=defaultEditor
                }
                return false; // picocli's internal parsing is resumed for this option
            }
        }
    }

    @Test
    public void testEditorHelp() {
        String actual = new CommandLine(new Edit()).getUsageMessage();

        String expected = String.format("" +
                "Usage: edit [-o[=<editor>]] <file>%n" +
                "      <file>              The file to edit.%n" +
                "  -o, --open[=<editor>]   Optionally specify the editor to use; if omitted the%n" +
                "                            default editor is used.%n" +
                "                          Example: edit --open=idea FILE opens IntelliJ IDEA%n" +
                "                            (notice the '=' separator)%n" +
                "                                   edit --open FILE opens the specified file in%n" +
                "                            the default editor%n" +
                "");
        assertEquals(expected, actual);
    }

    @Test
    public void testArgSpecWithoutSeparator() {
        Edit app = CommandLine.populateCommand(new Edit(), "-o somefile ignored".split(" "));
        assertEquals(Edit.Editor.defaultEditor, app.editor);
        assertEquals(new File("somefile"), app.file);
        assertEquals(Collections.singletonList("ignored"), app.unmatched);
    }

    @Test
    public void testArgSpecPositionalFirstWithoutSeparator() {
        Edit app = CommandLine.populateCommand(new Edit(), "file1 -o eclipse ignored".split(" "));
        assertEquals(Edit.Editor.defaultEditor, app.editor);
        assertEquals(new File("file1"), app.file);
        assertEquals(Arrays.asList("eclipse", "ignored"), app.unmatched);
    }

    @Test
    public void testArgSpecAttachedWithoutSeparator() {
        Edit app = CommandLine.populateCommand(new Edit(), "-oidea somefile".split(" "));
        assertEquals(Edit.Editor.idea, app.editor);
        assertEquals(new File("somefile"), app.file);
        assertNull(app.unmatched);
    }

    @Test
    public void testArgSpecAttachedWithSeparator() {
        Edit app = CommandLine.populateCommand(new Edit(), "--open=netbeans somefile".split(" "));
        assertEquals(Edit.Editor.netbeans, app.editor);
        assertEquals(new File("somefile"), app.file);
        assertNull(app.unmatched);
    }

    @Command(name = "blah", preprocessor = MyLazyPreProcessor.class)
    static class Blah {
        @Option(names = "-x", required = true) int x;
        @Parameters File f;

        @Unmatched List<String> unmatched;

        Stack<String> args;

        List<String> add = new ArrayList<String>();
    }

    static class MyLazyPreProcessor implements CommandLine.IParameterPreprocessor {
        @SuppressWarnings("unchecked")
        public boolean preprocess(Stack<String> args, CommandSpec commandSpec, ArgSpec argSpec, Map<String, Object> info) {
            Blah blah = commandSpec.commandLine().getCommand();
            blah.args = (Stack<String>) args.clone();
            for (String arg : blah.add) {
                args.push(arg);
            }
            return args.contains("done");
        }
    }

    @Test
    public void testCommandPreprocessorValidates() {
        Blah app = CommandLine.populateCommand(new Blah(), "a b c done".split(" "));
        assertEquals(0, app.x);
        assertNull(app.f);
        Stack<String> stack = new Stack<String>();
        stack.addAll(Arrays.asList("done", "c", "b", "a"));
        assertEquals(stack, app.args);
    }

    @Test
    public void testCommandPreprocessorCanModifyStack() {
        Blah app = new Blah();
        app.add.add("-x=3");
        app.add.add("file.txt");
        CommandLine.populateCommand(app, "a b c".split(" "));
        assertEquals(3, app.x);
        assertEquals(new File("file.txt"), app.f);
        assertEquals(Arrays.asList("a", "b", "c"), app.unmatched);

        Stack<String> stack = new Stack<String>();
        stack.addAll(Arrays.asList("c", "b", "a"));
        assertEquals(stack, app.args);
    }

    @Test
    public void testCommandPreprocessorDoesNotValidates() {
        try {
            CommandLine.populateCommand(new Blah(), "a b c x".split(" "));
            fail("Expected exception");
        } catch (CommandLine.MissingParameterException ok) {
            assertEquals("Missing required option: '-x=<x>'", ok.getMessage());
        }
    }


    static class Issue1004Command {
        @ArgGroup
        Group group;

        static class Group {
            @Option(names = "-foo", preprocessor = TestPreprocessor.class)
            String foo;
        }

        static class TestPreprocessor implements CommandLine.IParameterPreprocessor {
            public boolean preprocess(Stack<String> args, CommandSpec commandSpec, ArgSpec argSpec, Map<String, Object> info) {
                argSpec.setValue(args.pop());
                return true;
            }
        }
    }

    @Test //https://github.com/remkop/picocli/issues/1004
    public void testParameterPreprocessorInArgGroup() {
        Issue1004Command cmd = new Issue1004Command();
        assertNull(cmd.group);
        new CommandLine(cmd).parseArgs("-foo", "value");

        assertNotNull("Group was initialized", cmd.group);
        assertEquals("value", cmd.group.foo);
    }
}
