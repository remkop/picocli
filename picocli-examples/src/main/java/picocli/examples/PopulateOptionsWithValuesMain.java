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
 * This example demonstrates the usage with options which takes values.
 * 
 * <p>
 * 
 * When no arguments are provided, the options will have their default values:
 * 
 * <pre>
 * Arguments:
 *   
 * 
 * Options:
 *   name: default
 *   properties:
 * </pre>
 * 
 * When the option is provided without a value, an exception is thrown and in
 * this example the error message is printed to sysout.
 * 
 * <p>
 * 
 * The value can be provided either directly appended to the short option:
 * 
 * <pre>
 * Arguments:
 *   "-nvalue"
 * 
 * Options:
 *   name: value
 *   properties:
 * </pre>
 * 
 * Or to the long option:
 * 
 * <pre>
 * Arguments:
 *   "--name=value"
 * 
 * Options:
 *   name: value
 *   properties:
 * </pre>
 * 
 * Additionally, the following formats are also recognized:
 * 
 * <pre>
 * -n value
 * -n=value
 * --name value
 * </pre>
 * 
 * If the option is declared as array or list, the argument can be provided
 * multiple times to populate the list:
 * 
 * <pre>
 * Arguments:
 *   "--name=value" "--properties=smart" "-p intelligent" "-p strong"
 * 
 * Options:
 *   name: value
 *   properties: "smart" "intelligent" "strong"
 * </pre>
 * 
 * Alternatively, with the declaration of the option one can specify a separator
 * to be used:
 * 
 * <pre>
 * Arguments:
 *   "--name=value" "--properties=smart,intelligent,strong"
 * 
 * Options:
 *   name: value
 *   properties: "smart" "intelligent" "strong"
 * </pre>
 * 
 * @author Robert 'Bobby' Zenz
 */
public class PopulateOptionsWithValuesMain {
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
        System.out.println("Arguments:");
        System.out.print("  ");
        for (String arg : args) {
            System.out.print("\"" + arg + "\" ");
        }
        System.out.println();
        System.out.println();

        System.out.println("Options:");
        System.out.println("  name: " + options.getName());
        if (options.getProperties() != null) {
            System.out.print("  properties: ");
            for (String property : options.getProperties()) {
             System.out.print("\"" + property + "\" ");
            }
            System.out.println();
        } else {
            System.out.println("  properties:");
        }
    }

    /**
     * This is the main container which will be populated by picocli with values
     * from the arguments.
     */
    private static class Options {
        @Option(names = { "-n", "--name" })
        private String name = "default";

        @Option(names = { "-p", "--properties" }, split = ",")
        private String[] properties = null;

        public String getName() {
            return name;
        }

        public String[] getProperties() {
            return properties;
        }
    }
}
