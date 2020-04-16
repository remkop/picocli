package picocli.examples.completioncandidates;

import picocli.CommandLine;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

public class FileCompleterDemo implements Runnable {

    @Parameters(arity = "1", description = "A file. Valid values in this directory: ${COMPLETION-CANDIDATES}",
            completionCandidates = FileCompleter.class)
    File file;

    public void run() {
        System.out.printf("You selected %s%n", file);
    }

    public static void main(String[] args) {
        new CommandLine(new FileCompleterDemo()).execute(args);
    }

    static class FileCompleter implements Iterable<String> {
        @Override
        public Iterator<String> iterator() {
            String[] files = new File(".").list();
            return files == null ? Collections.emptyIterator() : Arrays.asList(files).iterator();
        }
    }
}
