package picocli.codegen.aot.graalvm.processor;

import picocli.CommandLine.Model.CommandSpec;
import picocli.codegen.aot.graalvm.DynamicProxyConfigGenerator;

import javax.annotation.processing.SupportedOptions;

/**
 * @see DynamicProxyConfigGenerator
 * @since 4.0
 */
@SupportedOptions({AbstractConfigGeneratorProcessor.OPTION_RELATIVE_PATH})
public class DynamicProxyConfigGeneratorProcessor extends AbstractConfigGeneratorProcessor {

    /**
     * Name of the processor option that can be used to specify a comma-separated list
     * of interface classes requiring a dynamic proxy to include in the native image.
     * The name of this constant is {@value}.
     */
    public static final String OPTION_INTERFACE_CLASSES = "interfaceClasses";

    @Override
    protected String generateConfig() {
        String interfaceClassesString = processingEnv.getOptions().get(OPTION_INTERFACE_CLASSES);
        String[] interfaceClasses = interfaceClassesString == null
                ? new String[0]
                : interfaceClassesString.split(",");

        String config = DynamicProxyConfigGenerator.generateProxyConfig(
                allCommands.values().toArray(new CommandSpec[0]), interfaceClasses);
        return config;
    }

    @Override
    protected String fileName() {
        return "proxy-config.json";
    }
}
