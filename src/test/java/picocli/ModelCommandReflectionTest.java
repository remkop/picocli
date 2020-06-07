package picocli;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;
import picocli.CommandLine.*;
import picocli.CommandLine.Model.CommandSpec;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class ModelCommandReflectionTest {

    // allows tests to set any kind of properties they like, without having to individually roll them back
    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");

    @Test
    public void testExtractCommandSpec() throws Exception {
        Class<?> reflection = Class.forName("picocli.CommandLine$Model$CommandReflection");
        Method extractCommandSpec = reflection.getDeclaredMethod("extractCommandSpec", Object.class, CommandLine.IFactory.class, boolean.class);

        CommandLine.IFactory myFactory = new CommandLine.IFactory() {
            public <K> K create(Class<K> cls) {
                throw new InitializationException("boom");
            }
        };

        CommandSpec spec = (CommandSpec) extractCommandSpec.invoke(null, Object.class, myFactory, false);
        try {
            spec.userObject();
            fail("expected Exception");
        } catch (InitializationException ex) {
            //assertEquals("Could not instantiate class java.lang.Object: picocli.CommandLine$InitializationException: boom", ex.getMessage());
            assertEquals("boom", ex.getMessage());
        }
    }

    @Command(subcommands = InvalidSub.class)
    static class InvalidTop {}

    @Command(name = "invalidsub")
    static class InvalidSub {
        public InvalidSub(int x) {}
    }

    @Test
    public void testInitSubcommands() {
        try {
            CommandSpec.forAnnotatedObject(InvalidTop.class);
        } catch (InitializationException ex) {
            assertEquals("Cannot instantiate subcommand picocli.ModelCommandReflectionTest$InvalidSub: the class has no constructor", ex.getMessage());
        }
    }

    @Command(subcommands = InvalidSub2.class)
    static class InvalidTop2 {}

    static class InvalidSub2 {
        @Option(names = "-x") int x;
    }

    @Test
    public void testSubcommandName() {
        try {
            CommandSpec.forAnnotatedObject(InvalidTop2.class);
        } catch (InitializationException ex) {
            assertEquals("Subcommand picocli.ModelCommandReflectionTest$InvalidSub2 is missing the mandatory @Command annotation with a 'name' attribute", ex.getMessage());
        }
    }

    static class ValidateArgSpecField {
        @Mixin
        @Option(names = "-x")
        int x;

        @Option(names = "-final")
        final Object f = new Object();

        int neither;
    }

    @Test
    public void testValidateArgSpecField() throws Exception {
        Class<?> reflection = Class.forName("picocli.CommandLine$Model$CommandReflection");
        Method validateArgSpecField = reflection.getDeclaredMethod("validateArgSpecMember", CommandLine.Model.TypedMember.class);
        validateArgSpecField.setAccessible(true);

        CommandLine.Model.TypedMember typedMember = new CommandLine.Model.TypedMember(ValidateArgSpecField.class.getDeclaredField("x"));
        try {
            validateArgSpecField.invoke(null, typedMember);
            fail("expected Exception");
        } catch (InvocationTargetException ite) {
            CommandLine.DuplicateOptionAnnotationsException ex = (CommandLine.DuplicateOptionAnnotationsException) ite.getCause();
            assertEquals("A member cannot have both @Option and @Mixin annotations, but 'int picocli.ModelCommandReflectionTest$ValidateArgSpecField.x' has both.", ex.getMessage());
        }
    }

    @Test
    public void testValidateArgSpecField_final() throws Exception {
        Class<?> reflection = Class.forName("picocli.CommandLine$Model$CommandReflection");
        Method validateArgSpecField = reflection.getDeclaredMethod("validateArgSpecMember", CommandLine.Model.TypedMember.class);
        validateArgSpecField.setAccessible(true);

        CommandLine.Model.TypedMember typedMember = new CommandLine.Model.TypedMember(ValidateArgSpecField.class.getDeclaredField("f"));
        validateArgSpecField.invoke(null, typedMember); // no error
    }

    @Ignore
    @Test
    public void testValidateArgSpecField_neither() throws Exception {
        Class<?> reflection = Class.forName("picocli.CommandLine$Model$CommandReflection");
        Method validateArgSpecField = reflection.getDeclaredMethod("validateArgSpecMember", CommandLine.Model.TypedMember.class);
        validateArgSpecField.setAccessible(true);

        CommandLine.Model.TypedMember typedMember = new CommandLine.Model.TypedMember(ValidateArgSpecField.class.getDeclaredField("neither"));
        validateArgSpecField.invoke(null, typedMember); // no error
    }

    static class ValidateInjectSpec {
        int notAnnotated;

        @Spec
        @Option(names = "-x")
        int x;

        @Spec
        @CommandLine.Parameters
        int y;

        @Spec
        @Unmatched
        List<String> unmatched;

        @Spec
        @Mixin
        Object mixin = new Object();

        @Spec
        Object invalidType;
    }
    @Test
    public void testValidateInjectSpec() throws Exception {
        Class<?> reflection = Class.forName("picocli.CommandLine$Model$CommandReflection");
        Method validateInjectSpec = reflection.getDeclaredMethod("validateInjectSpec", CommandLine.Model.TypedMember.class);
        validateInjectSpec.setAccessible(true);

        CommandLine.Model.TypedMember typedMember = new CommandLine.Model.TypedMember(ValidateInjectSpec.class.getDeclaredField("notAnnotated"));
        try {
            validateInjectSpec.invoke(null, typedMember);
            fail("expected Exception");
        } catch (InvocationTargetException ite) {
            IllegalStateException ex = (IllegalStateException) ite.getCause();
            assertEquals("Bug: validateInjectSpec() should only be called with @Spec members", ex.getMessage());
        }
    }

    @Test
    public void testValidateInjectSpec_Option() throws Exception {
        Class<?> reflection = Class.forName("picocli.CommandLine$Model$CommandReflection");
        Method validateInjectSpec = reflection.getDeclaredMethod("validateInjectSpec", CommandLine.Model.TypedMember.class);
        validateInjectSpec.setAccessible(true);

        CommandLine.Model.TypedMember typedMember = new CommandLine.Model.TypedMember(ValidateInjectSpec.class.getDeclaredField("x"));
        try {
            validateInjectSpec.invoke(null, typedMember);
            fail("expected Exception");
        } catch (InvocationTargetException ite) {
            CommandLine.DuplicateOptionAnnotationsException ex = (CommandLine.DuplicateOptionAnnotationsException) ite.getCause();
            assertEquals("A member cannot have both @Spec and @Option annotations, but 'int picocli.ModelCommandReflectionTest$ValidateInjectSpec.x' has both.", ex.getMessage());
        }
    }

    @Test
    public void testValidateInjectSpec_Positional() throws Exception {
        Class<?> reflection = Class.forName("picocli.CommandLine$Model$CommandReflection");
        Method validateInjectSpec = reflection.getDeclaredMethod("validateInjectSpec", CommandLine.Model.TypedMember.class);
        validateInjectSpec.setAccessible(true);

        CommandLine.Model.TypedMember typedMember = new CommandLine.Model.TypedMember(ValidateInjectSpec.class.getDeclaredField("y"));
        try {
            validateInjectSpec.invoke(null, typedMember);
            fail("expected Exception");
        } catch (InvocationTargetException ite) {
            CommandLine.DuplicateOptionAnnotationsException ex = (CommandLine.DuplicateOptionAnnotationsException) ite.getCause();
            assertEquals("A member cannot have both @Spec and @Parameters annotations, but 'int picocli.ModelCommandReflectionTest$ValidateInjectSpec.y' has both.", ex.getMessage());
        }
    }

    @Test
    public void testValidateInjectSpec_Unmatched() throws Exception {
        Class<?> reflection = Class.forName("picocli.CommandLine$Model$CommandReflection");
        Method validateInjectSpec = reflection.getDeclaredMethod("validateInjectSpec", CommandLine.Model.TypedMember.class);
        validateInjectSpec.setAccessible(true);

        CommandLine.Model.TypedMember typedMember = new CommandLine.Model.TypedMember(ValidateInjectSpec.class.getDeclaredField("unmatched"));
        try {
            validateInjectSpec.invoke(null, typedMember);
            fail("expected Exception");
        } catch (InvocationTargetException ite) {
            CommandLine.DuplicateOptionAnnotationsException ex = (CommandLine.DuplicateOptionAnnotationsException) ite.getCause();
            assertEquals("A member cannot have both @Spec and @Unmatched annotations, but 'java.util.List picocli.ModelCommandReflectionTest$ValidateInjectSpec.unmatched' has both.", ex.getMessage());
        }
    }

    @Test
    public void testValidateInjectSpec_Mixin() throws Exception {
        Class<?> reflection = Class.forName("picocli.CommandLine$Model$CommandReflection");
        Method validateInjectSpec = reflection.getDeclaredMethod("validateInjectSpec", CommandLine.Model.TypedMember.class);
        validateInjectSpec.setAccessible(true);

        CommandLine.Model.TypedMember typedMember = new CommandLine.Model.TypedMember(ValidateInjectSpec.class.getDeclaredField("mixin"));
        try {
            validateInjectSpec.invoke(null, typedMember);
            fail("expected Exception");
        } catch (InvocationTargetException ite) {
            CommandLine.DuplicateOptionAnnotationsException ex = (CommandLine.DuplicateOptionAnnotationsException) ite.getCause();
            assertEquals("A member cannot have both @Spec and @Mixin annotations, but 'java.lang.Object picocli.ModelCommandReflectionTest$ValidateInjectSpec.mixin' has both.", ex.getMessage());
        }
    }

    @Test
    public void testValidateInjectSpec_FieldType() throws Exception {
        Class<?> reflection = Class.forName("picocli.CommandLine$Model$CommandReflection");
        Method validateInjectSpec = reflection.getDeclaredMethod("validateInjectSpec", CommandLine.Model.TypedMember.class);
        validateInjectSpec.setAccessible(true);

        CommandLine.Model.TypedMember typedMember = new CommandLine.Model.TypedMember(ValidateInjectSpec.class.getDeclaredField("invalidType"));
        try {
            validateInjectSpec.invoke(null, typedMember);
            fail("expected Exception");
        } catch (InvocationTargetException ite) {
            InitializationException ex = (InitializationException) ite.getCause();
            assertEquals("@picocli.CommandLine.Spec annotation is only supported on fields of type picocli.CommandLine$Model$CommandSpec", ex.getMessage());
        }
    }
    @Test
    public void testValidateArgSpec() throws Exception {
        Class<?> reflection = Class.forName("picocli.CommandLine$Model$CommandReflection");
        Method validateArgSpec = reflection.getDeclaredMethod("validateArgSpecMember", CommandLine.Model.TypedMember.class);
        validateArgSpec.setAccessible(true);

        CommandLine.Model.TypedMember typedMember = new CommandLine.Model.TypedMember(ValidateInjectSpec.class.getDeclaredField("notAnnotated"));
        try {
            validateArgSpec.invoke(null, typedMember);
            fail("expected Exception");
        } catch (InvocationTargetException ite) {
            IllegalStateException ex = (IllegalStateException) ite.getCause();
            assertEquals("Bug: validateArgSpecMember() should only be called with an @Option or @Parameters member", ex.getMessage());
        }
    }

    @Command(mixinStandardHelpOptions = true)
    static class ValidMixin {
    }
    @Command
    static class MixeeInstantiated {
        @Mixin
        ValidMixin mixin = new ValidMixin();
    }
    @Command
    static class MixeeUninstantiated {
        @Mixin
        ValidMixin mixin;
    }

    @Test
    public void testBuildMixinForField_valid() {
        CommandSpec commandSpec = CommandSpec.forAnnotatedObject(new MixeeInstantiated());
        assertNotNull(commandSpec.findOption("h"));
    }

    @Test
    public void testBuildMixinForField_invalid() {
        CommandLine.IFactory myFactory = new CommandLine.IFactory() {
            public <K> K create(Class<K> cls) {
                throw new IllegalStateException("boom");
            }
        };

        try {
            CommandSpec.forAnnotatedObject(new MixeeUninstantiated(), myFactory);
        } catch (InitializationException ex) {
            assertEquals("Could not access or modify mixin member picocli.ModelCommandReflectionTest$ValidMixin picocli.ModelCommandReflectionTest$MixeeUninstantiated.mixin: java.lang.IllegalStateException: boom", ex.getMessage());
        }
    }

    @Command
    class MyUnmatched {
        @Unmatched
        List raw;
    }
    @Test(expected = InitializationException.class)
    public void testCommandReflection_buildUnmatchedForField_raw() {
        CommandSpec.forAnnotatedObject(new MyUnmatched());
    }

    @Command
    class MyUnmatched2 {
        @Unmatched
        List<String> raw = new ArrayList<String>();
    }
    @Test
    public void testBuildUnmatchedForField_valid() {
        CommandSpec.forAnnotatedObject(new MyUnmatched2());
    }
}
