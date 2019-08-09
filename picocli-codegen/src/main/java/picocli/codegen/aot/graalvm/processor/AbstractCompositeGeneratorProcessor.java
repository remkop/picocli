package picocli.codegen.aot.graalvm.processor;

import picocli.CommandLine.Model.CommandSpec;
import picocli.codegen.annotation.processing.AbstractCommandSpecProcessor;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Base class for writing annotation processors that do something with {@code CommandSpec} models.
 * The model is built up once, and from this model, multiple files can be generated.
 * The generation part is delegated to {@link IGenerator} implementations.
 * @since 4.0
 */
abstract class AbstractCompositeGeneratorProcessor extends AbstractCommandSpecProcessor {

    Map<Element, CommandSpec> allCommands = new LinkedHashMap<Element, CommandSpec>();

    protected List<IGenerator> generators = new ArrayList<IGenerator>();

    protected AbstractCompositeGeneratorProcessor() {}

    @Override
    protected boolean handleCommands(Map<Element, CommandSpec> commands,
                                     Set<? extends TypeElement> annotations,
                                     RoundEnvironment roundEnv) {

        if (!roundEnv.processingOver()) {
            allCommands.putAll(commands);
            return false;
        }

        try {
            for (IGenerator generator : generators) {
                generator.generate(allCommands);
            }
        } catch (Exception e) {
            // Generators are supposed to do their own error handling, but let's be paranoid.
            // We don't allow exceptions of any kind to propagate to the compiler
            fatalError(ProcessorUtil.stacktrace(e));
        }

        return false;
    }
}
