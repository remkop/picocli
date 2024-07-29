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

import java.lang.reflect.Field;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.IAnnotatedElementProvider;
import picocli.CommandLine.Model.OptionSpec;
import picocli.CommandLine.Option;

/**
 * Tests for {@link IAnnotatedElementProvider}.
 */
public class IAnnotatedElementProviderTest {

    @Before public void setUp() { System.clearProperty("picocli.trace"); }
    @After public void tearDown() { System.clearProperty("picocli.trace"); }
    
    @Command(name = "reflector-test")
    static class IAnnotatedElementProviderTestCommand {
        @Option(names = "-a") int a;
        @Option(names = "-b") long b;
    }

    @Test
    public void testFieldAccess() throws Exception {
    	IAnnotatedElementProviderTestCommand command = new IAnnotatedElementProviderTestCommand();
        CommandLine commandLine = new CommandLine(command);
        CommandSpec spec = commandLine.getCommandSpec();
        for (OptionSpec option: spec.options()) {
        	org.junit.Assert.assertTrue(option.setter() instanceof IAnnotatedElementProvider);
        	org.junit.Assert.assertTrue(option.getter() instanceof IAnnotatedElementProvider);
        	
        	org.junit.Assert.assertTrue(((IAnnotatedElementProvider) option.setter()).getAnnotatedElement() instanceof Field);
        	org.junit.Assert.assertTrue(((IAnnotatedElementProvider) option.getter()).getAnnotatedElement() instanceof Field);
        	        	
        	org.junit.Assert.assertEquals(IAnnotatedElementProviderTestCommand.class, ((Field) ((IAnnotatedElementProvider) option.setter()).getAnnotatedElement()).getDeclaringClass());
        	org.junit.Assert.assertEquals(IAnnotatedElementProviderTestCommand.class, ((Field) ((IAnnotatedElementProvider) option.getter()).getAnnotatedElement()).getDeclaringClass());        	
        }
    }
    
}
