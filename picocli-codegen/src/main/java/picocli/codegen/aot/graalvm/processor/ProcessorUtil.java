package picocli.codegen.aot.graalvm.processor;

import picocli.CommandLine;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

public final class ProcessorUtil {
    private ProcessorUtil() {}

    static void generate(String fileName, String reflectionConfig, Map<Element, CommandLine.Model.CommandSpec> commands, ProcessingEnvironment processingEnv) throws IOException {
        String relativeName = createRelativeName(processingEnv.getOptions(), fileName);
        FileObject resource = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT,
                "",
                relativeName,
                commands.keySet().toArray(new Element[0]));

        write(reflectionConfig, resource);
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

    static void write(String text, FileObject resource) throws IOException {
        Writer writer = null;
        try {
            writer = resource.openWriter();
            writer.write(text);
            writer.flush();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    static String stacktrace(Exception e) {
        StringWriter writer = new StringWriter();
        e.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }
}
