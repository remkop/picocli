package picocli.annotation.processing.tests;

import picocli.CommandLine.Model.CommandSpec;
import picocli.codegen.annotation.processing.AbstractCommandSpecProcessor;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class CommandSpec2YamlProcessor extends AbstractCommandSpecProcessor {

    public List<String> strings = new ArrayList<String>();
    public Map<Element, CommandSpec> commands;

    @Override
    protected boolean handleCommands(Map<Element, CommandSpec> commands,
                                     Set<? extends TypeElement> annotations,
                                     RoundEnvironment roundEnv) {
        System.out.println(commands);
        this.commands = commands;
        CommandSpecYamlPrinter printer = new CommandSpecYamlPrinter();
        for (Map.Entry<Element, CommandSpec> entry : commands.entrySet()) {
            StringWriter sw = new StringWriter();
            printer.print(entry.getValue(), new PrintWriter(sw));
            strings.add(sw.toString());
        }
        return false;
    }
}
