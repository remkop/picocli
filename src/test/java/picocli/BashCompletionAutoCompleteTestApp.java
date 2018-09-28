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

import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.List;
import java.util.concurrent.Callable;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.Model.CommandSpec;

@Command(name = "foo", description = "This is a foo")
class Foo {
    @Option(names = {"-f", "--foo1"}, description = "Enable foo1.")
    private boolean foo1 = false;

    @Parameters(arity = "1", paramLabel = "HOST_ONE", description = "SRC")
    private InetAddress src;

    @Parameters(arity = "1", paramLabel = "HOST_TWO", description = "DEST")
    private InetAddress dest;

    Foo() {

    }
}

enum BarEnum {
    foo,
    bar,
    baz
}

@Command(name = "bar", description = "This is bar")
class Bar {
    @Option(names = {"-b", "--bar1"}, description = "Enable bar1.")
    private boolean bar1 = false;

    @Parameters(arity = "2", paramLabel = "BAR", description = "Two bars.")
    private BarEnum bars;

    Bar() {

    }
}

@Command(abbreviateSynopsis = true, description = "Test app.", mixinStandardHelpOptions = true, name = "test-app", showDefaultValues = true, sortOptions = true, subcommands = {Foo.class, Bar.class})
public class BashCompletionAutoCompleteTestApp implements Callable<Void> {
    @Spec
    private CommandSpec commandSpec;

    @Option(names = {"-i", "--interface"}, paramLabel = "INTERFACE", description = "Set network interface.")
    private NetworkInterface networkInterface;

    @Parameters(arity = "1..*", paramLabel = "DIR", description = "One or more directories.")
    private List<File> files;

    public BashCompletionAutoCompleteTestApp() {

    }

    public static void main(String... args) {
        BashCompletionAutoCompleteTestApp app = new BashCompletionAutoCompleteTestApp();

        try {
            Ansi ansi = System.getProperty("picocli.ansi") == null ? Ansi.AUTO : (Boolean.getBoolean("picocli.ansi") ? Ansi.ON : Ansi.OFF);
            CommandLine.call(app, System.out, ansi, args);
            System.exit(0);
        } catch (picocli.CommandLine.ExecutionException e) {
            System.exit(1);
        }
    }

    public Void call() throws Exception {
        return null;
    }
}
