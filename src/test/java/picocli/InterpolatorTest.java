package picocli;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;
import picocli.CommandLine.Model.Interpolator;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Parameters;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Vector;

import static org.junit.Assert.*;

public class InterpolatorTest {

    // allows tests to set any kind of properties they like, without having to individually roll them back
    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");

    @Test
    public void interpolateCommandName() {
        CommandSpec hierarchy = createTestSpec();
        CommandSpec spec = hierarchy.subcommands().get("sub").getSubcommands().get("subsub").getCommandSpec();
        Interpolator interpolator = new Interpolator(spec);
        String original = "This is a description for ${COMMAND-NAME}, whose fqcn is ${COMMAND-FULL-NAME}. It's parent is ${PARENT-COMMAND-NAME}, also known as ${PARENT-COMMAND-FULL-NAME}. It's root is ${ROOT-COMMAND-NAME}.";
        String expected = "This is a description for subsub, whose fqcn is top sub subsub. It's parent is sub, also known as top sub. It's root is top.";
        assertEquals(expected, interpolator.interpolate(original));
    }

    @Test
    public void notInterpolateIfEscaped() {
        CommandSpec hierarchy = createTestSpec();
        Interpolator interpolator = new Interpolator(hierarchy);
        String original = "This is an undefined system property: $${sys:myProp:-defaultValue}.";
        String expected = "This is an undefined system property: ${sys:myProp:-defaultValue}.";

        System.clearProperty("myProp");
        assertEquals(expected, interpolator.interpolate(original));
    }

    @Test
    public void interpolateResources() {
        CommandSpec hierarchy = createTestSpec();
        hierarchy.resourceBundle(createResourceBundle("myProp=myValue"));
        Interpolator interpolator = new Interpolator(hierarchy);
        String original = "This is a resource value: ${bundle:myProp}.";
        String expected = "This is a resource value: myValue.";

        assertEquals(expected, interpolator.interpolate(original));
    }

    @Test
    public void interpolateSystemPropertyDefault() {
        CommandSpec hierarchy = createTestSpec();
        Interpolator interpolator = new Interpolator(hierarchy);
        String original = "This is an undefined system property: ${sys:myProp:-defaultValue}.";
        String expected = "This is an undefined system property: defaultValue.";

        System.clearProperty("myProp");
        assertEquals(expected, interpolator.interpolate(original));
    }

    @Test
    public void issue676interpolateReturnsNullIfNotFound() {
        CommandSpec hierarchy = createTestSpec();
        Interpolator interpolator = new Interpolator(hierarchy);
        String original = "${sys:notfound}";
        assertEquals(null, interpolator.interpolate(original));
    }

    @Test
    public void interpolateSystemPropertyWithLookupInDefaultNotFound() {
        CommandSpec hierarchy = createTestSpec();
        Interpolator interpolator = new Interpolator(hierarchy);
        String original = "This is an undefined system property with default lookup: ${sys:myProp:-${sys:lookup:-defaultValue}}.";
        String expected = "This is an undefined system property with default lookup: defaultValue.";

        System.clearProperty("myProp");
        assertEquals(expected, interpolator.interpolate(original));
    }

    @Test
    public void interpolateSystemPropertyWithLookupInDefaultFound() {
        CommandSpec hierarchy = createTestSpec();
        Interpolator interpolator = new Interpolator(hierarchy);
        String original = "This is an undefined system property with default lookup: ${sys:myProp:-${sys:lookup:-defaultValue}}.";
        String expected = "This is an undefined system property with default lookup: myValue.";

        System.setProperty("lookup", "myValue");
        assertEquals(expected, interpolator.interpolate(original));
        System.clearProperty("lookup");
    }

    @Test
    public void interpolateSystemPropertyWithDoublyNestedLookup() {
        CommandSpec hierarchy = createTestSpec();
        Interpolator interpolator = new Interpolator(hierarchy);
        String original = "abc ${sys:key:-${sys:other:-${env:__undefined__picocli:-fixedvalue}}} dev.";
        String expected = "abc fixedvalue dev.";
        assertEquals(expected, interpolator.interpolate(original));
    }

    @Test
    public void interpolateSystemPropertyWithMultipleLookupsInDefault() {
        CommandSpec hierarchy = createTestSpec();
        Interpolator interpolator = new Interpolator(hierarchy);
        String original = "abc ${sys:key:-${sys:blah} and ${sys:other:-${env:__undefined__picocli:-fixedvalue}}} dev.";
        String expected = "abc null and fixedvalue dev.";
        assertEquals(expected, interpolator.interpolate(original));
    }

    @Test
    public void interpolateSystemPropertyWithMultipleLookupsInDefaultFoundFirst() {
        CommandSpec hierarchy = createTestSpec();
        Interpolator interpolator = new Interpolator(hierarchy);
        String original = "abc ${sys:key:-${sys:blah} and ${sys:other:-${env:__undefined__picocli:-fixedvalue}}} dev.";
        String expected = "abc xxx and fixedvalue dev.";

        System.setProperty("blah", "xxx");
        assertEquals(expected, interpolator.interpolate(original));
        System.clearProperty("blah");
    }

    @Test
    public void interpolateSystemPropertyWithMultipleLookupsInDefaultFoundSecond() {
        CommandSpec hierarchy = createTestSpec();
        Interpolator interpolator = new Interpolator(hierarchy);
        String original = "abc ${sys:key:-${sys:blah} and ${sys:other:-${env:__undefined__picocli:-fixedvalue}}} dev.";
        String expected = "abc null and yyy dev.";

        System.setProperty("other", "yyy");
        assertEquals(expected, interpolator.interpolate(original));
        System.clearProperty("other");
    }

