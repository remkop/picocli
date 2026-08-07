package picocli.annotation.processing.tests;

import org.junit.Test;
import picocli.codegen.aot.graalvm.processor.NativeImageConfigGeneratorProcessor;

import java.io.IOException;
import java.io.InputStream;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Ensures {@code info.picocli.codegen} declares a {@code provides ... with}
 * for {@link javax.annotation.processing.Processor} so the annotation processor
 * is discovered when placed on {@code --processor-module-path}.
 *
 * @see <a href="https://github.com/remkop/picocli/issues/2464">#2464</a>
 */
public class Issue2464Test {

    private static final String MODULE_NAME = "info.picocli.codegen";
    private static final String PROCESSOR_SERVICE = "javax.annotation.processing.Processor";
    private static final String PROCESSOR_IMPL =
            "picocli.codegen.aot.graalvm.processor.NativeImageConfigGeneratorProcessor";

    @Test
    public void moduleInfoProvidesAnnotationProcessor() throws Exception {
        ModuleDescriptor descriptor = readCodegenModuleDescriptor();
        assertEquals(MODULE_NAME, descriptor.name());

        Set<String> providers = descriptor.provides().stream()
                .filter(p -> PROCESSOR_SERVICE.equals(p.service()))
                .flatMap(p -> p.providers().stream())
                .collect(Collectors.toSet());

        assertTrue(
                "info.picocli.codegen must provide " + PROCESSOR_SERVICE
                        + " with " + PROCESSOR_IMPL
                        + " (needed for --processor-module-path discovery). Actual provides: "
                        + descriptor.provides(),
                providers.contains(PROCESSOR_IMPL));
    }

    private static ModuleDescriptor readCodegenModuleDescriptor()
            throws IOException, URISyntaxException {
        URL location = NativeImageConfigGeneratorProcessor.class
                .getProtectionDomain()
                .getCodeSource()
                .getLocation();
        Path path = Paths.get(location.toURI());

        if (Files.isRegularFile(path)) {
            Optional<ModuleReference> ref = ModuleFinder.of(path).find(MODULE_NAME);
            if (ref.isPresent()) {
                return ref.get().descriptor();
            }
            fail("Could not find module " + MODULE_NAME + " in jar " + path);
        }

        // Exploded multi-release layout used by the Gradle build
        Path moduleInfo = path.resolve("META-INF/versions/9/module-info.class");
        if (Files.exists(moduleInfo)) {
            try (InputStream in = Files.newInputStream(moduleInfo)) {
                return ModuleDescriptor.read(in);
            }
        }

        fail("Could not locate module-info for " + MODULE_NAME + " at " + path);
        return null; // unreachable
    }
}
