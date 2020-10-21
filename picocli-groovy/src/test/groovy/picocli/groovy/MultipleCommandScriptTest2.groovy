/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package picocli.groovy

/**
 * Test PicocliBaseScript2's multiple command feature in a simple Script.
 * More tests are embedded in PicocliBaseScriptTest strings.
 *
 * @author Remko Popma
 */
import groovy.transform.Field
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters
import picocli.CommandLine.ParentCommand

import java.util.concurrent.Callable

@Command(name = "git", subcommands = [picocli.groovy.CommandCommit2, picocli.groovy.CommandAdd2])
@PicocliScript2 PicocliBaseScript2 thisScript


@Option(names = ["-log", "-verbose" ], description = "Level of verbosity")
@Field Integer verbose = 1;

@Command(name = "commit", description = "Record changes to the repository")
class CommandCommit2 implements Runnable {
    @ParentCommand
    MultipleCommandScriptTest2 parent

    @Parameters(description = "The list of files to commit")
    private List<String> files;

    @Option(names = "--amend", description = "Amend")
    private Boolean amend = false;

    @Option(names = "--author")
    private String author;

    @Override
    void run() {
        assert parent.verbose == 2
        assert amend
        assert author == "Remko"
        assert files.contains("MultipleCommandScriptTest2.groovy")
    }
}

@Command(name = "add", separator = "=", description = "Add file contents to the index")
public class CommandAdd2 implements Callable<Object> {
    @Parameters(description = "File patterns to add to the index")
    List<String> patterns;

    @Option(names = "-i")
    Boolean interactive = false;

    @Override
    Object call() throws Exception { // called from
        assert interactive
        assert patterns.contains("zoos")
        return patterns
    }
}

throw new IllegalStateException("never reached")
