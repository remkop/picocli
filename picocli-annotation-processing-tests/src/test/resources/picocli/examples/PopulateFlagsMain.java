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
 * This example demonstrates the usage with a simple flag class.
 * 
 * <p>
 * 
 * When no arguments are provided, the flags will have their default values:
 * 
 * <pre>
 * Arguments:
 *   
 * 
 * Options:
 *         buffered: false
 *  overwriteOutput: true
 *          verbose: false
 * </pre>
 * 
 * When the flag is provided in the arguments, the default value of the flag
 * will be inverted and set. So if we can provide all flags as arguments:
 * 
 * <pre>
 * Arguments:
 *   "-b" "-o" "-v"
 * 
 * Options:
 *         buffered: true
 *  overwriteOutput: false
 *          verbose: true
 * </pre>
 * 
 * Because these flags are single letter names, we can also provide them
 * concatenated in one argument:
 * 
 * <pre>
 * Arguments:
 *   "-bov"
 * 
 * Options:
 *         buffered: true
 *  overwriteOutput: false
 *          verbose: true
 * </pre>
 * 
 * Or in any derivation there of.
 * 
 * @author Robert 'Bobby' Zenz
 */
public class PopulateFlagsMain {
    public static void main(String[] args) {
        // Create a new Options class, which holds our flags.
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
        System.out.println("Arguments:");
        System.out.print("  ");
        for (String arg : args) {
            System.out.print("\"" + arg + "\" ");
        }
        System.out.println();
        System.out.println();
        
        System.out.println("Options:");
        System.out.println("         buffered: " + options.isBuffered());
        System.out.println("  overwriteOutput: " + options.isOverwriteOutput());
        System.out.println("          verbose: " + options.isVerbose());
    }

    /**
     * This is the main container which will be populated by picocli with values
     * from the arguments.
     */
    private static class Options {
        @Option(names = "-b")
        private boolean buffered = false;

        @Option(names = "-o")
        private boolean overwriteOutput; //TODO = true;

        @Option(names = "-v")
        private boolean verbose = false;

        public boolean isBuffered() {
            return buffered;
        }

        public boolean isOverwriteOutput() {
            return overwriteOutput;
        }

        public boolean isVerbose() {
            return verbose;
        }
    }
}
