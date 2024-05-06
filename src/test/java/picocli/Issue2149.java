package picocli;

import org.junit.Test;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParseResult;

import static org.junit.Assert.*;

import static picocli.CommandLine.Spec.Target.MIXEE;

public class Issue2149 {
    static class InputOptions {
        final static String DEFAULT_ENV = "env";
        final static String DEFAULT_REGION = "region";

        private String env;
        private String region;

        @CommandLine.Spec(MIXEE)
        private CommandSpec mixee;

        @Option(
            names = {"-e", "--env"},
            defaultValue = DEFAULT_ENV)
        public void setEnv(String env) {
            this.env = env;
            if (!DEFAULT_ENV.equals(env) && !this.equals(getRoot().inputOptions)) {
                getRoot().inputOptions.setEnv(env);
            }
        }

        @Option(
            names = {"-r", "--region"},
            defaultValue = DEFAULT_REGION)
        public void setRegion(String region) {
            this.region = region;
            if (!DEFAULT_REGION.equals(region) && !this.equals(getRoot().inputOptions)) {
                getRoot().inputOptions.setRegion(region);
            }
        }


        public String getEnv() {
            if (this.equals(getRoot().inputOptions)) return env;
            return getRoot().inputOptions.getEnv();
        }

        public String getRegion() {
            if (this.equals(getRoot().inputOptions)) return region;
            return getRoot().inputOptions.getRegion();
        }

        private A getRoot() {
            return (A) mixee.root().userObject();
        }
    }

    @Command(name = "A", subcommands = B.class)
    static class A {
        @Mixin InputOptions inputOptions;
    }

    @Command(name = "B", subcommands = C.class)
    static class B {
        @Mixin InputOptions inputOptions;
    }

    @Command(name = "C")
    static class C {
        @Mixin InputOptions inputOptions;
    }

    @Test
    public void testDefaultValueInvoked() {
        A a = new A();
        ParseResult parseResult = new CommandLine(a).parseArgs("B -e XX C".split(" "));
        assertEquals(InputOptions.DEFAULT_ENV, a.inputOptions.env);

        B b = (B) parseResult.subcommand().commandSpec().userObject();
        assertEquals("XX", b.inputOptions.env);

        C c = (C) parseResult.subcommand().subcommand().commandSpec().userObject();
        assertEquals(InputOptions.DEFAULT_ENV, c.inputOptions.env);
    }

    @Test
    public void testDefaultValueInvoked2() {
        A a = new A();
        ParseResult parseResult = new CommandLine(a).parseArgs("B C -e XX".split(" "));
        assertEquals(InputOptions.DEFAULT_ENV, a.inputOptions.env);

        B b = (B) parseResult.subcommand().commandSpec().userObject();
        assertEquals(InputOptions.DEFAULT_ENV, b.inputOptions.env);

        C c = (C) parseResult.subcommand().subcommand().commandSpec().userObject();
        assertEquals("XX", c.inputOptions.env);
    }
}
