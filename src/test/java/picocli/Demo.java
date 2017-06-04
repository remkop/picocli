/*
   Copyright 2017 Remko Popma

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package picocli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Demonstrates picocli subcommands.
 */
@Command(name = "picocli.Demo", sortOptions = false,
        header = {
                "@|green        .__                    .__  .__ |@",
                "@|green ______ |__| ____  ____   ____ |  | |__||@",
                "@|green \\____ \\|  |/ ___\\/  _ \\_/ ___\\|  | |  ||@",
                "@|green |  |_> >  \\  \\__(  <_> )  \\___|  |_|  ||@",
                "@|green |   __/|__|\\___  >____/ \\___  >____/__||@",
                "@|green |__|           \\/           \\/         |@",
                ""},
        //descriptionHeading = "@|bold %nDescription|@:%n",
        description = {
                "",
                "Demonstrates picocli subcommands parsing and usage help.", },
        optionListHeading = "@|bold %nOptions|@:%n",
        footer = {
                "",
                "@|bold VM Options|@:",
                "Run with @|yellow -ea|@ to enable assertions used in the tests.",
                "Run with @|yellow -Dpicocli.ansi|@=@|italic true|@ to force picocli to use ansi codes,",
                " or with @|yellow -Dpicocli.ansi|@=@|italic false|@ to force picocli to NOT use ansi codes.",
                "(By default picocli will use ansi codes if the platform supports it.)",
                "",
                "@|cyan If you would like to contribute or report an issue|@",
                "@|cyan go to github: https://github.com/remkop/picocli|@",
                "",
                "@|cyan If you like the project star it on github and follow me on twitter!|@",
                "@|cyan This project is created and maintained by Remko Popma (@remkopopma)|@",
                ""})
public class Demo implements Runnable {
    public static void main(String[] args) {
        CommandLine.run(new Demo(), System.err, args);
    }

    @Option(names = {"-1", "--showUsageForSubcommandGitCommit"}, description = "Shows usage help for the git-commit subcommand")
    private boolean showUsageForSubcommandGitCommit;

    @Option(names = {"-2", "--showUsageForMainCommand"}, description = "Shows usage help for a command with subcommands")
    private boolean showUsageForMainCommand;

    @Option(names = {"-3", "--showUsageForSubcommandGitStatus"}, description = "Shows usage help for the git-status subcommand")
    private boolean showUsageForSubcommandGitStatus;

    @Option(names = "--simple", description = "Show help for the first simple Example in the manual")
    private boolean showSimpleExample;

    @Option(names = "--mixed", hidden = true, description = "Show help with mixed Ansi colors and styles in description")
    private boolean showAnsiInDescription;

    @Option(names = {"-i", "--index"}, description = "" +
            "@|fg(21) S|@" +
            "@|fg(57) h|@" +
            "@|fg(93) o|@" +
            "@|fg(129) w|@" +
            "@|fg(129)  |@" +
            "@|fg(165) 2|@" +
            "@|fg(201) 5|@" +
            "@|fg(225) 6|@" +
            "@|fg(123)  |@" +
            "@|fg(122) c|@" +
            "@|fg(120) o|@" +
            "@|fg(118) l|@" +
            "@|fg(148) o|@" +
            "@|fg(142) r|@" +
            "@|fg(136)  |@" +
            "@|fg(136) p|@" +
            "@|fg(130) a|@" +
            "@|fg(124) l|@" +
            "@|fg(160) e|@" +
            "@|fg(196) t|@" +
            "@|fg(198) t|@" +
            "@|fg(199) e|@" +
            "@|fg(200)  |@" +
            "@|fg(201) i|@" +
            "@|fg(213) n|@" +
            "@|fg(219) d|@" +
            "@|fg(225) e|@" +
            "@|fg(231) x|@" +
            "@|fg(230)  |@" +
            "@|fg(229) v|@" +
            "@|fg(228) a|@" +
            "@|fg(227) l|@" +
            "@|fg(226) u|@" +
            "@|fg(190) e|@" +
            "@|fg(154) s|@")
    private boolean showIndexedColorPalette;

