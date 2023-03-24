package picocli;

import org.fusesource.jansi.AnsiConsole;
import org.junit.jupiter.api.Test;
import picocli.CommandLine.Help.Ansi;

import static com.github.stefanbirkner.systemlambda.SystemLambda.restoreSystemProperties;
import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests related to the heuristics for enabling ANSI escape codes
 * in the usage help message.
 * <p>
 * <b>Implementation Note</b><br />
 * These tests use the System Lambdas library for setting and clearing environment
 * variables.
 * </p>
 * <p>
 *   Tests that use the {@code org.junit.contrib.java.lang.system.EnvironmentVariables}
 *   rule have been moved to the "legacy" test module that is executed in Java 5, 6 and 7.
 * </p>
 */
public class HelpAnsiHeuristicsTest {

    private static final String[] ANSI_ENVIRONMENT_VARIABLES = new String[] {
            "TERM", "OSTYPE", "NO_COLOR", "ANSICON", "CLICOLOR", "ConEmuANSI", "CLICOLOR_FORCE"
    };
    private static final int TERM = 0;
    private static final int OSTYPE = 1;
    private static final int NO_COLOR = 2;
    private static final int ANSICON = 3;
    private static final int CLICOLOR = 4;
    private static final int ConEmuANSI = 5;
    private static final int CLICOLOR_FORCE = 6;


    @Test
    public void testAnsiIsWindowsDependsOnSystemProperty() throws Exception {
        restoreSystemProperties(() -> {
            System.setProperty("os.name", "MMIX");
            assertFalse(Ansi.isWindows());

            System.setProperty("os.name", "Windows");
            assertTrue(Ansi.isWindows());

            System.setProperty("os.name", "Windows 10 build 12345");
            assertTrue(Ansi.isWindows());
        });
    }

    @Test
    public void testAnsiIsXtermDependsOnEnvironmentVariable() throws Exception {
        withEnvironmentVariable(ANSI_ENVIRONMENT_VARIABLES[0], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[1], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[2], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[3], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[4], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[5], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[6], null)
            .execute(() -> {
                assertFalse(Ansi.isXterm());
            });

        withEnvironmentVariable("TERM", "random value")
            .execute(() -> {
                assertFalse(Ansi.isXterm());
            });

        withEnvironmentVariable("TERM", "xterm")
            .execute(() -> {
                assertTrue(Ansi.isXterm());
            });

        withEnvironmentVariable("TERM", "xterm asfasfasf")
            .execute(() -> {
                assertTrue(Ansi.isXterm());
            });
    }

    @Test
    public void testAnsiIsCygwinDependsOnEnvironmentVariable() throws Exception {
        withEnvironmentVariable(ANSI_ENVIRONMENT_VARIABLES[0], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[1], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[2], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[3], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[4], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[5], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[6], null)
            .execute(() -> {
                assertFalse(Ansi.isCygwin());
            });

        withEnvironmentVariable("TERM", "random value")
            .execute(() -> {
                assertFalse(Ansi.isCygwin());
            });


        withEnvironmentVariable("TERM", "xterm")
            .execute(() -> {
                assertFalse(Ansi.isCygwin());
            });

        withEnvironmentVariable("TERM", "xterm cygwin")
            .execute(() -> {
                assertTrue(Ansi.isCygwin());
            });

        withEnvironmentVariable("TERM", "cygwin")
            .execute(() -> {
                assertTrue(Ansi.isCygwin());
            });
    }

    @Test
    public void testAnsiHasOstypeDependsOnEnvironmentVariable() throws Exception {
        withEnvironmentVariable(ANSI_ENVIRONMENT_VARIABLES[0], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[1], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[2], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[3], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[4], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[5], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[6], null)
            .execute(() -> {
                assertFalse(Ansi.hasOsType());
            });

        withEnvironmentVariable("OSTYPE", "")
            .execute(() -> {
                assertTrue(Ansi.hasOsType());
            });

        withEnvironmentVariable("OSTYPE", "42")
            .execute(() -> {
                assertTrue(Ansi.hasOsType());
            });
    }

