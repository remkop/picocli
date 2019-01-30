package picocli;

import org.junit.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ModelTypedMemberTest {

    @Test
    public void testInferTypes() {
        class App {
            @CommandLine.Parameters
            List<Class<? extends Class<? extends String>[]>> list;
        }
        assertEquals("<list>", CommandLine.Model.CommandSpec.forAnnotatedObject(new App()).positionalParameters().get(0).paramLabel());
    }
}