    @Option(names = {"-r", "--rgb"}, description =  "" +
            "@|fg(0;0;5) S|@" +
            "@|fg(1;0;5) h|@" +
            "@|fg(2;0;5) o|@" +
            "@|fg(3;0;5) w|@" +
            "@|fg(3;0;5)  |@" +
            "@|fg(4;0;5) 2|@" +
            "@|fg(5;0;5) 5|@" +
            "@|fg(5;4;5) 6|@" +
            "@|fg(2;5;5)  |@" +
            "@|fg(2;5;4) c|@" +
            "@|fg(2;5;2) o|@" +
            "@|fg(2;5;0) l|@" +
            "@|fg(3;4;0) o|@" +
            "@|fg(3;3;0) r|@" +
            "@|fg(3;2;0)  |@" +
            "@|fg(3;2;0) p|@" +
            "@|fg(3;1;0) a|@" +
            "@|fg(3;0;0) l|@" +
            "@|fg(4;0;0) e|@" +
            "@|fg(5;0;0) t|@" +
            "@|fg(5;0;2) t|@" +
            "@|fg(5;0;3) e|@" +
            "@|fg(5;0;4)  |@" +
            "@|fg(5;0;5) R|@" +
            "@|fg(5;2;5) G|@" +
            "@|fg(5;3;5) B|@" +
            "@|fg(5;4;5)  |@" +
            "@|fg(5;5;5) c|@" +
            "@|fg(5;5;4) o|@" +
            "@|fg(5;5;3) m|@" +
            "@|fg(5;5;2) p|@" +
            "@|fg(5;5;1) o|@" +
            "@|fg(5;5;0) n|@" +
            "@|fg(4;5;0) e|@" +
            "@|fg(3;5;0) n|@" +
            "@|fg(2;5;0) t|@" +
            "@|fg(1;5;0)  |@" +
            "@|fg(1;5;0) v|@" +
            "@|fg(0;5;0) a|@" +
            "@|fg(0;4;0) l|@" +
            "@|fg(0;3;0) u|@" +
            "@|fg(0;2;0) e|@" +
            "@|fg(0;1;0) s|@" +
            "")
    private boolean showRgbColorPalette;

    @Option(names = {"-t", "--tests"}, description = "Runs all tests in this class")
    private boolean runTests;

    public void run() {
        if (!runTests &&
                !showSimpleExample &&
                !showAnsiInDescription &&
                !showIndexedColorPalette &&
                !showRgbColorPalette &&
                !showUsageForMainCommand &&
                !showUsageForSubcommandGitCommit &&
                !showUsageForSubcommandGitStatus) {
            CommandLine.usage(this, System.err);
            return;
        }
        if (runTests)                        { testParseSubCommands(); System.out.println("Ran tests OK.");}
        if (showSimpleExample)               { showSimpleExampleUsage(); }
        if (showAnsiInDescription)           { showAnsiInDescription(); }
        if (showIndexedColorPalette)         { showIndexedColorPalette(); }
        if (showRgbColorPalette)             { showRgbColorPalette(); }
        if (showUsageForMainCommand)         { testUsageMainCommand(); }
        if (showUsageForSubcommandGitStatus) { testUsageSubCommandStatus(); }
        if (showUsageForSubcommandGitCommit) { testUsageSubCommandCommit(); }
    }

    private void showSimpleExampleUsage() {
        class Example {
            @Option(names = { "-v", "--verbose" }, description = "Be verbose.")
            private boolean verbose = false;

            @Option(names = { "-h", "--help" }, help = true,
                    description = "Displays this help message and quits.")
            private boolean helpRequested = false;

            @Parameters(arity = "1..*", paramLabel = "FILE", description = "File(s) to process.")
            private File[] inputFiles;
        }
        CommandLine.usage(new Example(), System.out);
    }

    private void showAnsiInDescription() {
        @Command(description = "Custom @|bold,underline styles|@ and @|fg(red) colors|@.")
        class AnsiDescription { }
        CommandLine.usage(new AnsiDescription(), System.out);
    }

