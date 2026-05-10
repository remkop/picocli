package picocli.codegen.ksp

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

/**
 * KSP [SymbolProcessorProvider] for picocli that generates GraalVM native-image configuration files
 * ([reflect-config.json][picocli.codegen.aot.graalvm.ReflectionConfigGenerator],
 * [resource-config.json][picocli.codegen.aot.graalvm.ResourceConfigGenerator] and
 * [proxy-config.json][picocli.codegen.aot.graalvm.DynamicProxyConfigGenerator])
 * for picocli-annotated classes.
 *
 * Register this provider by adding `info.picocli:picocli-codegen-ksp` as a `ksp` dependency.
 *
 * **Supported KSP options:**
 * - `project` – subdirectory under `META-INF/native-image/picocli-generated/` for the output files
 * - `verbose` – if present, log progress messages
 * - `disable.reflect.config` – if present, skip generating `reflect-config.json`
 * - `disable.resource.config` – if present, skip generating `resource-config.json`
 * - `disable.proxy.config` – if present, skip generating `proxy-config.json`
 * - `other.resource.bundles` – comma-separated additional resource bundle base names to include
 * - `other.resource.patterns` – comma-separated additional Java regex resource patterns to include
 * - `other.proxy.interfaces` – comma-separated additional interface names to include in the proxy config
 *
 * @since 4.7.8
 */
class PicocliKspProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
        PicocliKspProcessor(environment)
}
