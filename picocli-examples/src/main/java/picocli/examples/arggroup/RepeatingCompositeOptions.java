package picocli.examples.arggroup;

import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.util.Objects;

public class RepeatingCompositeOptions {

    @Command(name = "print")
    static class PrintCommand {
        enum PaperFormat { A1, A2, A3, A4 }

        @Option(names = "--paper")
        PaperFormat format;

        @ArgGroup(exclusive = false, multiplicity = "0..*")
        Document[] documents;
    }

    static class Document {
        @Parameters(index = "0", arity = "1")
        File file;

        @Option(names = {"-c", "--count"}, required = false)
        int count = 1; // the default

        enum Rotate { left, right, straight }

        @Option(names = "--rotate", required = false)
        Rotate rotate = Rotate.straight; // the default

        public Document() {}

        public Document(File f, int count, Rotate rotate) {
            this.file = f;
            this.count = count;
            this.rotate = rotate;
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof Document)) return false;
            Document other = (Document) obj;
            return other.count == this.count
                    && Objects.equals(other.file, this.file)
                    && Objects.equals(other.rotate, this.rotate);
        }

        public int hashCode() {
            return Objects.hash(count, file, rotate);
        }
    }

    public static void main(String[] args) {
        System.setProperty("picocli.trace", "DEBUG");
        PrintCommand printCommand = new PrintCommand();
        CommandLine cmd = new CommandLine(printCommand);

        args = "--paper A4 A.pdf --count 3 B.pdf --rotate left C.pdf ** D.pdf --rotate right E.pdf".split(" ");
        cmd.parseArgs(args);

        assert printCommand.format == PrintCommand.PaperFormat.A4;

        Document[] expected = new Document[] {
                new Document(new File("A.pdf"), 3, Document.Rotate.straight),
                new Document(new File("B.pdf"), 1, Document.Rotate.left),
                new Document(new File("C.pdf"), 1, Document.Rotate.straight),
                new Document(new File("**"), 1, Document.Rotate.straight),
                new Document(new File("D.pdf"), 1, Document.Rotate.right),
                new Document(new File("E.pdf"), 1, Document.Rotate.straight),
        };
        assert printCommand.documents.length == expected.length;
        for (int i = 0; i < expected.length; i++) {
            assert printCommand.documents[i].equals(expected[i]);
        }
    }
}
