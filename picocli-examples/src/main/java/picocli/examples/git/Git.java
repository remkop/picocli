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


@Command(name = "status",
        header = "Show the working tree status.",
        showDefaultValues = true,
        customSynopsis = "@|bold git status|@ [@|yellow <options>|@...] [--] [@|yellow <pathspec>|@...]",
        description = "Displays paths that have differences between the index file and the current HEAD commit, " +
                "paths that have differences between the working tree and the index file, and paths in the " +
                "working tree that are not tracked by Git (and are not ignored by gitignore(5)). The first " +
                "are what you would commit by running git commit; the second and third are what you could " +
                "commit by running git add before running git commit."
)
class GitStatus {
    @Option(names = {"-s", "--short"}, description = "Give the output in the short-format")
    boolean shortFormat;

    @Option(names = {"-b", "--branch"}, description = "Show the branch and tracking info even in short-format")
    boolean branchInfo;

    @Option(names = "--ignored", description = "Show ignored files as well")
    boolean showIgnored;

    @Option(names = {"-u", "--untracked"}, paramLabel = "<mode>", description = {
            "Show untracked files.",
            "The mode parameter is optional (defaults to `all`), and is used to specify the handling of untracked files.",
            "The possible options are:",
            " * @|yellow no|@ - Show no untracked files.",
            " * @|yellow normal|@ - Shows untracked files and directories.",
            " * @|yellow all|@ - Also shows individual files in untracked directories."
    })
    GitStatusMode mode = GitStatusMode.all;

    // the "status" subcommand's has an option "mode" with a fixed number of values, modeled by this enum
    enum GitStatusMode {all, no, normal};

}

@Command(name = "commit",
        sortOptions = false,
        headerHeading = "@|bold,underline Usage:|@%n%n",
        synopsisHeading = "%n",
        descriptionHeading = "%n@|bold,underline Description:|@%n%n",
        parameterListHeading = "%n@|bold,underline Parameters:|@%n",
        optionListHeading = "%n@|bold,underline Options:|@%n",
        header = "Record changes to the repository.",
        description = "Stores the current contents of the index in a new commit " +
                "along with a log message from the user describing the changes.")
class GitCommit {
    @Option(names = {"-a", "--all"},
            description = "Tell the command to automatically stage files that have been modified " +
                    "and deleted, but new files you have not told Git about are not affected.")
    boolean all;

    @Option(names = {"-p", "--patch"}, description = "Use the interactive patch selection interface to chose which changes to commit")
    boolean patch;

    @Option(names = {"-C", "--reuse-message"}, paramLabel = "<commit>",
            description = "Take an existing commit object, and reuse the log message and the " +
                    "authorship information (including the timestamp) when creating the commit.")
    String reuseMessageCommit;

    @Option(names = {"-c", "--reedit-message"}, paramLabel = "<commit>",
            description = "Like -C, but with -c the editor is invoked, so that the user can" +
                    "further edit the commit message.")
    String reEditMessageCommit;

    @Option(names = "--fixup", paramLabel = "<commit>",
            description = "Construct a commit message for use with rebase --autosquash.")
    String fixupCommit;

    @Option(names = "--squash", paramLabel = "<commit>",
            description = "Construct a commit message for use with rebase --autosquash. The commit" +
                    "message subject line is taken from the specified commit with a prefix of " +
                    "\"squash! \". Can be used with additional commit message options (-m/-c/-C/-F).")
    String squashCommit;

    @Option(names = {"-F", "--file"}, paramLabel = "<file>",
            description = "Take the commit message from the given file. Use - to read the message from the standard input.")
    File file;

    @Option(names = {"-m", "--message"}, paramLabel = "<msg>",
            description = "Use the given <msg> as the commit message. If multiple -m options" +
                    " are given, their values are concatenated as separate paragraphs.")
    List<String> message = new ArrayList<String>();

    @CommandLine.Parameters(paramLabel = "<files>", description = "the files to commit")
    List<File> files = new ArrayList<File>();
}

// defines some commands to show in the list (option/parameters fields omitted for this demo)
@Command(name = "add", header = "Add file contents to the index.")
class GitAdd {
}

@Command(name = "branch", header = "List, create, or delete branches.")
class GitBranch {
}

@Command(name = "checkout", header = "Checkout a branch or paths to the working tree.")
class GitCheckout {
}

@Command(name = "clone", header = "Clone a repository into a new directory.")
class GitClone {
}

@Command(name = "diff", header = "Show changes between commits, commit and working tree, etc.")
class GitDiff {
}

@Command(name = "merge", header = "Join two or more development histories together.")
class GitMerge {
}

@Command(name = "push", header = "Update remote refs along with associated objects.")
class GitPush {
}

@Command(name = "rebase", header = "Forward-port local commits to the updated upstream head.")
class GitRebase {
}

@Command(name = "tag", header = "Create, list, delete or verify a tag object signed with GPG.")
class GitTag {
}

