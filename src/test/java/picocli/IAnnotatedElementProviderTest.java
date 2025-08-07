/*
   Copyright 2017 Remko Popma

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package picocli;

import java.io.File;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.IAnnotatedElementProvider;
import picocli.CommandLine.Model.OptionSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * Tests for {@link IAnnotatedElementProvider}.
 * @since 4.8.0
 */
public class IAnnotatedElementProviderTest {

    @Before public void setUp() { System.clearProperty("picocli.trace"); }
    @After public void tearDown() { System.clearProperty("picocli.trace"); }

    @Command(name = "annotated-element-provider-test")
    static class IAnnotatedElementProviderTestCommand {
        @Option(names = "-a") int a;
        @Option(names = "-b") long b;
        @Option(names = "-c")
        void setC(long c) {

        }
        @Command
        void commit(
            @Option(names = {"-m", "--message"}, description = "Commit message")
            String commitMessage,

            @Option(names = "--squash", paramLabel = "<commit>")
            String squash,

            @Parameters(paramLabel = "<file>") File[] files) {
            // ... implement business logic
        }

    }

    @Test
    public void testAnnotatedElementAccess() throws Exception {
    	IAnnotatedElementProviderTestCommand command = new IAnnotatedElementProviderTestCommand();
        CommandLine commandLine = new CommandLine(command);
        CommandSpec spec = commandLine.getCommandSpec();
        for (OptionSpec option: spec.options()) {
        	org.junit.Assert.assertTrue(option.setter() instanceof IAnnotatedElementProvider);
        	org.junit.Assert.assertTrue(option.getter() instanceof IAnnotatedElementProvider);

        	String optionName = option.names()[0];
            Option optionAnnotation = option.getAnnotation(Option.class);
            org.junit.Assert.assertNotNull(optionAnnotation);
            org.junit.Assert.assertEquals(option.shortestName(), optionAnnotation.names()[0]);
            AnnotatedElement setterAnnotatedElement = ((IAnnotatedElementProvider) option.setter()).getAnnotatedElement();
            AnnotatedElement getterAnnotatedElement = ((IAnnotatedElementProvider) option.getter()).getAnnotatedElement();
            if ("-a".equals(optionName) || "-b".equals(optionName)) {
                org.junit.Assert.assertTrue(setterAnnotatedElement instanceof Field);
                org.junit.Assert.assertTrue(getterAnnotatedElement instanceof Field);

                Field setterField = (Field) setterAnnotatedElement;
                org.junit.Assert.assertEquals(IAnnotatedElementProviderTestCommand.class, setterField.getDeclaringClass());
                org.junit.Assert.assertEquals(optionName.substring(1), setterField.getName());

                Field getterField = (Field) getterAnnotatedElement;
                org.junit.Assert.assertEquals(IAnnotatedElementProviderTestCommand.class, getterField.getDeclaringClass());
                org.junit.Assert.assertEquals(optionName.substring(1), getterField.getName());
            } else if ("-c".equals(optionName)) {
                org.junit.Assert.assertTrue(setterAnnotatedElement instanceof Method);
                org.junit.Assert.assertTrue(getterAnnotatedElement instanceof Method);

                Method setterMethod = (Method) setterAnnotatedElement;
                org.junit.Assert.assertEquals(IAnnotatedElementProviderTestCommand.class, setterMethod.getDeclaringClass());
                org.junit.Assert.assertEquals("setC", setterMethod.getName());

                Method getterMethod = (Method) getterAnnotatedElement;
                org.junit.Assert.assertEquals(IAnnotatedElementProviderTestCommand.class, getterMethod.getDeclaringClass());
                org.junit.Assert.assertEquals("setC", getterMethod.getName());
            } else {
                org.junit.Assert.fail("Unexpected option: " + optionName);
        	}
        }

        // Testing sub-command
        for (Map.Entry<String, CommandLine> subCommandEntry: commandLine.getSubcommands().entrySet()) {
            if ("commit".equals(subCommandEntry.getKey())) {
                CommandLine subCommand = subCommandEntry.getValue();
                CommandSpec subCommandSpec = subCommand.getCommandSpec();
                Object userObject = subCommandSpec.userObject();
                org.junit.Assert.assertTrue(userObject instanceof Method);
                 for (OptionSpec option: subCommandSpec.options()) {
                     Option optionAnnotation = option.getAnnotation(Option.class);
                     org.junit.Assert.assertNotNull(optionAnnotation);
                    if ("-m".equals(option.shortestName())) {
                        org.junit.Assert.assertEquals(1, optionAnnotation.description().length);
                        org.junit.Assert.assertEquals("Commit message", optionAnnotation.description()[0]);
                    } else if ("--squash".equals(option.shortestName())) {
                        org.junit.Assert.assertEquals(0, optionAnnotation.description().length);
                    } else {
                        org.junit.Assert.fail("Unexpected option: " + option.shortestName());
                    }
                }
            } else {
                org.junit.Assert.fail("Unexpected sub-command: " + subCommandEntry.getKey());
            }
        }
    }

}
