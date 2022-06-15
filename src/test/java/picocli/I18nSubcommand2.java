package picocli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Mixin;

/**
 * This command was created to test Issue #1706.
 * We are testing here to ensure that the description for a shared Mixin option, where the description for that option
 * is defined in a parent command's resource bundle, can still be displayed correctly by a subcommand that's using a
 * resource bundle that doesn't have that Mixin's option description defined. Ideally, if the subcommand's resource
 * bundle doesn't have the needed description key, picocli SHOULD check the resource bundle of the parent command to see
 * if the description key exists.
 */
@Command(name = "sub2",
        subcommands = CommandLine.HelpCommand.class,
        mixinStandardHelpOptions = true,
        resourceBundle = "picocli/ResourceBundlePropagationTest")
public class I18nSubcommand2 {
    @Option(names = {"-a", "--aaa"}, descriptionKey = "additional.value")
    String a;

    // This is what we're really testing.
    // The description key for this Mixin's options are defined in the parent command's resource bundle.
    @Mixin
    private I18nMixinOption i18nMixinOption;
}
