package picocli.codegen.aot.graalvm.processor;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public final class ProcessorUtil {
    private ProcessorUtil() {}

    static void generate(String fileName, String content, ProcessingEnvironment processingEnv, Element... elements) throws IOException {
        FileObject resource = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT,
                "",
                fileName,
                elements);

        write(content, resource);
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
