package picocli.examples.interactive;

import picocli.CommandLine.Command;

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.Callable;

@Command(name = "interactive", subcommands = EnterPasswordCommand.class)
public class InteractivePositionalParam implements Callable<Integer> {
    @Override
    public Integer call() throws Exception {
        System.out.println("This command needs a subcommand");
        return null;
    }
}

@Command(name = "password",
        description = "Prompts for a password and prints its SHA-256 hash")
class EnterPasswordCommand implements Callable<Void> {

    // https://github.com/remkop/picocli/issues/840
    // Picocli cannot handle _single_ interactive positional parameters:
    // an interactive positional parameter must be followed by another positional parameter.
    //
    // Alternatively: use the System.console().readPassword() API directly:

    @Override
    public Void call() throws Exception {
        char[] password = System.console().readPassword("Password to encrypt");

        byte[] raw = char2bytes(password);
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        System.out.printf("Your password is hashed to %s.%n", base64(sha256.digest(raw)));

        Arrays.fill(password, '*');
        Arrays.fill(raw, (byte) 0);
        return null;
    }

    private byte[] char2bytes(char[] password) {
        byte[] raw = new byte[password.length];
        for (int i = 0; i < raw.length; i++) { raw[i] = (byte) password[i]; }
        return raw;
    }

    private String base64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }
}
