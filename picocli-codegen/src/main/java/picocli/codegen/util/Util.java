package picocli.codegen.util;

import picocli.CommandLine;
import picocli.CommandLine.IFactory;
import picocli.CommandLine.Model.CommandSpec;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;

public final class Util {
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

    public static List<CommandSpec> flattenHierarchy(CommandSpec root) {
        IdentityHashMap<CommandSpec, CommandSpec> result = new IdentityHashMap<CommandSpec, CommandSpec>();
        addRecursively(root, result);
        return new ArrayList<CommandSpec>(result.keySet());
    }

    private static void addRecursively(CommandSpec command, IdentityHashMap<CommandSpec, CommandSpec> result) {
        result.put(command, command);
        for (CommandLine sub : command.subcommands().values()) {
            addRecursively(sub.getCommandSpec(), result);
        }
    }

    public static void closeSilently(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
            }
        }
    }
}
