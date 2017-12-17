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
package picocli.examples.subcommands;

import java.io.File;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

public class ParentCommandDemo {

    @Command(name = "top", subcommands = Sub.class)
    static class Top implements Runnable {

        @Option(names = {"-d", "--directory"}, description = "this option applies to all subcommands")
        private File baseDirectory;

        public void run() { System.out.println("Hello from top"); }
    }

    @Command(name = "sub")
    static class Sub implements Runnable {

        @ParentCommand
        private Top parent;

        @Parameters(description = "The number of times to print the result")
        private int count;

        @Override
        public void run() {
            for (int i = 0; i < count; i++) {
                System.out.println("Subcommand: parent command 'directory' is " + parent.baseDirectory);
            }
        }
    }

    public static void main(String[] args) {
        CommandLine.run(new Top(), System.out, "--directory=/tmp/parentCommandDemo", "sub", "3");
    }
}