    @Test
    public void interpolateSystemPropertyWithMultipleLookupsPlusPlainTextInDefaultFoundSecond() {
        CommandSpec hierarchy = createTestSpec();
        Interpolator interpolator = new Interpolator(hierarchy);
        String original = "abc ${sys:key:-aa${sys:other:-bb${env:var:-fixedvalue}x}Y} dev.";
        String expected = "abc aayyyY dev.";

        System.setProperty("other", "yyy");
        assertEquals(expected, interpolator.interpolate(original));
        System.clearProperty("other");
    }

    @Test
    public void interpolateSystemPropertyWithMultipleLookupsPlusPlainTextInDefault() {
        CommandSpec hierarchy = createTestSpec();
        Interpolator interpolator = new Interpolator(hierarchy);
        String original = "abc ${sys:key:-aa${sys:other:-bb${env:var:-fixedvalue}x}y} dev.";
        String expected = "abc aabbfixedvaluexy dev.";

        System.clearProperty("other");
        assertEquals(expected, interpolator.interpolate(original));
    }

    @Test
    public void interpolateSystemPropertyWithMultipleSequentialLookupsInDefaultNothingFound() {
        CommandSpec hierarchy = createTestSpec();
        Interpolator interpolator = new Interpolator(hierarchy);
        String original = "abc ${sys:key:-${sys:first:-firstDefault}${sys:second:-secondDefault}} dev.";
        String expected = "abc firstDefaultsecondDefault dev.";

        System.clearProperty("key");
        System.clearProperty("first");
        System.clearProperty("second");
        assertEquals(expected, interpolator.interpolate(original));
    }

    @Test
    public void interpolateSystemPropertyWithMultipleSequentialLookupsInDefaultSecondFound() {
        CommandSpec hierarchy = createTestSpec();
        Interpolator interpolator = new Interpolator(hierarchy);
        String original = "abc ${sys:key:-X${sys:first:-firstDefault}Y${sys:second:-ZsecondDefault0}1} dev.";
        String expected = "abc XfirstDefaultY2221 dev.";

        System.clearProperty("key");
        System.clearProperty("first");
        System.setProperty("second", "222");
        assertEquals(expected, interpolator.interpolate(original));
        System.clearProperty("second");
    }

    @Test
    public void interpolateMultipleOccurrences() {
        CommandSpec hierarchy = createTestSpec();
        Interpolator interpolator = new Interpolator(hierarchy);
        String original = "abc ${sys:key} def ${sys:key}.";
        String expected = "abc 111 def 111.";

        System.setProperty("key", "111");
        assertEquals(expected, interpolator.interpolate(original));
        System.clearProperty("key");
    }

    private CommandSpec createTestSpec() {
        CommandSpec result = CommandSpec.create().name("top")
                .addSubcommand("sub", CommandSpec.create().name("sub")
                        .addSubcommand("subsub", CommandSpec.create().name("subsub")));
        return result;
    }

    @Test
    public void testCreateResourceBundle() {
        ResourceBundle rb = createResourceBundle("a=b", "c=d");
        assertEquals("b", rb.getString("a"));
        assertEquals("d", rb.getString("c"));

        Set<String> expected = new HashSet<String>(Arrays.asList("a", "c"));
        Enumeration<String> keys = rb.getKeys();
        assertTrue(keys.hasMoreElements());
        assertTrue(expected.remove(keys.nextElement()));
        assertTrue(keys.hasMoreElements());
        assertTrue(expected.remove(keys.nextElement()));
        assertTrue(expected.isEmpty());
    }

    private ResourceBundle createResourceBundle(String... keyValuePairs) {
        Map<String, String> map = new HashMap<String, String>();
        for (String keyValue : keyValuePairs) {
            String[] pair = keyValue.split("=", 2);
            map.put(pair[0], pair[1]);
        }
        return createResourceBundle(map);
    }

    private ResourceBundle createResourceBundle(final Map<String, String> map) {
        return new ResourceBundle() {
            @Override
            protected Object handleGetObject(String key) {
                return map.get(key);
            }

            @Override
            public Enumeration<String> getKeys() {
                return new Vector<String>(map.keySet()).elements();
            }
        };
    }

    @Test(expected = CommandLine.MissingParameterException.class)
    public void testRequiredPositionalWithoutVariable() {
        class Cmd {
            @Parameters(arity = "1..*") List<String> positional;
        }
        new CommandLine(new Cmd()).parseArgs(); // missing params
    }

    @Test(expected = CommandLine.MissingParameterException.class)
    public void testRequiredPositionalWithUndefinedVariable() {
        class Cmd {
            @Parameters(arity = "${xxx1:-1..*}") List<String> positional;
        }
        new CommandLine(new Cmd()).parseArgs(); // missing params
        System.out.println();
    }

    @Test(expected = CommandLine.MissingParameterException.class)
    public void testRequiredPositionalWithDefinedVariable() {
        class Cmd {
            @Parameters(arity = "${xxx2:-1..*}") List<String> positional;
        }
        System.setProperty("xxx2", "2..3");
        new CommandLine(new Cmd()).parseArgs(); // missing params
    }

    @Test()
    public void testOptionalPositionalWithDefinedVariable() {
        class Cmd {
            @Parameters(arity = "${xxx3:-1..*}") List<String> positional;
        }
        System.setProperty("xxx3", "0..*");
        new CommandLine(new Cmd()).parseArgs();
    }
}

