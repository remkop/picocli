package picocli.issue1134;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.IVersionProvider;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;


@Command(name = "top",
        versionProvider = MyVersionProvider.class,
        resourceBundle = "mybundle5")
public class Issue1134 {

    @CommandLine.Option(names = "--level")
    private String level;

    @Spec
    CommandSpec spec;
}

class MyVersionProvider implements IVersionProvider {
    @Spec
    CommandSpec spec;

    public String[] getVersion() {
        return new String[] {spec.qualifiedName() + " 1.0"};
    }
}