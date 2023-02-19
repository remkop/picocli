package picocli.test_jpms.modular_app.it;

import org.junit.jupiter.api.Test;
import picocli.test_jpms.modular_app.JpmsModularApp;

import static com.github.stefanbirkner.systemlambda.SystemLambda.tapSystemOut;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class JpmsModularAppIntegrationTest {

    @Test
    public void testOutput() throws Exception {
        String out = tapSystemOut(() -> {
            JpmsModularApp.main("-y=SomeText", "-x", "123");
        });
        String expected = String.format("-x=123, -y=SomeText, -q=false%n");
        assertEquals(expected, out);
    }
}
