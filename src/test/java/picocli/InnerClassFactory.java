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

import picocli.CommandLine.IFactory;
import picocli.CommandLine.InitializationException;

import java.lang.reflect.Constructor;

/**
 * Factory for instantiating subcommands etc whose classes are declared inline in a test function. For example:
 * <pre>
 * //
 * // This test should fail because the subcommand to instantiate
 * // does not have a no-argument public constructor.
 * //
 * &#064;Test(expected = InitializationException.class)
 * public void testDeclarativelyAddSubcommandsFailsWithoutNoArgConstructor() {
 *
 *     &#064;Command(name = "sub1")
 *     class ABC {
 *         public ABC(String constructorParam) {
 *         }
 *     }
 *
 *     &#064;Command(subcommands = {ABC.class})
 *     class MainCommand { }
 *
 *     new CommandLine(new MainCommand(), new InnerClassFactory(this));
 * }
 * </pre>
 */
public class InnerClassFactory implements IFactory {
    private final Object outer;
    public InnerClassFactory(Object outer) { this.outer = outer; }

    public <K> K create(final Class<K> cls) throws Exception {
        try {
            return CommandLine.defaultFactory().create(cls);
        } catch (Exception ex0) {
            try {
                Constructor<K> constructor = cls.getDeclaredConstructor(outer.getClass());
                return constructor.newInstance(outer);
            } catch (Exception ex) {
                try {
                    @SuppressWarnings("deprecation") // Class.newInstance is deprecated in Java 9
                    K result = cls.newInstance();
                    return result;
                } catch (Exception ex2) {
                    try {
                        Constructor<K> constructor = cls.getDeclaredConstructor();
                        return constructor.newInstance();
                    } catch (Exception ex3) {
                        throw new InitializationException("Could not instantiate " + cls.getName() + " either with or without construction parameter " + outer + ": " + ex, ex);
                    }
                }
            }
        }
    }
}
