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
package picocli.examples.exitcode;

import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

@Command
public class CustomExitCodeCallable implements Callable<Integer> {

    @Override
    public Integer call() {
        return 123;
    }

    public static void main(String... args) {
        int exitCode = new CommandLine(new CustomExitCodeCallable()).execute(args);
        assert exitCode == 123;
        System.exit(exitCode);
    }
}
