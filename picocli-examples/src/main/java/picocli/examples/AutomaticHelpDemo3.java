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
package picocli.examples;

import picocli.CommandLine;

import static picocli.CommandLine.Command;
import static picocli.CommandLine.Option;

@Command(version = "Help demo for picocli v3.0", header = "%nFully Automatic Help Demo%n",
        description = "Prints usage help and version help when requested.%n",
        mixinStandardHelpOptions = true)
public class AutomaticHelpDemo3 implements Runnable {

    @Option(names = "--count", description = "The number of times to repeat.")
    int count;

    public void run() {
        for (int i = 0; i < count; i++) {
            System.out.println("Hello world");
        }
    }

    // to run, execute:
    // java picocli.examples.AutomaticHelpDemo3 help
    public static void main(String... args) {
        new CommandLine(new AutomaticHelpDemo3()).execute(args);
    }
}
