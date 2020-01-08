package picocli.examples.validation;

import static picocli.CommandLine.Option;

public class Invalid3 {

    @Option(names = "-not-getter-or-setter")
    public void invalidNeitherGetterNorSetter() {
    }
}
