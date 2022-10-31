package picocli.groovy

import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Ignore
import org.junit.Test
import picocli.CommandLine
import picocli.CommandLine.InitializationException

@Ignore
class ClosureInAnnotationsDisabledTest {
    @BeforeClass
    public static void beforeClass() {
        System.setProperty("picocli.disable.closures", "true")
    }

    @AfterClass
    public static void afterClass() {
        System.clearProperty("picocli.disable.closures")
    }


    @CommandLine.Command(name = "ClosureDemo",
        versionProvider = { {-> ["line1" , "line2"] as String[] } as CommandLine.IVersionProvider },
        defaultValueProvider = {{argSpec -> "some default" } as CommandLine.IDefaultValueProvider}
    )
    static class ClosureDemo {
        @CommandLine.Option(names = '-x', completionCandidates = {["A", "B", "C"]})
        String x

        @CommandLine.Option(names = '-y',
            parameterConsumer = {{args, argSpec, commandSpec ->
                argSpec.setValue(args.toString() + commandSpec.name())
                args.clear()
            } as CommandLine.IParameterConsumer })
        String y
    }

    @Test
    void testClosureDemo() {
        try {
            new CommandLine(new ClosureDemo())
        } catch (InitializationException ex) {
            String name = ClosureDemo.class.getName()
            assert ex.message.startsWith("Cannot instantiate ${name}\$_closure1: the class has no constructor")
        }
    }

}
