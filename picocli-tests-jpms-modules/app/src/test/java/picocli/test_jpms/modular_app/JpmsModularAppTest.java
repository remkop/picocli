package picocli.test_jpms.modular_app;

import org.junit.jupiter.api.Test;

import picocli.CommandLine;
import picocli.CommandLine.Help.Ansi;

import static com.github.stefanbirkner.systemlambda.SystemLambda.restoreSystemProperties;
import static com.github.stefanbirkner.systemlambda.SystemLambda.tapSystemErr;
import static com.github.stefanbirkner.systemlambda.SystemLambda.tapSystemOut;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class JpmsModularAppTest {

    @Test
    public void testOutput() throws Exception {
        String out = tapSystemOut(() -> {
            JpmsModularApp.main("-y=SomeText", "-x", "123");
        });
        String expected = String.format("-x=123, -y=SomeText, -q=false%n");
        assertEquals(expected, out);
    }

    @Test
    public void testUsageHelpFromResourceBundle() throws Exception {
        String expected = String.format("" +
            "the headerHeadingFromProperties%n" +
            "header0%n" +
            "header1%n" +
            "header2%n" +
            "header3%n" +
            "Usage: jpms-app [-q] [-x=<x>] [-y=<y>]%n" +
            "the descriptionHeading%n" +
            "Version control system%n" +
            "description0%n" +
            "description1%n" +
            "description2%n" +
            "description3%n" +
            "  -q%n" +
            "  -x=<x>%n" +
            "  -y=<y>%n" +
            "the footerHeading%n" +
            "footer0%n" +
            "footer1%n" +
            "footer2%n" +
            "footer3%n");

        String err = null;
        err = tapSystemErr(() -> {
//            CommandLine.tracer().setLevel(CommandLine.TraceLevel.DEBUG);
//            try {
                String actual = new CommandLine(new JpmsModularApp()).getUsageMessage(Ansi.OFF);
                assertEquals(expected, actual);
//            } catch (Exception ignored ) {}
        });
        assertEquals("", err);
    }
}
