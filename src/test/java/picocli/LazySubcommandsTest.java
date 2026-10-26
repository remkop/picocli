package picocli;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;
import picocli.CommandLine.Command;
import picocli.CommandLine.DuplicateNameException;
import picocli.CommandLine.DuplicateOptionAnnotationsException;
import picocli.CommandLine.IModelTransformer;
import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.OptionSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.ParseResult;
import picocli.CommandLine.Spec;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.*;
import static picocli.CommandLine.ScopeType.INHERIT;

public class LazySubcommandsTest {
    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");

    static int alphaInstances;
    static int betaInstances;

    @Before
    public void resetCounters() {
        alphaInstances = 0;
        betaInstances = 0;
    }

    enum Color { RED, GREEN }

    @Command(name = "top", subcommands = {Alpha.class, Beta.class}, scope = INHERIT, mixinStandardHelpOptions = true)
    static class Top implements Runnable {
        @Option(names = "--verbose", scope = INHERIT) boolean verbose;
        public void run() { }
    }

    @Command(name = "alpha", aliases = "a", description = "The first command.", subcommands = Gamma.class)
    static class Alpha implements Runnable {
        @Spec CommandSpec spec;
        @ParentCommand Top parent;
        @Option(names = "--color") Color color;
        Alpha() { alphaInstances++; }
        public void run() { }
    }

    @Command(name = "beta", description = "The second command.", sortOptions = false)
    static class Beta implements Runnable {
        @Spec CommandSpec spec;
        @Option(names = "--own") boolean own;
        Beta() { betaInstances++; }
        public void run() { }
    }

    @Command(name = "gamma")
    static class Gamma implements Runnable {
        @Option(names = "--color") Color color;
        public void run() { }
    }

    @Test
    public void subcommandsAreEagerByDefault() {
        new CommandLine(new Top());
        assertEquals(1, alphaInstances);
        assertEquals(1, betaInstances);
    }

    @Test
    public void onlyTheInvokedSubcommandIsInstantiated() {
        System.setProperty("picocli.lazy.subcommands", "true");
        Top top = new Top();
        CommandLine cmd = new CommandLine(top);
        assertEquals(0, alphaInstances);
        assertEquals(0, betaInstances);

        assertEquals(0, cmd.execute("a"));
        assertEquals(1, alphaInstances);
        assertEquals(0, betaInstances);
        Alpha alpha = cmd.getSubcommands().get("alpha").getCommand();
        assertSame(top, alpha.parent);
        assertEquals("alpha", alpha.spec.name());
    }

    @Test
    public void settingsAppliedBeforeASubcommandIsBuiltReachIt() {
        System.setProperty("picocli.lazy.subcommands", "true");
        CommandLine cmd = new CommandLine(new Top()).setCaseInsensitiveEnumValuesAllowed(true);

        ParseResult result = cmd.parseArgs("alpha", "gamma", "--color", "green");
        Gamma gamma = (Gamma) result.subcommand().subcommand().commandSpec().userObject();
        assertEquals(Color.GREEN, gamma.color);
    }

    @Test
    public void inheritedOptionsReachALazySubcommand() {
        System.setProperty("picocli.lazy.subcommands", "true");
        ParseResult result = new CommandLine(new Top()).parseArgs("beta", "--verbose");
        assertTrue(result.subcommand().hasMatchedOption("--verbose"));
    }

    @Test
    public void usageHelpIsTheSameAsWhenEager() {
        String eager = usages(new CommandLine(new Top()));
        System.setProperty("picocli.lazy.subcommands", "true");
        assertEquals(eager, usages(new CommandLine(new Top())));
        assertTrue(eager.contains("alpha, a  The first command."));
    }

    @Command(name = "leaf")
    static class Leaf implements Runnable {
        public void run() { }
    }

    @Command(name = "mid", subcommands = Leaf.class)
    static class Mid implements Runnable {
        public void run() { }
    }

