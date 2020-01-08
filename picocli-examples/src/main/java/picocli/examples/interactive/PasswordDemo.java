package picocli.examples.interactive;

import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Spec;

import java.io.File;

public class PasswordDemo implements Runnable {
    @Option(names = "--password:file")
    File passwordFile;

    @Option(names = "--password:env")
    String passwordEnvironmentVariable;

    @Option(names = "--password", interactive = true)
    String password;

    @Spec
    CommandSpec spec;

    public void run() {
        if (password != null) {
            login(password);
        } else if (passwordEnvironmentVariable != null) {
            login(System.getenv(passwordEnvironmentVariable));
        } else if (passwordFile != null) {
            // below uses Java 8 NIO, create your own on older Java versions
            /*
            login(new String(Files.readAllBytes(passwordFile.toPath())));
            */
        } else {
            throw new ParameterException(spec.commandLine(), "Password required");
        }
    }

    private void login(String pwd) {
        System.out.printf("Password: %s%n", pwd);
    }

    public static void main(String[] args) {
        new CommandLine(new PasswordDemo()).execute(args);
    }
}