    @Test
    public void testAnsiIsPseudoTtyDependsOnWindowsXtermOrCygwinOrOsType() throws Exception {
        restoreSystemProperties(() -> {
            System.setProperty("os.name", "MMIX");
            withEnvironmentVariable(ANSI_ENVIRONMENT_VARIABLES[0], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[1], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[2], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[3], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[4], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[5], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[6], null)
                .execute(() -> {
                        assertFalse(Ansi.isPseudoTTY(), "OSTYPE and XTERM are not set");
                    });

            System.setProperty("os.name", "Windows 10 build 12345");
            withEnvironmentVariable(ANSI_ENVIRONMENT_VARIABLES[TERM], "xterm")
                .and(ANSI_ENVIRONMENT_VARIABLES[OSTYPE], "222")
                .and(ANSI_ENVIRONMENT_VARIABLES[2], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[3], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[4], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[5], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[6], null)
                .execute(() -> {
                    assertTrue(Ansi.isPseudoTTY());
                });

            System.setProperty("os.name", "MMIX");
            withEnvironmentVariable(ANSI_ENVIRONMENT_VARIABLES[TERM], "xterm")
                .and(ANSI_ENVIRONMENT_VARIABLES[OSTYPE], "222")
                .and(ANSI_ENVIRONMENT_VARIABLES[2], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[3], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[4], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[5], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[6], null)
                .execute(() -> {
                    assertFalse(Ansi.isPseudoTTY(), "Not Windows");
                });

            System.setProperty("os.name", "Windows 10 build 12345"); // restore
            withEnvironmentVariable(ANSI_ENVIRONMENT_VARIABLES[TERM], "xterm")
                .and(ANSI_ENVIRONMENT_VARIABLES[OSTYPE], "222")
                .and(ANSI_ENVIRONMENT_VARIABLES[2], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[3], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[4], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[5], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[6], null)
                .execute(() -> {
                    assertTrue(Ansi.isPseudoTTY(), "restored");
                });

            withEnvironmentVariable(ANSI_ENVIRONMENT_VARIABLES[TERM], "xterm")
                .and(ANSI_ENVIRONMENT_VARIABLES[1], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[2], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[3], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[4], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[5], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[6], null)
                .execute(() -> {
                    assertTrue(Ansi.isPseudoTTY(), "Missing OSTYPE, but TERM=xterm");
                });

            withEnvironmentVariable(ANSI_ENVIRONMENT_VARIABLES[TERM], "abcygwinxyz")
                .and(ANSI_ENVIRONMENT_VARIABLES[1], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[2], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[3], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[4], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[5], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[6], null)
                .execute(() -> {
                    assertTrue(Ansi.isPseudoTTY(), "Missing OSTYPE, but TERM=cygwin");
                });

            withEnvironmentVariable(ANSI_ENVIRONMENT_VARIABLES[TERM], "abcygwinxyz")
                .and(ANSI_ENVIRONMENT_VARIABLES[OSTYPE], "anything")
                .and(ANSI_ENVIRONMENT_VARIABLES[2], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[3], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[4], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[5], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[6], null)
                .execute(() -> {
                    assertTrue(Ansi.isPseudoTTY(), "restored");
                });

            withEnvironmentVariable(ANSI_ENVIRONMENT_VARIABLES[0], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[OSTYPE], "anything")
                .and(ANSI_ENVIRONMENT_VARIABLES[2], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[3], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[4], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[5], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[6], null)
                .execute(() -> {
                    assertTrue(Ansi.isPseudoTTY(), "Missing XTERM, but OSTYPE defined");
                });
        });
    }

