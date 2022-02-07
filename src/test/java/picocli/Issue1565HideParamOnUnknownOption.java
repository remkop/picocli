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

    @Test
    public void testIssue1565() {
        CommandLine cmd = new CommandLine(new Issue1565());
        cmd.setParameterExceptionHandler(new IParameterExceptionHandler() {
            public int handleParseException(ParameterException ex, String[] args) throws Exception {
                CommandLine cmd = ex.getCommandLine();
                PrintWriter writer = cmd.getErr();
                CommandLine.Help.ColorScheme colorScheme = cmd.getColorScheme();
                String errorMessage = ex.getMessage();
                int equalsPosition = errorMessage.indexOf("=");
                if (equalsPosition >= 1 && errorMessage.endsWith("'")) {
                    errorMessage = errorMessage.substring(0, equalsPosition) + "'";
                }
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
        });
        cmd.execute("--aption=SENSITIVE_VALUE");

        String expected = String.format("" +
            "Unknown option: '--aption'%n" +
            "Usage: <main class> [--option=<option>]%n" +
            "      --option=<option>%n");
        assertEquals(expected, systemErrRule.getLog());
    }
}
