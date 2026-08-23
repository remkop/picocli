package picocli.codegen.aot.graalvm.processor;

import picocli.codegen.aot.graalvm.DynamicProxyConfigGenerator;
import picocli.codegen.aot.graalvm.ReflectionConfigGenerator;
import picocli.codegen.aot.graalvm.ResourceConfigGenerator;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.SupportedOptions;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Annotation processor that validates picocli annotations and optionally generates GraalVM
 * native-image configuration files.
 * <p>
 * Gradle incremental annotation processing: this processor is registered as
 * {@code dynamic}. It reports {@link #GRADLE_ISOLATING} when all GraalVM generators are
 * disabled (validation only), and {@link #GRADLE_AGGREGATING} otherwise because each
 * generated {@code *-config.json} file is built from every {@code @Command} in the compilation.
 * </p>
 *
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

    /**
     * Gradle {@linkplain #getSupportedOptions() supported option} that marks this processor
     * as <em>isolating</em> for incremental annotation processing.
     * @since 4.7.8
     */
    public static final String GRADLE_ISOLATING = "org.gradle.annotation.processing.isolating";
    /**
     * Gradle {@linkplain #getSupportedOptions() supported option} that marks this processor
     * as <em>aggregating</em> for incremental annotation processing.
     * @since 4.7.8
     */
    public static final String GRADLE_AGGREGATING = "org.gradle.annotation.processing.aggregating";

    public NativeImageConfigGeneratorProcessor() {}

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        generators.add(new ReflectConfigGen(processingEnv));
        generators.add(new ResourceConfigGen(processingEnv));
        generators.add(new ProxyConfigGen(processingEnv));
    }

    /**
     * Includes the Gradle incremental-processing category so a {@code dynamic} registration
     * can resolve to isolating or aggregating at runtime.
     *
     * @since 4.7.8
     */
    @Override
    public Set<String> getSupportedOptions() {
        Set<String> result = new LinkedHashSet<String>(super.getSupportedOptions());
        result.add(gradleIncrementalProcessingCategory());
        return result;
    }

    /**
     * Isolating when no GraalVM files are written (all three disable options present);
     * aggregating when any generated config file combines multiple originating commands.
     */
    String gradleIncrementalProcessingCategory() {
        return allNativeImageGeneratorsDisabled() ? GRADLE_ISOLATING : GRADLE_AGGREGATING;
    }

    private boolean allNativeImageGeneratorsDisabled() {
        if (processingEnv == null) {
            return false;
        }
        Map<String, String> options = processingEnv.getOptions();
        return options.containsKey(ReflectConfigGen.OPTION_DISABLE)
                && options.containsKey(ResourceConfigGen.OPTION_DISABLE)
                && options.containsKey(ProxyConfigGen.OPTION_DISABLE);
    }
}
