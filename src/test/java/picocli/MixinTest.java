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
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;
import picocli.CommandLine.*;
import picocli.CommandLine.Model.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import static org.junit.Assert.*;
import static picocli.TestUtil.usageString;

public class MixinTest {

    // allows tests to set any kind of properties they like, without having to individually roll them back
    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");

    @Test
    public void testMixinAnnotationMustBeValidCommand_CommandAnnotation() {
        @Command class ValidMixin {} // valid command because it has @Command annotation

        class Receiver { // valid command because it has @Mixin annotated field
            @Mixin ValidMixin mixMeIn;
        }
        new CommandLine(new Receiver(), new InnerClassFactory(this)); // no exception
    }

    @Test
    public void testMixinAnnotationMustBeValidCommand_OptionAnnotatedField() {
        class ValidMixin { // valid command because it has @Option annotation
            @Option(names = "-a") int a;
        }

        class Receiver { // valid command because it has @Mixin annotated field
            @Mixin ValidMixin mixMeIn;
        }
        new CommandLine(new Receiver(), new InnerClassFactory(this)); // no exception
    }

    @Test
    public void testMixinAnnotationMustBeValidCommand_ParametersAnnotatedField() {
        class ValidMixin { // valid command because it has @Parameters annotation
            @Parameters int a;
        }

        class Receiver { // valid command because it has @Mixin annotated field
            @Mixin ValidMixin mixMeIn;
        }
        new CommandLine(new Receiver(), new InnerClassFactory(this)); // no exception
    }

    @Test
    public void testAddMixinMustBeValidCommand_CommandAnnotation() {
        @Command class ValidMixin {} // valid command because it has @Command annotation
        @Command class Receiver {}
        CommandLine commandLine = new CommandLine(new Receiver());
        commandLine.addMixin("valid", new ValidMixin()); // no exception
    }

    @Test
    public void testAddMixinMustBeValidCommand_OptionAnnotatedField() {
        class ValidMixin { // valid command because it has @Option annotation
            @Option(names = "-a") int a;
        }
        @Command class Receiver {}
        CommandLine commandLine = new CommandLine(new Receiver());
        commandLine.addMixin("valid", new ValidMixin()); // no exception
    }

    @Test
    public void testAddMixinMustBeValidCommand_ParametersAnnotatedField() {
        class ValidMixin { // valid command because it has @Parameters annotation
            @Parameters int a;
        }
        @Command class Receiver {}
        CommandLine commandLine = new CommandLine(new Receiver());
        commandLine.addMixin("valid", new ValidMixin()); // no exception
    }

    @Test
    public void testAddMixinMustBeValidCommand_SubCommandMethod() {
        @Command class ValidMixin {  // valid command because it has @Command annotation
        }
        @Command class Receiver {
            @Command void sub(@Mixin ValidMixin mixin) {
            }
        }
        CommandLine commandLine = new CommandLine(new Receiver(), new InnerClassFactory(this));
        CommandSpec commandSpec = commandLine.getCommandSpec().subcommands().get("sub").getCommandSpec().mixins().get("arg0");
        assertEquals(ValidMixin.class, commandSpec.userObject().getClass());
        commandLine.addMixin("valid", new ValidMixin()); // no exception
    }

    @Test
    public void testMixinIn_SubCommandMethod() {
        @Command class ValidMixin {
            @Option(names = "-x") int x;
        }
        @Command class Mixee {
            @Command int sub(@Mixin ValidMixin mixin) {
                return mixin.x;
            }
        }
        IFactory fact = new InnerClassFactory(this);
        assertEquals(3, new CommandLine(new Mixee(), fact).execute("sub", "-x=3"));
        assertEquals(4, new CommandLine(new Mixee(), fact).execute("sub", "-x=4"));
    }

    @Test
    public void testMixinMixeeIn_SubCommandMethod() {
        @Command class ValidMixin {
            @Spec CommandSpec spec;
            @Spec(Spec.Target.MIXEE) CommandSpec mixee;
            @Option(names = "-x") int x;
        }
        @Command class Mixee {
            @Command int sub(@Mixin ValidMixin mixin) throws NoSuchMethodException {
                assertNotSame  (mixin.mixee, mixin.spec);
                assertNotEquals(mixin.mixee, mixin.spec);
                assertEquals(ValidMixin.class.getName(), mixin.spec.userObject().getClass().getName());
                assertEquals(Mixee.class.getDeclaredMethod("sub", ValidMixin.class).toString(),
                        mixin.mixee.userObject().toString());
                return mixin.x;
            }
        }
        IFactory fact = new InnerClassFactory(this);
        assertEquals(3, new CommandLine(new Mixee(), fact).execute("sub", "-x=3"));
    }

    @Test
    public void testMixeeWithoutMixin() {
        @Command class SomeMixin {
            @Spec CommandSpec spec;
            @Spec(Spec.Target.MIXEE) CommandSpec mixee;
            @Option(names = "-x") int x;
        }
        SomeMixin mixin = new SomeMixin();
        new CommandLine(mixin).parseArgs();
        assertNull("Not mixed into anything: no mixee", mixin.mixee);
        assertNotNull(mixin.spec);
    }

    @Test
    public void testMixinAnnotationRejectedIfNotAValidCommand() {
        class Invalid {}
        class Receiver {
            @Mixin Invalid mixMeIn;
        }
        Receiver userObject = new Receiver();
        try {
            new CommandLine(userObject, new InnerClassFactory(this));
        } catch (InitializationException ex) {
            assertEquals(userObject.mixMeIn + " is not a command: it has no @Command, @Option, @Parameters or @Unmatched annotations", ex.getMessage());
        }
    }

