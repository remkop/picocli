package picocli.examples.model;

import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.OptionSpec;
import picocli.CommandLine.ParseResult;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ParseResultDemo {
    public static void main(String[] args) {
        CommandSpec spec = CommandSpec.create();
        spec.addOption(OptionSpec.builder("-V", "--verbose").build());
        spec.addOption(OptionSpec.builder("-f", "--file")
                .paramLabel("FILES")
                .type(List.class)
                .auxiliaryTypes(File.class) // so, this option is of type List<File>
                .description("The files to process").build());
        spec.addOption(OptionSpec.builder("-n", "--num")
                .paramLabel("COUNT")
                .type(int[].class)
                .splitRegex(",")
                .description("Comma-separated list of integers").build());
        CommandLine commandLine = new CommandLine(spec);

        args = new String[]{ "--verbose", "-f", "file1", "--file=file2", "-n1,2,3" };
        ParseResult pr = commandLine.parseArgs(args);

        // Querying for options
        List<String> originalArgs = pr.originalArgs(); // lists all command line args
        assert Arrays.asList(args).equals(originalArgs);

        assert pr.hasMatchedOption("--verbose"); // as specified on command line
        assert pr.hasMatchedOption("-V");        // other aliases work also
        assert pr.hasMatchedOption('V');         // single-character alias works too
        assert pr.hasMatchedOption("verbose");   // and, command name without hyphens

        // Matched Option Values
        List<File> defaultValue = Collections.emptyList();
        List<File> expected     = Arrays.asList(new File("file1"), new File("file2"));

        assert expected.equals(pr.matchedOptionValue('f', defaultValue));
        assert expected.equals(pr.matchedOptionValue("--file", defaultValue));
        assert Arrays.equals(new int[]{1,2,3}, pr.matchedOptionValue('n', new int[0]));

        // Command line arguments after splitting but before type conversion
        assert "1".equals(pr.matchedOption('n').stringValues().get(0));
        assert "2".equals(pr.matchedOption('n').stringValues().get(1));
        assert "3".equals(pr.matchedOption('n').stringValues().get(2));

        // Command line arguments as found on the command line
        assert "1,2,3".equals(pr.matchedOption("--num").originalStringValues().get(0));
    }
}
