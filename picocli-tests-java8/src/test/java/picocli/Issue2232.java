package picocli;

import org.junit.Test;
import picocli.CommandLine;
import picocli.CommandLine.*;

import java.io.File;
import java.util.Optional;

import static org.junit.Assert.*;

public class Issue2232 {
    static class Tar {
        @Option(names = { "-f", "--file" }, paramLabel = "ARCHIVE", description = "the archive file")
        Optional<File> archive;

        public Tar() {
            archive = Optional.of(new File("helloworld"));
        }
    }

    @Test
    public void testDefault() {
        Tar tar = new Tar();
        System.out.println(tar.archive);
        assertEquals(Optional.of(new File("helloworld")), tar.archive);
        new CommandLine(tar).parseArgs();
        assertEquals(Optional.of(new File("helloworld")), tar.archive);
    }
}