    @Test
    public void testAddMixinRejectedIfNotAValidCommand() {
        class Invalid {}
        @Command class Receiver {}

        Invalid mixin = new Invalid();
        CommandLine commandLine = new CommandLine(new Receiver());
        try {
            commandLine.addMixin("invalid", mixin);
        } catch (InitializationException ex) {
            assertEquals(mixin + " is not a command: it has no @Command, @Option, @Parameters or @Unmatched annotations", ex.getMessage());
        }
    }

    static class CommandAttributes {
        @Command(name = "mixmein",
                version = "Mixin 1.0",
                separator = ":",
                description = "description from mixin",
                descriptionHeading = "Mixin Description Heading%n",
                header = "Mixin Header",
                headerHeading = "Mixin Header Heading%n",
                footer = "Mixin Footer",
                footerHeading = "Mixin Footer Heading%n",
                optionListHeading = "Mixin Option List Heading%n",
                parameterListHeading = "Mixin Parameter List Heading%n",
                commandListHeading = "Mixin Command List Heading%n",
                requiredOptionMarker = '%',
                synopsisHeading = "Mixin Synopsis Heading%n",
                abbreviateSynopsis = true,
                customSynopsis = "Mixin custom synopsis",
                showDefaultValues = true,
                sortOptions = false)
        static class MixMeIn {}
    }

    @Test
    public void testMixinAnnotationCommandAttributes() throws Exception {
        class Receiver {
            @Mixin
            CommandAttributes.MixMeIn mixMeIn;
        }

        CommandLine commandLine = new CommandLine(new Receiver());
        verifyMixinCommandAttributes(commandLine);
    }

    @Test
    public void testAddMixinCommandAttributes() throws Exception {
        @Command class Receiver {}

        CommandLine commandLine = new CommandLine(new Receiver());
        commandLine.addMixin("mixmein", new CommandAttributes.MixMeIn());
        verifyMixinCommandAttributes(commandLine);
    }

    private void verifyMixinCommandAttributes(CommandLine commandLine) throws UnsupportedEncodingException {
        CommandSpec commandSpec = commandLine.getCommandSpec();
        assertEquals("mixmein", commandSpec.name());
        assertArrayEquals(new String[] {"Mixin 1.0"}, commandSpec.version());
        assertEquals(":", commandSpec.parser().separator());
        assertArrayEquals(new String[] {"description from mixin"}, commandSpec.usageMessage().description());
        assertEquals("Mixin Description Heading%n", commandSpec.usageMessage().descriptionHeading());
        assertArrayEquals(new String[] {"Mixin Header"}, commandSpec.usageMessage().header());
        assertEquals("Mixin Header Heading%n", commandSpec.usageMessage().headerHeading());
        assertArrayEquals(new String[] {"Mixin Footer"}, commandSpec.usageMessage().footer());
        assertEquals("Mixin Footer Heading%n", commandSpec.usageMessage().footerHeading());
        assertEquals("Mixin Option List Heading%n", commandSpec.usageMessage().optionListHeading());
        assertEquals("Mixin Parameter List Heading%n", commandSpec.usageMessage().parameterListHeading());
        assertEquals("Mixin Command List Heading%n", commandSpec.usageMessage().commandListHeading());
        assertEquals('%', commandSpec.usageMessage().requiredOptionMarker());
        assertEquals("Mixin Synopsis Heading%n", commandSpec.usageMessage().synopsisHeading());
        assertTrue("abbreviateSynopsis", commandSpec.usageMessage().abbreviateSynopsis());
        assertArrayEquals(new String[] {"Mixin custom synopsis"}, commandSpec.usageMessage().customSynopsis());
        assertTrue("showDefaultValues", commandSpec.usageMessage().showDefaultValues());
        assertFalse("sortOptions", commandSpec.usageMessage().sortOptions());

        String expected = String.format("" +
                "Mixin Header Heading%n" +
                "Mixin Header%n" +
                "Mixin Synopsis Heading%n" +
                "Mixin custom synopsis%n" +
                "Mixin Description Heading%n" +
                "description from mixin%n" +
                "Mixin Footer Heading%n" +
                "Mixin Footer%n");
        assertEquals(expected, usageString(commandLine, Help.Ansi.OFF));
    }

    static class CommandAttributesDontOverwriteReceiverAttributes {
        @Command(name = "mixmein",
                version = "Mixin 1.0",
                separator = ":",
                description = "description from mixin",
                descriptionHeading = "Mixin Description Heading%n",
                header = "Mixin Header",
                headerHeading = "Mixin Header Heading%n",
                footer = "Mixin Footer",
                footerHeading = "Mixin Footer Heading%n",
                optionListHeading = "Mixin Option List Heading%n",
                parameterListHeading = "Mixin Parameter List Heading%n",
                commandListHeading = "Mixin Command List Heading%n",
                requiredOptionMarker = '%',
                synopsisHeading = "Mixin Synopsis Heading%n",
                abbreviateSynopsis = true,
                customSynopsis = "Mixin custom synopsis",
                showDefaultValues = true,
                sortOptions = false)
        static class MixMeIn {}
    }

    @Test
    public void testMixinAnnotationCommandAttributesDontOverwriteReceiverAttributes() throws Exception {

        @Command(name = "receiver",
                version = "Receiver 1.0",
                separator = "~",
                description = "Receiver description",
                descriptionHeading = "Receiver Description Heading%n",
                header = "Receiver Header",
                headerHeading = "Receiver Header Heading%n",
                footer = "Receiver Footer",
                footerHeading = "Receiver Footer Heading%n",
                optionListHeading = "Receiver Option List Heading%n",
                parameterListHeading = "Receiver Parameter List Heading%n",
                commandListHeading = "Receiver Command List Heading%n",
                requiredOptionMarker = '#',
                synopsisHeading = "Receiver Synopsis Heading%n",
                customSynopsis = "Receiver custom synopsis")
        class Receiver {
            @Mixin
            CommandAttributesDontOverwriteReceiverAttributes.MixMeIn mixMeIn;
        }

        CommandLine commandLine = new CommandLine(new Receiver());
        verifyMixinCommandAttributesDontOverwriteReceiverAttributes(commandLine);
    }

