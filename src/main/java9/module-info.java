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

// When compiling in Gradle, the org.beryx.jar plugin will be able to
// compile this file even with Java versions 8 or older.

// Your IDE may complain that "Modules are not supported at language level '5'",
// and javac may give an error: "java: class, interface or enum expected".
// To resolve this, exclude the 'java9' folder from the sources.
// IntelliJ IDEA:
//     File > Project Structure... > Modules > picocli_main > Sources:
//     select the 'java9' folder and click 'Mark as: Excluded'.

/**
 * Defines API and implementation for parsing command line arguments and creating command line (CLI) applications.
 * <p>
 *     The parser supports a variety of command line syntax styles including POSIX, GNU, MS-DOS and more.
 *     Generates highly customizable usage help messages with <a href="https://picocli.info/#_ansi_colors_and_styles">ANSI colors and styles</a>.
 *     Picocli-based applications can have <a href="https://picocli.info/autocomplete.html">command line TAB completion</a>
 *     showing available options, option parameters and subcommands, for any level of nested subcommands.
 * </p>
 * <p>
 *     How it works: annotate your class and pass it to the <code>CommandLine</code> constructor.
 *     Then invoke <code>CommandLine.parseArgs</code> or <code>CommandLine.execute</code> with
 *     the command line parameters, and picocli parses the command line arguments and converts them
 *     to strongly typed values, which are then injected in the annotated fields and methods of your class.
 * </p>
 * <p>
 *     Picocli provides an <a href="https://picocli.info/#execute">execute method</a>
 *     that allows applications to omit error handling and other boilerplate code for common use cases.
 *     Here is a small example application that uses the <code>CommandLine.execute</code> method
 *     to do parsing and error handling in one line of code.
 * </p>
 * <p>
 *     The full user manual is hosted at <a href="https://picocli.info/">https://picocli.info</a>.
 * </p>
 * <pre>
 * &#064;Command(name = "checksum", mixinStandardHelpOptions = true, version = "Checksum 4.0",
 *          description = "Prints the checksum (SHA-1 by default) of a file to STDOUT.")
 * class CheckSum implements Callable&lt;Integer&gt; {
 *
 *     &#064;Parameters(index = "0", description = "The file whose checksum to calculate.")
 *     private File file;
 *
 *     &#064;Option(names = {"-a", "--algorithm"}, description = "MD5, SHA-1, SHA-256, ...")
 *     private String algorithm = "SHA-1";
 *
 *     &#064;Override
 *     public Integer call() throws Exception { // your business logic goes here
 *         byte[] fileContents = Files.readAllBytes(file.toPath());
 *         byte[] digest = MessageDigest.getInstance(algorithm).digest(fileContents);
 *         System.out.printf("%0" + (digest.length*2) + "x%n", new BigInteger(1, digest));
 *         return 0;
 *     }
 *
 *     // CheckSum implements Callable, so parsing, error handling and handling user
 *     // requests for usage help or version help can be done with one line of code.
 *     public static void main(String[] args) {
 *         int exitCode = new CommandLine(new CheckSum()).execute(args);
 *         System.exit(exitCode);
 *     }
 * }
 * </pre>
 * @since 4.0.0
 */
module info.picocli {
    exports picocli;
    requires static java.sql;
}
