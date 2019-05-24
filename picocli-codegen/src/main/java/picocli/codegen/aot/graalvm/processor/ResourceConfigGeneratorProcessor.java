package picocli.codegen.aot.graalvm.processor;

import picocli.CommandLine.Model.CommandSpec;
import picocli.codegen.aot.graalvm.ResourceConfigGenerator;

import javax.annotation.processing.SupportedOptions;

/**
 * @see ResourceConfigGenerator
 * @since 4.0
 */
@SupportedOptions({AbstractConfigGeneratorProcessor.OPTION_RELATIVE_PATH,
        ResourceConfigGeneratorProcessor.OPTION_BUNDLES,
        ResourceConfigGeneratorProcessor.OPTION_RESOURCE_REGEX})
public class ResourceConfigGeneratorProcessor extends AbstractConfigGeneratorProcessor {

    /**
     * Name of the processor option that can be used to specify a comma-separated list
     * of additional resource bundles to include in the native image.
     * The name of this constant is {@value}.
     */
    public static final String OPTION_BUNDLES = "bundles";
    /**
     * Name of the processor option that can be used to specify a comma-separated list
     * of regular expressions pointing to additional resource to include in the native image.
     * The name of this constant is {@value}.
     */
    public static final String OPTION_RESOURCE_REGEX = "resourceRegex";

    @Override
    protected String generateConfig() throws Exception {
        String bundlesString = processingEnv.getOptions().get(OPTION_BUNDLES);
        String[] bundles = bundlesString == null
                ? new String[0]
                : bundlesString.split(",");

        String resourceRegexString = processingEnv.getOptions().get(OPTION_RESOURCE_REGEX);
        String[] resourceRegex = resourceRegexString == null
                ? new String[0]
                : resourceRegexString.split(",");

        String config = ResourceConfigGenerator.generateResourceConfig(
                allCommands.values().toArray(new CommandSpec[0]), bundles, resourceRegex);
        return config;
    }

    @Override
    protected String fileName() {
        return "resource-config.json";
    }
}
