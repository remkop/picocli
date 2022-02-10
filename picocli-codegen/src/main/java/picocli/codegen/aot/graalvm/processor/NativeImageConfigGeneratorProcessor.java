package picocli.codegen.aot.graalvm.processor;

import picocli.codegen.aot.graalvm.DynamicProxyConfigGenerator;
import picocli.codegen.aot.graalvm.ReflectionConfigGenerator;
import picocli.codegen.aot.graalvm.ResourceConfigGenerator;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.SupportedOptions;

/**
 * @see ReflectionConfigGenerator
 * @see ResourceConfigGenerator
 * @see DynamicProxyConfigGenerator
 * @since 4.0
 */
@SupportedOptions({
    AbstractGenerator.OPTION_VERBOSE,
    NativeImageConfigGeneratorProcessor.OPTION_PROJECT,
    ReflectConfigGen.OPTION_DISABLE,
    ResourceConfigGen.OPTION_BUNDLES,
    ResourceConfigGen.OPTION_DISABLE,
    ResourceConfigGen.OPTION_RESOURCE_REGEX,
    ProxyConfigGen.OPTION_DISABLE,
    ProxyConfigGen.OPTION_INTERFACE_CLASSES,
})
public class NativeImageConfigGeneratorProcessor extends AbstractCompositeGeneratorProcessor {
    /**
     * Base path where generated files will be written to: {@value}.
     */
    public static final String BASE_PATH = "META-INF/native-image/picocli-generated/";
    /**
     * Name of the annotation processor {@linkplain ProcessingEnvironment#getOptions() option}
     * that can be used to control the actual location where the generated file(s)
     * are to be written to, relative to the {@link #BASE_PATH}.
     * The value of this constant is {@value}.
     */
    public static final String OPTION_PROJECT = "project";

    public NativeImageConfigGeneratorProcessor() {}

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        generators.add(new ReflectConfigGen(processingEnv));
        generators.add(new ResourceConfigGen(processingEnv));
        generators.add(new ProxyConfigGen(processingEnv));
    }
}
