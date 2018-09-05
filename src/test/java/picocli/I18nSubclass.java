package picocli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "i18n-sub",
        description = {"orig sub desc 1", "orig sub desc 2"},
        descriptionHeading = "orig sub desc heading:%n",
        headerHeading = "orig sub header heading%n")
public class I18nSubclass extends I18nSuperclass {
    @Option(names = {"-a", "--aaa"})
    String a;

    @Option(names = {"-b", "--bbb"}, description = {"orig sub bbb description 1", "orig sub bbb description 2"})
    String b;

    @Option(names = {"-c", "--ccc"}, description = "orig sub ccc description")
    String c;

    @Parameters(index = "2", description = "sub")
    String param2;

    @Parameters(index = "3", description = "orig sub param1 description")
    String param3;
}
