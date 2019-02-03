package picocli.codegen.aot.graalvm;

import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;

public class Issue622AbstractCommand {
    @Option(names = "-v")
    boolean verbose;

    @Parameters
    File file;
}
