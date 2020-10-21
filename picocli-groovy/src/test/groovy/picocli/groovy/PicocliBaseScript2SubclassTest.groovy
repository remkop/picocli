package picocli.groovy

import groovy.transform.SourceURI
import org.junit.Rule
import org.junit.Test
import org.junit.contrib.java.lang.system.ProvideSystemProperty

class PicocliBaseScript2SubclassTest {
    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");

    @SourceURI URI sourceURI

    @Test
    void testPicocliScript2Subclass() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream()
        System.setOut(new PrintStream(baos))

        GroovyShell shell = new GroovyShell()
        shell.context.setVariable('args', [] as String[])
        def result = shell.evaluate(new File(new File(sourceURI).parentFile, 'ScriptExtendingPicocliBaseScript2Subclass.groovy'))
        assert result == 42

        String expected = String.format("" +
                "picocli.groovy.ScriptExtendingPicocliBaseScript2Subclass%n" +
                "picocli.groovy.ScriptExtendingPicocliBaseScript2Subclass%n" +
                "picocli.groovy.PicocliBaseScript2Subclass%n" +
                "picocli.groovy.PicocliBaseScript2%n")
        assert expected == baos.toString()
    }
}
