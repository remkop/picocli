package picocli.annotation.processing.tests;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.Test;
import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.IAnnotatedElement;
import picocli.CommandLine.Spec;
import picocli.codegen.annotation.processing.AbstractCommandSpecProcessor;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;
import static org.junit.Assert.*;
import static picocli.annotation.processing.tests.Resources.slurp;
import static picocli.annotation.processing.tests.Resources.slurpAll;
import static picocli.annotation.processing.tests.YamlAssert.compareCommandYamlDump;

public class MixeeTest {
    static Predicate<IAnnotatedElement> mixeeFilter = element -> {
        Spec ann = element.getAnnotation(Spec.class);
        return ann.value() == Spec.Target.MIXEE;
    };

    static class MixeeProcessor extends AbstractCommandSpecProcessor {
        /** The mixin that requests the MIXEE to be injected */
        List<CommandSpec> mixins;

        /** The MIXEEs that use the above mixin. */
        IdentityHashMap<CommandSpec, List<CommandSpec>> mixeesPerMixin = new IdentityHashMap<>();

        @Override
        protected boolean handleCommands(Map<Element, CommandSpec> commands,
                                         Set<? extends TypeElement> annotations,
                                         RoundEnvironment roundEnv) {
            if (roundEnv.processingOver()) { return false; }
            mixins = commands.values().stream().filter(spec ->
                    spec.specElements().stream().anyMatch(mixeeFilter)
            ).collect(Collectors.toList());

            for (CommandSpec mixin : mixins) {
                System.out.println(mixin);

                List<CommandSpec> mixees = commands.values().stream().filter(spec -> spec.mixins().containsValue(mixin)).collect(Collectors.toList());
                mixeesPerMixin.put(mixin, mixees);
            }

            return false;
        }
    }
    @Test
    public void testMixee() {
        JavaFileObject[] sources = new JavaFileObject[] {
                JavaFileObjects.forResource("picocli/examples/logging/LoggingMixin.java"),
                JavaFileObjects.forResource("picocli/examples/logging/LoggingSub.java"),
                JavaFileObjects.forResource("picocli/examples/logging/LoggingSubSub.java"),
                JavaFileObjects.forResource("picocli/examples/logging/MyApp.java")
        };

        MixeeProcessor processor = new MixeeProcessor();
        Compilation compilation =
                javac()
                        .withProcessors(processor)
                        .compile(sources);

        assertThat(compilation).succeeded();
        assertEquals(1, processor.mixins.size());
        assertEquals("picocli.examples.logging.LoggingMixin", qualifiedName(processor.mixins.get(0)));

        assertEquals(1, processor.mixeesPerMixin.size());
        List<CommandSpec> mixees = processor.mixeesPerMixin.get(processor.mixins.get(0));
        assertEquals(5, mixees.size());
        assertEquals("picocli.examples.logging.LoggingSub", qualifiedName(mixees.get(0)));
        assertEquals("picocli.examples.logging.LoggingSubSub", qualifiedName(mixees.get(1)));
        assertEquals("LoggingSubSub.commandMethodSub", qualifiedName(mixees.get(2)));
        assertEquals("LoggingSub.commandMethodSub", qualifiedName(mixees.get(3)));
        assertEquals("picocli.examples.logging.MyApp", qualifiedName(mixees.get(4)));
    }

    private static String qualifiedName(CommandSpec spec) {
        if (spec.userObject() instanceof TypeElement) {
            TypeElement type = (TypeElement) spec.userObject();
            return type.getQualifiedName().toString();
        }
        if (spec.userObject() instanceof ExecutableElement) {
            ExecutableElement method = (ExecutableElement) spec.userObject();
            return method.getEnclosingElement().getSimpleName() + "." + method.getSimpleName().toString();
        }
        return null;
    }

}
