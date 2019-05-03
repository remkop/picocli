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

@Command(version = "Help demo for picocli v2.0", header = "%nAutomatic Help Demo%n",
        description = "Prints usage help and version help when requested.%n",
        footer = "See AutomaticHelpDemo3 for the more compact syntax supported by picocli 3.0.")
public class AutomaticHelpDemo implements Runnable {

    @Option(names = "--count", description = "The number of times to repeat.")
    int count;

    @Option(names = {"-h", "--help"}, usageHelp = true,
            description = "Print usage help and exit.")
    boolean usageHelpRequested;

    @Option(names = {"-V", "--version"}, versionHelp = true,
            description = "Print version information and exit.")
    boolean versionHelpRequested;

    public void run() {
        // NOTE: code like below is no longer required:
        //
        // if (usageHelpRequested) {
        //     new CommandLine(this).usage(System.err);
        // } else if (versionHelpRequested) {
        //     new CommandLine(this).printVersionHelp(System.err);
        // } else { ... the business logic

        for (int i = 0; i < count; i++) {
            System.out.println("Hello world");
        }
    }

    public static void main(String... args) {
        new CommandLine(new AutomaticHelpDemo()).execute(args);
    }
}
