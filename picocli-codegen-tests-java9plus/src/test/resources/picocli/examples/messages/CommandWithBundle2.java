package picocli.example.messages.CommandWithBundle;

import picocli.CommandLine.Command;

@Command(resourceBundle = "command-method-demo")
public class CommandWithBundle2 {
// we need a separate class, identical to CommandWithBundle, to avoid caching across tests
}