    @Test
    public void testAddMixinCommandAttributesDontOverwriteReceiverAttributes() throws Exception {
        @Command(name = "receiver",
                version = "Receiver 1.0",
                separator = "~",
                description = "Receiver description",
                descriptionHeading = "Receiver Description Heading%n",
                header = "Receiver Header",
                headerHeading = "Receiver Header Heading%n",
                footer = "Receiver Footer",
                footerHeading = "Receiver Footer Heading%n",
                optionListHeading = "Receiver Option List Heading%n",
                parameterListHeading = "Receiver Parameter List Heading%n",
                commandListHeading = "Receiver Command List Heading%n",
                requiredOptionMarker = '#',
                synopsisHeading = "Receiver Synopsis Heading%n",
                customSynopsis = "Receiver custom synopsis")
        class Receiver {}

        CommandLine commandLine = new CommandLine(new Receiver(), new InnerClassFactory(this));
        commandLine.addMixin("mixMeIn", new CommandAttributesDontOverwriteReceiverAttributes.MixMeIn());
        verifyMixinCommandAttributesDontOverwriteReceiverAttributes(commandLine);
    }

    private void verifyMixinCommandAttributesDontOverwriteReceiverAttributes(CommandLine commandLine) throws UnsupportedEncodingException {
        CommandSpec commandSpec = commandLine.getCommandSpec();
        assertEquals("receiver", commandSpec.name());
        assertArrayEquals(new String[] {"Receiver 1.0"}, commandSpec.version());
        assertEquals("~", commandSpec.parser().separator());
        assertArrayEquals(new String[] {"Receiver description"}, commandSpec.usageMessage().description());
        assertEquals("Receiver Description Heading%n", commandSpec.usageMessage().descriptionHeading());
        assertArrayEquals(new String[] {"Receiver Header"}, commandSpec.usageMessage().header());
        assertEquals("Receiver Header Heading%n", commandSpec.usageMessage().headerHeading());
        assertArrayEquals(new String[] {"Receiver Footer"}, commandSpec.usageMessage().footer());
        assertEquals("Receiver Footer Heading%n", commandSpec.usageMessage().footerHeading());
        assertEquals("Receiver Option List Heading%n", commandSpec.usageMessage().optionListHeading());
        assertEquals("Receiver Parameter List Heading%n", commandSpec.usageMessage().parameterListHeading());
        assertEquals("Receiver Command List Heading%n", commandSpec.usageMessage().commandListHeading());
        assertEquals('#', commandSpec.usageMessage().requiredOptionMarker());
        assertEquals("Receiver Synopsis Heading%n", commandSpec.usageMessage().synopsisHeading());
        assertArrayEquals(new String[] {"Receiver custom synopsis"}, commandSpec.usageMessage().customSynopsis());

        String expected = String.format("" +
                "Receiver Header Heading%n" +
                "Receiver Header%n" +
                "Receiver Synopsis Heading%n" +
                "Receiver custom synopsis%n" +
                "Receiver Description Heading%n" +
                "Receiver description%n" +
                "Receiver Footer Heading%n" +
                "Receiver Footer%n");
        assertEquals(expected, usageString(commandLine, Help.Ansi.OFF));
    }

    static class SuperClassCommandAttributesDontOverwriteSubclassAttributes {
        @Command(name = "mixmein",
                version = "Mixin 1.0",
                separator = ":",
                description = "description from mixin",
                descriptionHeading = "Mixin Description Heading%n",
                header = "Mixin Header",
                headerHeading = "Mixin Header Heading%n",
                footer = "Mixin Footer",
                footerHeading = "Mixin Footer Heading%n",
                optionListHeading = "Mixin Option List Heading%n",
                parameterListHeading = "Mixin Parameter List Heading%n",
                commandListHeading = "Mixin Command List Heading%n",
                requiredOptionMarker = '%',
                synopsisHeading = "Mixin Synopsis Heading%n",
                abbreviateSynopsis = true,
                customSynopsis = "Mixin custom synopsis",
                showDefaultValues = true,
                sortOptions = false)
        static class MixMeInSuper {}

        @Command(name = "mixmeinSub",
                version = "MixinSub 1.0",
                separator = "~",
                description = "description from mixinSub",
                descriptionHeading = "MixinSub Description Heading%n",
                header = "MixinSub Header",
                headerHeading = "MixinSub Header Heading%n",
                footer = "MixinSub Footer",
                footerHeading = "MixinSub Footer Heading%n",
                optionListHeading = "MixinSub Option List Heading%n",
                parameterListHeading = "MixinSub Parameter List Heading%n",
                commandListHeading = "MixinSub Command List Heading%n",
                requiredOptionMarker = '#',
                synopsisHeading = "MixinSub Synopsis Heading%n",
                abbreviateSynopsis = true,
                customSynopsis = "MixinSub custom synopsis",
                showDefaultValues = true,
                sortOptions = false)
        static class MixMeInSub extends MixMeInSuper {}
    }

