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
 * {@link IDefaultValueProvider IDefaultValueProvider} implementation that loads default values for command line
 * options and positional parameters from a properties file or {@code Properties} object.
 * <h2>Location</h2>
 * By default, this implementation tries to find a properties file named
 * {@code ".<YOURCOMMAND>.properties"} in the user home directory, where {@code "<YOURCOMMAND>"} is the {@linkplain CommandLine.Command#name() name} of the command.
 * If a command has {@linkplain CommandLine.Command#aliases() aliases} in addition to its {@linkplain CommandLine.Command#name() name},
 * these aliases are also used to try to find the properties file. For example:
 * <pre>{@code
 * @Command(name = "git", defaultValueProvider = PropertiesDefaultProvider.class)
 * class Git { }
 * }</pre>
 * <p>The above will try to load default values from {@code new File(System.getProperty("user.home"), ".git.properties")}.
 * </p>
 * <p>
 * The location of the properties file can also be controlled with system property {@code "picocli.defaults.<YOURCOMMAND>.path"},
 * in which case the value of the property must be the path to the file containing the default values.
 * </p>
 * <p>
 * The location of the properties file may also be specified programmatically. For example:
 * </p>
 * <pre>
 * CommandLine cmd = new CommandLine(new MyCommand());
 * File defaultsFile = new File("path/to/config/mycommand.properties");
 * cmd.setDefaultValueProvider(new PropertiesDefaultProvider(defaultsFile));
 * cmd.execute(args);
 * </pre>
 * <h2>Format</h2>
 * <p>
 * For options, the key is either the {@linkplain CommandLine.Option#descriptionKey() descriptionKey},
 * or the option's {@linkplain OptionSpec#longestName() longest name}.
 * </p><p>
 * For positional parameters, the key is either the
 * {@linkplain CommandLine.Parameters#descriptionKey() descriptionKey},
 * or the positional parameter's {@linkplain PositionalParamSpec#paramLabel() param label}.
 * </p><p>
 * End users may not know what the {@code descriptionKey} of your options and positional parameters are, so be sure
 * to document that with your application.
 * </p>
 * <h2>Subcommands</h2>
 * <p>
 * The default values for options and positional parameters of subcommands can be included in the
 * properties file for the top-level command, so that end users need to maintain only a single file.
 * This can be achieved by prefixing the key with the command's qualified name.
 * For example, to give the {@code `git commit`} command's {@code --cleanup} option a
 * default value of {@code strip}, define a key of {@code git.commit.cleanup} and assign
 * it a default value.
 * </p><pre>
 * # /home/remko/.git.properties
 * git.commit.cleanup = strip
 * </pre>
 */
public class PropertiesDefaultProvider implements IDefaultValueProvider {

    private Properties properties;

    /**
     * Default constructor, used when this default value provider is specified in
     * the annotations:
     * <pre>
     * {@code
     * @Command(name = "mycmd",
     *     defaultValueProvider = PropertiesDefaultProvider.class)
     * class MyCommand // ...
     * }
     * </pre>
     * <p>
     * This loads default values from a properties file named
     * {@code ".mycmd.properties"} in the user home directory.
     * </p><p>
     * The location of the properties file can also be controlled with system property {@code "picocli.defaults.<YOURCOMMAND>.path"},
     * in which case the value of the property must be the path to the file containing the default values.
     * </p>
     * @see PropertiesDefaultProvider the PropertiesDefaultProvider class description
     */
    public PropertiesDefaultProvider() {}

    /**
     * This constructor loads default values from the specified properties object.
     * This may be used programmatically. For example:
     * <pre>
     * CommandLine cmd = new CommandLine(new MyCommand());
     * Properties defaults = getProperties();
     * cmd.setDefaultValueProvider(new PropertiesDefaultProvider(defaults));
     * cmd.execute(args);
     * </pre>
     * @param properties the properties containing the default values
     * @see PropertiesDefaultProvider the PropertiesDefaultProvider class description
     */
    public PropertiesDefaultProvider(Properties properties) {
        this.properties = properties;
    }

    /**
     * This constructor loads default values from the specified properties file.
     * This may be used programmatically. For example:
     * <pre>
     * CommandLine cmd = new CommandLine(new MyCommand());
     * File defaultsFile = new File("path/to/config/file.properties");
     * cmd.setDefaultValueProvider(new PropertiesDefaultProvider(defaultsFile));
     * cmd.execute(args);
     * </pre>
     * @param file the file to load default values from. Must be non-{@code null} and
     *             must contain default values in the standard java {@link Properties} format.
     * @see PropertiesDefaultProvider the PropertiesDefaultProvider class description
     */
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
                System.err.println("WARN - could not read defaults from " + file.getAbsolutePath() + ": " + ioe);
            }
        } else {
            System.err.println("WARN - defaults configuration file " + file.getAbsolutePath() + " does not exist or is not readable");
        }
        return result;
    }

    private static Properties loadProperties(CommandSpec commandSpec) {
        if (commandSpec == null) { return null; }
        Properties p = System.getProperties();
        for (String name : commandSpec.names()) {
            String path = p.getProperty("picocli.defaults." + name + ".path");
            File defaultPath = new File(p.getProperty("user.home"), "." + name + ".properties");
            File file = path == null ? defaultPath : new File(path);
            if (file.canRead()) {
                return createProperties(file);
            }
        }
        return loadProperties(commandSpec.parent());
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
