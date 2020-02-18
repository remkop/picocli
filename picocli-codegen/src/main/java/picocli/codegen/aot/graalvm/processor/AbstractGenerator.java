package picocli.codegen.aot.graalvm.processor;

import picocli.CommandLine;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;
import javax.tools.StandardLocation;
import java.util.Map;

abstract class AbstractGenerator implements IGenerator {
    public static final String OPTION_VERBOSE = "verbose";
    protected final ProcessingEnvironment processingEnv;
    private final String fileName;
    private final String disableKey;

    public AbstractGenerator(ProcessingEnvironment processingEnv, String fileName, String disableKey) {
        this.processingEnv = processingEnv;
        this.fileName = fileName;
        this.disableKey = disableKey;
    }

    protected abstract String generateConfig(CommandLine.Model.CommandSpec[] commands) throws Exception;

    @Override
    public void generate(Map<Element, CommandLine.Model.CommandSpec> allCommands) {
        if (!enabled()) {
            logInfo("is not enabled");
            return;
        }
        try {
            String path = createRelativePath(fileName());
            logInfo("writing to: " + StandardLocation.CLASS_OUTPUT + "/" + path);
            String text = generateConfig(allCommands);
            ProcessorUtil.generate(StandardLocation.CLASS_OUTPUT, path, text, processingEnv, allCommands.keySet().toArray(new Element[0]));
        } catch (Exception e) {
            // We don't allow exceptions of any kind to propagate to the compiler
            fatalError(ProcessorUtil.stacktrace(e));
        }
    }

    protected boolean enabled() {
        Map<String, String> options = processingEnv.getOptions();
        return !options.containsKey(disableKey);
    }

    protected String fileName() { return fileName; }

    protected String createRelativePath(String fileName) {
        Map<String, String> options = processingEnv.getOptions();
        String id = options.get(NativeImageConfigGeneratorProcessor.OPTION_PROJECT);
        String relativeName = NativeImageConfigGeneratorProcessor.BASE_PATH;
        if (id != null) { relativeName += id.replace('\\', '/') + "/"; }
        return relativeName + fileName;
    }

    protected String generateConfig(Map<Element, CommandLine.Model.CommandSpec> allCommands) throws Exception {
        return generateConfig(allCommands.values().toArray(new CommandLine.Model.CommandSpec[0]));
    }

    /**
     * Prints a compile-time NOTE message.
     * @param msg the info message
     */
    protected void logInfo(String msg) {
        if (processingEnv.getOptions().containsKey(OPTION_VERBOSE)) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, getClass().getSimpleName() + " " + msg);
        }
    }
    /**
     * Prints a compile-time error message prefixed with "FATAL ERROR".
     * @param msg the error message with optional format specifiers
     */
    protected void fatalError(String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "FATAL ERROR: " + msg);
    }
}
