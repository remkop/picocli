package picocli.examples.passthrough;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Unmatched;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "find")
public class Find implements Callable<Integer> {
    enum FileType {
        b, c, d, l, p, f, s, D,
    }

    @Option(names = "-type", split = ",",
            description = {"The file type to search for. Valid values are: ${COMPLETION-CANDIDATES}, where:",
                        "b      block (buffered) special",
                        "c      character (unbuffered) special",
                        "d      directory",
                        "p      named pipe (FIFO)",
                        "f      regular file",
                        "l      symbolic  link;  this  is never true if the -L option or" +
                                "the -follow option is in effect, unless the symbolic link is broken.  " +
                                "If you want to search for symbolic links when -L is in effect, use -xtype.",
                        "s      socket",
                        "D      door (Solaris)",
                        "To search for more than one type at once, you can supply the combined list " +
                                "of type letters separated by a comma `,' (GNU extension)."})
    List<FileType> types = new ArrayList<FileType>(Arrays.asList(FileType.c));

    @Option(names = "-exec", arity = "*",
            description = "Execute  command;  true if 0 status is returned.  All following arguments " +
                    "to `find` are taken to be arguments to the command until an argument " +
                    "consisting of `;' is encountered.  The string `{}'  is  replaced by the " +
                    "current file name being processed everywhere it occurs in the arguments " +
                    "to the command, not just in arguments where it is alone, as in some " +
                    "versions of find.  Both of these constructions might need to be  escaped " +
                    "(with a `\\') or quoted to protect them from expansion by the shell.  " +
                    "See the EXAMPLES section for examples of the use of the -exec option.  " +
                    "The specified command is run once for  each  matched file.  " +
                    "The command is executed in the starting directory.  " +
                    "There are unavoidable security problems surrounding use of the -exec action; " +
                    "you should use the -execdir option instead.")
    List<String> execCommandAndArgs;

    @Parameters(index = "0")
    File startingPoint;

    @Unmatched
    List<String> unmatched;

    @Override
    public Integer call() throws Exception {
        System.out.printf("Finding files of type %s, startingPoint: %s, invoking command '%s' on each...%n", types, startingPoint, execCommandAndArgs);
        System.out.printf("Unmatched positional arguments following ';' were: %s%n", unmatched);
        return 0;
    }

    public static void main(String[] args) {

        //args = ". -type d -exec ls -la {} ;".split(" ");
        //args = ". -type d -exec ls -la {} +".split(" ");
//        args = ". -type d -exec ls -la {} -- this is unmatched".split(" ");
        args = ". -type d -exec ls -la {} ; this is unmatched".split(" "); // exposes the bug

        int exitCode = new CommandLine(new Find())
                .setEndOfOptionsDelimiter(";")
                .execute(args);
    }
}
