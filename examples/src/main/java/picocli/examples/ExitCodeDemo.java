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
import picocli.CommandLine.Command;
import picocli.CommandLine.ParameterException;

import java.util.Collections;
import java.util.List;

@Command
public class ExitCodeDemo implements Runnable {
    public void run() { throw new ParameterException(new CommandLine(this), "exit code demo"); }

    public static void main(String... args) {
        CommandLine cmd = new CommandLine(new ExitCodeDemo());
        cmd.parseWithHandlers(
                new CommandLine.RunLast().andExit(123),
                CommandLine.defaultExceptionHandler().andExit(456),
                args);
    }
}
