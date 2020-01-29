package picocli.codegen.aot.graalvm;

import picocli.CommandLine;
import picocli.CommandLine.IFactory;
import picocli.CommandLine.Model.CommandSpec;

import java.util.ArrayList;
import java.util.List;

class Util {
    private Util() {}

    static List<CommandSpec> getCommandSpecs(String factoryClass, Class<?>[] classes) throws Exception {
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
