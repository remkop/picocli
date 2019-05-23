package picocli.codegen.aot.graalvm.processor;

import picocli.CommandLine.Model.CommandSpec;
import picocli.codegen.aot.graalvm.ReflectionConfigGenerator;

import javax.annotation.processing.SupportedOptions;

@SupportedOptions({"groupId", "artifactId"})
public class ReflectionConfigGeneratorProcessor extends AbstractConfigGeneratorProcessor {

    @Override
    protected String generateConfig() throws Exception {
        String reflectionConfig = ReflectionConfigGenerator.generateReflectionConfig(
                allCommands.values().toArray(new CommandSpec[0]));
        return reflectionConfig;
    }

    @Override
    protected String fileName() {
        return "reflect-config.json";
    }
}