    @Test
    public void testAnsiHintDisabledTrueIfCLICOLORZero() throws Exception {
        withEnvironmentVariable(ANSI_ENVIRONMENT_VARIABLES[0], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[1], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[2], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[3], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[4], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[5], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[6], null)
            .execute(() -> {
                assertFalse(Ansi.hintDisabled(), "no env vars set");
            });

        withEnvironmentVariable("CLICOLOR", "")
            .execute(() -> {
                assertFalse(Ansi.hintDisabled(), "Just defining CLICOLOR is not enough");
            });

        withEnvironmentVariable("CLICOLOR", "")
            .execute(() -> {
                assertFalse(Ansi.hintDisabled(), "Just defining CLICOLOR is not enough");
            });

        withEnvironmentVariable("CLICOLOR", "1")
            .execute(() -> {
                assertFalse(Ansi.hintDisabled(), "CLICOLOR=1 is not enough");
            });

        withEnvironmentVariable("CLICOLOR", "false")
            .execute(() -> {
                assertFalse(Ansi.hintDisabled(), "CLICOLOR=false is not enough");
            });

        withEnvironmentVariable("CLICOLOR", "0")
            .execute(() -> {
                assertTrue(Ansi.hintDisabled(), "CLICOLOR=0 disables");
            });
    }

    @Test
    public void testAnsiHintDisabledTrueIfConEmuANSIisOFF() throws Exception {
        withEnvironmentVariable(ANSI_ENVIRONMENT_VARIABLES[0], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[1], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[2], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[3], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[4], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[5], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[6], null)
            .execute(() -> {
                assertFalse(Ansi.hintDisabled(), "no env vars set");
            });

        withEnvironmentVariable("ConEmuANSI", "")
            .execute(() -> {
                assertFalse(Ansi.hintDisabled(), "Just defining ConEmuANSI is not enough");
            });

        withEnvironmentVariable("ConEmuANSI", "0")
            .execute(() -> {
                assertFalse(Ansi.hintDisabled(), "ConEmuANSI=0 is not enough");
            });

        withEnvironmentVariable("ConEmuANSI", "false")
            .execute(() -> {
                assertFalse(Ansi.hintDisabled(), "ConEmuANSI=false is not enough");
            });

        withEnvironmentVariable("ConEmuANSI", "off")
            .execute(() -> {
                assertFalse(Ansi.hintDisabled(), "ConEmuANSI=off does not disable");
            });

        withEnvironmentVariable("ConEmuANSI", "Off")
            .execute(() -> {
                assertFalse(Ansi.hintDisabled(), "ConEmuANSI=Off does not disable");
            });

        withEnvironmentVariable("ConEmuANSI", "OFF")
            .execute(() -> {
                assertTrue(Ansi.hintDisabled(), "ConEmuANSI=OFF disables");
            });
    }


    @Test
    public void testAnsiHintEnbledTrueIfANSICONDefined() throws Exception {
        withEnvironmentVariable(ANSI_ENVIRONMENT_VARIABLES[0], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[1], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[2], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[3], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[4], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[5], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[6], null)
            .execute(() -> {
                assertFalse(Ansi.hintEnabled(), "no env vars set");
            });

        withEnvironmentVariable("ANSICON", "")
            .execute(() -> {
                assertTrue(Ansi.hintEnabled(), "ANSICON defined without value");
            });

        withEnvironmentVariable("ANSICON", "abc")
            .execute(() -> {
                assertTrue(Ansi.hintEnabled(), "ANSICON defined any value");
            });
    }

    @Test
    public void testAnsiHintEnbledTrueIfCLICOLOROne() throws Exception {
        withEnvironmentVariable(ANSI_ENVIRONMENT_VARIABLES[0], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[1], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[2], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[3], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[4], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[5], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[6], null)
            .execute(() -> {
                assertFalse(Ansi.hintEnabled(), "no env vars set");
            });

        withEnvironmentVariable("CLICOLOR", "")
            .execute(() -> {
                assertFalse(Ansi.hintEnabled(), "Just defining CLICOLOR is not enough");
            });

        withEnvironmentVariable("CLICOLOR", "0")
            .execute(() -> {
                assertFalse(Ansi.hintEnabled(), "CLICOLOR=0 is not enough");
            });
        withEnvironmentVariable("CLICOLOR", "true")
            .execute(() -> {
                assertFalse(Ansi.hintEnabled(), "CLICOLOR=true is not enough");
            });
        withEnvironmentVariable("CLICOLOR", "1")
            .execute(() -> {
                assertTrue(Ansi.hintEnabled(), "CLICOLOR=1 enables");
            });
    }

