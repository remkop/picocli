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

/**
 * Demonstrates usage help with ansi colors on Windows DOS (without Cygwin or MSYS(2)).
 * Requires Jansi on the classpath.
 */
@Command(name = "picocli.WindowsJansiDemo")
public class WindowsJansiDemo extends Demo {
    public static void main(String[] args) {

        // Since https://github.com/remkop/picocli/issues/491 was fixed in picocli 3.6.0,
        // Ansi.AUTO is automatically enabled if the AnsiConsole is installed.
        AnsiConsole.systemInstall(); // Jansi magic
        new CommandLine(new WindowsJansiDemo()).execute(args);
        AnsiConsole.systemUninstall();
    }
}