    @Test
    public void testMixinAnnotationSuperClassCommandAttributesDontOverwriteSubclassAttributes() throws Exception {
        class Receiver {
            @Mixin SuperClassCommandAttributesDontOverwriteSubclassAttributes.MixMeInSub mixMeIn;
        }

        CommandLine commandLine = new CommandLine(new Receiver());
        verifyMixinSuperClassCommandAttributesDontOverwriteSubclassAttributes(commandLine);
    }

    @Test
    public void testAddMixinSuperClassCommandAttributesDontOverwriteSubclassAttributes() throws Exception {
        @Command class Receiver {}

        CommandLine commandLine = new CommandLine(new Receiver());
        commandLine.addMixin("mixMeIn", new SuperClassCommandAttributesDontOverwriteSubclassAttributes.MixMeInSub());
        verifyMixinSuperClassCommandAttributesDontOverwriteSubclassAttributes(commandLine);
    }

    private void verifyMixinSuperClassCommandAttributesDontOverwriteSubclassAttributes(CommandLine commandLine) throws UnsupportedEncodingException {
        CommandSpec commandSpec = commandLine.getCommandSpec();
        assertEquals("mixmeinSub", commandSpec.name());
        assertArrayEquals(new String[] {"MixinSub 1.0"}, commandSpec.version());
        assertEquals("~", commandSpec.parser().separator());
        assertArrayEquals(new String[] {"description from mixinSub"}, commandSpec.usageMessage().description());
        assertEquals("MixinSub Description Heading%n", commandSpec.usageMessage().descriptionHeading());
        assertArrayEquals(new String[] {"MixinSub Header"}, commandSpec.usageMessage().header());
        assertEquals("MixinSub Header Heading%n", commandSpec.usageMessage().headerHeading());
        assertArrayEquals(new String[] {"MixinSub Footer"}, commandSpec.usageMessage().footer());
        assertEquals("MixinSub Footer Heading%n", commandSpec.usageMessage().footerHeading());
        assertEquals("MixinSub Option List Heading%n", commandSpec.usageMessage().optionListHeading());
        assertEquals("MixinSub Parameter List Heading%n", commandSpec.usageMessage().parameterListHeading());
        assertEquals("MixinSub Command List Heading%n", commandSpec.usageMessage().commandListHeading());
        assertEquals('#', commandSpec.usageMessage().requiredOptionMarker());
        assertEquals("MixinSub Synopsis Heading%n", commandSpec.usageMessage().synopsisHeading());
        assertArrayEquals(new String[] {"MixinSub custom synopsis"}, commandSpec.usageMessage().customSynopsis());

        String expected = String.format("" +
                "MixinSub Header Heading%n" +
                "MixinSub Header%n" +
                "MixinSub Synopsis Heading%n" +
                "MixinSub custom synopsis%n" +
                "MixinSub Description Heading%n" +
                "description from mixinSub%n" +
                "MixinSub Footer Heading%n" +
                "MixinSub Footer%n");
        assertEquals(expected, usageString(commandLine, Help.Ansi.OFF));
    }

    static class CombinesAttributes {
        @Command(name = "superName",
                version = "MixMeInSuper 1.0",
                separator = "$",
                description = "33",
                descriptionHeading = "333",
                header = "3333",
                headerHeading = "33333",
                //footer = "333 3",
                //footerHeading = "333 33",
                //optionListHeading = "333 333",
                parameterListHeading = "333 333 3",
                commandListHeading = "333 333 33",
                requiredOptionMarker = '3',
                synopsisHeading = "3333 3")
        static class MixMeInSuper {}

        @Command(description = "description from mixinSub",
                descriptionHeading = "MixinSub Description Heading%n",
                header = "MixinSub Header",
                headerHeading = "MixinSub Header Heading%n",
                //footer = "222",
                //footerHeading = "222 222",
                //optionListHeading = "222 222 222",
                parameterListHeading = "2 22",
                commandListHeading = "222 2",
                requiredOptionMarker = '2',
                synopsisHeading = "22222")
        static class MixMeInSub extends MixMeInSuper {}

        @Command(
                //name = "000 - set by MixinMeInSuper",
                //version = "0.0 - set by MixinMeInSuper",
                //separator = "0 - set by MixinMeInSuper",
                //description = "00 - set by MixMeInSub",
                //descriptionHeading = "000 - set by MixMeInSub",
                //header = "0000 - set by MixMeInSub",
                //headerHeading = "00000 - set by MixMeInSub",

                footer = "ReceiverSuper Footer",
                footerHeading = "ReceiverSuper Footer Heading%n",
                optionListHeading = "ReceiverSuper Option List Heading%n",
                parameterListHeading = "-1-1-1",
                commandListHeading = "--1--1--1",
                requiredOptionMarker = '1',
                synopsisHeading = "---1---1---1")
        static class ReceiverSuper {}
    }

    @Test
    public void testMixinAnnotationCombinesAttributes() throws Exception {
        @Command(parameterListHeading = "Receiver Parameter List Heading%n",
                commandListHeading = "Receiver Command List Heading%n",
                requiredOptionMarker = '#',
                synopsisHeading = "Receiver Synopsis Heading%n",
                //customSynopsis = "Receiver custom synopsis", // use standard generated synopsis
                showDefaultValues = true,
                sortOptions = false)
        class Receiver extends CombinesAttributes.ReceiverSuper {
            @Mixin CombinesAttributes.MixMeInSub mixMeIn;
            @Parameters(description = "some files") File[] files;
        }

        CommandLine commandLine = new CommandLine(new Receiver());
        verifyMixinCombinesAttributes(commandLine);
    }

