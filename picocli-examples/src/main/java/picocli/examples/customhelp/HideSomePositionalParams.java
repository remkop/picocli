package picocli.examples.customhelp;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.PositionalParamSpec;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * https://github.com/remkop/picocli/issues/1348
 */
@Command(name = "rename")
public class HideSomePositionalParams {

    static class MyHelpFactory implements CommandLine.IHelpFactory {
        @Override
        public CommandLine.Help create(CommandSpec commandSpec, CommandLine.Help.ColorScheme colorScheme) {
            return new CommandLine.Help(commandSpec, colorScheme) {
                @Override
                public String parameterList(List<PositionalParamSpec> positionalParams) {
                    return super.parameterList(filter(positionalParams));
                }

                private List<PositionalParamSpec> filter(List<PositionalParamSpec> optionList) {
                    List<PositionalParamSpec> shown = new ArrayList<PositionalParamSpec>();
                    for (PositionalParamSpec param : optionList) {

                        // exclude parameters whose type has the IExcluded marker interface
                        if (!IExcluded.class.isAssignableFrom(param.auxiliaryTypes()[0])) {
                            shown.add(param);
                        }
                    }
                    return shown;
                }
            };
        }
    }
    interface IExcluded {}
    enum ToKeyword implements IExcluded {to}

    @Parameters(index = "0", description = "File to rename.")
    File file;

    @Parameters(index = "1", paramLabel = "to")
    ToKeyword to;

    @Parameters(index = "2", description = "New file name.")
    File newFile;

    public static void main(String[] args) {
        StringWriter sw = new StringWriter();

        CommandLine cmd = new CommandLine(new HideSomePositionalParams())
                .setHelpFactory(new MyHelpFactory())
                .setErr(new PrintWriter(sw, true));
        
        String expected = String.format("" +
                "Missing required parameters: '<file>', 'to', '<newFile>'%n" +
                "Usage: rename <file> to <newFile>%n" +
                "      <file>      File to rename.%n" +
                "      <newFile>   New file name.%n");

        cmd.execute(); // no args
        if (!expected.equals(sw.toString())) {
            throw new IllegalStateException(expected + " != " + sw.toString());
        }
    }
}