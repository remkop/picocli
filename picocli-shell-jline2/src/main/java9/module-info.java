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
 * Defines picocli-based implementations of JLine 2 APIs
 * for building interactive command line (REPL) applications.
 *
 * @since 4.7.0
 */
module info.picocli.shell.jline2 {
    requires info.picocli;
    requires jline;

    exports picocli.shell.jline2;
}