    @Test
    public void testAddMixinCombinesAttributes() throws Exception {
        @Command(parameterListHeading = "Receiver Parameter List Heading%n",
                commandListHeading = "Receiver Command List Heading%n",
                requiredOptionMarker = '#',
                synopsisHeading = "Receiver Synopsis Heading%n",
                //customSynopsis = "Receiver custom synopsis", // use standard generated synopsis
                showDefaultValues = true,
                sortOptions = false)
        class Receiver extends CombinesAttributes.ReceiverSuper {
            @Parameters(description = "some files") File[] files;
        }

        CommandLine commandLine = new CommandLine(new Receiver());
        commandLine.addMixin("mixMeIn", new CombinesAttributes.MixMeInSub());

        verifyMixinCombinesAttributes(commandLine);
    }

    private void verifyMixinCombinesAttributes(CommandLine commandLine) throws UnsupportedEncodingException {
        CommandSpec commandSpec = commandLine.getCommandSpec();
        assertEquals("superName", commandSpec.name());
        assertArrayEquals(new String[] {"MixMeInSuper 1.0"}, commandSpec.version());
        assertEquals("$", commandSpec.parser().separator());

        UsageMessageSpec usage = commandSpec.usageMessage();
        assertArrayEquals(new String[] {"description from mixinSub"}, usage.description());
        assertEquals("MixinSub Description Heading%n", usage.descriptionHeading());
        assertArrayEquals(new String[] {"MixinSub Header"}, usage.header());
        assertEquals("MixinSub Header Heading%n", usage.headerHeading());

        assertArrayEquals(new String[] {"ReceiverSuper Footer"}, usage.footer());
        assertEquals("ReceiverSuper Footer Heading%n", usage.footerHeading());
        assertEquals("ReceiverSuper Option List Heading%n", usage.optionListHeading());

        assertEquals("Receiver Parameter List Heading%n", usage.parameterListHeading());
        assertEquals("Receiver Command List Heading%n", usage.commandListHeading());
        assertEquals('#', usage.requiredOptionMarker());
        assertEquals("Receiver Synopsis Heading%n", usage.synopsisHeading());
        assertArrayEquals(new String[0], usage.customSynopsis());

        String expected = String.format("" +
                "MixinSub Header Heading%n" +
                "MixinSub Header%n" +
                "Receiver Synopsis Heading%n" +
                "superName [<files>...]%n" +
                "MixinSub Description Heading%n" +
                "description from mixinSub%n" +
                "Receiver Parameter List Heading%n" +
                "      [<files>...]   some files%n" +
                "ReceiverSuper Footer Heading%n" +
                "ReceiverSuper Footer%n");
        assertEquals(expected, usageString(commandLine, Help.Ansi.OFF));
    }

    static class InjectsOptionsAndParameters {
        static class MixMeIn {
            @Option(names = {"-a", "--alpha"}, description = "option from mixin")
            private int alpha;

            @Parameters(description = "parameters from mixin")
            File[] files;
        }
    }
    @Test
    public void testMixinAnnotationInjectsOptionsAndParameters() throws UnsupportedEncodingException {
        @Command(sortOptions = false)
        class Receiver {
            @Option(names = {"-b", "--beta"}, description = "Receiver option")
            private int beta;

            @Parameters(description = "parameters from receiver")
            File[] receiverFiles;

            @Mixin
            InjectsOptionsAndParameters.MixMeIn mixMeIn;
        }
        CommandLine commandLine = new CommandLine(new Receiver());
        verifyMixinInjectsOptionsAndParameters(commandLine);
    }

    @Test
    public void testAddMixinInjectsOptionsAndParameters() throws UnsupportedEncodingException {
        @Command(sortOptions = false)
        class Receiver {
            @Option(names = {"-b", "--beta"}, description = "Receiver option")
            private int beta;

            @Parameters(description = "parameters from receiver")
            File[] receiverFiles;
        }
        CommandLine commandLine = new CommandLine(new Receiver());
        commandLine.addMixin("mixMeIn", new InjectsOptionsAndParameters.MixMeIn());
        verifyMixinInjectsOptionsAndParameters(commandLine);
    }

    private void verifyMixinInjectsOptionsAndParameters(CommandLine commandLine) throws UnsupportedEncodingException {
        CommandSpec commandSpec = commandLine.getCommandSpec();
        assertEquals(2, commandSpec.options().size());
        assertArrayEquals(new String[]{"-b", "--beta"},  commandSpec.options().get(0).names());
        assertArrayEquals(new String[]{"-a", "--alpha"}, commandSpec.options().get(1).names());

        assertTrue(commandSpec.optionsMap().containsKey("--alpha"));
        assertTrue(commandSpec.optionsMap().containsKey("--beta"));
        assertTrue(commandSpec.optionsMap().containsKey("-a"));
        assertTrue(commandSpec.optionsMap().containsKey("-b"));

        assertTrue(commandSpec.posixOptionsMap().containsKey('a'));
        assertTrue(commandSpec.posixOptionsMap().containsKey('b'));

        assertEquals(2, commandSpec.positionalParameters().size());
        assertEquals("<receiverFiles>", commandSpec.positionalParameters().get(0).paramLabel());
        assertEquals("<files>",         commandSpec.positionalParameters().get(1).paramLabel());

        String expects = String.format("" +
                "Usage: <main class> [-a=<alpha>] [-b=<beta>] [<receiverFiles>...] [<files>...]%n" +
                "      [<receiverFiles>...]   parameters from receiver%n" +
                "      [<files>...]           parameters from mixin%n" +
                "  -b, --beta=<beta>          Receiver option%n" +
                "  -a, --alpha=<alpha>        option from mixin%n");
        assertEquals(expects, usageString(commandLine, Help.Ansi.OFF));
    }
    @Test
    public void testMixinAnnotationParsesOptionsAndParameters() throws UnsupportedEncodingException {
        @Command(sortOptions = false)
        class Receiver {
            @Option(names = {"-b", "--beta"}, description = "Receiver option")
            private int beta;

            @Parameters(description = "parameters from receiver")
            File[] receiverFiles;

            @Mixin
            InjectsOptionsAndParameters.MixMeIn mixMeIn;
        }
        CommandLine commandLine = new CommandLine(new Receiver());
        commandLine.parseArgs("-a", "111", "-b", "222", "a", "b");
        Receiver receiver = commandLine.getCommand();
        assertEquals(222, receiver.beta);
        assertEquals(111, receiver.mixMeIn.alpha);
        assertArrayEquals(new File[] {new File("a"), new File("b")}, receiver.receiverFiles);
        assertArrayEquals(new File[] {new File("a"), new File("b")}, receiver.mixMeIn.files);
    }

