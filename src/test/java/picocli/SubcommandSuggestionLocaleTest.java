/*
   Copyright 2026 Remko Popma

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

import org.junit.Test;
import picocli.CommandLine.Model.CommandSpec;

import java.util.Collections;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class SubcommandSuggestionLocaleTest {
    @Test
    public void testSuggestionsIgnoreDefaultLocale() throws Exception {
        Locale original = Locale.getDefault();
        // Use reflection so this test also runs on Java 5 and 6, before locale categories existed.
        Class<?> categoryClass = null;
        try {
            categoryClass = Class.forName("java.util.Locale$Category");
        } catch (ClassNotFoundException ignored) {
            // Java 5 and 6 only have the general default locale.
        }
        Object[] categories = categoryClass == null ? new Object[0] : categoryClass.getEnumConstants();
        Locale[] categoryDefaults = new Locale[categories.length];
        for (int i = 0; i < categories.length; i++) {
            categoryDefaults[i] = (Locale) Locale.class.getMethod("getDefault", categoryClass).invoke(null, categories[i]);
        }
        try {
            for (Locale locale : new Locale[] {Locale.ENGLISH, new Locale("tr", "TR")}) {
                Locale.setDefault(locale);
                CommandLine command = new CommandLine(CommandSpec.create().name("app"));
                command.addSubcommand("init", new CommandLine(CommandSpec.create().name("init")));
                try {
                    command.parseArgs("INIT");
                    fail("Expected an unmatched subcommand");
                } catch (CommandLine.UnmatchedArgumentException ex) {
                    assertEquals(locale.toString(), Collections.singletonList("init"), ex.getSuggestions());
                }
            }
        } finally {
            Locale.setDefault(original);
            for (int i = 0; i < categories.length; i++) {
                Locale.class.getMethod("setDefault", categoryClass, Locale.class).invoke(null, categories[i], categoryDefaults[i]);
            }
        }
    }
}
