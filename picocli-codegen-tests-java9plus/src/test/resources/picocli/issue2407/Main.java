package picocli.issue2407;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "top", subcommands = {Main.Edit.class, Main.Update.class})
public class Main {

    static class UsernameAndPassword {
        @Option(names = "--user", required = false)
        String username;
        @Option(names = "--pwd", required = false)
        String password;
    }

    @Command(name = "edit")
    static class Update {
        @ArgGroup
        UsernameAndPassword usernamePassword;
    }

    @Command(name = "update")
    static class Edit {
        @ArgGroup
        UsernameAndPassword usernamePassword;
    }
}
