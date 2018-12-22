package picocli;

import org.junit.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ModelArgsReflectionTest {

    @Test
    public void testInstantiatable() {
        new CommandLine.Model.ArgsReflection(); // no error
    }

    @Test
    public void testInferLabel() throws Exception{
        Method m = CommandLine.Model.ArgsReflection.class.getDeclaredMethod("inferLabel", String.class, String.class, Class.class, Class[].class);
        m.setAccessible(true);
        assertEquals("<String=String>", m.invoke(null, "", "fieldName", Map.class, new Class[0]));
        assertEquals("<String=String>", m.invoke(null, "", "fieldName", Map.class, new Class[]{Integer.class}));
        assertEquals("<String=String>", m.invoke(null, "", "fieldName", Map.class, new Class[]{null, Integer.class}));
        assertEquals("<String=String>", m.invoke(null, "", "fieldName", Map.class, new Class[]{Integer.class, null}));
        assertEquals("<Integer=Integer>", m.invoke(null, "", "fieldName", Map.class, new Class[]{Integer.class, Integer.class}));
    }

    @Test
    public void testInferTypes() {
        class App {
            @CommandLine.Parameters
            List<Class<? extends Class<? extends String>[]>> list;
        }
        assertEquals("<list>", CommandLine.Model.CommandSpec.forAnnotatedObject(new App()).positionalParameters().get(0).paramLabel());
    }
}
