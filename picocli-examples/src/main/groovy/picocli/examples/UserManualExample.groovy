package picocli.examples

import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Option

@Command(name = "MyApp", version = "Groovy picocli demo v4.1",
        mixinStandardHelpOptions = true,
        description = "@|bold Groovy|@ @|underline picocli|@ example")
class UserManualExample implements Runnable {

    @Option(names = ["-c", "--count"], paramLabel = "COUNT",
            description = "the count")
    int count = 0

    void run() {
        count.times {
            println("hello world $it...")
        }
    }

    static void main(String[] args) {
        CommandLine cmd = new CommandLine(new UserManualExample())
        cmd.execute(args)
    }
}
