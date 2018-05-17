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
import picocli.CommandLine.Model.*;

public class ModelTestUtil {
    public static Field field(Class<?> cls, String fieldName) throws NoSuchFieldException {
        return cls.getDeclaredField(fieldName);
    }
    public static Field[] fields(Class<?> cls, String... fieldNames) throws NoSuchFieldException {
        Field[] result = new Field[fieldNames.length];
        for (int i = 0; i < fieldNames.length; i++) {
            result[i] = cls.getDeclaredField(fieldNames[i]);
        }
        return result;
    }
    public static OptionSpec option(Object obj, String fieldName) throws Exception {
        return option(obj, fieldName, CommandLine.defaultFactory());
    }
    public static OptionSpec option(Object obj, String fieldName, CommandLine.IFactory factory) throws Exception {
        return ArgsReflection.extractOptionSpec(TypedMember.createIfAnnotated(obj.getClass().getDeclaredField(fieldName), obj), factory);
    }
    public static OptionSpec[] options(Object obj, String... fieldNames) throws Exception {
        OptionSpec[] result = new OptionSpec[fieldNames.length];
        for (int i = 0; i < fieldNames.length; i++) {
            result[i] = option(obj, fieldNames[i]);
        }
        return result;
    }
}
