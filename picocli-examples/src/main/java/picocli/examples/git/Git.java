package picocli.examples.git;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * This example shows how git could be implemented using picocli.
 * <p>
 * The following subcommands show detailed help: {@code git status} and {@code git commit}.
 * </p><p>
 * Invoking the {@code git} command without a subcommand shows the usage help.
 * </p>
 */
@Command(name = "git", mixinStandardHelpOptions = true, version = "subcommand demo 3.0",
        description = "Git is a fast, scalable, distributed revision control " +
                "system with an unusually rich command set that provides both " +
                "high-level operations and full access to internals.",
        commandListHeading = "%nCommands:%n%nThe most commonly used git commands are:%n",
        footer = "%nSee 'git help <command>' to read about a specific subcommand or concept.",
        subcommands = {
                GitAdd.class,
                GitBranch.class,
                GitClone.class,
                GitCommit.class,
                GitDiff.class,
                GitMerge.class,
                GitPush.class,
                GitRebase.class,
                GitStatus.class,
                GitTag.class,
                CommandLine.HelpCommand.class
        })
public class Git implements Runnable {
    @Option(names = "--git-dir", description = "Set the path to the repository")
    File path;

    @Option(names = "--exec-path", description = "Path to wherever your core Git programs are installed.")
    File execPath;

    @Option(names = {"-C"}, paramLabel = "<path>",
            description = "Run as if git was started in <path> instead of the current working directory")
    File currentDir;

    @Option(names = {"-c"}, description = "Pass a configuration parameter to the command.")
    Map<String, String> configParameters;

    @Option(names = {"--namespace"}, description = "Set the Git namespace. See gitnamespaces(7) for more details.")
    String name;

    @Option(names = {"-p", "--paginate"}, description = "Pipe all output into less (or if set, $PAGER) if standard output is a terminal.")
    boolean paginate;

    @Option(names = {"--bare"}, description = "Treat the repository as a bare repository.")
    boolean bare;

    @Spec
    CommandSpec spec;

    @Override
    public void run() {
        // if the command was invoked without subcommand, show the usage help
        spec.commandLine().usage(System.err);
    }

    public static void main(String[] args) {
        System.exit(new CommandLine(new Git()).execute(args));
    }
}


// defines some commands to show in the list (option/parameters fields omitted for this demo)
@Command(name = "add", header = "Add file contents to the index.")
class GitAdd implements Runnable {
    @Override public void run() { }
}

@Command(name = "branch", header = "List, create, or delete branches.")
class GitBranch implements Runnable {
    @Override public void run() { }
}

@Command(name = "checkout", header = "Checkout a branch or paths to the working tree.")
class GitCheckout implements Runnable {
    @Override public void run() { }
}

@Command(name = "clone", header = "Clone a repository into a new directory.")
class GitClone implements Runnable {
    @Override public void run() { }
}

@Command(name = "diff", header = "Show changes between commits, commit and working tree, etc.")
class GitDiff implements Runnable {
    @Override public void run() { }
}

@Command(name = "merge", header = "Join two or more development histories together.")
class GitMerge implements Runnable {
    @Override public void run() { }
}

@Command(name = "push", header = "Update remote refs along with associated objects.")
class GitPush implements Runnable {
    @Override public void run() { }
}

@Command(name = "rebase", header = "Forward-port local commits to the updated upstream head.")
class GitRebase implements Runnable {
    @Override public void run() { }
}

@Command(name = "tag", header = "Create, list, delete or verify a tag object signed with GPG.")
class GitTag implements Runnable {
    @Override public void run() { }
}

