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
package picocli.examples.exitcode;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.lang.reflect.Method;

public class CustomExitCodeCommandMethod {

    @Command
    public int doit(@Option(names = "-x") int x) {
        return 456;
    }

    public static void main(String... args) {
        Method doit = CommandLine.getCommandMethods(CustomExitCodeCommandMethod.class, "doit").get(0);
        int exitCode = new CommandLine(doit).execute(args);
        assert exitCode == 456;
        System.exit(exitCode);
    }
}
