package picocli;

import picocli.CommandLine.Option;

public class I18nMixinOption {
    @Option(
        names = {"--optionWithDescriptionFromParent"},
        descriptionKey = "optionWithDescriptionFromParent"
    )
    public boolean parentOption1 = false;
}
