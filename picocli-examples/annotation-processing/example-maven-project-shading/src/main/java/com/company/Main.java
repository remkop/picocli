package com.company;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

import java.util.UUID;
import java.util.concurrent.Callable;

@Command(name = "top", mixinStandardHelpOptions = true,
        version = {"test 1.0", "picocli " + CommandLine.VERSION},
        resourceBundle = "mybundle5")
public class Main implements Callable<Integer> {

    @Option(names = "-x") int x;
    @Option(names = "-y") int y;
    @Option(names = "-z") int z;

    @Spec
    CommandSpec spec;

    public static void main(String[] args) {
        System.exit(new CommandLine(new Main()).execute(args));
    }

    @Override
    public Integer call() throws Exception {
        System.out.println("Hi!");
        System.out.println("x = " + x);

        System.out.println("from bundle: " + spec.usageMessage().messages().getString("a", "not found"));

        System.out.println(UUID.randomUUID());
        return 0;
    }
}
