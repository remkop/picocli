package picocli.codegen.aot.graalvm;

import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

@Command
public class Issue1274AbstractCommand {
    @Spec
    CommandSpec commandSpec;

    @Option(names = {"-v", "--verbose"})
    private void setVerbosity(boolean[] verbosity) {
    }
}
