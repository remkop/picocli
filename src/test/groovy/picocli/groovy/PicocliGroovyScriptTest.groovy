/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package picocli.groovy

import groovy.transform.SourceURI
import org.junit.Ignore
import org.junit.Test

/**
 * @author Jim White
 * @author Remko Popma
 */

public class PicocliGroovyScriptTest {
    @SourceURI URI sourceURI

    @Test
    void testParameterizedScript() {
        GroovyShell shell = new GroovyShell()
        shell.context.setVariable('args',
                ["--codepath", "/usr/x.jar", "-cp", "/bin/y.jar", "-cp", "z", "--", "placeholder", "another"] as String[])
        def result = shell.evaluate '''
@groovy.transform.BaseScript(picocli.groovy.PicocliGroovyScript)
import groovy.transform.Field
import picocli.CommandLine

@CommandLine.Parameters
@Field List<String> parameters

@CommandLine.Option(names = ["-cp", "--codepath"])
@Field List<String> codepath = []

//println parameters
//println codepath

assert parameters == ['placeholder', 'another']
assert codepath == ['/usr/x.jar', '/bin/y.jar', 'z']

[parameters.size(), codepath.size()]
'''
        assert result == [2, 3]
    }

    @Ignore("Requires #130 support for options _following_ positional parameters enhancement")
    @Test
    void testSimpleCommandScript() {
        GroovyShell shell = new GroovyShell()
        shell.context.setVariable('args',
                [ "--codepath", "/usr/x.jar", "placeholder", "-cp", "/bin/y.jar", "another" ] as String[])
        def result = shell.evaluate(new File(new File(sourceURI).parentFile, 'SimpleCommandScriptTest.groovy'))
        assert result == [777]
    }

    @Test
    void testMultipleCommandScript() {
        GroovyShell shell = new GroovyShell()
        def result = shell.evaluate(new File(new File(sourceURI).parentFile, 'MultipleCommandScriptTest.groovy'))
        assert result == [33]
    }
}