    private void showIndexedColorPalette() {
        int[] foregroundBackground = {38, 48};
        for (int fbg : foregroundBackground) {
            for (int r = 0; r < 2; r++) {
                for (int g = 0; g < 6; g++) {
                    for (int b = 0; b < 6; b++) {
                        int col = 16 + 36 * (0 + 3 * r) + 6 * g + b;
                        System.out.printf("\u001B[%d;5;%dm%3d \u001B[0m", fbg, col, col);
                    }
                    for (int b = 0; b < 6; b++) {
                        int col = 16 + 36 * (1 + 3 * r) + 6 * g + b;
                        System.out.printf("\u001B[%d;5;%dm%3d \u001B[0m", fbg, col, col);
                    }
                    for (int b = 0; b < 6; b++) {
                        int col = 16 + 36 * (2 + 3 * r) + 6 * g + b;
                        System.out.printf("\u001B[%d;5;%dm%3d \u001B[0m", fbg, col, col);
                    }
                    System.out.println();
                }
                System.out.println();
            }
            int r = 6;
            for (int g = 0; g < 4; g++) {
                for (int b = 0; b < 6; b++) {
                    int col = 16 + 36 * r + 6 * g + b;
                    System.out.printf("\u001B[%d;5;%dm%3d \u001B[0m", fbg, col, col);
                }
                System.out.println();
            }
            System.out.println();
        }
    }

    private void showRgbColorPalette() {
        int[] foregroundBackground = {38, 48};
        for (int fbg : foregroundBackground) {
            for (int r = 0; r < 2; r++) {
                System.out.println("RGB RGB RGB RGB RGB RGB RGB RGB RGB RGB RGB RGB RGB RGB RGB RGB RGB RGB ");
                for (int g = 0; g < 6; g++) {
                    for (int b = 0; b < 6; b++) {
                        int col = 16 + 36 * (0 + 3 * r) + 6 * g + b;
                        System.out.printf("\u001B[%d;5;%dm%d%d%d \u001B[0m", fbg, col, (0 + 3 * r),g,b);
                    }
                    for (int b = 0; b < 6; b++) {
                        int col = 16 + 36 * (1 + 3 * r) + 6 * g + b;
                        System.out.printf("\u001B[%d;5;%dm%d%d%d \u001B[0m", fbg, col, (1 + 3 * r),g,b);
                    }
                    for (int b = 0; b < 6; b++) {
                        int col = 16 + 36 * (2 + 3 * r) + 6 * g + b;
                        System.out.printf("\u001B[%d;5;%dm%d%d%d \u001B[0m", fbg, col, (2 + 3 * r),g,b);
                    }
                    System.out.println();
                }
                System.out.println();
            }
            System.out.println("RGB RGB RGB RGB RGB RGB");
            int r = 6;
            for (int g = 0; g < 4; g++) {
                for (int b = 0; b < 6; b++) {
                    int col = 16 + 36 * r + 6 * g + b;
                    System.out.printf("\u001B[%d;5;%dm%d%d%d \u001B[0m", fbg, col, r,g,b);
                }
                System.out.println();
            }
            System.out.println();
        }
    }

    //------------------------------------------
    static
    // tag::Git[]
    // tag::Git-declaration[]
    @Command(name = "git", sortOptions = false,
            description = "Git is a fast, scalable, distributed revision control " +
                          "system with an unusually rich command set that provides both " +
                          "high-level operations and full access to internals.",
            commandListHeading = "%nCommands:%n%nThe most commonly used git commands are:%n")
    class Git { // end::Git-declaration[]
        @Option(names = {"-V", "--version"}, help = true, description = "Prints version information and exits")
        boolean isVersionRequested;

        @Option(names = {"-h", "--help"}, help = true, description = "Prints this help message and exits")
        boolean isHelpRequested;

        @Option(names = "--git-dir", description = "Set the path to the repository")
        File gitDir;
    }
    // end::Git[]

    // the "status" subcommand's has an option "mode" with a fixed number of values, modeled by this enum
    enum GitStatusMode {all, no, normal};

