/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache license, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the license for the specific language governing permissions and
 * limitations under the license.
 */
package picocli;

import org.fusesource.jansi.AnsiConsole;
import picocli.CommandLine.Command;

/**
 * Demonstrates usage help with ansi colors on Windows DOS (without Cygwin or MSYS(2)).
 * Requires Jansi on the classpath.
 */
@Command(name = "picocli.WindowsJansiDemo")
public class WindowsJansiDemo extends SubcommandDemo {
    public static void main(String[] args) {
        AnsiConsole.systemInstall(); // Jansi magic
        if (System.getProperty("picocli.ansi") == null) { // user did not set preference
            CommandLine.ansi = true; // force picocli to use ansi colors
        }
        CommandLine.run(new WindowsJansiDemo(), args);
        CommandLine.ansi = null; // back to platform default
        AnsiConsole.systemUninstall();
    }
}
