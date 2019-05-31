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
 * Test PicocliBaseScript's multiple command feature in a simple Script.
 * More tests are embedded in PicocliBaseScriptTest strings.
 *
 * @author Jim White
 * @author Remko Popma
 */
import groovy.transform.BaseScript
import groovy.transform.Field
import picocli.CommandLine

import java.util.concurrent.Callable

@CommandLine.Command(name = "git", subcommands = [picocli.groovy.CommandCommit, picocli.groovy.CommandAdd])
@PicocliScript PicocliBaseScript thisScript


@CommandLine.Option(names = ["-log", "-verbose" ], description = "Level of verbosity")
@Field Integer verbose = 1;

@CommandLine.Command(name = "commit", description = "Record changes to the repository")
class CommandCommit implements Runnable {
    @CommandLine.Parameters(description = "The list of files to commit")
    private List<String> files;

    @CommandLine.Option(names = "--amend", description = "Amend")
    private Boolean amend = false;

    @CommandLine.Option(names = "--author")
    private String author;

    @Override
    void run() {
        assert amend
        assert author == "Remko"
        assert files.contains("MultipleCommandScriptTest.groovy")
    }
}

@CommandLine.Command(name = "add", separator = "=", description = "Add file contents to the index")
public class CommandAdd implements Callable<Object> {
    @CommandLine.Parameters(description = "File patterns to add to the index")
    List<String> patterns;

    @CommandLine.Option(names = "-i")
    Boolean interactive = false;

    @Override
    Object call() throws Exception { // called from
        assert interactive
        assert patterns.contains("zoos")
        return null
    }
}

@Field List<CommandLine> parsed;
public List<CommandLine> parseScriptArguments(CommandLine commandLine, String[] args) {
    parsed = commandLine.parse(args);
    return parsed
}

assert verbose == 2
assert 2 == parsed.size()

result = []
parsed.each {
    if (it.command instanceof CommandAdd) {
        CommandAdd addCommand = it.command
        result = addCommand.patterns
    } else if (it.command instanceof CommandCommit) {
        CommandCommit commitCommand = it.command
        result = commitCommand.files
    }
}

result