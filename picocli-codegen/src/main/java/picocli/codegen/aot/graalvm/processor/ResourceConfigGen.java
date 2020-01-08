package picocli.codegen.aot.graalvm.processor;

import picocli.CommandLine;
import picocli.codegen.aot.graalvm.ResourceConfigGenerator;

import javax.annotation.processing.ProcessingEnvironment;

class ResourceConfigGen extends AbstractGenerator {
    /**
     * Name of the processor option that can be used to disable generation of the resource-config.json file.
     * The name of this constant is {@value}.
     */
    public static final String OPTION_DISABLE = "disable.resource.config";
    /**
     * Name of the processor option that can be used to specify a comma-separated list
     * of additional resource bundles to include in the native image.
     * The name of this constant is {@value}.
     */
    public static final String OPTION_BUNDLES = "other.resource.bundles";
    /**
     * Name of the processor option that can be used to specify a comma-separated list
     * of regular expressions pointing to additional resource to include in the native image.
     * The name of this constant is {@value}.
     */
    public static final String OPTION_RESOURCE_REGEX = "other.resource.patterns";

    ResourceConfigGen(ProcessingEnvironment env) {
        super(env, "resource-config.json", OPTION_DISABLE);
    }

    @Override
    protected String generateConfig(CommandLine.Model.CommandSpec[] commands) throws Exception {
        String bundlesString = processingEnv.getOptions().get(OPTION_BUNDLES);
        String[] bundles = bundlesString == null
                ? new String[0]
                : bundlesString.split(",");

        String resourceRegexString = processingEnv.getOptions().get(OPTION_RESOURCE_REGEX);
        String[] resourceRegex = resourceRegexString == null
                ? new String[0]
                : resourceRegexString.split(",");
        return ResourceConfigGenerator.generateResourceConfig(
                commands, bundles, resourceRegex);
    }
}
