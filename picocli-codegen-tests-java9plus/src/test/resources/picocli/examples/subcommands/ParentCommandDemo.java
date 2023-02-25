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

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

import java.io.File;

public class ParentCommandDemo {

    @Command(name = "fileutils", subcommands = List.class)
    static class FileUtils implements Runnable {

        @Option(names = {"-d", "--directory"}, description = "this option applies to all subcommands")
        private File baseDirectory;

        public void run() { System.out.println("FileUtils: my dir is " + baseDirectory); }
    }

    @Command(name = "list")
    static class List implements Runnable {

        @ParentCommand
        private FileUtils parent;

        @Option(names = {"-r", "--recursive"}, description = "Recursively list subdirectories")
        private boolean recursive;

        public void run() {
            list(new File(parent.baseDirectory, ""));
        }

        private void list(File dir) {
            System.out.println(dir.getAbsolutePath());
            if (dir.isDirectory()) {
                for (File f : dir.listFiles()) {
                    if (f.isDirectory() && recursive) {
                        list(f);
                    } else {
                        System.out.println(f.getAbsolutePath());
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        new CommandLine(new FileUtils()).execute("--directory=examples/src", "list", "-r");
    }
}
