package picocli.examples.arggroup;

import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.List;

@Command(name = "repeating-composite-demo")
public class CompositeGroupDemo {

    @ArgGroup(exclusive = false, multiplicity = "1..*")
    List<Composite> composites;

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

    public static void main(String[] args) {
        CompositeGroupDemo example = new CompositeGroupDemo();
        CommandLine cmd = new CommandLine(example);

        cmd.parseArgs("-x", "-a=1", "-b=1", "-c=1", "-a=2", "-b=2", "-c=2", "-y");
        assert example.composites.size() == 2;

        Composite c1 = example.composites.get(0);
        assert c1.exclusive.x;
        assert c1.dependent.a == 1;
        assert c1.dependent.b == 1;
        assert c1.dependent.c == 1;

        Composite c2 = example.composites.get(1);
        assert c2.exclusive.y;
        assert c2.dependent.a == 2;
        assert c2.dependent.b == 2;
        assert c2.dependent.c == 2;
    }
}

