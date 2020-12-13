package picocli.examples.interactive;

import picocli.CommandLine;
import picocli.CommandLine.Parameters;

public class UserPasswordDemo implements Runnable {

    @Parameters(index = "0", description = {"User"}, interactive = true, echo = true, prompt = "Enter your user: ")
    String user;

    @Parameters(index = "1", description = {"Password"}, interactive = true, prompt = "Enter your password: ")
    String password;

    @Parameters(index = "2", description = {"Action"})
    String action;

    public void run() {
        // See also PasswordDemo
        login(user, password);
        doAction(action);
    }

    private void login(String user, String pwd) {
        System.out.printf("User: %s, Password: %s%n", user, pwd);
    }

    private void doAction(String action) {
        System.out.printf("Action: %s%n", action);
    }

    public static void main(String[] args) {
        new CommandLine(new UserPasswordDemo()).execute(args);
    }
}
