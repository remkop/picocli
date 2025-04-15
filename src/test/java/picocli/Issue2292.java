package picocli;

import org.junit.Test;

public class Issue2292 {
    static class ResponsePathMixin {

        @CommandLine.ArgGroup(exclusive = true)
        PropertiesSelection selection;

        static class PropertiesSelection {
            @CommandLine.Option(
                names = {"--all-columns", "-A"},
                description = "all columns option",
                negatable = true)
            boolean printAllColumns;

            @CommandLine.Option(
                names = {"--columns", "-c"},
                description = "list of columns option",
                split = ",")
            String[] columnsList;
        }
    }

    @Test
    public void testOptionSpecHashCodeBug() {
        Runnable runnable = new Runnable() {
                public void run() {
                    System.out.println("Hello World!");
                }
            };
        CommandLine.Model.CommandSpec commandSpec =
            CommandLine.Model.CommandSpec.wrapWithoutInspection(runnable);
        commandSpec.addMixin(
            "cols", CommandLine.Model.CommandSpec.forAnnotatedObject(new ResponsePathMixin()));
        CommandLine commandLine = new CommandLine(commandSpec);
        commandLine.execute();
    }
}
