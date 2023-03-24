package picocli;

import org.fusesource.jansi.AnsiConsole;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.rules.TestRule;
import picocli.CommandLine.Help.Ansi;


import static org.junit.Assert.*;
import static picocli.TestUtil.usageString;

/**
 * Tests related to the heuristics for enabling ANSI escape codes
 * in the usage help message.
 * <p>
 * <b>Implementation Note</b><br />
 * These tests use the System Rules library for setting and clearing environment
 * variables. This relies on the Security Manager and reflection to modify
 * internal fields of the environment variable map.
 * </p><p>
 * The Security Manager has been removed in Java 19.
 * Reflection on private fields fails with IllegalAccessError on Java 16+.
 * It may be possible to resolve this with command line arguments to explicitly enable this:
 * </p>
 * <pre>
 * {@code
 * tasks.withType(Test.class) {
 *     if (org.gradle.api.JavaVersion.current().isCompatibleWith(
 *                 org.gradle.api.JavaVersion.VERSION_19)) {
 *         it.jvmArgs '--add-opens java.base/java.util=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED'
 *     }
 * }
 * }
 * </pre>
 * <p>
 *   To resolve this, tests that use the {@code org.junit.contrib.java.lang.system.EnvironmentVariables}
 *   rule have been moved to the "legacy" test module that is executed in Java 5, 6 and 7.
 * </p>
 * <p>
 *   Tests that use {@code com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable}
 *   have been moved to the "java8plus" test module that is executed in Java 8 and later.
 * </p>
 */
public class HelpAnsiHeuristicsTest {

    private static final String[] ANSI_ENVIRONMENT_VARIABLES = new String[] {
            "TERM", "OSTYPE", "NO_COLOR", "ANSICON", "CLICOLOR", "ConEmuANSI", "CLICOLOR_FORCE"
    };

    @Rule
    // allows tests to set any kind of properties they like, without having to individually roll them back
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule().enableLog().muteForSuccessfulTests();

    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @Test
    public void testAnsiIsWindowsDependsOnSystemProperty() {
        System.setProperty("os.name", "MMIX");
        assertFalse(Ansi.isWindows());

        System.setProperty("os.name", "Windows");
        assertTrue(Ansi.isWindows());

        System.setProperty("os.name", "Windows 10 build 12345");
        assertTrue(Ansi.isWindows());
    }

    @Test
    public void testAnsiIsXtermDependsOnEnvironmentVariable() {
        environmentVariables.clear(ANSI_ENVIRONMENT_VARIABLES);
        assertFalse(Ansi.isXterm());

        environmentVariables.set("TERM", "random value");
        assertFalse(Ansi.isXterm());

        environmentVariables.set("TERM", "xterm");
        assertTrue(Ansi.isXterm());

        environmentVariables.set("TERM", "xterm asfasfasf");
        assertTrue(Ansi.isXterm());
    }

    @Test
    public void testAnsiIsCygwinDependsOnEnvironmentVariable() {
        environmentVariables.clear(ANSI_ENVIRONMENT_VARIABLES);
        assertFalse(Ansi.isCygwin());

        environmentVariables.set("TERM", "random value");
        assertFalse(Ansi.isCygwin());

        environmentVariables.set("TERM", "xterm");
        assertFalse(Ansi.isCygwin());

        environmentVariables.set("TERM", "xterm cygwin");
        assertTrue(Ansi.isCygwin());

        environmentVariables.set("TERM", "cygwin");
        assertTrue(Ansi.isCygwin());
    }

    @Test
    public void testAnsiHasOstypeDependsOnEnvironmentVariable() {
        environmentVariables.clear(ANSI_ENVIRONMENT_VARIABLES);
        assertFalse(Ansi.hasOsType());

        environmentVariables.set("OSTYPE", "");
        assertTrue(Ansi.hasOsType());

        environmentVariables.set("OSTYPE", "42");
        assertTrue(Ansi.hasOsType());
    }

