package picocli.ls;

import picocli.AutoComplete;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

@Command(name = "filelist", mixinStandardHelpOptions = true,
        description = {
                "List information about the <file> (the current directory by default).",
                "Sort entries alphabetically if no other sort order is specified." },
        version = {
                "filelist 2.0",
                "Picocli " + picocli.CommandLine.VERSION,
                "JVM: ${java.version} (${java.vendor} ${java.vm.name} ${java.vm.version})",
                "OS: ${os.name} ${os.version} ${os.arch}"},
        subcommands = AutoComplete.GenerateCompletion.class)
public class FileList implements Callable<Integer> {

    @Parameters(index = "0", defaultValue = ".", description = "The file to list.")
    private File file;

    @Option(names = {"-S", "--sort"}, description = "Sort order. One of: ${COMPLETION-CANDIDATES}. Default value: ${DEFAULT-VALUE}.")
    private Sort sort = Sort.NONE;

    @Option(names = {"-r", "--reverse"}, description = "Reverse order while sorting.")
    private boolean reverse;

    @Option(names = {"-F", "--format"}, description = "Output format. One of: ${COMPLETION-CANDIDATES}. Default value: ${DEFAULT-VALUE}.")
    private Format format = Format.LONG;

    public static void main(String... args) {
        System.exit(new CommandLine(new FileList()).execute(args));
    }

    @Override
    public Integer call() throws Exception {
        Comparator<File> order = reverse ? sort.order.reversed() : sort.order;
        Arrays.stream(file.listFiles()).sorted(order).forEach(format.print);
        return 0;
    }

    enum Sort {
        NONE((f1, f2) -> 0),
        SIZE((f1, f2) -> Long.compare(f1.length(), f2.length())),
        NAME((f1, f2) -> f1.getName().compareTo(f2.getName())),
        TIME((f1, f2) -> Long.compare(f1.lastModified(), f2.lastModified())),
        ;
        public final Comparator<File> order;
        Sort(Comparator<File> c) { order = c; }
    }

    enum Format {
        HORIZONTAL(f -> System.out.printf("%1$-20s", f.getName())),
        VERTICAL  (f -> System.out.printf("%s%n", f.getName())),
        LONG      (f -> System.out.printf("%2$10d %3$tb %3$2te %3$tY %3$tH:%3$tM %1$-20s%n",
                                    f.getName(), f.length(), new Date(f.lastModified()))),
        ;
        public final Consumer<File> print;
        Format(Consumer<File> c) { print = c; }
    }
}