    @Test
    public void testAnsiHintEnabledTrueIfConEmuANSIisON() throws Exception {
        withEnvironmentVariable(ANSI_ENVIRONMENT_VARIABLES[0], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[1], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[2], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[3], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[4], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[5], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[6], null)
            .execute(() -> {
                assertFalse(Ansi.hintEnabled(), "no env vars set");
            });

        withEnvironmentVariable("ConEmuANSI", "")
            .execute(() -> {
                assertFalse(Ansi.hintEnabled(), "Just defining ConEmuANSI is not enough");
            });

        withEnvironmentVariable("ConEmuANSI", "1")
            .execute(() -> {
                assertFalse(Ansi.hintEnabled(), "ConEmuANSI=1 is not enough");
            });

        withEnvironmentVariable("ConEmuANSI", "true")
            .execute(() -> {
                assertFalse(Ansi.hintEnabled(), "ConEmuANSI=true is not enough");
            });

        withEnvironmentVariable("ConEmuANSI", "on")
            .execute(() -> {
                assertFalse(Ansi.hintEnabled(), "ConEmuANSI=on does not enables");
            });

        withEnvironmentVariable("ConEmuANSI", "On")
            .execute(() -> {
                assertFalse(Ansi.hintEnabled(), "ConEmuANSI=On does not enables");
            });

        withEnvironmentVariable("ConEmuANSI", "ON")
            .execute(() -> {
                assertTrue(Ansi.hintEnabled(), "ConEmuANSI=ON enables");
            });
    }

    @Test
    public void testAnsiForceEnabledTrueIfCLICOLOR_FORCEisDefinedAndNonZero() throws Exception {
        withEnvironmentVariable(ANSI_ENVIRONMENT_VARIABLES[0], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[1], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[2], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[3], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[4], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[5], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[6], null)
            .execute(() -> {
                assertFalse(Ansi.forceEnabled(), "no env vars set");
            });

        withEnvironmentVariable("CLICOLOR_FORCE", "")
            .execute(() -> {
                assertTrue(Ansi.forceEnabled(), "Just defining CLICOLOR_FORCE is enough");
            });

        withEnvironmentVariable("CLICOLOR_FORCE", "1")
            .execute(() -> {
                assertTrue(Ansi.forceEnabled(), "CLICOLOR_FORCE=1 is enough");
            });

        withEnvironmentVariable("CLICOLOR_FORCE", "0")
            .execute(() -> {
                assertFalse(Ansi.forceEnabled(), "CLICOLOR_FORCE=0 is not forced");
            });
    }