    @Test
    public void testAnsiIsPseudoTtyDependsOnWindowsXtermOrCygwinOrOsType() {
        System.setProperty("os.name", "MMIX");
        environmentVariables.clear(ANSI_ENVIRONMENT_VARIABLES);
        assertFalse("OSTYPE and XTERM are not set", Ansi.isPseudoTTY());

        System.setProperty("os.name", "Windows 10 build 12345");
        environmentVariables.set("OSTYPE", "222");
        environmentVariables.set("TERM", "xterm");
        assertTrue(Ansi.isPseudoTTY());

        System.setProperty("os.name", "MMIX");
        assertFalse("Not Windows", Ansi.isPseudoTTY());

        System.setProperty("os.name", "Windows 10 build 12345"); // restore
        assertTrue("restored", Ansi.isPseudoTTY());
        environmentVariables.clear("OSTYPE");
        assertTrue("Missing OSTYPE, but TERM=xterm", Ansi.isPseudoTTY());
        environmentVariables.set("TERM", "abcygwinxyz");
        assertTrue("Missing OSTYPE, but TERM=cygwin", Ansi.isPseudoTTY());

        environmentVariables.set("OSTYPE", "anything");
        assertTrue("restored", Ansi.isPseudoTTY());
        environmentVariables.clear("XTERM");
        assertTrue("Missing XTERM, but OSTYPE defined", Ansi.isPseudoTTY());
    }

    @Test
    public void testAnsiHintDisabledTrueIfCLICOLORZero() {
        environmentVariables.clear(ANSI_ENVIRONMENT_VARIABLES);
        assertFalse("no env vars set", Ansi.hintDisabled());

        environmentVariables.set("CLICOLOR", "");
        assertFalse("Just defining CLICOLOR is not enough", Ansi.hintDisabled());

        environmentVariables.set("CLICOLOR", "1");
        assertFalse("CLICOLOR=1 is not enough", Ansi.hintDisabled());

        environmentVariables.set("CLICOLOR", "false");
        assertFalse("CLICOLOR=false is not enough", Ansi.hintDisabled());

        environmentVariables.set("CLICOLOR", "0");
        assertTrue("CLICOLOR=0 disables", Ansi.hintDisabled());
    }

    @Test
    public void testAnsiHintDisabledTrueIfConEmuANSIisOFF() {
        environmentVariables.clear(ANSI_ENVIRONMENT_VARIABLES);
        assertFalse("no env vars set", Ansi.hintDisabled());

        environmentVariables.set("ConEmuANSI", "");
        assertFalse("Just defining ConEmuANSI is not enough", Ansi.hintDisabled());

        environmentVariables.set("ConEmuANSI", "0");
        assertFalse("ConEmuANSI=0 is not enough", Ansi.hintDisabled());

        environmentVariables.set("ConEmuANSI", "false");
        assertFalse("ConEmuANSI=false is not enough", Ansi.hintDisabled());

        environmentVariables.set("ConEmuANSI", "off");
        assertFalse("ConEmuANSI=off does not disable", Ansi.hintDisabled());

        environmentVariables.set("ConEmuANSI", "Off");
        assertFalse("ConEmuANSI=Off does not disable", Ansi.hintDisabled());

        environmentVariables.set("ConEmuANSI", "OFF");
        assertTrue("ConEmuANSI=OFF disables", Ansi.hintDisabled());
    }


    @Test
    public void testAnsiHintEnbledTrueIfANSICONDefined() {
        environmentVariables.clear(ANSI_ENVIRONMENT_VARIABLES);
        assertFalse("no env vars set", Ansi.hintEnabled());

        environmentVariables.set("ANSICON", "");
        assertTrue("ANSICON defined without value", Ansi.hintEnabled());

        environmentVariables.set("ANSICON", "abc");
        assertTrue("ANSICON defined any value", Ansi.hintEnabled());
    }

    @Test
    public void testAnsiHintEnbledTrueIfCLICOLOROne() {
        environmentVariables.clear(ANSI_ENVIRONMENT_VARIABLES);
        assertFalse("no env vars set", Ansi.hintEnabled());

        environmentVariables.set("CLICOLOR", "");
        assertFalse("Just defining CLICOLOR is not enough", Ansi.hintEnabled());

        environmentVariables.set("CLICOLOR", "0");
        assertFalse("CLICOLOR=0 is not enough", Ansi.hintEnabled());

        environmentVariables.set("CLICOLOR", "true");
        assertFalse("CLICOLOR=true is not enough", Ansi.hintEnabled());

        environmentVariables.set("CLICOLOR", "1");
        assertTrue("CLICOLOR=1 enables", Ansi.hintEnabled());
    }

