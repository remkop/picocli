package picocli.codegen.aot.graalvm.processor;

import picocli.CommandLine;
import picocli.codegen.aot.graalvm.DynamicProxyConfigGenerator;

import javax.annotation.processing.ProcessingEnvironment;

class ProxyConfigGen extends AbstractGenerator {
    /**
     * Name of the processor option that can be used to disable generation of the proxy-config.json file.
     * The name of this constant is {@value}.
     */
    public static final String OPTION_DISABLE = "disable.proxy.config";
    /**
     * Name of the processor option that can be used to specify a comma-separated list
     * of interface classes requiring a dynamic proxy to include in the native image.
     * The name of this constant is {@value}.
     */
    public static final String OPTION_INTERFACE_CLASSES = "other.proxy.interfaces";

    ProxyConfigGen(ProcessingEnvironment env) {
        super(env, "proxy-config.json", OPTION_DISABLE);
    }

    @Override
    protected String generateConfig(CommandLine.Model.CommandSpec[] commands) {
        String interfaceClassesString = processingEnv.getOptions().get(OPTION_INTERFACE_CLASSES);
        String[] interfaceClasses = interfaceClassesString == null
                ? new String[0]
                : interfaceClassesString.split(",");

        return DynamicProxyConfigGenerator.generateProxyConfig(commands, interfaceClasses);
    }
}