    static
    @Command(name = "git-status",
            header = "Show the working tree status.",
            showDefaultValues = true,
            customSynopsis = "@|bold git-status|@ [@|yellow <options>|@...] [--] [@|yellow <pathspec>|@...]",
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

        @Option(names = "--ignored", description = "Show ignored files as well") boolean showIgnored;

        @Option(names = {"-u", "--untracked"}, paramLabel = "<mode>", description = {
                "Show untracked files.",
                "The mode parameter is optional (defaults to `all`), and is used to specify the handling of untracked files.",
                "The possible options are:",
                " * @|yellow no|@ - Show no untracked files.",
                " * @|yellow normal|@ - Shows untracked files and directories.",
                " * @|yellow all|@ - Also shows individual files in untracked directories."
        })
        GitStatusMode mode = GitStatusMode.all;
    }

    static
    // tag::GitCommit[]
    // tag::GitCommit-declaration[]
    @Command(name = "git-commit",
            sortOptions = false,
            headerHeading = "@|bold,underline Usage:|@%n%n",
            synopsisHeading = "%n",
            descriptionHeading = "%n@|bold,underline Description:|@%n%n",
            parameterListHeading = "%n@|bold,underline Parameters:|@%n",
            optionListHeading = "%n@|bold,underline Options:|@%n",
            header = "Record changes to the repository.",
            description = "Stores the current contents of the index in a new commit " +
                    "along with a log message from the user describing the changes.")
    class GitCommit { // end::GitCommit-declaration[]
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
                description = " Use the given <msg> as the commit message. If multiple -m options" +
                        " are given, their values are concatenated as separate paragraphs.")
        List<String> message = new ArrayList<String>();