    @Test
    public void testAddMixinParsesOptionsAndParameters() throws UnsupportedEncodingException {
        @Command(sortOptions = false)
        class Receiver {
            @Option(names = {"-b", "--beta"}, description = "Receiver option")
            private int beta;

            @Parameters(description = "parameters from receiver")
            File[] receiverFiles;
        }
        CommandLine commandLine = new CommandLine(new Receiver());
        InjectsOptionsAndParameters.MixMeIn mixin = new InjectsOptionsAndParameters.MixMeIn();
        commandLine.addMixin("mixin", mixin);

        commandLine.parseArgs("-a", "111", "-b", "222", "a", "b");
        Receiver receiver = commandLine.getCommand();
        assertEquals(222, receiver.beta);
        assertEquals(111, mixin.alpha);
        assertArrayEquals(new File[] {new File("a"), new File("b")}, receiver.receiverFiles);
        assertArrayEquals(new File[] {new File("a"), new File("b")}, mixin.files);

        assertSame(mixin, commandLine.getMixins().get("mixin"));
        assertSame(mixin, commandLine.getCommandSpec().mixins().get("mixin").userObject());
    }

    @Test
    public void testMixinAnnotationInjectsOptionsAndParametersInDeclarationOrder() throws Exception {
        @Command(sortOptions = false)
        class Receiver {
            @Mixin
            InjectsOptionsAndParameters.MixMeIn mixMeIn;

            @Option(names = {"-b", "--beta"}, description = "Receiver option")
            private int beta;

            @Parameters(description = "parameters from receiver")
            File[] receiverFiles;
        }
        CommandLine commandLine = new CommandLine(new Receiver());
        CommandSpec commandSpec = commandLine.getCommandSpec();
        assertEquals(2, commandSpec.options().size());
        assertArrayEquals(new String[]{"-a", "--alpha"}, commandSpec.options().get(0).names());
        assertArrayEquals(new String[]{"-b", "--beta"},  commandSpec.options().get(1).names());

        assertEquals(2, commandSpec.positionalParameters().size());
        assertEquals("<files>",         commandSpec.positionalParameters().get(0).paramLabel());
        assertEquals("<receiverFiles>", commandSpec.positionalParameters().get(1).paramLabel());

        String expects = String.format("" +
                "Usage: <main class> [-a=<alpha>] [-b=<beta>] [<files>...] [<receiverFiles>...]%n" +
                "      [<files>...]           parameters from mixin%n" +
                "      [<receiverFiles>...]   parameters from receiver%n" +
                "  -a, --alpha=<alpha>        option from mixin%n" +
                "  -b, --beta=<beta>          Receiver option%n");
        assertEquals(expects, usageString(commandLine, Help.Ansi.OFF));
    }

    @Test
    public void testMixinAnnotationRejectsDuplicateOptions() {
        class MixMeInDuplicate {
            @Option(names = {"-a", "--alpha"}, description = "option from mixin")
            private int alpha;
        }

        class ReceiverDuplicate {
            @Option(names = {"-a"}, description = "Receiver option")
            private int beta;

            @Mixin
            MixMeInDuplicate mixMeIn;
        }
        try {
            new CommandLine(new ReceiverDuplicate(), new InnerClassFactory(this));
            fail("Expected exception");
        } catch (DuplicateOptionAnnotationsException ex) {
            assertEquals("Option name '-a' is used by both field int picocli.MixinTest$1MixMeInDuplicate.alpha and field int picocli.MixinTest$1ReceiverDuplicate.beta", ex.getMessage());
        }
    }

    @Test
    public void testMixinAnnotationWithSubcommands() {
        @Command(name = "mixinsub")
        class MixedInSubCommand {}

        @Command(subcommands = MixedInSubCommand.class)
        class MixMeIn {}

        class Receiver {
            @Mixin
            MixMeIn mixMeIn;
        }
        CommandLine commandLine = new CommandLine(new Receiver(), new InnerClassFactory(this));
        CommandSpec commandSpec = commandLine.getCommandSpec();

        assertEquals(1, commandLine.getSubcommands().size());
        assertEquals(1, commandSpec.subcommands().size());
        CommandLine subcommandLine = commandSpec.subcommands().get("mixinsub");
        assertSame(subcommandLine, commandLine.getSubcommands().get("mixinsub"));
        assertTrue(((Object) subcommandLine.getCommand()) instanceof MixedInSubCommand);
    }

