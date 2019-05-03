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

@Command(mixinStandardHelpOptions = true, version = "ExitCodeOnSuccess 4.0",
        exitCodeOnSuccess = -1,    // default = 0
        exitCodeOnVersionHelp = 1, // default = 0
        exitCodeOnUsageHelp = 2)   // default = 0
public class ExitCodeOnSuccess implements Runnable {
    public void run() {
        System.out.println("hello");
    }

    public static void main(String... args) {
        int exitCode = new CommandLine(new ExitCodeOnSuccess()).execute();
        assert exitCode == -1;

        int exitCodeVersion = new CommandLine(new ExitCodeOnSuccess()).execute("--version");
        assert exitCodeVersion == 1;

        int exitCodeHelp = new CommandLine(new ExitCodeOnSuccess()).execute("--help");
        assert exitCodeHelp == 2;

        System.exit(exitCode);
    }
}
