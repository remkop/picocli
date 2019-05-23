package picocli.codegen.aot.graalvm.processor;

import picocli.CommandLine.Model.CommandSpec;
import picocli.codegen.aot.graalvm.DynamicProxyConfigGenerator;

import javax.annotation.processing.SupportedOptions;

@SupportedOptions({"groupId", "artifactId"})
public class DynamicProxyConfigGeneratorProcessor extends AbstractConfigGeneratorProcessor {

    @Override
    protected String generateConfig() {
        String[] interfaceClasses = new String[0]; // TODO get from options
        String config = DynamicProxyConfigGenerator.generateProxyConfig(
                allCommands.values().toArray(new CommandSpec[0]), interfaceClasses);
        return config;
    }

    @Override
    protected String fileName() {
        return "proxy-config.json";
    }
}
