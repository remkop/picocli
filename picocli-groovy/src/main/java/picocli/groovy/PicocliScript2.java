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
 * <p>
 * Annotation to give Groovy scripts convenient access to picocli functionality, updated for picocli version 4 and greater.
 * Scripts may annotate the package statement, an import statement or a local variable with
 * {@code @PicocliScript2} and the script base class will be {@link PicocliScriptASTTransformation transformed} to
 * {@link PicocliBaseScript2}.
 * </p><p>
 * Also, any {@link picocli.CommandLine.Command} annotation on the same variable or import statement will be added to
 * the script class. With the {@code @Command} annotation scripts can customize elements shown in the usage message
 * like command name, description, headers, footers etc.
 * </p>
 * <h1>Example usage</h1>
 * <pre>
 * &#64;Command(name = "myCommand", description = "does something special")
 * &#64;PicocliScript2
 * import picocli.groovy.PicocliScript2
 * import picocli.CommandLine.Command
 * import picocli.CommandLine.Option
 * import groovy.transform.Field
 *
 * &#64;Option(names = "-x", description = "number of repetitions")
 * &#64;Field int count;
 *
 * &#64;Option(names = ["-h", "--help"], usageHelp = true, description = "print this help message and exit")
 * &#64;Field boolean helpRequested;
 *
 * //if (helpRequested) { CommandLine.usage(this, System.err); return 0; } // PicocliBaseScript takes care of this
 * count.times {
 *     println "hi"
 * }
 * assert this == theScript
 * assert this.commandLine.commandName == "myCommand"
 * </pre>
 * <p>
 * Otherwise, this annotation works similar to the Groovy built-in {@link groovy.transform.BaseScript}.
 * Using this annotation will override the base script set by Groovy compiler or
 * {@link org.codehaus.groovy.control.CompilerConfiguration} of {@link groovy.lang.GroovyShell}.
 * </p>
 * <h1>Customizing</h1>
 * <p>
 * To customize, a base script class extending {@link PicocliBaseScript2}
 * may be specified as the value of this annotation, for example:
 * </p><pre>
 * &#64;PicocliScript2(com.mycompany.MyScriptBaseClass)
 * import picocli.groovy.PicocliScript2
 * </pre><p>
 * An alternative way to customize the base script is annotating a local variable with {@code @PicocliScript2}.
 * This way the variable type will be used as the base script class and
 * the annotated variable will become a shortcut to {@code this} object.
 * The type of the annotated variable must extend {@link PicocliBaseScript2}.
 * </p><pre>
 * import picocli.groovy.PicocliScript2
 * import com.mycompany.MyScriptBaseClass
 * &#64;PicocliScript2 MyScriptBaseClass theScript;
 * </pre>
 * <h1>PicocliBaseScript2 vs PicocliBaseScript</h1>
 * <p>
 *     See the {@link PicocliBaseScript2} documentation for details.
 * </p>
 *
 * @see PicocliScriptASTTransformation
 * @author Remko Popma
 * @since 2.0
 */
@java.lang.annotation.Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.LOCAL_VARIABLE, ElementType.PACKAGE, ElementType.TYPE, ElementType.FIELD /*, ElementType.IMPORT*/})
@GroovyASTTransformationClass("picocli.groovy.PicocliScriptASTTransformation")
public @interface PicocliScript2 {
    Class<?> value() default PicocliBaseScript2.class;
}
