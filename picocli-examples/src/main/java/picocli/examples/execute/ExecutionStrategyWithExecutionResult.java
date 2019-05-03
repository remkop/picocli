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
package picocli.examples.execute;

import picocli.CommandLine;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.IExecutionStrategy;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.OptionSpec;
import picocli.CommandLine.Model.PositionalParamSpec;
import picocli.CommandLine.ParseResult;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class ExecutionStrategyWithExecutionResult {
    public static void main(final String[] args) {

        CommandSpec spec = CommandSpec.create();
        spec.mixinStandardHelpOptions(true);
        spec.addOption(OptionSpec.builder("-c", "--count")
                .paramLabel("COUNT")
                .type(int.class)
                .description("number of times to execute").build());
        spec.addPositional(PositionalParamSpec.builder()
                .paramLabel("FILES")
                .type(List.class)
                .auxiliaryTypes(File.class)
                .description("The files to process").build());
        CommandLine commandLine = new CommandLine(spec);

        class Handler implements IExecutionStrategy {
            @Override
            public int execute(ParseResult pr) {
                int count = pr.matchedOptionValue('c', 1);
                List<File> files = pr.matchedPositionalValue(0, Collections.<File>emptyList());
                for (File f : files) {
                    for (int i = 0; i < count; i++) {
                        System.out.println(i + " " + f.getName());
                    }
                }
                pr.commandSpec().commandLine().setExecutionResult(files.size());
                return ExitCode.OK;
            }
        }

        commandLine.setExecutionStrategy(new Handler());
        commandLine.execute(args);
        int processed = commandLine.getExecutionResult();
    }
}
