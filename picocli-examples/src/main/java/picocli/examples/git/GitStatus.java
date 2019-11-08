package picocli.examples.git;

import picocli.CommandLine;

@CommandLine.Command(name = "status",
        header = "Show the working tree status.",
        showDefaultValues = true,
        customSynopsis = "@|bold git status|@ [@|yellow <options>|@...] [--] [@|yellow <pathspec>|@...]",
        description = "Displays paths that have differences between the index file and the current HEAD commit, " +
                "paths that have differences between the working tree and the index file, and paths in the " +
                "working tree that are not tracked by Git (and are not ignored by gitignore(5)). The first " +
                "are what you would commit by running git commit; the second and third are what you could " +
                "commit by running git add before running git commit."
)
class GitStatus implements Runnable {
    @CommandLine.Option(names = {"-s", "--short"}, description = "Give the output in the short-format")
    boolean shortFormat;

    @CommandLine.Option(names = {"-b", "--branch"}, description = "Show the branch and tracking info even in short-format")
    boolean branchInfo;

    @CommandLine.Option(names = "--ignored", description = "Show ignored files as well")
    boolean showIgnored;

    @CommandLine.Option(names = {"-u", "--untracked"}, paramLabel = "<mode>", description = {
            "Show untracked files.",
            "The mode parameter is optional (defaults to `all`), and is used to specify the handling of untracked files.",
            "The possible options are:",
            " * @|yellow no|@ - Show no untracked files.",
            " * @|yellow normal|@ - Shows untracked files and directories.",
            " * @|yellow all|@ - Also shows individual files in untracked directories."
    })
    GitStatusMode mode = GitStatusMode.all;

    @Override
    public void run() {
        System.out.println("Your status: ok, I guess...");
    }

    // the "status" subcommand's has an option "mode" with a fixed number of values, modeled by this enum
    enum GitStatusMode {all, no, normal};

}
