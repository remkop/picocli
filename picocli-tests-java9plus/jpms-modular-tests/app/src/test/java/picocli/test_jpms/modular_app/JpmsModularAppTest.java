package picocli.test_jpms.modular_app;

import org.junit.jupiter.api.Test;

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
}
