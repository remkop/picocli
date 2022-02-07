package picocli;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.rules.TestRule;
import picocli.CommandLine.IParameterExceptionHandler;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;

import java.io.PrintWriter;

import static org.junit.Assert.*;

public class Issue1565HideParamOnUnknownOption {

    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public final SystemErrRule systemErrRule = new SystemErrRule().enableLog();

    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");

    static class Issue1565 {
        @Option(names = "--option") String option;
    }

    /**
     * Parameter exception handler that does not display the attached option parameter
     * when an unknown option was specified.
     * <p>
     * Example: when the end user entered `mycommand --passworf=TOPSECRET`, then the
     * default handler would show a message saying "Unknown option: '--passworf=TOPSECRET'".
     * </p><p>
     * Instead, this handler shows the message "Unknown option: '--passworf'".
     * </p><p>
     * Other than that, this handler behaves identical to the picocli built-in default
     * parameter exception handler, except that the built-in exception handler also
     * shows a stack trace when the picocli trace level is set to DEBUG.
     * </p>
     */
    static class MyUnknownOptionHandler implements IParameterExceptionHandler {
        public int handleParseException(ParameterException ex, String[] args) throws Exception {
            String errorMessage = ex.getMessage();
            int equalsPosition = errorMessage.indexOf("=");
            if (equalsPosition >= 1 && errorMessage.endsWith("'")) {
                errorMessage = errorMessage.substring(0, equalsPosition) + "'";
            }

            CommandLine cmd = ex.getCommandLine();
            PrintWriter writer = cmd.getErr();
            CommandLine.Help.ColorScheme colorScheme = cmd.getColorScheme();
            writer.println(colorScheme.errorText(errorMessage));
            if (!CommandLine.UnmatchedArgumentException.printSuggestions(ex, writer)) {
                ex.getCommandLine().usage(writer, colorScheme);
            }
            return mappedExitCode(ex, cmd.getExitCodeExceptionMapper(), cmd.getCommandSpec().exitCodeOnInvalidInput());
        }
        private int mappedExitCode(Throwable t, CommandLine.IExitCodeExceptionMapper mapper, int defaultExitCode) {
            try {
                return (mapper != null) ? mapper.getExitCode(t) : defaultExitCode;
            } catch (Exception ex) {
                ex.printStackTrace();
                return defaultExitCode;
            }
        }
    }

    @Test
    public void testIssue1565() {
        CommandLine cmd = new CommandLine(new Issue1565());
        cmd.setParameterExceptionHandler(new MyUnknownOptionHandler());
        cmd.execute("--aption=SENSITIVE_VALUE");

        String expected = String.format("" +
            "Unknown option: '--aption'%n" +
            "Usage: <main class> [--option=<option>]%n" +
            "      --option=<option>%n");
        assertEquals(expected, systemErrRule.getLog());
    }
}
