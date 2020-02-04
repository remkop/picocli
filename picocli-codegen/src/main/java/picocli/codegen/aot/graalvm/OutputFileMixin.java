package picocli.codegen.aot.graalvm;

import picocli.CommandLine.Option;
import picocli.codegen.util.Util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

class OutputFileMixin {

    @Option(names = {"-o", "--output"}, description = "Output file to write the configuration to. " +
            "If not specified, the configuration is written to the standard output stream.")
    File outputFile;

    void write(String text) throws IOException {
        if (text != null && text.length() > 0) {
            if (outputFile == null) {
                System.out.print(text); // assume that text ends in line separator
            } else {
                writeToFile(text);
            }
        }
    }

    private void writeToFile(String result) throws IOException {
        FileWriter writer = null;
        try {
            File parent = outputFile.getAbsoluteFile().getParentFile();
            if (parent != null && !parent.exists() && !parent.mkdirs()) {
                System.err.println("Unable to mkdirs for " + outputFile.getAbsolutePath());
            }
            writer = new FileWriter(outputFile);
            writer.write(result);
        } finally {
            Util.closeSilently(writer);
        }
    }
}
