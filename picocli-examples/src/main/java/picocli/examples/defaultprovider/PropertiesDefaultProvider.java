package picocli.examples.defaultprovider;

import picocli.CommandLine;
import picocli.CommandLine.IDefaultValueProvider;
import picocli.CommandLine.Model.ArgSpec;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.OptionSpec;
import picocli.CommandLine.Model.PositionalParamSpec;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Properties;

/**
 * {@link IDefaultValueProvider} implementation that loads default values from a
 * properties file named {@code "." + command.name + ".properties"} in the user home directory.
 * <p>
 * For options, the key is either the {@linkplain CommandLine.Option#descriptionKey() descriptionKey},
 * or the option's {@linkplain OptionSpec#longestName() longest name}.
 * </p><p>
 * For positional parameters, the key is either the
 * {@linkplain CommandLine.Parameters#descriptionKey() descriptionKey},
 * or the positional parameter's {@linkplain PositionalParamSpec#paramLabel() param label}.
 * </p><p>
 * In addition, it is possible to prefix the key with the command's qualified name.
 * For example, to give the {@code git commit} command's {@code --cleanup} option a
 * default value of {@code strip}, define a key of {@code git.commit.cleanup} and assign
 * it a default value.
 * </p><pre>
 * # /home/remko/.git.properties
 * git.commit.cleanup = strip
 * </pre>
 */
public class PropertiesDefaultProvider implements IDefaultValueProvider {

    private Properties properties;

    public PropertiesDefaultProvider() {}

    public PropertiesDefaultProvider(Properties properties) {
        this.properties = properties;
    }

    public PropertiesDefaultProvider(File file) {
        this(createProperties(file));
    }

    private static Properties createProperties(File file) {
        if (file == null) {
            throw new NullPointerException("file is null");
        }
        Properties result = new Properties();
        if (file.exists() && file.canRead()) {
            Reader reader = null;
            try {
                reader = new FileReader(file);
                result.load(reader);
            } catch (IOException ioe) {
                System.err.println("INFO - could not read defaults from " + file.getAbsolutePath() + ": " + ioe);
            }
        } else {
            System.err.println("INFO - defaults configuration file " + file.getAbsolutePath() + " does not exist or is not readable");
        }
        return result;
    }

    private static Properties loadProperties(CommandSpec commandSpec) {
        for (String name : commandSpec.names()) {
            File file = new File(System.getProperty("user.home"), "." + name + ".properties");
            if (file.canRead()) {
                return createProperties(file);
            }
        }
        return null;
    }

    @Override
    public String defaultValue(ArgSpec argSpec) throws Exception {
        if (properties == null) {
            properties = loadProperties(argSpec.command());
        }
        if (properties == null || properties.isEmpty()) {
            return null;
        }
        return argSpec.isOption()
                ? optionDefaultValue((OptionSpec) argSpec)
                : positionalDefaultValue((PositionalParamSpec) argSpec);
    }

    private String optionDefaultValue(OptionSpec option) {
        String result = getValue(option.descriptionKey(), option.command());
        result = result != null ? result : getValue(stripPrefix(option.longestName()), option.command());
        return result;
    }
    private static String stripPrefix(String prefixed) {
        for (int i = 0; i < prefixed.length(); i++) {
            if (Character.isJavaIdentifierPart(prefixed.charAt(i))) {
                return prefixed.substring(i);
            }
        }
        return prefixed;
    }

    private String positionalDefaultValue(PositionalParamSpec positional) {
        String result = getValue(positional.descriptionKey(), positional.command());
        result = result != null ? result : getValue(positional.paramLabel(), positional.command());
        return result;
    }

    private String getValue(String key, CommandSpec spec) {
        String result = null;
        if (spec != null) {
            String cmd = spec.qualifiedName(".");
            result = properties.getProperty(cmd + "." + key);
        }
        return result != null ? result : properties.getProperty(key);
    }
}
