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
package picocli;

import org.fusesource.jansi.AnsiConsole;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Ansi;

/**
 * Demonstrates usage help with ansi colors on Windows DOS (without Cygwin or MSYS(2)).
 * Requires Jansi on the classpath.
 */
@Command(name = "picocli.WindowsJansiDemo")
public class WindowsJansiDemo extends Demo {
    public static void main(String[] args) {

        // On Windows without Cygwin or similar, picocli will not emit ANSI escape codes by default.
        // Force ANSI ON if no user preference, otherwise let user decide.
        Ansi ansi = System.getProperty("picocli.ansi") == null ? Ansi.ON : Ansi.AUTO;

        AnsiConsole.systemInstall(); // Jansi magic
        CommandLine.run(new WindowsJansiDemo(), System.err, ansi, args);
        AnsiConsole.systemUninstall();
    }
}
