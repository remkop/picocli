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

@Command(name = "demo", mixinStandardHelpOptions = true, version = "3.9.3",
        description = "Demonstrate parsing & type conversion",
        defaultValueProvider = PropertyDefaultProvider.class)
public class SimplePropertyDefaultProviderDemo implements Runnable { // ...

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
        new CommandLine(new SimplePropertyDefaultProviderDemo()).execute(args);
    }
}

class PropertyDefaultProvider implements IDefaultValueProvider {
    private Properties properties;

    @Override
    public String defaultValue(ArgSpec argSpec) throws Exception {
        if (properties == null) {
            properties = new Properties();
            File file = new File(System.getProperty("user.home"), "defaults.properties");
            try (Reader reader = new FileReader(file)) {
                properties.load(reader);
            }
        }
        return argSpec.isOption()
                ? properties.getProperty(((OptionSpec) argSpec).longestName())
                : properties.getProperty(argSpec.paramLabel());
    }
}