    @Command(name = "bundled", resourceBundle = "picocli.SharedMessages", subcommands = Mid.class)
    static class Bundled implements Runnable {
        public void run() { }
    }

    @Test
    public void subcommandsGetTheResourceBundleTheyGetWhenEager() {
        String eagerI18n = usages(new CommandLine(new I18nSubclass2()));
        System.setProperty("picocli.lazy.subcommands", "true");
        assertEquals(eagerI18n, usages(new CommandLine(new I18nSubclass2())));

        CommandLine leaf = new CommandLine(new Bundled()).getSubcommands().get("mid").getSubcommands().get("leaf");
        assertEquals("picocli.SharedMessages", leaf.getCommandSpec().resourceBundleBaseName());
    }

    private static String usages(CommandLine cmd) {
        StringWriter sw = new StringWriter();
        usages(cmd, new PrintWriter(sw), new IdentityHashMap<CommandLine, Boolean>());
        return sw.toString();
    }

    private static void usages(CommandLine cmd, PrintWriter out, Map<CommandLine, Boolean> seen) {
        if (seen.put(cmd, Boolean.TRUE) == null) {
            cmd.usage(out);
            for (CommandLine sub : cmd.getSubcommands().values()) {
                usages(sub, out, seen);
            }
        }
    }

    @Test
    public void caseInsensitiveSubcommandsKeepTheirOrder() {
        List<String> eager = visitInOrder(new CommandLine(new Top()).setSubcommandsCaseInsensitive(true));
        System.setProperty("picocli.lazy.subcommands", "true");
        assertEquals(eager, visitInOrder(new CommandLine(new Top()).setSubcommandsCaseInsensitive(true)));
    }

    private static List<String> visitInOrder(CommandLine cmd) {
        Map<String, CommandLine> subcommands = cmd.getCommandSpec().subcommands();
        for (String name : subcommands.keySet()) {
            subcommands.get(name);
        }
        return new ArrayList<String>(subcommands.keySet());
    }

    @Command(name = "verbose")
    static class Verbose implements Runnable {
        @Option(names = "--verbose") boolean verbose;
        public void run() { }
    }

    @Command(name = "strict", subcommands = Verbose.class)
    static class Strict implements Runnable {
        @Option(names = "--verbose", scope = INHERIT) boolean verbose;
        public void run() { }
    }

    public static class Fails implements IModelTransformer {
        public CommandSpec transform(CommandSpec spec) {
            throw new NoClassDefFoundError("fails");
        }
    }

    @Command(name = "broken", modelTransformer = Fails.class)
    static class Broken implements Runnable {
        public void run() { }
    }

    @Command(name = "holder", subcommands = Broken.class)
    static class Holder implements Runnable {
        public void run() { }
    }

