package picocli.issue777;

import picocli.CommandLine.*;
import java.util.Collection;
import java.util.List;

@Command(name = "repeating-composite-demo")
public class CompositeGroupDemo {

    @ArgGroup(exclusive = true, multiplicity = "1")
    Collection<All> allCollection;

    static class Composite {
        @ArgGroup(exclusive = false, multiplicity = "0..1")
        Dependent dependent;

        @ArgGroup(exclusive = true, multiplicity = "1")
        Exclusive exclusive;
    }

    static class Dependent {
        @Option(names = "-a", required = true) int a;
        @Option(names = "-b", required = true) int b;
        @Option(names = "-c", required = true) int c;
    }

    static class Exclusive {
        @Option(names = "-x", required = true) boolean x;
        @Option(names = "-y", required = true) boolean y;
        @Option(names = "-z", required = true) boolean z;
    }

    static class Exclusive2 {
        @Option(names = "-t", required = true) boolean t;
        @Option(names = "-v", required = true) boolean v;
        @Option(names = "-w", required = true) boolean w;
    }

    static class Composite2 {
        @ArgGroup(exclusive = true, multiplicity = "1")
        Exclusive2 exclusive2;

        @Option(names = "-f") boolean f;
    }

    static class All {
        @ArgGroup(exclusive = false, multiplicity = "1..*")
        List<Composite> composites;

        @ArgGroup(exclusive = false, multiplicity = "1..*")
        Composite2[] composite2array;
    }
}
