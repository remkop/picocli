package picocli.annotation.processing.tests;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.Ignore;
import org.junit.Test;

import javax.annotation.processing.Processor;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

public class Issue2407Test
{
    @Ignore("https://github.com/remkop/picocli/issues/2407")
    @Test
    public void testIssue2407() {
        Processor processor = new AnnotatedCommandSourceGeneratorProcessor();
        Compilation compilation =
            javac()
                .withProcessors(processor)
                .compile(JavaFileObjects.forResource(
                    "picocli/issue2407/Main.java"));

        assertThat(compilation).succeeded();
    }
}
//error: FATAL ERROR: picocli.CommandLine$InitializationException:
// ArgGroup has no options or positional parameters, and no subgroups:
// AnnotatedElementHolder(FIELD usernamePassword in picocli.issue2407.Main.Update) in null
//    at picocli.CommandLine$Model$ArgGroupSpec.<init>(CommandLine.java:10430)
//    at picocli.CommandLine$Model$ArgGroupSpec$Builder.build(CommandLine.java:10928)
//    at picocli.codegen.annotation.processing.AbstractCommandSpecProcessor$Context.connectArgGroups(AbstractCommandSpecProcessor.java:1043)
