package picocli.codegen.aot.graalvm.processor;

import picocli.CommandLine.Model.CommandSpec;
import picocli.codegen.annotation.processing.AbstractCommandSpecProcessor;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public abstract class AbstractConfigGeneratorProcessor extends AbstractCommandSpecProcessor {
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
            String path = createRelativeName(processingEnv.getOptions(), fileName());
            ProcessorUtil.generate(path, generateConfig(), processingEnv, commands.keySet().toArray(new Element[0]));
        } catch (Exception e) {
            // We don't allow exceptions of any kind to propagate to the compiler
            fatalError(ProcessorUtil.stacktrace(e));
        }
        return false;
    }

    static String createRelativeName(Map<String, String> options, String fileName) {
        String groupId = options.get("groupId");
        String artifactId = options.get("artifactId");

        String relativeName = "META-INF/native-image/";
        if (groupId != null) { relativeName += groupId + "/"; }
        if (artifactId != null) { relativeName += artifactId + "/"; }
        relativeName += "picocli-generated/" + fileName;
        return relativeName;
    }

    protected abstract String generateConfig() throws Exception;
    protected abstract String fileName();
}
