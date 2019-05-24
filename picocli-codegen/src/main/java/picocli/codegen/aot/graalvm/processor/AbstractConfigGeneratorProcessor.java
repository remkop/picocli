package picocli.codegen.aot.graalvm.processor;

import picocli.CommandLine.Model.CommandSpec;
import picocli.codegen.annotation.processing.AbstractCommandSpecProcessor;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.StandardLocation;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Base class for writing annotation processors that do something with {@code CommandSpec} models.
 * @since 4.0
 */
public abstract class AbstractConfigGeneratorProcessor extends AbstractCommandSpecProcessor {
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
    public static final String OPTION_RELATIVE_PATH = "relative.path";

    Map<Element, CommandSpec> allCommands = new LinkedHashMap<Element, CommandSpec>();

    @Override
    protected boolean handleCommands(Map<Element, CommandSpec> commands,
                                     Set<? extends TypeElement> annotations,
                                     RoundEnvironment roundEnv) {

        if (!roundEnv.processingOver()) {
            allCommands.putAll(commands);
            return false;
        }

        try {
            String path = createRelativePath(processingEnv.getOptions(), fileName());
            logInfo("writing to: " + StandardLocation.CLASS_OUTPUT + "/" + path);
            ProcessorUtil.generate(StandardLocation.CLASS_OUTPUT, path, generateConfig(), processingEnv, allCommands.keySet().toArray(new Element[0]));
        } catch (Exception e) {
            // We don't allow exceptions of any kind to propagate to the compiler
            fatalError(ProcessorUtil.stacktrace(e));
        }
        return false;
    }

    protected String createRelativePath(Map<String, String> options, String fileName) {
        String id = options.get(OPTION_RELATIVE_PATH);
        String relativeName = BASE_PATH;
        if (id != null) { relativeName += id.replace('\\', '/') + "/"; }
        return relativeName + fileName;
    }

    protected abstract String generateConfig() throws Exception;
    protected abstract String fileName();
}