        @Parameters(paramLabel = "<files>", description = "the files to commit")
        List<File> files = new ArrayList<File>();
    }
    // end::GitCommit[]

    // defines some commands to show in the list (option/parameters fields omitted for this demo)
    @Command(name = "git-add", header = "Add file contents to the index.") static class GitAdd {}
    @Command(name = "git-branch", header = "List, create, or delete branches.") static class GitBranch {}
    @Command(name = "git-checkout", header = "Checkout a branch or paths to the working tree.") static class GitCheckout{}
    @Command(name = "git-clone", header = "Clone a repository into a new directory.") static class GitClone{}
    @Command(name = "git-diff", header = "Show changes between commits, commit and working tree, etc.") static class GitDiff{}
    @Command(name = "git-merge", header = "Join two or more development histories together.") static class GitMerge{}
    @Command(name = "git-push", header = "Update remote refs along with associated objects.") static class GitPush{}
    @Command(name = "git-rebase", header = "Forward-port local commits to the updated upstream head.") static class GitRebase{}
    @Command(name = "git-tag", header = "Create, list, delete or verify a tag object signed with GPG.") static class GitTag{}

    /** @see CommandLineTest#testParseSubCommands() The JUnit test implementation of this test. */
    public static void testParseSubCommands() {
        CommandLine commandLine = mainCommand();

        String[] args = { "--git-dir=/home/rpopma/picocli", "status", "-sbuno"};
        List<CommandLine> parsed = commandLine.parse(args);
        assert parsed.size() == 2 : "found 2 commands";

        assert parsed.get(0).getCommand().getClass() == Git.class;
        assert parsed.get(1).getCommand().getClass() == GitStatus.class;

        Git git = (Git) parsed.get(0).getCommand();
        assert git.gitDir.equals(new File("/home/rpopma/picocli"));

        GitStatus status = (GitStatus) parsed.get(1).getCommand();
        assert  status.shortFormat : "status -s";
        assert  status.branchInfo  : "status -b";
        assert !status.showIgnored : "status --showIgnored not specified";
        assert  status.mode == GitStatusMode.no : "status -u=no";
    }

    static CommandLine mainCommand() {
        CommandLine commandLine = new CommandLine(new Git());
        commandLine.addSubcommand("status", new GitStatus());
        commandLine.addSubcommand("commit", new GitCommit());
        commandLine.addSubcommand("add", new GitAdd());
        commandLine.addSubcommand("branch", new GitBranch());
        commandLine.addSubcommand("checkout", new GitCheckout());
        commandLine.addSubcommand("clone", new GitClone());
        commandLine.addSubcommand("diff", new GitDiff());
        commandLine.addSubcommand("merge", new GitMerge());
        commandLine.addSubcommand("push", new GitPush());
        commandLine.addSubcommand("rebase", new GitRebase());
        commandLine.addSubcommand("tag", new GitTag());
        return commandLine;
    }

    public void testUsageMainCommand()  {
        CommandLine commandLine = mainCommand();
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            commandLine.usage(new PrintStream(baos, true, "UTF8"));
            String result = baos.toString("UTF8");
            System.out.println(result);
            assert String.format(EXPECTED_USAGE_MAIN).equals(result);
        } catch (UnsupportedEncodingException ex) {
            throw new InternalError(ex.toString());
        }
    }
    static final String EXPECTED_USAGE_MAIN = "Usage: git [-hV] [--git-dir=<gitDir>]%n" +
            "Git is a fast, scalable, distributed revision control system with an unusually%n" +
            "rich command set that provides both high-level operations and full access to%n" +
            "internals.%n" +
            "  -V, --version               Prints version information and exits%n" +
            "  -h, --help                  Prints this help message and exits%n" +
            "      --git-dir=<gitDir>      Set the path to the repository%n" +
            "%n" +
            "Commands:%n" +
            "%n" +
            "The most commonly used git commands are:%n" +
            "  status    Show the working tree status.%n" +
            "  commit    Record changes to the repository.%n" +
            "  add       Add file contents to the index.%n" +
            "  branch    List, create, or delete branches.%n" +
            "  checkout  Checkout a branch or paths to the working tree.%n" +
            "  clone     Clone a repository into a new directory.%n" +
            "  diff      Show changes between commits, commit and working tree, etc.%n" +
            "  merge     Join two or more development histories together.%n" +
            "  push      Update remote refs along with associated objects.%n" +
            "  rebase    Forward-port local commits to the updated upstream head.%n" +
            "  tag       Create, list, delete or verify a tag object signed with GPG.%n";

    static final String EXPECTED_USAGE_MAIN_ANSI = "Usage: @|bold git|@ [@|yellow -hV|@] [@|yellow --git-dir|@=@|italic <gitDir>|@]%n" +
            "Git is a fast, scalable, distributed revision control system with an unusually%n" +
            "rich command set that provides both high-level operations and full access to%n" +
            "internals.%n" +
            "  @|yellow -V|@, @|yellow --version|@               Prints version information and exits%n" +
            "  @|yellow -h|@, @|yellow --help|@                  Prints this help message and exits%n" +
            "      @|yellow --git-dir|@=@|italic <gitDir>|@      Set the path to the repository%n" +
            "%n" +
            "Commands:%n" +
            "%n" +
            "The most commonly used git commands are:%n" +
            "  @|bold status|@    Show the working tree status.%n" +
            "  @|bold commit|@    Record changes to the repository.%n" +
            "  @|bold add|@       Add file contents to the index.%n" +
            "  @|bold branch|@    List, create, or delete branches.%n" +
            "  @|bold checkout|@  Checkout a branch or paths to the working tree.%n" +
            "  @|bold clone|@     Clone a repository into a new directory.%n" +
            "  @|bold diff|@      Show changes between commits, commit and working tree, etc.%n" +
            "  @|bold merge|@     Join two or more development histories together.%n" +
            "  @|bold push|@      Update remote refs along with associated objects.%n" +
            "  @|bold rebase|@    Forward-port local commits to the updated upstream head.%n" +
            "  @|bold tag|@       Create, list, delete or verify a tag object signed with GPG.%n";

    public void testUsageSubCommandStatus() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            CommandLine.usage(new GitStatus(), new PrintStream(baos, true, "UTF8"));
            String result = baos.toString("UTF8");
            System.out.println(result);
            assert String.format(EXPECTED_USAGE_GITSTATUS).equals(result);
        } catch (UnsupportedEncodingException ex) {
            throw new InternalError(ex.toString());
        }
    }
    static final String EXPECTED_USAGE_GITSTATUS = "Show the working tree status.%n" +
            "Usage: git-status [<options>...] [--] [<pathspec>...]%n" +
            "Displays paths that have differences between the index file and the current%n" +
            "HEAD commit, paths that have differences between the working tree and the index%n" +
            "file, and paths in the working tree that are not tracked by Git (and are not%n" +
            "ignored by gitignore(5)). The first are what you would commit by running git%n" +
            "commit; the second and third are what you could commit by running git add%n" +
            "before running git commit.%n" +
            "      --ignored               Show ignored files as well%n" +
            "  -b, --branch                Show the branch and tracking info even in%n" +
            "                                short-format%n" +
            "  -s, --short                 Give the output in the short-format%n" +
            "  -u, --untracked=<mode>      Show untracked files.%n" +
            "                              The mode parameter is optional (defaults to%n" +
            "                                `all`), and is used to specify the handling of%n" +
            "                                untracked files.%n" +
            "                              The possible options are:%n" +
            "                               * no - Show no untracked files.%n" +
            "                               * normal - Shows untracked files and directories.%n" +
            "                               * all - Also shows individual files in untracked%n" +
            "                                directories.%n" +
            "                                Default: all%n";

    static final String EXPECTED_USAGE_GITSTATUS_ANSI = "Show the working tree status.%n" +
            "Usage: @|bold git-status|@ [@|yellow <options>|@...] [--] [@|yellow <pathspec>|@...]%n" +
            "Displays paths that have differences between the index file and the current%n" +
            "HEAD commit, paths that have differences between the working tree and the index%n" +
            "file, and paths in the working tree that are not tracked by Git (and are not%n" +
            "ignored by gitignore(5)). The first are what you would commit by running git%n" +
            "commit; the second and third are what you could commit by running git add%n" +
            "before running git commit.%n" +
            "      @|yellow --ignored|@               Show ignored files as well%n" +
            "  @|yellow -b|@, @|yellow --branch|@                Show the branch and tracking info even in%n" +
            "                                short-format%n" +
            "  @|yellow -s|@, @|yellow --short|@                 Give the output in the short-format%n" +
            "  @|yellow -u|@, @|yellow --untracked|@=@|italic <mode>|@      Show untracked files.%n" +
            "                              The mode parameter is optional (defaults to%n" +
            "                                `all`), and is used to specify the handling of%n" +
            "                                untracked files.%n" +
            "                              The possible options are:%n" +
            "                               * @|yellow no|@ - Show no untracked files.%n" +
            "                               * @|yellow normal|@ - Shows untracked files and directories.%n" +
            "                               * @|yellow all|@ - Also shows individual files in untracked%n" +
            "                                directories.%n" +
            "                                Default: all%n";

    public void testUsageSubCommandCommit() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            CommandLine.usage(new GitCommit(), new PrintStream(baos, true, "UTF8"));
            String result = baos.toString("UTF8");
            System.out.println(result);
            assert String.format(EXPECTED_USAGE_GITCOMMIT).equals(result);
        } catch (UnsupportedEncodingException ex) {
            throw new InternalError(ex.toString());
        }
    }
    static final String EXPECTED_USAGE_GITCOMMIT = "Usage:%n" +
            "%n" +
            "Record changes to the repository.%n" +
            "%n" +
            "git-commit [-ap] [--fixup=<commit>] [--squash=<commit>] [-c=<commit>]%n" +
            "           [-C=<commit>] [-F=<file>] [-m[=<msg>...]] [<files>...]%n" +
            "%n" +
            "Description:%n" +
            "%n" +
            "Stores the current contents of the index in a new commit along with a log%n" +
            "message from the user describing the changes.%n" +
            "%n" +
            "Parameters:%n" +
            "      <files>                 the files to commit%n" +
            "%n" +
            "Options:%n" +
            "  -a, --all                   Tell the command to automatically stage files%n" +
            "                                that have been modified and deleted, but new%n" +
            "                                files you have not told Git about are not%n" +
            "                                affected.%n" +
            "  -p, --patch                 Use the interactive patch selection interface to%n" +
            "                                chose which changes to commit%n" +
            "  -C, --reuse-message=<commit>%n" +
            "                              Take an existing commit object, and reuse the log%n" +
            "                                message and the authorship information%n" +
            "                                (including the timestamp) when creating the%n" +
            "                                commit.%n" +
            "  -c, --reedit-message=<commit>%n" +
            "                              Like -C, but with -c the editor is invoked, so%n" +
            "                                that the user canfurther edit the commit%n" +
            "                                message.%n" +
            "      --fixup=<commit>        Construct a commit message for use with rebase%n" +
            "                                --autosquash.%n" +
            "      --squash=<commit>       Construct a commit message for use with rebase%n" +
            "                                --autosquash. The commitmessage subject line is%n" +
            "                                taken from the specified commit with a prefix%n" +
            "                                of \"squash! \". Can be used with additional%n" +
            "                                commit message options (-m/-c/-C/-F).%n" +
            "  -F, --file=<file>           Take the commit message from the given file. Use%n" +
            "                                - to read the message from the standard input.%n" +
            "  -m, --message[=<msg>...]     Use the given <msg> as the commit message. If%n" +
            "                                multiple -m options are given, their values are%n" +
            "                                concatenated as separate paragraphs.%n";

    static final String EXPECTED_USAGE_GITCOMMIT_ANSI = "@|bold,underline Usage:|@%n" +
            "%n" +
            "Record changes to the repository.%n" +
            "%n" +
            "@|bold git-commit|@ [@|yellow -ap|@] [@|yellow --fixup|@=@|italic <commit>|@] [@|yellow --squash|@=@|italic <commit>|@] [@|yellow -c|@=@|italic <commit>|@]%n" +
            "           [@|yellow -C|@=@|italic <commit>|@] [@|yellow -F|@=@|italic <file>|@] [@|yellow -m|@[=@|italic <msg>|@...]] [@|yellow <files>|@...]%n" +
            "%n" +
            "@|bold,underline Description:|@%n" +
            "%n" +
            "Stores the current contents of the index in a new commit along with a log%n" +
            "message from the user describing the changes.%n" +
            "%n" +
            "@|bold,underline Parameters:|@%n" +
            "      @|yellow <files>|@                 the files to commit%n" +
            "%n" +
            "@|bold,underline Options:|@%n" +
            "  @|yellow -a|@, @|yellow --all|@                   Tell the command to automatically stage files%n" +
            "                                that have been modified and deleted, but new%n" +
            "                                files you have not told Git about are not%n" +
            "                                affected.%n" +
            "  @|yellow -p|@, @|yellow --patch|@                 Use the interactive patch selection interface to%n" +
            "                                chose which changes to commit%n" +
            "  @|yellow -C|@, @|yellow --reuse-message|@=@|italic <commit|@@|italic >|@%n" +
            "                              Take an existing commit object, and reuse the log%n" +
            "                                message and the authorship information%n" +
            "                                (including the timestamp) when creating the%n" +
            "                                commit.%n" +
            "  @|yellow -c|@, @|yellow --reedit-message|@=@|italic <commi|@@|italic t>|@%n" +
            "                              Like -C, but with -c the editor is invoked, so%n" +
            "                                that the user canfurther edit the commit%n" +
            "                                message.%n" +
            "      @|yellow --fixup|@=@|italic <commit>|@        Construct a commit message for use with rebase%n" +
            "                                --autosquash.%n" +
            "      @|yellow --squash|@=@|italic <commit>|@       Construct a commit message for use with rebase%n" +
            "                                --autosquash. The commitmessage subject line is%n" +
            "                                taken from the specified commit with a prefix%n" +
            "                                of \"squash! \". Can be used with additional%n" +
            "                                commit message options (-m/-c/-C/-F).%n" +
            "  @|yellow -F|@, @|yellow --file|@=@|italic <file>|@           Take the commit message from the given file. Use%n" +
            "                                - to read the message from the standard input.%n" +
            "  @|yellow -m|@, @|yellow --message|@[=@|italic <msg>|@...]     Use the given <msg> as the commit message. If%n" +
            "                                multiple -m options are given, their values are%n" +
            "                                concatenated as separate paragraphs.%n";
}
