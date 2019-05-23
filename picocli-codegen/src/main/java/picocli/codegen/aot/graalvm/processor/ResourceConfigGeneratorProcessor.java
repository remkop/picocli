package picocli.codegen.aot.graalvm.processor;

import picocli.CommandLine.Model.CommandSpec;
import picocli.codegen.aot.graalvm.ResourceConfigGenerator;

import javax.annotation.processing.SupportedOptions;

@SupportedOptions({"groupId", "artifactId"})
public class ResourceConfigGeneratorProcessor extends AbstractConfigGeneratorProcessor {

    @Override
    protected String generateConfig() throws Exception {
        String[] bundles = new String[0]; // TODO get from options
        String[] resourceRegex = new String[0]; // TODO get from options
        String config = ResourceConfigGenerator.generateResourceConfig(
                allCommands.values().toArray(new CommandSpec[0]), bundles, resourceRegex);
        return config;
    }

    @Override
    protected String fileName() {
        return "resource-config.json";
    }
}
