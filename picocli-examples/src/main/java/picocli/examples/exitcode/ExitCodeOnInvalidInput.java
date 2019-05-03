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
import picocli.CommandLine.ParameterException;

@Command(exitCodeOnInvalidInput = 3)
public class ExitCodeOnInvalidInput implements Runnable {
    public void run() {
        throw new ParameterException(new CommandLine(this), "exit code demo");
    }

    public static void main(String... args) {
        int exitCode = new CommandLine(new ExitCodeOnInvalidInput()).execute(args);
        assert exitCode == 3;

        System.exit(exitCode);
    }
}
