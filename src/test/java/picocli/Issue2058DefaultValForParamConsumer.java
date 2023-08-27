package picocli;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Stack;

import org.junit.Assert;
import org.junit.Test;

import picocli.CommandLine.IParameterConsumer;
import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.Model.ArgSpec;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;

public class Issue2058DefaultValForParamConsumer {

    // Toy custom class
    static class CustomType {
        private final String lowerCase;

        public CustomType(String value){
            lowerCase = value.toLowerCase(Locale.ENGLISH);
        }

        // Check equals in the test-case
        public String toString(){
            return lowerCase;
        }
    }

    // Custom converter version
    static class MyTypeConverter implements ITypeConverter<CustomType> {
        public CustomType convert(String value)  {
            return new CustomType(value);
        }
    }

    static class CmdWithTypeConverter {

        @Option(names =  {"-f"},
            defaultValue = "defaultArg",
            converter = MyTypeConverter.class,
            split = "\\s")
        public List<CustomType> arguments = new ArrayList<CustomType>();
    }

    // Consumer version
    static class MyParamConsumer implements IParameterConsumer {

        public void consumeParameters(Stack<String> args, ArgSpec argSpec, CommandSpec commandSpec) {
            List<CustomType> list = argSpec.getValue();

            // Simple example, all remaining input are of this type
            while (!args.empty()){
                list.add(new CustomType(args.pop()));
            }
        }
    }

    static class CmdWithParamConsumer {

        @Option(names =  {"-f"},
            defaultValue = "defaultArg",
            parameterConsumer = MyParamConsumer.class,
            split = "\\s")
        public List<CustomType> arguments = new ArrayList<CustomType>();
    }

    @Test
    public void testDefaultValueUsedInCmdWithTypeConverter()  {
        // Converter implementation - no argument given
        CmdWithTypeConverter program = new CmdWithTypeConverter();
        new CommandLine(program).parseArgs();
        Assert.assertEquals(1, program.arguments.size());
        Assert.assertEquals("defaultarg", program.arguments.get(0).toString());
    }

    @Test
    public void testSpecifiedValueUsedInCmdWithTypeConverter()  {
        // Converter implementation - when giving an expecit argument (works as well)
        CmdWithTypeConverter program = new CmdWithTypeConverter();
        new CommandLine(program).parseArgs("-f", "explicitArg");
        Assert.assertEquals(1, program.arguments.size());
        Assert.assertEquals("explicitarg",program.arguments.get(0).toString());
    }

    @Test
    public void testDefaultValueUsedInCmdWithParamConsumer()  {
        // Test consumer - no argument
        CmdWithParamConsumer cmd = new CmdWithParamConsumer();
        new CommandLine(cmd).parseArgs();
        Assert.assertEquals(1, cmd.arguments.size());
        Assert.assertEquals("defaultarg",cmd.arguments.get(0).toString());
    }

    @Test
    public void testOnlySpecifiedValueUsedInCmdWithParamConsumer()  {
        // Consumer with explicit arg
        CmdWithParamConsumer cmd = new CmdWithParamConsumer();
        new CommandLine(cmd).parseArgs("-f", "explicitArgument");
        Assert.assertEquals(1, cmd.arguments.size()); // fails, cmd.arguments.size == 2 (explicit argument and "defaultarg")
        Assert.assertEquals("explicitargument", cmd.arguments.get(0).toString());
    }
}
