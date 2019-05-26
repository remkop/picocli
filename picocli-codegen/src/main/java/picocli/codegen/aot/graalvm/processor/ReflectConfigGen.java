package picocli.codegen.aot.graalvm.processor;

import picocli.CommandLine;
import picocli.codegen.aot.graalvm.ReflectionConfigGenerator;

import javax.annotation.processing.ProcessingEnvironment;

class ReflectConfigGen extends AbstractGenerator {
    /**
     * Name of the processor option that can be used to disable generation of the reflect-config.json file.
     * The name of this constant is {@value}.
     */
    public static final String OPTION_DISABLE = "disable.reflect.config";

    ReflectConfigGen(ProcessingEnvironment env) {
        super(env, "reflect-config.json", OPTION_DISABLE);
    }

    @Override
    protected String generateConfig(CommandLine.Model.CommandSpec[] commands) throws Exception {
        return ReflectionConfigGenerator.generateReflectionConfig(commands);
    }
}
