package picocli.examples.git;

import picocli.CommandLine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "commit",
        sortOptions = false,
        headerHeading = "@|bold,underline Usage:|@%n%n",
        synopsisHeading = "%n",
        descriptionHeading = "%n@|bold,underline Description:|@%n%n",
        parameterListHeading = "%n@|bold,underline Parameters:|@%n",
        optionListHeading = "%n@|bold,underline Options:|@%n",
        header = "Record changes to the repository.",
        description = "Stores the current contents of the index in a new commit " +
                "along with a log message from the user describing the changes.")
class GitCommit implements Callable<Integer> {
    @CommandLine.Option(names = {"-a", "--all"},
            description = "Tell the command to automatically stage files that have been modified " +
                    "and deleted, but new files you have not told Git about are not affected.")
    boolean all;

    @CommandLine.Option(names = {"-p", "--patch"}, description = "Use the interactive patch selection interface to chose which changes to commit")
    boolean patch;

    @CommandLine.Option(names = {"-C", "--reuse-message"}, paramLabel = "<commit>",
            description = "Take an existing commit object, and reuse the log message and the " +
                    "authorship information (including the timestamp) when creating the commit.")
    String reuseMessageCommit;

    @CommandLine.Option(names = {"-c", "--reedit-message"}, paramLabel = "<commit>",
            description = "Like -C, but with -c the editor is invoked, so that the user can" +
                    "further edit the commit message.")
    String reEditMessageCommit;

    @CommandLine.Option(names = "--fixup", paramLabel = "<commit>",
            description = "Construct a commit message for use with rebase --autosquash.")
    String fixupCommit;

    @CommandLine.Option(names = "--squash", paramLabel = "<commit>",
            description = "Construct a commit message for use with rebase --autosquash. The commit" +
                    "message subject line is taken from the specified commit with a prefix of " +
                    "\"squash! \". Can be used with additional commit message options (-m/-c/-C/-F).")
    String squashCommit;

    @CommandLine.Option(names = {"-F", "--file"}, paramLabel = "<file>",
            description = "Take the commit message from the given file. Use - to read the message from the standard input.")
    File file;

    @CommandLine.Option(names = {"-m", "--message"}, paramLabel = "<msg>",
            description = "Use the given <msg> as the commit message. If multiple -m options" +
                    " are given, their values are concatenated as separate paragraphs.")
    List<String> message = new ArrayList<String>();

    @CommandLine.Parameters(paramLabel = "<files>", description = "the files to commit")
    List<File> files = new ArrayList<File>();

    @Override
    public Integer call() throws Exception {
        System.out.println("Your files have been committed.");
        boolean ok = true;
        return ok ? 0 : 1;
    }
}
