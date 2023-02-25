package picocli.test_jpms.modular_app;

import picocli.CommandLine;
import picocli.CommandLine.*;

@Command( name = "jpms-app",
   description = "I'm a JPMS modular app for testing",
       version = CommandLine.VERSION,
resourceBundle = "picocli.test_jpms.modular_app.messages"
)
public class JpmsModularApp implements Runnable {

    @Option(names = "-x")
    int x;

    @Option(names = "-y")
    String y;

    @Option(names = "-q")
    boolean quiet;
    @Override
    public void run() {
        System.out.printf("-x=%d, -y=%s, -q=%s%n", x, y, quiet);
    }

    public static void main(String... args) {
        new CommandLine(new JpmsModularApp()).execute(args);
    }
}