    @Test
    public void testAnsiHintEnabledTrueIfConEmuANSIisON() {
        environmentVariables.clear(ANSI_ENVIRONMENT_VARIABLES);
        assertFalse("no env vars set", Ansi.hintEnabled());

        environmentVariables.set("ConEmuANSI", "");
        assertFalse("Just defining ConEmuANSI is not enough", Ansi.hintEnabled());

        environmentVariables.set("ConEmuANSI", "1");
        assertFalse("ConEmuANSI=1 is not enough", Ansi.hintEnabled());

        environmentVariables.set("ConEmuANSI", "true");
        assertFalse("ConEmuANSI=true is not enough", Ansi.hintEnabled());

        environmentVariables.set("ConEmuANSI", "on");
        assertFalse("ConEmuANSI=on does not enables", Ansi.hintEnabled());

        environmentVariables.set("ConEmuANSI", "On");
        assertFalse("ConEmuANSI=On does not enables", Ansi.hintEnabled());

        environmentVariables.set("ConEmuANSI", "ON");
        assertTrue("ConEmuANSI=ON enables", Ansi.hintEnabled());
    }

    @Test
    public void testAnsiForceEnabledTrueIfCLICOLOR_FORCEisDefinedAndNonZero() {
        environmentVariables.clear(ANSI_ENVIRONMENT_VARIABLES);
        assertFalse("no env vars set", Ansi.forceEnabled());

        environmentVariables.set("CLICOLOR_FORCE", "");
        assertTrue("Just defining CLICOLOR_FORCE is enough", Ansi.forceEnabled());

        environmentVariables.set("CLICOLOR_FORCE", "1");
        assertTrue("CLICOLOR_FORCE=1 is enough", Ansi.forceEnabled());

        environmentVariables.set("CLICOLOR_FORCE", "0");
        assertFalse("CLICOLOR_FORCE=0 is not forced", Ansi.forceEnabled());
    }

    @Test
    public void testAnsiForceDisabledTrueIfNO_COLORDefined() {
        environmentVariables.clear(ANSI_ENVIRONMENT_VARIABLES);
        assertFalse("no env vars set", Ansi.forceDisabled());

        environmentVariables.set("NO_COLOR", "");
        assertTrue("NO_COLOR defined without value", Ansi.forceDisabled());

        environmentVariables.set("NO_COLOR", "abc");
        assertTrue("NO_COLOR defined any value", Ansi.forceDisabled());
    }

    @Test
    public void testAnsiOnEnabled() {
        assertTrue(Ansi.ON.enabled());
    }

    @Test
    public void testAnsiOffDisabled() {
        assertFalse(Ansi.OFF.enabled());
    }

    @Test
    public void testAnsiAutoIfSystemPropertyPicocliAnsiCleared() {
        environmentVariables.set("CLICOLOR_FORCE", "1");

        System.clearProperty("picocli.ansi");
        assertTrue(Ansi.AUTO.enabled());
    }

    @Test
    public void testAnsiAutoIfSystemPropertyPicocliAnsiIsAuto() {
        environmentVariables.set("CLICOLOR_FORCE", "1");

        System.setProperty("picocli.ansi", "auto");
        assertTrue(Ansi.AUTO.enabled());

        System.setProperty("picocli.ansi", "Auto");
        assertTrue(Ansi.AUTO.enabled());

        System.setProperty("picocli.ansi", "AUTO");
        assertTrue(Ansi.AUTO.enabled());
    }

    @Test
    public void testAnsiOffIfSystemPropertyPicocliAnsiIsNotAuto() {
        System.setProperty("picocli.ansi", "auto1");

        environmentVariables.set("CLICOLOR_FORCE", "1");
        assertFalse(Ansi.AUTO.enabled());
    }