    @Test
    public void testMixinAnnotationCanBeRetrievedByAnnotationName() {
        @Command class MixMeIn {}

        @Command class Receiver {
            @Mixin(name = "aMixin") MixMeIn mixMeIn;
        }
        CommandLine commandLine = new CommandLine(new Receiver(), new InnerClassFactory(this));
        assertFalse("mixin was registered", commandLine.getMixins().isEmpty());
        assertTrue(commandLine.getMixins().get("aMixin") instanceof MixMeIn);

        Receiver receiver = commandLine.getCommand();
        assertNotNull(receiver.mixMeIn);
        assertSame(receiver.mixMeIn, commandLine.getMixins().get("aMixin"));
        assertSame(receiver.mixMeIn, commandLine.getCommandSpec().mixins().get("aMixin").userObject());
    }

    @Test
    public void testMixinAnnotationCanBeRetrievedByFieldName() {
        @Command class MixMeIn {}

        @Command class Receiver {
            @Mixin MixMeIn mixMeIn;
        }
        CommandLine commandLine = new CommandLine(new Receiver(), new InnerClassFactory(this));
        assertFalse("mixin was registered", commandLine.getMixins().isEmpty());
        assertTrue(commandLine.getMixins().get("mixMeIn") instanceof MixMeIn);

        Receiver receiver = commandLine.getCommand();
        assertNotNull(receiver.mixMeIn);
        assertSame(receiver.mixMeIn, commandLine.getMixins().get("mixMeIn"));
        assertSame(receiver.mixMeIn, commandLine.getCommandSpec().mixins().get("mixMeIn").userObject());
    }

    @Test
    public void testAddMixin_CanBeRetrievedByFieldName() {
        @Command class MixMeIn {}
        @Command class Receiver {}

        CommandLine commandLine = new CommandLine(new Receiver(), new InnerClassFactory(this));
        commandLine.addMixin("mixin", new MixMeIn());
        assertFalse("mixin was registered", commandLine.getMixins().isEmpty());
        assertTrue(commandLine.getMixins().get("mixin") instanceof MixMeIn);
    }


    @Test
    public void testMixinStandardHelpOptions_AreAddedLast() {
        @Command(mixinStandardHelpOptions = true, sortOptions = false) class App {
            @Option(names = "-a", description = "a option") boolean aOpt;
            @Option(names = "-z", description = "z option") boolean zOpt;
        }
        CommandLine commandLine = new CommandLine(new App(), new InnerClassFactory(this));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        commandLine.usage(new PrintStream(baos));

        String expected = String.format("" +
                "Usage: <main class> [-ahVz]%n" +
                "  -a              a option%n" +
                "  -z              z option%n" +
                "  -h, --help      Show this help message and exit.%n" +
                "  -V, --version   Print version information and exit.%n"
        );
        assertEquals(expected, baos.toString());
    }

    static class Issue439Mixin {
        @Spec CommandSpec spec;

        @Option(names = "--trex")
        void setTRexFences(final String value) {
            throw new ParameterException(spec.commandLine(), "TREX error");
        }
    }

    static class Issue439Command {
        @Mixin Issue439Mixin mixin;
        @Spec CommandSpec spec;

        @Option(names = "--raptor")
        void setRaptorFences(final String value) {
            throw new ParameterException(spec.commandLine(), "RAPTOR error");
        }
    }

    @Test
    public void testIssue439InjectedSpecInMixinHasNullCommandLineAnnotations() {
        CommandLine cmd = new CommandLine(new Issue439Command());
        assertExceptionThrownFromSetter(cmd);
    }

    @Test
    public void testIssue439InjectedSpecInMixinHasNullCommandLineProgrammatic() {
        final CommandSpec mixinSpec = CommandSpec.create();
        ISetter trexSetter = new ISetter() {
            public <T> T set(T value) {
                throw new ParameterException(mixinSpec.commandLine(), "TREX error");
            }
        };
        mixinSpec.addOption(OptionSpec.builder("--trex").
                type(String.class).setter(trexSetter).build());

        final CommandSpec commandSpec = CommandSpec.create();
        commandSpec.addMixin("mixin", mixinSpec);
        ISetter raptorSetter = new ISetter() {
            public <T> T set(T value) {
                throw new ParameterException(commandSpec.commandLine(), "RAPTOR error");
            }
        };
        commandSpec.addOption(OptionSpec.builder("--raptor").
                type(String.class).setter(raptorSetter).build());

        CommandLine cmd = new CommandLine(commandSpec);
        assertExceptionThrownFromSetter(cmd);
    }

    private void assertExceptionThrownFromSetter(CommandLine cmd) {
        try {
            cmd.parseArgs("--trex", "abc");
            fail("expected ParameterException");
        } catch (ParameterException ex) {
            assertEquals("TREX error", ex.getMessage());
        }
        try {
            cmd.parseArgs("--raptor", "xyz");
            fail("expected ParameterException");
        } catch (ParameterException ex) {
            assertEquals("RAPTOR error", ex.getMessage());
        }
    }

    @Command(name="super")
    static class SuperClass {
    }

    @Command(name="sub")
    static class SubClass extends SuperClass {
        @Command(name="method")
        public void method() {}
    }
    @Command(name="main", subcommands= {SuperClass.class, SubClass.class})
    static class Main {
    }

    @Test
    public void testIssue619MethodSubcommandInSubclassAddedTwice() {
        //setTraceLevel(CommandLine.TraceLevel.DEBUG);
        CommandLine commandLine = new CommandLine(new Main());
        assertEquals(2, commandLine.getSubcommands().size());

        CommandLine zuper = commandLine.getSubcommands().get("super");
        assertEquals(0, zuper.getSubcommands().size());

        CommandLine sub = commandLine.getSubcommands().get("sub");
        assertEquals(1, sub.getSubcommands().size());

        CommandLine method = sub.getSubcommands().get("method");
        assertEquals(0, method.getSubcommands().size());
    }

    @Command
    static class Issue924 {
        @Mixin Issue924Mixin myMixin;

