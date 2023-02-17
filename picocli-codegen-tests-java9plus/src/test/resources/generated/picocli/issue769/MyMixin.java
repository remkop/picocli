package generated.picocli.issue769;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command
class MyMixin {
    @Option(names = "--some-option")
    public String someOption;
}
