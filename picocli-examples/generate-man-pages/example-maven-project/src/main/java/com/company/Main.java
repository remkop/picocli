package com.company;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;

@Command(name = "top", mixinStandardHelpOptions = true,
        version = {"test 1.0", "picocli " + CommandLine.VERSION})
public class Main implements Callable<Integer> {

    @Option(names = "-x") int x;
    @Option(names = "-y") int y;
    @Option(names = "-z") int z;

    public static void main(String[] args) {
        System.exit(new CommandLine(new Main()).execute(args));
    }

    @Override
    public Integer call() throws Exception {
        System.out.println("Hi!");
        return 0;
    }
}