        Issue924Mixin methodMixin;

        @Command
        void subcommand(@Mixin(name = "blah") Issue924Mixin myMixin) {
            methodMixin = myMixin;
        }
    }
    static class Issue924Mixin {
        @Option(names = "-v") boolean verbose;
    }
    @Test
    public void testIssue924mixinAnnotatedElement() {
        Issue924 userObject = new Issue924();
        CommandLine commandLine = new CommandLine(userObject);
        assertNotNull(userObject.myMixin);

        Map<String, IAnnotatedElement> mixinElements = commandLine.getCommandSpec().mixinAnnotatedElements();
        assertEquals(1, mixinElements.size());
        IAnnotatedElement element = mixinElements.get("myMixin");
        assertNotNull(element);
        assertEquals("myMixin", element.getName());
        assertEquals(Issue924Mixin.class, element.getTypeInfo().getType());
        assertTrue(String.valueOf(element.getter()), element.getter() instanceof FieldBinding);

        CommandLine subcommand = commandLine.getSubcommands().get("subcommand");
        Map<String, IAnnotatedElement> mixinMethodElements = subcommand.getCommandSpec().mixinAnnotatedElements();
        assertEquals(1, mixinMethodElements.size());
        IAnnotatedElement methodElement = mixinMethodElements.get("blah");
        assertNotNull(methodElement);
        assertEquals("arg0", methodElement.getName());
        assertEquals(Issue924Mixin.class, methodElement.getTypeInfo().getType());
    }

    @Command(subcommands = Issue925Sub.class)
    static class Issue925Top {
        boolean verbose;

        @Option(names = "-v")
        public void setVerbose(boolean verbose) {
            this.verbose = verbose;
        }
    }

    // Define a mixin that delegates to the parent command.
    static class Issue925MyMixin {
        @ParentCommand Issue925Top top;

        @Option(names = "-v")
        public void setVerbose(boolean verbose) {
            top.setVerbose(verbose);
        }
    }

    // Now subcommands just need to mix in the `MyMixin`
    // to get a `-v` option that delegates to the parent command.
    @Command(name = "sub")
    static class Issue925Sub {
        @Mixin Issue925MyMixin mymixin;

        // This works for @Command-annotated methods also.
        // But needs to have the correct parent!
        @Command(name = "subsub")
        void subsub(@Mixin(name = "key") Issue925SubMixin mymixin) {
            // business logic for the subsub command...
        }
    }
    static class Issue925SubMixin {
        @ParentCommand Issue925Sub sub;

        @Option(names = "-v")
        public void setVerbose(boolean verbose) {
            sub.mymixin.setVerbose(verbose);
        }
    }

    @Test
    public void testIssue925ParentCommandAnnotationInMixins() {
        Issue925Top top = new Issue925Top();
        CommandLine commandLine = new CommandLine(top);

        Issue925Sub subCmdObject = commandLine.getSubcommands().get("sub").getCommand();
        assertNotNull(subCmdObject.mymixin);
        assertNotNull(subCmdObject.mymixin.top);
        assertSame(top, subCmdObject.mymixin.top);

        CommandSpec subsub = commandLine.getSubcommands().get("sub").getSubcommands().get("subsub").getCommandSpec();
        assertEquals(1, subsub.mixins().size());
        CommandSpec mixin = subsub.mixins().get("key");
        Issue925SubMixin mixinInstance = (Issue925SubMixin) mixin.userObject();
        assertNotNull(mixinInstance.sub);
        assertSame(subCmdObject, mixinInstance.sub);
    }

    static class VerbosityMixin {
        @Spec CommandSpec mySpec;
        @Spec(Spec.Target.MIXEE) CommandSpec commandSpec;

        @Option(names = "-v") boolean verbose;
    }

    @Test
    public void testMixinSpecTarget() {
        class MyApp {
            @Mixin VerbosityMixin verbosityMixin;
        }

        MyApp myApp = new MyApp();
        new CommandLine(myApp).parseArgs("-v");
        assertNotNull(myApp.verbosityMixin.mySpec);
        assertNotNull(myApp.verbosityMixin.commandSpec);

        assertNotSame(myApp.verbosityMixin.commandSpec, myApp.verbosityMixin.mySpec);
        assertTrue(myApp.verbosityMixin.mySpec.userObject() instanceof VerbosityMixin);

        assertSame(myApp, myApp.verbosityMixin.commandSpec.userObject());
    }

    @Command(usageHelpWidth = 123 )
    static class MixinWidth {}

    @Test
    public void testMixinWidth() {
        class MyApp {
            @Mixin
            MixinWidth mixin;
        }
        CommandLine cmd = new CommandLine(new MyApp());
        assertEquals(123, cmd.getCommandSpec().usageMessage().width());
    }

    @Test
    public void testMixinLongOptionsMaxWidth() {
        CommandSpec spec = CommandSpec.create();
        assertNotEquals(43, spec.usageMessage().longOptionsMaxWidth());
        spec.usageMessage().longOptionsMaxWidth(43);

        CommandSpec another = CommandSpec.create();
        another.addMixin("a", spec);

        assertEquals(43, another.usageMessage().longOptionsMaxWidth());
    }

    @Command(aliases = {"ls"} )
    public static class ListAliasMixin {
    }

    @Command(name="list")
    public static class MyListCommand {
        @Mixin() public ListAliasMixin aliasMixin;
    }

    @Command(subcommands = {MyListCommand.class})
    public static class App_Issue1836 {
    }

    @Test
    public void testIssue1836CommandAliasOnMixin() {
        Help help = new Help(new App_Issue1836());
        assertEquals("list, ls", help.commandList().trim());
    }
}
