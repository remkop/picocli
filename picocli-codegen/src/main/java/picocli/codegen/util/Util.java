package picocli.codegen.util;

import picocli.CommandLine;
import picocli.CommandLine.IFactory;
import picocli.CommandLine.Model.CommandSpec;

import java.util.ArrayList;
import java.util.List;

public class Util {
    private Util() {}

    public static List<CommandSpec> getCommandSpecs(String factoryClass, Class<?>[] classes) throws Exception {
        IFactory factory = CommandLine.defaultFactory();
        if (factoryClass != null) {
            factory = (IFactory) factory.create(Class.forName(factoryClass));
        }
        List<CommandSpec> specs = new ArrayList<CommandSpec>();
        for (Class<?> cls : classes) {
            specs.add(new CommandLine(cls, factory).getCommandSpec());
        }
        return specs;
    }
}
