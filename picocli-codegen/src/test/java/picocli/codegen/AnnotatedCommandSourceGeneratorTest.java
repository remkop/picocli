package picocli.codegen;

import org.junit.Ignore;
import org.junit.Test;
import picocli.CommandLine.Model.CommandSpec;
import picocli.codegen.aot.graalvm.Example;
import picocli.codegen.util.Resources;

import static org.junit.Assert.*;

public class AnnotatedCommandSourceGeneratorTest {

    @Ignore
    @Test
    public void generate() {
        CommandSpec spec = CommandSpec.forAnnotatedObject(Example.class);
        String generated = new AnnotatedCommandSourceGenerator(spec).generate();
        //System.out.println(generated);
        
        String expected = Resources.slurp("/picocli/codegen/aot/graalvm/Example.txt");
        assertEquals(expected, generated);
    }
}
