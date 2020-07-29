package picocli.issue1137;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.Callable;

@Command(name = "top",
        version = {"test 1.0"},
        resourceBundle = "mybundle5")
public class Issue1137 implements Callable<Integer> {

    @CommandLine.Option(names = "--level",
            description = "Level.",
            defaultValue = "OFF",
            showDefaultValue = CommandLine.Help.Visibility.ALWAYS,
            completionCandidates = LevelCompletion.class)
    private String level;

    @Spec
    CommandSpec spec;

    @Override
    public Integer call() throws Exception {
        return 0;
    }

    public static class LevelCompletion extends ArrayList<String> {
        public LevelCompletion() {
            super(Arrays.asList("OFF", "INFO", "WARN"));
        }
    }
}