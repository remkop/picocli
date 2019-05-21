package picocli.codegen.aot.graalvm.processor;

import picocli.CommandLine.Model.CommandSpec;
import picocli.codegen.annotation.processing.AbstractCommandSpecProcessor;
import picocli.codegen.aot.graalvm.ReflectionConfigGenerator;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@SupportedOptions({"groupId", "artifactId"})
public class ReflectionConfigGeneratorProcessor extends AbstractCommandSpecProcessor {
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
            String reflectionConfig = ReflectionConfigGenerator.generateReflectionConfig(
                    allCommands.values().toArray(new CommandSpec[0]));

            String relativeName = createRelativeName();
            FileObject resource = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT,
                    "",
                    relativeName,
                    commands.keySet().toArray(new Element[0]));

            write(reflectionConfig, resource);
        } catch (Exception e) {
            // We don't allow exceptions of any kind to propagate to the compiler
            StringWriter writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            fatalError(writer.toString());
        }
        return false;
    }

    private String createRelativeName() {
        Map<String, String> options = processingEnv.getOptions();
        String groupId = options.get("groupId");
        String artifactId = options.get("artifactId");

        String relativeName = "META-INF/native-image/";
        if (groupId != null) { relativeName += groupId + "/"; }
        if (artifactId != null) { relativeName += artifactId + "/"; }
        relativeName += "picocli-generated/reflect-config.json";
        return relativeName;
    }

    private void write(String reflectionConfig, FileObject resource) throws IOException {
        Writer writer = null;
        try {
            writer = resource.openWriter();
            writer.write(reflectionConfig);
            writer.flush();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }
}