    @Test
    public void testAnsiForceDisabledTrueIfNO_COLORDefined() throws Exception {
        withEnvironmentVariable(ANSI_ENVIRONMENT_VARIABLES[0], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[1], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[2], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[3], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[4], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[5], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[6], null)
            .execute(() -> {
                assertFalse(Ansi.forceDisabled(), "no env vars set");
            });

        withEnvironmentVariable("NO_COLOR", "")
            .execute(() -> {
                assertTrue(Ansi.forceDisabled(), "NO_COLOR defined without value");
            });

        withEnvironmentVariable("NO_COLOR", "abc")
            .execute(() -> {
                assertTrue(Ansi.forceDisabled(), "NO_COLOR defined without value");
            });
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
    public void testAnsiAutoIfSystemPropertyPicocliAnsiCleared() throws Exception {
        withEnvironmentVariable("CLICOLOR_FORCE", "1")
            .execute(() -> {
                restoreSystemProperties(() -> {
                    System.clearProperty("picocli.ansi");
                    assertTrue(Ansi.AUTO.enabled());
                });
            });
    }

    @Test
    public void testAnsiAutoIfSystemPropertyPicocliAnsiIsAuto() throws Exception {
        withEnvironmentVariable("CLICOLOR_FORCE", "1")
            .execute(() -> {
                restoreSystemProperties(() -> {
                    System.setProperty("picocli.ansi", "auto");
                    assertTrue(Ansi.AUTO.enabled());

                    System.setProperty("picocli.ansi", "Auto");
                    assertTrue(Ansi.AUTO.enabled());

                    System.setProperty("picocli.ansi", "AUTO");
                    assertTrue(Ansi.AUTO.enabled());
                });
            });
    }

    @Test
    public void testAnsiOffIfSystemPropertyPicocliAnsiIsNotAuto() throws Exception {
        restoreSystemProperties(() -> {
            System.setProperty("picocli.ansi", "auto1");

            withEnvironmentVariable("CLICOLOR_FORCE", "1")
                .execute(() -> {
                    assertFalse(Ansi.AUTO.enabled());
                });
        });
    }

    @Test
    public void testAnsiAutoForceDisabledOverridesForceEnabled() throws Exception {
        withEnvironmentVariable(ANSI_ENVIRONMENT_VARIABLES[0], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[1], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[NO_COLOR], "")
            .and(ANSI_ENVIRONMENT_VARIABLES[3], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[4], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[5], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[CLICOLOR_FORCE], "1")
            .execute(() -> {
                assertTrue(Ansi.forceDisabled());
                assertTrue(Ansi.forceEnabled());
                assertFalse(Ansi.hintDisabled());
                assertFalse(Ansi.hintEnabled());
                assertFalse(Ansi.AUTO.enabled(), "forceDisabled overrides forceEnabled");
            });
    }

    @Test
    public void testAnsiAutoForceDisabledOverridesHintEnabled() throws Exception {
        withEnvironmentVariable(ANSI_ENVIRONMENT_VARIABLES[0], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[1], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[NO_COLOR], "")
            .and(ANSI_ENVIRONMENT_VARIABLES[3], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[CLICOLOR], "1")
            .and(ANSI_ENVIRONMENT_VARIABLES[5], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[6], null)
            .execute(() -> {
                assertTrue(Ansi.forceDisabled());
                assertFalse(Ansi.forceEnabled());
                assertFalse(Ansi.hintDisabled());
                assertTrue(Ansi.hintEnabled());
                assertFalse(Ansi.AUTO.enabled(), "forceDisabled overrides hintEnabled");
            });
    }

    @Test
    public void testAnsiAutoForcedEnabledOverridesHintDisabled() throws Exception {
        withEnvironmentVariable(ANSI_ENVIRONMENT_VARIABLES[0], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[1], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[2], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[3], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[CLICOLOR], "0")
            .and(ANSI_ENVIRONMENT_VARIABLES[5], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[CLICOLOR_FORCE], "1")
            .execute(() -> {
                assertFalse(Ansi.forceDisabled());
                assertTrue(Ansi.hintDisabled());
                assertTrue(Ansi.forceEnabled());
                assertFalse(Ansi.hintEnabled());
                assertTrue(Ansi.AUTO.enabled(), "forceEnabled overrides hintDisabled");
            });

        withEnvironmentVariable(ANSI_ENVIRONMENT_VARIABLES[0], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[1], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[2], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[3], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[4], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[ConEmuANSI], "OFF")
            .and(ANSI_ENVIRONMENT_VARIABLES[CLICOLOR_FORCE], "1")
            .execute(() -> {
                assertFalse(Ansi.forceDisabled());
                assertTrue(Ansi.hintDisabled());
                assertTrue(Ansi.forceEnabled());
                assertFalse(Ansi.hintEnabled());
                assertTrue(Ansi.AUTO.enabled(), "forceEnabled overrides hintDisabled 2");
            });
    }

    @Test
    public void testAnsiAutoJansiConsoleInstalledOverridesHintDisabled() throws Exception {
        withEnvironmentVariable(ANSI_ENVIRONMENT_VARIABLES[0], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[1], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[2], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[3], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[CLICOLOR], "0")// hint disabled
            .and(ANSI_ENVIRONMENT_VARIABLES[5], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[6], null)
            .execute(() -> {
                restoreSystemProperties(() -> {

                    System.setProperty("os.name", "Windows");
                    assertTrue(Ansi.isWindows());
                    assertFalse(Ansi.isPseudoTTY());
                    assertFalse(Ansi.forceDisabled());
                    assertFalse(Ansi.forceEnabled());
                    assertTrue(Ansi.hintDisabled());
                    assertFalse(Ansi.hintEnabled());

                    assertFalse(Ansi.isJansiConsoleInstalled());

                    // Clear the globally cached jansiInstalled value that might
                    // have been set in a previous test to force the
                    // Ansi#isJansiConsoleInstalled method to recalculate
                    // the cached value.
                    Ansi.jansiInstalled = null;
                    AnsiConsole.systemInstall();
                    try {
                        assertTrue(Ansi.isJansiConsoleInstalled());
                        assertTrue(Ansi.AUTO.enabled());
                    } finally {
                        AnsiConsole.systemUninstall();
                    }
                });
            });
    }

    @Test
    public void testAnsiAutoHintDisabledOverridesHintEnabled() throws Exception {
        restoreSystemProperties(() -> {

            System.setProperty("os.name", "Windows");
            Ansi.jansiInstalled = null;
            withEnvironmentVariable(ANSI_ENVIRONMENT_VARIABLES[0], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[1], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[2], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[ANSICON], "1") // hint enabled
                .and(ANSI_ENVIRONMENT_VARIABLES[CLICOLOR], "0")// hint disabled
                .and(ANSI_ENVIRONMENT_VARIABLES[5], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[6], null)
                .execute(() -> {

                        assertTrue(Ansi.isWindows());
                    });

            withEnvironmentVariable(ANSI_ENVIRONMENT_VARIABLES[TERM], "xterm")// fake Cygwi
                .and(ANSI_ENVIRONMENT_VARIABLES[1], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[2], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[ANSICON], "1") // hint enabled
                .and(ANSI_ENVIRONMENT_VARIABLES[CLICOLOR], "0")// hint disabled
                .and(ANSI_ENVIRONMENT_VARIABLES[5], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[6], null)
                .execute(() -> {
                    assertTrue(Ansi.isPseudoTTY());
                });

            withEnvironmentVariable(ANSI_ENVIRONMENT_VARIABLES[TERM], "cygwin")
                .and(ANSI_ENVIRONMENT_VARIABLES[1], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[2], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[ANSICON], "1") // hint enabled
                .and(ANSI_ENVIRONMENT_VARIABLES[CLICOLOR], "0")// hint disabled
                .and(ANSI_ENVIRONMENT_VARIABLES[5], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[6], null)
                .execute(() -> {
                    assertTrue(Ansi.isPseudoTTY());

                    assertFalse(Ansi.isJansiConsoleInstalled());

                    assertFalse(Ansi.forceDisabled());
                    assertFalse(Ansi.forceEnabled());
                    assertTrue(Ansi.hintDisabled());
                    assertTrue(Ansi.hintEnabled());

                    assertFalse(Ansi.AUTO.enabled(), "Disabled overrides enabled");
                });
        });
    }

    @Test
    public void testAnsiAutoDisabledIfNoTty() throws Exception {
        if (Ansi.isTTY()) { return; } //
        withEnvironmentVariable(ANSI_ENVIRONMENT_VARIABLES[0], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[1], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[2], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[3], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[4], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[5], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[6], null)
            .execute(() -> {

                restoreSystemProperties(() -> {

                    System.setProperty("os.name", "Windows");
                    Ansi.jansiInstalled = null;
                    assertTrue(Ansi.isWindows());
                    assertFalse(Ansi.isPseudoTTY());
                    assertFalse(Ansi.isJansiConsoleInstalled());

                    assertFalse(Ansi.forceDisabled());
                    assertFalse(Ansi.forceEnabled());
                    assertFalse(Ansi.hintDisabled());
                    assertFalse(Ansi.hintEnabled());

                    assertFalse(Ansi.AUTO.enabled(), "Must have TTY if no JAnsi");
                });
            });
    }

    @Test
    public void testAnsiAutoEnabledIfNotWindows() throws Exception {
        if (!Ansi.isTTY()) { return; } // needs TTY for this test
        withEnvironmentVariable(ANSI_ENVIRONMENT_VARIABLES[0], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[1], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[2], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[3], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[4], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[5], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[6], null)
            .execute(() -> {
                restoreSystemProperties(() -> {

                    System.setProperty("os.name", "MMIX");
                    assertFalse(Ansi.isWindows());
                    assertFalse(Ansi.isPseudoTTY()); // TODO Mock this?
                    assertFalse(Ansi.isJansiConsoleInstalled());

                    assertFalse(Ansi.forceDisabled());
                    assertFalse(Ansi.forceEnabled());
                    assertFalse(Ansi.hintDisabled());
                    assertFalse(Ansi.hintEnabled());

                    assertTrue(Ansi.AUTO.enabled(), "If have TTY, enabled on non-Windows");
                });
            });
    }

    @Test
    public void testAnsiAutoEnabledIfWindowsPseudoTTY() throws Exception {
        withEnvironmentVariable(ANSI_ENVIRONMENT_VARIABLES[0], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[1], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[2], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[3], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[4], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[5], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[6], null)
            .execute(() -> {
                restoreSystemProperties(() -> {
                    System.setProperty("os.name", "Windows");
                    assertTrue(Ansi.isWindows());
                    assertFalse(Ansi.isJansiConsoleInstalled());

                    assertFalse(Ansi.forceDisabled());
                    assertFalse(Ansi.forceEnabled());
                    assertFalse(Ansi.hintDisabled());
                    assertFalse(Ansi.hintEnabled());
                });
            });

        withEnvironmentVariable(ANSI_ENVIRONMENT_VARIABLES[TERM], "xterm")
            .and(ANSI_ENVIRONMENT_VARIABLES[1], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[2], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[3], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[4], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[5], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[6], null)
            .execute(() -> {
                restoreSystemProperties(() -> {
                    System.setProperty("os.name", "Windows");

                    assertTrue(Ansi.isPseudoTTY());
                    assertTrue(Ansi.AUTO.enabled(), "If have Cygwin pseudo-TTY, enabled on Windows");
                });
            });

        withEnvironmentVariable(ANSI_ENVIRONMENT_VARIABLES[TERM], "cygwin")
            .and(ANSI_ENVIRONMENT_VARIABLES[1], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[2], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[3], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[4], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[5], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[6], null)
            .execute(() -> {
                restoreSystemProperties(() -> {
                    System.setProperty("os.name", "Windows");

                    assertTrue(Ansi.isPseudoTTY());
                    assertTrue(Ansi.AUTO.enabled(), "If have Cygwin pseudo-TTY, enabled on Windows");
                });
            });

        withEnvironmentVariable(ANSI_ENVIRONMENT_VARIABLES[TERM], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[OSTYPE], "Windows")
            .and(ANSI_ENVIRONMENT_VARIABLES[2], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[3], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[4], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[5], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[6], null)
            .execute(() -> {
                restoreSystemProperties(() -> {
                    System.setProperty("os.name", "Windows");

                    assertTrue(Ansi.isPseudoTTY());
                    assertTrue(Ansi.AUTO.enabled(), "If have MSYS pseudo-TTY, enabled on Windows");
                });
            });
    }
}
