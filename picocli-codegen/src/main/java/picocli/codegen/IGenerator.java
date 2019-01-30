package picocli.codegen;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

public interface IGenerator {
    void generate(OutputStream out, Map<String, ?> options) throws IOException;
}
