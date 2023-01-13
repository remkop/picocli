package picocli.examples.defaultprovider;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.IDefaultValueProvider;
import picocli.CommandLine.Model.ArgSpec;
import picocli.CommandLine.Model.OptionSpec;
import picocli.CommandLine.Option;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

@Command(name = "demo", mixinStandardHelpOptions = true, version = CommandLine.VERSION,
        description = "Demonstrates default provider",
        defaultValueProvider = SimpleDefaultProvider.class)
public class SimpleDefaultProviderDemo implements Runnable { // ...

    @Option(names = "-x", description = "Print count. ${DEFAULT-VALUE} by default.")
    int x;

    @Option(names = "-d") double d;
    @Option(names = { "-u", "--timeUnit"}) TimeUnit unit;

    @Override
    public void run() {
        for (int i = 0; i < x; i++) {
            System.out.printf("You selected %f, %s.%n", d, unit);
        }
    }

    public static void main(String[] args) {
        new CommandLine(new SimpleDefaultProviderDemo()).execute(args);
    }
}

class SimpleDefaultProvider implements IDefaultValueProvider {

    @Override
    public String defaultValue(ArgSpec argSpec) throws Exception {

        if (argSpec.isOption()) {
            OptionSpec option = (OptionSpec) argSpec;
            if ("--timeUnit".equals(option.longestName())) {
                return TimeUnit.SECONDS.name();
            }
            if ("-x".equals(option.longestName())) {
                return "3";
            }
        }
        return null;
    }
}
