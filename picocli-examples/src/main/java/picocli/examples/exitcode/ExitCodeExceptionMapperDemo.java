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
import picocli.CommandLine.IExitCodeExceptionMapper;

@Command
public class ExitCodeExceptionMapperDemo implements Runnable {
    public void run() {
        throw new IndexOutOfBoundsException("abc");
    }

    public static void main(String... args) {

        IExitCodeExceptionMapper mapper = new IExitCodeExceptionMapper() {
            @Override
            public int getExitCode(Throwable t) {
                if (t instanceof IndexOutOfBoundsException) {
                    return 987;
                }
                return 123;
            }
        };

        int exitCode = new CommandLine(new ExitCodeExceptionMapperDemo())
                .setExitCodeExceptionMapper(mapper)
                .execute(args);
        assert exitCode == 987;
        System.exit(exitCode);
    }
}