    @Test
    public void testAnsiAutoForceDisabledOverridesForceEnabled() {
        environmentVariables.clear(ANSI_ENVIRONMENT_VARIABLES);
        environmentVariables.set("NO_COLOR", "");
        environmentVariables.set("CLICOLOR_FORCE", "1");
        assertTrue(Ansi.forceDisabled());
        assertTrue(Ansi.forceEnabled());
        assertFalse(Ansi.hintDisabled());
        assertFalse(Ansi.hintEnabled());
        assertFalse("forceDisabled overrides forceEnabled", Ansi.AUTO.enabled());
    }

    @Test
    public void testAnsiAutoForceDisabledOverridesHintEnabled() {
        environmentVariables.clear(ANSI_ENVIRONMENT_VARIABLES);
        environmentVariables.set("NO_COLOR", "");
        environmentVariables.set("CLICOLOR", "1");
        assertTrue(Ansi.forceDisabled());
        assertFalse(Ansi.forceEnabled());
        assertFalse(Ansi.hintDisabled());
        assertTrue(Ansi.hintEnabled());
        assertFalse("forceDisabled overrides hintEnabled", Ansi.AUTO.enabled());
    }

    @Test
    public void testAnsiAutoForcedEnabledOverridesHintDisabled() {
        environmentVariables.clear(ANSI_ENVIRONMENT_VARIABLES);
        environmentVariables.set("CLICOLOR", "0");
        environmentVariables.set("CLICOLOR_FORCE", "1");
        assertFalse(Ansi.forceDisabled());
        assertTrue(Ansi.hintDisabled());
        assertTrue(Ansi.forceEnabled());
        assertFalse(Ansi.hintEnabled());
        assertTrue("forceEnabled overrides hintDisabled", Ansi.AUTO.enabled());

        environmentVariables.clear(ANSI_ENVIRONMENT_VARIABLES);
        environmentVariables.set("ConEmuANSI", "OFF");
        environmentVariables.set("CLICOLOR_FORCE", "1");
        assertFalse(Ansi.forceDisabled());
        assertTrue(Ansi.hintDisabled());
        assertTrue(Ansi.forceEnabled());
        assertFalse(Ansi.hintEnabled());
        assertTrue("forceEnabled overrides hintDisabled 2", Ansi.AUTO.enabled());
    }

    @Test
    public void testAnsiAutoJansiConsoleInstalledOverridesHintDisabled() {
        environmentVariables.clear(ANSI_ENVIRONMENT_VARIABLES);
        environmentVariables.set("CLICOLOR", "0"); // hint disabled
        System.setProperty("os.name", "Windows");
        // Clear the globally cached jansiInstalled value that might
        // have been set in a previous test to force the
        // Ansi#isJansiConsoleInstalled method to recalculate
        // the cached value.
        Ansi.jansiInstalled = null;
        assertTrue(Ansi.isWindows());
        assertFalse(Ansi.isPseudoTTY());
        assertFalse(Ansi.forceDisabled());
        assertFalse(Ansi.forceEnabled());
        assertTrue(Ansi.hintDisabled());
        assertFalse(Ansi.hintEnabled());


        // Clear the globally cached jansiInstalled value that might
        // have been set in a previous test to force the
        // Ansi#isJansiConsoleInstalled method to recalculate
        // the cached value.
        Ansi.jansiInstalled = null;
        assertFalse(Ansi.isJansiConsoleInstalled());
        AnsiConsole.systemInstall();
        try {

            // Clear the cached jansiInstalled value to force the
            // Ansi#isJansiConsoleInstalled method to recalculate
            Ansi.jansiInstalled = null;
            assertTrue(Ansi.isJansiConsoleInstalled());
            assertTrue(Ansi.AUTO.enabled());
        } finally {
            AnsiConsole.systemUninstall();

            // Clear the cached jansiInstalled value
            Ansi.jansiInstalled = null;
        }
    }

