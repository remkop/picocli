package picocli.codegen.aot.graalvm.processor;

import javax.tools.FileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

public final class ProcessorUtil {
    private ProcessorUtil() {}

    static String createRelativeName(Map<String, String> options) {
        String groupId = options.get("groupId");
        String artifactId = options.get("artifactId");

        String relativeName = "META-INF/native-image/";
        if (groupId != null) { relativeName += groupId + "/"; }
        if (artifactId != null) { relativeName += artifactId + "/"; }
        relativeName += "picocli-generated/reflect-config.json";
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