    @Test
    public void aSubcommandThatFailsToBuildFailsOnEveryUse() {
        System.setProperty("picocli.lazy.subcommands", "true");
        CommandLine cmd = new CommandLine(new Strict());
        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                cmd.parseArgs("verbose");
                fail("attempt " + attempt + " should fail");
            } catch (DuplicateOptionAnnotationsException expected) {
                assertTrue(expected.getMessage().contains("--verbose"));
            }
        }

        CommandLine holder = new CommandLine(new Holder());
        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                holder.parseArgs("broken");
                fail("attempt " + attempt + " should fail");
            } catch (NoClassDefFoundError expected) {
                assertEquals("fails", expected.getMessage());
            }
        }
    }

    static class Name {
        final String value;
        Name(String value) { this.value = value; }
    }

    static class Size {
        final int value;
        Size(int value) { this.value = value; }
    }

    @Command(name = "typed")
    static class Typed implements Runnable {
        @Option(names = "--name") Name name;
        @Option(names = "--size") Size size;
        public void run() { }
    }

    @Command(name = "types", subcommands = Typed.class)
    static class Types implements Runnable {
        public void run() { }
    }

    @Test
    public void convertersForDifferentTypesBothReachALazySubcommand() {
        System.setProperty("picocli.lazy.subcommands", "true");
        CommandLine cmd = new CommandLine(new Types())
                .registerConverter(Name.class, new ITypeConverter<Name>() {
                    public Name convert(String value) { return new Name(value); }
                })
                .registerConverter(Size.class, new ITypeConverter<Size>() {
                    public Size convert(String value) { return new Size(value.length()); }
                });

        Typed typed = (Typed) cmd.parseArgs("typed", "--name", "x", "--size", "abc").subcommand().commandSpec().userObject();
        assertEquals("x", typed.name.value);
        assertEquals(3, typed.size.value);
    }

    @Test
    public void anAbbreviatedNameBuildsOnlyTheSubcommandItMatches() {
        System.setProperty("picocli.lazy.subcommands", "true");
        CommandLine cmd = new CommandLine(new Top()).setAbbreviatedSubcommandsAllowed(true);
        cmd.parseArgs("alp");
        assertEquals(1, alphaInstances);
        assertEquals(0, betaInstances);
    }

    @Test
    public void concurrentFirstReadsSeeOneSubcommand() throws InterruptedException {
        System.setProperty("picocli.lazy.subcommands", "true");
        for (int round = 0; round < 50; round++) {
            final CommandLine cmd = new CommandLine(new Top());
            final CountDownLatch start = new CountDownLatch(1);
            final Map<Object, Boolean> seen = Collections.synchronizedMap(new IdentityHashMap<Object, Boolean>());
            List<Thread> threads = new ArrayList<Thread>();
            for (int i = 0; i < 4; i++) {
                Thread thread = new Thread() {
                    public void run() {
                        try {
                            start.await();
                            seen.put(cmd.getCommandSpec().subcommands().get("beta").getCommand(), Boolean.TRUE);
                        } catch (InterruptedException ignored) {
                        }
                    }
                };
                threads.add(thread);
                thread.start();
            }
            start.countDown();
            for (Thread thread : threads) {
                thread.join();
            }
            assertEquals("round " + round, 1, seen.size());
        }
    }

    public static class AddsOption implements IModelTransformer {
        public CommandSpec transform(CommandSpec spec) {
            return spec.addOption(OptionSpec.builder("--added").build());
        }
    }

    @Command(name = "transformed", modelTransformer = AddsOption.class)
    static class Transformed implements Runnable {
        public void run() { }
    }

    @Command(name = "root", subcommands = Transformed.class)
    static class Root implements Runnable {
        public void run() { }
    }

    @Test
    public void modelTransformerRunsWhenTheSubcommandIsBuilt() {
        System.setProperty("picocli.lazy.subcommands", "true");
        ParseResult result = new CommandLine(new Root()).parseArgs("transformed", "--added");
        assertTrue(result.subcommand().hasMatchedOption("--added"));
    }

    @Command(aliases = "mx")
    static class AliasMixin {
    }

    @Command(name = "mixed")
    static class Mixed implements Runnable {
        @CommandLine.Mixin AliasMixin aliasMixin;
        public void run() { }
    }

    @Command(name = "mixes", subcommands = Mixed.class)
    static class Mixes implements Runnable {
        public void run() { }
    }

    @Test
    public void anAliasFromAMixinFailsTheBuild() {
        System.setProperty("picocli.lazy.subcommands", "true");
        try {
            new CommandLine(new Mixes()).parseArgs("mixed");
            fail("an alias that only the built subcommand knows cannot be matched before it is built");
        } catch (CommandLine.InitializationException expected) {
            assertTrue(expected.getMessage(), expected.getMessage().contains("'mx'"));
        }
    }

    @Command(name = "beta")
    static class OtherBeta implements Runnable {
        public void run() { }
    }

    @Command(name = "clash", subcommands = {Beta.class, OtherBeta.class})
    static class Clash implements Runnable {
        public void run() { }
    }

    @Test(expected = DuplicateNameException.class)
    public void duplicateNamesStillFailAtConstruction() {
        System.setProperty("picocli.lazy.subcommands", "true");
        new CommandLine(new Clash());
    }
}
