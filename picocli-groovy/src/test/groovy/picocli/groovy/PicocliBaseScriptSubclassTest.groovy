package picocli.groovy

import groovy.transform.SourceURI
import org.junit.Rule
import org.junit.Test
import org.junit.contrib.java.lang.system.ProvideSystemProperty

class PicocliBaseScriptSubclassTest {
    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");

    @SourceURI URI sourceURI

    @Test
    void testPicocliScriptSubclass() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream()
        System.setOut(new PrintStream(baos))

        GroovyShell shell = new GroovyShell()
        shell.context.setVariable('args', [] as String[])
        def result = shell.evaluate(new File(new File(sourceURI).parentFile, 'ScriptExtendingPicocliBaseScriptSubclass.groovy'))
        assert result == 42

        String expected = String.format("" +
                "picocli.groovy.ScriptExtendingPicocliBaseScriptSubclass%n" +
                "picocli.groovy.ScriptExtendingPicocliBaseScriptSubclass%n" +
                "picocli.groovy.PicocliBaseScriptSubclass%n" +
                "picocli.groovy.PicocliBaseScript%n")
        assert expected == baos.toString()
    }
}
