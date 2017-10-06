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

package picocli.groovy;

import org.codehaus.groovy.transform.GroovyASTTransformationClass;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Variable annotation used for changing the base script class of the current script.
 * {@link picocli.CommandLine.Command} annotations on the same variable or import statement will be added to the script class.
 * <p>
 * The type of the variable annotated with {@code @PicocliScript} must extend {@link picocli.groovy.PicocliBaseScript}.
 * Otherwise, this annotation works similar to the Groovy built-in {@link groovy.transform.BaseScript}.
 * </p><p>
 * It will be used as the base script class.
 * The annotated variable will become shortcut to <code>this</code> object.
 * Using this annotation will override base script set by Groovy compiler or
 * {@link org.codehaus.groovy.control.CompilerConfiguration} of {@link groovy.lang.GroovyShell}
 * </p><p>
 * Example usage:
 * </p>
 * <pre>
 * &#64;Command(name = "myCommand", description = "does something special")
 * &#64;CommandScript CustomScript theScript
 *
 * &#64;Option(names = "-x", description = "number of repetitions")
 * &#64;Field int count;
 *
 * &#64;Option(names = "-h", usageHelp = true, description = "print this help message and exit")
 * &#64;Field boolean helpRequested;
 *
 * count.times {
 *     println "hi"
 * }
 * assert this == theScript
 * assert this.scriptCommandLine.commandName == "myCommand"
 * </pre>
 *
 * @author Remko Popma
 * @since 2.0
 */
@java.lang.annotation.Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.LOCAL_VARIABLE, ElementType.PACKAGE, ElementType.TYPE /*, ElementType.IMPORT*/})
@GroovyASTTransformationClass("picocli.groovy.PicocliScriptASTTransformation")
public @interface PicocliScript {
    Class value() default PicocliBaseScript.class;
}
