package picocli.annotation.processing.tests;

import picocli.CommandLine.Model.CommandSpec;
import picocli.codegen.annotation.processing.AbstractCommandSpecProcessor;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import static javax.tools.StandardLocation.SOURCE_OUTPUT;
import static picocli.annotation.processing.tests.AnnotatedCommandSourceGenerator.isBuiltInMixin;
import static picocli.annotation.processing.tests.AnnotatedCommandSourceGenerator.isBuiltInSubcommand;
import static picocli.annotation.processing.tests.AnnotatedCommandSourceGenerator.isNestedCommand;

public class AnnotatedCommandSourceGeneratorProcessor extends AbstractCommandSpecProcessor {
    private static Logger logger = Logger.getLogger(AnnotatedCommandSourceGeneratorProcessor.class.getName());

    @Override
    protected boolean handleCommands(Map<Element, CommandSpec> commands,
                                     Set<? extends TypeElement> annotations,
                                     RoundEnvironment roundEnv) {

        List<SourceUnit> list = new ArrayList<SourceUnit>();
        for (Map.Entry<Element, CommandSpec> entry : commands.entrySet()) {
            SourceUnit sourceUnit = find(entry.getKey(), list);
            sourceUnit.commands.add(entry.getValue());
        }
        for (SourceUnit sourceUnit : list) {
            try {
                generateCode(sourceUnit);
            } catch (IOException e) {
                error(sourceUnit.topLevel, "Unable to generated code for %s: %s", sourceUnit.topLevel, e);
            }
        }
        return true;
    }

    private SourceUnit find(Element element, List<SourceUnit> list) {
        for (SourceUnit sourceUnit : list) {
            if (sourceUnit.contains(element)) {
                return sourceUnit;
            }
        }
        SourceUnit result = new SourceUnit(element);
        list.add(result);
        return result;
    }

    private void generateCode(SourceUnit sourceUnit) throws IOException {
        TypeElement typeElement = (TypeElement) sourceUnit.topLevel;
        int count = 0;
        for (CommandSpec spec : sourceUnit.commandHierarchies()) {
            AnnotatedCommandSourceGenerator generator = new AnnotatedCommandSourceGenerator(spec);
            generator.setOutputPackage("generated." + generator.getOutputPackage());

            String unique = count == 0 ? "" : count + "";
            count++;

            // create a resource to prevent recursive processing of the annotations in the generated file
            FileObject sourceFile = processingEnv.getFiler().createResource(
                    SOURCE_OUTPUT,
                    generator.getOutputPackage(),
                    typeElement.getSimpleName() + unique + ".java");

            Writer writer = null;
            try {
                writer = sourceFile.openWriter();
                //PrintWriter pw = new PrintWriter(writer);
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                generator.writeTo(pw, "");
                pw.flush();
                //System.out.println(sw);
                writer.write(sw.toString());
                writer.flush();
            } finally {
                if (writer != null) {
                    writer.close();
                }
            }
        }
    }

    static class SourceUnit {
        String packageName;
        String className;
        Element topLevel;
        List<CommandSpec> commands = new ArrayList<CommandSpec>();

        public SourceUnit(Element element) {
            topLevel = topLevel(element);
        }

        public boolean contains(Element element) {
            Element topLevelELement = topLevel(element);
            return equals(topLevelELement, topLevel);
        }

        private boolean equals(Element topLevelELement, Element topLevel) {
            return topLevelELement.toString().equals(topLevel.toString());
        }

        static Element topLevel(Element element) {
            while (element.getEnclosingElement().getKind() != ElementKind.PACKAGE) {
                element = element.getEnclosingElement();
            }
            return element;
        }

        public List<CommandSpec> commandHierarchies() {
            List<CommandSpec> result = new ArrayList<CommandSpec>();
            for (CommandSpec cmd : commands) {
                String excludeReason = null;
                for (CommandSpec any : commands) {
                    if (cmd != any && isNestedCommand(cmd, any)) {
                        // TODO Exclude if nested in shared surrounding element
                        excludeReason = "Excluding " + cmd + ": it is nested in " + any;
                        break;
                    }
                    if (isBuiltInMixin(cmd) || isBuiltInSubcommand(cmd)) {
                        excludeReason = "Excluding built-in " + cmd.userObject();
                        break;
                    }
                }
                if (excludeReason == null) {
                    result.add(cmd);
                } else {
                    logger.info(excludeReason);
                }
            }
            return result;
        }
    }

    private Map<Element, CommandSpec> removeNested(Map<Element, CommandSpec> commands) {
        Map<Element, CommandSpec> result = new LinkedHashMap<Element, CommandSpec>();
        for (Map.Entry<Element, CommandSpec> entry : commands.entrySet()) {
            CommandSpec cmd = entry.getValue();
            String excludeReason = null;
        }
        return result;
    }
}
