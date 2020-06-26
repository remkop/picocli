package picocli;

import org.junit.Ignore;
import org.junit.Test;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import static org.junit.Assert.*;

public class ArgGroupHelpRegressionTest {

    @Command(name = "ami", description = "ami description", customSynopsis = "ami [OPTIONS]")
    static class Issue1119 {
        @ArgGroup(exclusive = true, heading = "", order = 9)
        ProjectOrTreeOptions projectOrTreeOptions = new ProjectOrTreeOptions();

        @ArgGroup(validate = false, heading = "General Options:%n", order = 30)
        GeneralOptions generalOptions = new GeneralOptions();
    }


    static class ProjectOrTreeOptions {
        @ArgGroup(exclusive = false, multiplicity = "0..1",
                heading = "CProject Options:%n", order = 10)
        CProjectOptions cProjectOptions = new CProjectOptions();

        @ArgGroup(exclusive = false, multiplicity = "0..1",
                heading = "CTree Options:%n", order = 20)
        CTreeOptions cTreeOptions = new CTreeOptions();
    }

    static class CProjectOptions {
        @Option(names = {"-p", "--cproject"}, paramLabel = "DIR",
                description = "The CProject (directory) to process. The cProject name is the basename of the file."
        )
        protected String cProjectDirectory = null;

        protected static class TreeOptions {
            @Option(names = {"-r", "--includetree"}, paramLabel = "DIR", order = 12,
                    arity = "1..*",
                    description = "Include only the specified CTrees."
            )
            protected String[] includeTrees;

            @Option(names = {"-R", "--excludetree"}, paramLabel = "DIR", order = 13,
                    arity = "1..*",
                    description = "Exclude the specified CTrees."
            )
            protected String[] excludeTrees;
        }

        @ArgGroup(exclusive = true, multiplicity = "0..1", order = 11, heading = "")
        TreeOptions treeOptions = new TreeOptions();
    }

    static class CTreeOptions {
        @Option(names = {"-t", "--ctree"}, paramLabel = "DIR",
                description = "The CTree (directory) to process. The cTree name is the basename of the file."
        )
        protected String cTreeDirectory = null;

        protected static class BaseOptions {

            @Option(names = {"-b", "--includebase"}, paramLabel = "PATH", order = 22,
                    arity = "1..*",
                    description = "Include child files of cTree (only works with --ctree)."
            )
            protected String[] includeBase;

            @Option(names = {"-B", "--excludebase"}, paramLabel = "PATH",
                    order = 23,
                    arity = "1..*",
                    description = "Exclude child files of cTree (only works with --ctree)."
            )
            protected String[] excludeBase;
        }

        @ArgGroup(exclusive = true, multiplicity = "0..1", heading = "", order = 21)
        BaseOptions baseOptions = new BaseOptions();
    }

    static class GeneralOptions {
        @Option(names = {"-i", "--input"}, paramLabel = "FILE",
                description = "Input filename (no defaults)"
        )
        protected String input = null;

        @Option(names = {"-n", "--inputname"}, paramLabel = "PATH",
                description = "User's basename for input files (e.g. foo/bar/<basename>.png) or directories."
        )
        protected String inputBasename;
    }

    @Ignore("needs fix for https://github.com/remkop/picocli/issues/1119")
    @Test // https://github.com/remkop/picocli/issues/1119
    public void testRegression988() {
        String expected = String.format("" +
                "Usage: ami [OPTIONS]%n" +
                "ami description%n" +
                "CProject Options:%n" +
                "  -p, --cproject=DIR         The CProject (directory) to process. The cProject%n" +
                "                               name is the basename of the file.%n" +
                "  -r, --includetree=DIR...   Include only the specified CTrees.%n" +
                "  -R, --excludetree=DIR...   Exclude the specified CTrees.%n" +
                "CTree Options:%n" +
                "  -b, --includebase=PATH...  Include child files of cTree (only works with%n" +
                "                               --ctree).%n" +
                "  -B, --excludebase=PATH...  Exclude child files of cTree (only works with%n" +
                "                               --ctree).%n" +
                "  -t, --ctree=DIR            The CTree (directory) to process. The cTree name%n" +
                "                               is the basename of the file.%n" +
                "General Options:%n" +
                "  -i, --input=FILE           Input filename (no defaults)%n" +
                "  -n, --inputname=PATH       User's basename for input files (e.g.%n" +
                "                               foo/bar/<basename>.png) or directories.%n");
        assertEquals(expected, new CommandLine(new Issue1119()).getUsageMessage());
    }
}
