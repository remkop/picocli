/*
   Copyright 2017 Robert 'Bobby' Zenz

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
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;

/**
 * This example demonstrates the printing of the usage/help message when
 * requested.
 * 
 * <p>
 * 
 * When no arguments are provided, no usage/help information will be printed:
 * 
 * <pre>
 * No help requested, application can continue.
 * </pre>
 * 
 * When the flag is provided in the arguments, with either {@code -h} or
 * {@code --help}, the default usage/help text will be printed:
 * 
 * <pre>
 * Usage: &lt;main class&gt; [-h]
 *  -h, --help                  Prints this help text.
 * </pre>
 * 
 * @author Robert 'Bobby' Zenz
 */
public class PopulateHelpRequestedMain {
    public static void main(String[] args) {
        // Create a new Options class, which holds our options.
        Options options = new Options();

        try {
            // Populate the created class from the command line arguments.
            CommandLine.populateCommand(options, args);
        } catch (ParameterException e) {
            // The given command line arguments are invalid, for example there
            // are options specified which do not exist or one of the options
            // is malformed (missing a value, for example).
            System.out.println(e.getMessage());
            CommandLine.usage(options, System.out);
            return;
        }

        // Print the state.
        if (options.isHelpRequested()) {
            CommandLine.usage(options, System.out);
        } else {
            System.out.println("No help requested, application can continue.");
        }
    }

    /**
     * This is the main container which will be populated by picocli with values
     * from the arguments.
     */
    private static class Options {
        @Option(names = { "-h", "--help" }, description = "Prints this help text.")
        private boolean helpRequested = false;

        public boolean isHelpRequested() {
            return helpRequested;
        }
    }
}
