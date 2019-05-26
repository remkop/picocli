package picocli.codegen.aot.graalvm.processor;

import picocli.CommandLine;

import javax.lang.model.element.Element;
import java.util.Map;

interface IGenerator {
    void generate(Map<Element, CommandLine.Model.CommandSpec> allCommands);
}
