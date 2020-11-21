package picocli.groovy

import org.junit.Ignore
import org.junit.Test
import picocli.CommandLine

import java.security.MessageDigest

import picocli.CommandLine.Command;
import picocli.CommandLine.Option
import picocli.CommandLine.Model.ArgSpec;
import picocli.CommandLine.Model.CommandSpec;

class ClosureInAnnotationsTest {

    //static class ConverterDemoCompilerFailure  {
    //    @Option(names = ['-a', '--algorithm'],
    //            converter = { str -> MessageDigest.getInstance(str) }, // compiler error
    //            description = ['MD2, MD5, SHA-1, SHA-256, SHA-384, SHA-512,',
    //            '  or any other MessageDigest algorithm.'])
    //    MessageDigest algorithm;
    //}

    static class ConverterDemoFailure  {
        @Option(names = ['-a', '--algorithm'],
                converter = [{ str -> MessageDigest.getInstance(str) } ],
                description = ['MD2, MD5, SHA-1, SHA-256, SHA-384, SHA-512,',
                        '  or any other MessageDigest algorithm.'])
        MessageDigest algorithm;
    }

    @Ignore("Requires Groovy 3.0.7 or higher")
    @Test
    void testTypeConverter() {
        ConverterDemoFailure demo = CommandLine.populateCommand(new ConverterDemoFailure(), "-a=SHA-256")
        assert demo.algorithm == MessageDigest.getInstance("SHA-256")

        // gives
        // picocli.CommandLine$MissingTypeConverterException:
        // No TypeConverter registered for java.security.MessageDigest of
        // field java.security.MessageDigest picocli.groovy.ClosureInAnnotationsTest$ConverterDemoFailure.algorithm
    }

    static class CompletionCandidatesDemo  {
        @Option(names = ['-a', '--algorithm'],
                completionCandidates = {["A", "B", "C"]},
                description = ['MD2, MD5, SHA-1, SHA-256, SHA-384, SHA-512,',
                        '  or any other MessageDigest algorithm.'])
        String algorithm;
    }
    @Test
    void testCompletionCandidates() {
        CommandLine cmd = new CommandLine(new CompletionCandidatesDemo())
        def actual = cmd.getCommandSpec().findOption("-a").completionCandidates()
        assert ["A", "B", "C"] == actual
    }

    @Command(name = "ParameterConsumerDemo1")
    static class ParameterConsumerDemo  {
        @Option(names = ['-x'],
                parameterConsumer = {{Stack<String> args, ArgSpec argSpec, CommandSpec commandSpec ->
                    argSpec.setValue(args.toString() + commandSpec.name())
                    args.clear()
                } as CommandLine.IParameterConsumer })
        String x
    }
    @Test
    void testParameterConsumer() {
        //System.setProperty("picocli.trace", "debug")
        ParameterConsumerDemo demo = CommandLine.populateCommand(new ParameterConsumerDemo(), "-x", "1", "2", "3");
        assert "[3, 2, 1]ParameterConsumerDemo1" == demo.x
    }

    @Command(name = "ParameterConsumerDemo2")
    static class ParameterConsumerDemo2  {
        @Option(names = ['-x'],
                parameterConsumer = {{args, argSpec, commandSpec ->
                    argSpec.setValue(args.toString() + commandSpec.name())
                    args.clear()
                } as CommandLine.IParameterConsumer })
        String x
    }
    @Test
    void testParameterConsumer2() {
        ParameterConsumerDemo2 demo = CommandLine.populateCommand(new ParameterConsumerDemo2(), "-x", "4", "5", "6");
        assert "[6, 5, 4]ParameterConsumerDemo2" == demo.x
    }

    @Command(name = "VersionProviderDemo",
            versionProvider = { {-> ["line1" , "line2"] as String[] } as CommandLine.IVersionProvider },
            defaultValueProvider = {{argSpec -> argSpec.command().name() + argSpec.descriptionKey() } as CommandLine.IDefaultValueProvider}
    )
    static class VersionAndDefaultProviderDemo {
        @Option(names = ['-x'], descriptionKey = "xxxyyy") String x
    }
    @Test
    void testVersionAndDefaultProviderDemo() {
        VersionAndDefaultProviderDemo demo = new VersionAndDefaultProviderDemo()
        CommandLine cmd = new CommandLine(demo)
        StringWriter sw = new StringWriter()
        cmd.printVersionHelp(new PrintWriter(sw))
        assert String.format("line1%nline2%n") == sw.toString()

        cmd.parseArgs()
        assert "VersionProviderDemoxxxyyy" == demo.x
    }

    @Command(name = "ClosureDemo",
            versionProvider = { {-> ["line1" , "line2"] as String[] } as CommandLine.IVersionProvider },
            defaultValueProvider = {{argSpec -> "some default" } as CommandLine.IDefaultValueProvider}
    )
    static class ClosureDemo {
        @Option(names = '-x', completionCandidates = {["A", "B", "C"]})
        String x

        @Option(names = '-y',
                parameterConsumer = {{args, argSpec, commandSpec ->
                    argSpec.setValue(args.toString() + commandSpec.name())
                    args.clear()
                } as CommandLine.IParameterConsumer })
        String y

//        @Option(names = '-z', converter = [ // requires Groovy 3.0.7
//                { { str -> MessageDigest.getInstance(str) } as CommandLine.ITypeConverter }
//        ])
//        MessageDigest z
    }

    @Test
    void testClosureDemo() {
        ClosureDemo demo = new ClosureDemo()
        CommandLine cmd = new CommandLine(demo)
        StringWriter sw = new StringWriter()
        cmd.printVersionHelp(new PrintWriter(sw))
        assert String.format("line1%nline2%n") == sw.toString()

        cmd.parseArgs()
        assert "some default" == demo.x
        assert "[some default]ClosureDemo" == demo.y

        def candidates = cmd.getCommandSpec().findOption("-x").completionCandidates()
        assert ["A", "B", "C"] == candidates
    }
}