    @Test
    public void testAnsiAutoHintDisabledOverridesHintEnabled() {
        environmentVariables.clear(ANSI_ENVIRONMENT_VARIABLES);
        environmentVariables.set("CLICOLOR", "0"); // hint disabled
        environmentVariables.set("ANSICON", "1"); // hint enabled
        System.setProperty("os.name", "Windows");
        assertTrue(Ansi.isWindows());
        environmentVariables.set("TERM", "xterm"); // fake Cygwin
        assertTrue(Ansi.isPseudoTTY());
        environmentVariables.set("TERM", "cygwin"); // fake Cygwin
        assertTrue(Ansi.isPseudoTTY());

        assertFalse(Ansi.isJansiConsoleInstalled());

        // Clear the globally cached jansiInstalled value that might
        // have been set in a previous test to force the
        // Ansi#isJansiConsoleInstalled method to recalculate
        // the cached value.
        Ansi.jansiInstalled = null;

        assertFalse(Ansi.forceDisabled());
        assertFalse(Ansi.forceEnabled());
        assertTrue(Ansi.hintDisabled());
        assertTrue(Ansi.hintEnabled());

        assertFalse("Disabled overrides enabled", Ansi.AUTO.enabled());
    }

    @Test
    public void testAnsiAutoDisabledIfNoTty() {
        if (Ansi.isTTY()) { return; } //
        environmentVariables.clear(ANSI_ENVIRONMENT_VARIABLES);
        System.setProperty("os.name", "Windows");
        assertTrue(Ansi.isWindows());
        assertFalse(Ansi.isPseudoTTY());
        assertFalse(Ansi.isJansiConsoleInstalled());

        // Clear the globally cached jansiInstalled value that might
        // have been set in a previous test to force the
        // Ansi#isJansiConsoleInstalled method to recalculate
        // the cached value.
        Ansi.jansiInstalled = null;

        assertFalse(Ansi.forceDisabled());
        assertFalse(Ansi.forceEnabled());
        assertFalse(Ansi.hintDisabled());
        assertFalse(Ansi.hintEnabled());

        assertFalse("Must have TTY if no JAnsi", Ansi.AUTO.enabled());
    }

    @Test
    public void testAnsiAutoEnabledIfNotWindows() {
        if (!Ansi.isTTY()) { return; } // needs TTY for this test
        environmentVariables.clear(ANSI_ENVIRONMENT_VARIABLES);
        System.setProperty("os.name", "MMIX");
        assertFalse(Ansi.isWindows());
        assertFalse(Ansi.isPseudoTTY()); // TODO Mock this?
        assertFalse(Ansi.isJansiConsoleInstalled());

        // Clear the globally cached jansiInstalled value that might
        // have been set in a previous test to force the
        // Ansi#isJansiConsoleInstalled method to recalculate
        // the cached value.
        Ansi.jansiInstalled = null;

        assertFalse(Ansi.forceDisabled());
        assertFalse(Ansi.forceEnabled());
        assertFalse(Ansi.hintDisabled());
        assertFalse(Ansi.hintEnabled());

        assertTrue("If have TTY, enabled on non-Windows", Ansi.AUTO.enabled());
    }

    @Test
    public void testAnsiAutoEnabledIfWindowsPseudoTTY() {
        environmentVariables.clear(ANSI_ENVIRONMENT_VARIABLES);
        System.setProperty("os.name", "Windows");
        assertTrue(Ansi.isWindows());
        assertFalse(Ansi.isJansiConsoleInstalled());

        // Clear the globally cached jansiInstalled value that might
        // have been set in a previous test to force the
        // Ansi#isJansiConsoleInstalled method to recalculate
        // the cached value.
        Ansi.jansiInstalled = null;

        assertFalse(Ansi.forceDisabled());
        assertFalse(Ansi.forceEnabled());
        assertFalse(Ansi.hintDisabled());
        assertFalse(Ansi.hintEnabled());

        environmentVariables.set("TERM", "xterm");
        assertTrue(Ansi.isPseudoTTY());
        assertTrue("If have Cygwin pseudo-TTY, enabled on Windows", Ansi.AUTO.enabled());

        environmentVariables.set("TERM", "cygwin");
        assertTrue(Ansi.isPseudoTTY());
        assertTrue("If have Cygwin pseudo-TTY, enabled on Windows", Ansi.AUTO.enabled());

        environmentVariables.clear(ANSI_ENVIRONMENT_VARIABLES);
        environmentVariables.set("OSTYPE", "Windows");
        assertTrue(Ansi.isPseudoTTY());
        assertTrue("If have MSYS pseudo-TTY, enabled on Windows", Ansi.AUTO.enabled());
    }
}
