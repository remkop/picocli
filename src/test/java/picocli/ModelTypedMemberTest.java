package picocli;

import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.Assert.*;

public class ModelTypedMemberTest {

    @Test
    public void testInferTypes() {
        class App {
            @CommandLine.Parameters
            List<Class<? extends Class<? extends String>[]>> list;
        }
        assertEquals("<list>", CommandLine.Model.CommandSpec.forAnnotatedObject(new App()).positionalParameters().get(0).paramLabel());
    }

    @Test
    public void testTypedMemberGetAuxiliaryTypes() throws Exception {
        class App {
            @CommandLine.Option(names = "-x") public char x;
        }
        Field f = App.class.getDeclaredField("x");
        CommandLine.Model.TypedMember typedMember = new CommandLine.Model.TypedMember(f);
        assertArrayEquals(new Class[]{char.class}, typedMember.getAuxiliaryTypes());

        assertEquals(-1, typedMember.getMethodParamPosition());
    }

    @Test
    public void testTypedMemberGetMethodParamPosition() throws Exception {
        class App {
            @CommandLine.Command
            public void mymethod(@CommandLine.Option(names = "-x") char x) {}
        }
        Method method = App.class.getDeclaredMethod("mymethod", char.class);
        CommandLine.Model.MethodParam param = new CommandLine.Model.MethodParam(method, 0);

        CommandLine.Model.TypedMember typedMember = new CommandLine.Model.TypedMember(param, new App());
        assertEquals(0, typedMember.getMethodParamPosition());
    }
}
