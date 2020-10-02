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
package picocli.examples.subcommands;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.RunAll;

import java.util.Locale;

public class SubcommandDemo {

    @Command(name = "ISOCodeResolve", subcommands = { Subcommand1.class, Subcommand2.class,
            CommandLine.HelpCommand.class }, description = "Resolve ISO country codes (ISO-3166-1) or language codes (ISO 639-1 or -2)")
    static class ParentCommand implements Runnable {

        @Override
        public void run() { }
    }

    @Command(name = "country", description = "Resolve ISO country code (ISO-3166-1, Alpha-2 code)")
    static class Subcommand1 implements Runnable {

        @Parameters(arity = "1..*", paramLabel = "<country code>", description = "country code(s) to be resolved")
        private String[] countryCodes;

        @Override
        public void run() {
            for (String code : countryCodes) {
                System.out.println(String.format("%s: %s", code.toUpperCase(), new Locale("", code).getDisplayCountry()));
            }
        }
    }

    @Command(name = "language", description = "Resolve ISO language code (ISO 639-1 or -2, two/three letters)")
    static class Subcommand2 implements Runnable {

        @Parameters(arity = "1..*", paramLabel = "<language code>", description = "language code(s) to be resolved")
        private String[] languageCodes;

        @Override
        public void run() {
            for (String code : languageCodes) {
                System.out.println(String.format("%s: %s", code.toLowerCase(), new Locale(code).getDisplayLanguage()));
            }
        }
    }

    public static void main(String[] args) {
        CommandLine cmd = new CommandLine(new ParentCommand());
        cmd.setExecutionStrategy(new RunAll()); // default is RunLast
        cmd.execute(args);

        if (args.length == 0) { cmd.usage(System.out); }
    }
}