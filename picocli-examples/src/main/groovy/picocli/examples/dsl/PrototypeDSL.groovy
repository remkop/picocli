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
package picocli.examples.dsl

import picocli.CommandLine
import picocli.CommandLine.Model.CommandSpec
import picocli.CommandLine.Model.OptionSpec
import picocli.CommandLine.Model.PositionalParamSpec

class PrototypeDSL {
    static void main(String[] args) {

        // The factory method takes key-value pairs for the CommandSpec itself,
        // or its UsageMessageSpec or ParserSpec
        CliBuilder2 cli = CliBuilder2.create(headerHeading: "test header heading%n", footer: ["test footer"]) {
            // CommandSpec attributes
            name  "dsl-command"
            version  ("DSL v1.0-alpha1", "Second line")

            // UsageMessageSpec attributes
            usage {
                header ("this is a header", "header line 2")
                description "command description"
                showDefaultValues true
            }

            // OptionSpec attributes on a single line
            option("-h", "--help") { usageHelp true; type boolean; description "display this help and exit" }

            // OptionSpec attributes on multiple lines
            option ("-V", "--version") {
                versionHelp true
                type  boolean
                description "display version info and exit"
            }
            option { // names in named attribute
                names ("-c", "--count") type int description "Number of times to repeat"
            }
            positional {index "0..*"; type int; description "a bunch of integers"; paramLabel "INT"}
            addSubcommand("sub", create {
                usage {description("subcommand description")}
                option("-x") {
                    description("option X description")
                }
            }.commandSpec)
        }

        // adding an option outside the closure
        cli.option {
            names "-o", "--option"
            type List
            auxiliaryTypes String
            description "list of options"
        }

        new CommandLine(cli.commandSpec).usage(System.out)
        println()
        cli.commandSpec.subcommands().sub.usage(System.out)
    }

    // prototype cli builder
    static class CliBuilder2 {
        private @Delegate CommandSpec commandSpec

        // no user object needed when only using programmatic API
        CliBuilder2() { this.commandSpec = CommandSpec.create()}

        // Alternatively, automatically build (partial) model from annotations.
        // This impl uses picocli annotations, but CliBuilder2 could use Groovy cli annotations instead.
        CliBuilder2(userObject) { this.commandSpec = CommandSpec.forAnnotatedObject(userObject)}

        static CliBuilder2 create(@DelegatesTo(CliBuilder2) Closure c) {
            CliBuilder2 spec = new CliBuilder2()
            spec.with c
            spec
        }

        static CliBuilder2 create(Map<String, Object> map, @DelegatesTo(CliBuilder2) Closure c) {
            def invokeOn = {}
            CliBuilder2 cli = new CliBuilder2()

            // There is probably a cleaner way to implement this...
            // The intention is that key-value pairs are applied to either the CommandSpec itself,
            // its UsageMessageSpec or its ParserSpec.
            // TODO fail with better error message for unknown keys.
            map.each {key, value ->
                if (cli.commandSpec.usageMessage().hasProperty(key)) {
                    cli.commandSpec.usageMessage()[key] = value
                    return
                } else if (CommandLine.Model.UsageMessageSpec.declaredMethods.find {m -> m.name == key} != null) {
                    if (value instanceof List) {
                        cli.commandSpec.invokeMethod(key, (value as List).toArray())
                    } else {
                        cli.commandSpec.invokeMethod(key, value)
                    }
                    return
                }
                if (cli.commandSpec.hasProperty(key)) {
                    cli.commandSpec[key] = value
                } else {
                    if (value instanceof List) {
                        cli.commandSpec.invokeMethod(key, (value as List).toArray())
                    } else {
                        cli.commandSpec.invokeMethod(key, value)
                    }
                }
            }
            cli.with c
            cli
        }

        void option(String names, @DelegatesTo(OptionSpec.Builder) Closure details) { option(details, names) }
        void option(String name1, String name2, @DelegatesTo(OptionSpec.Builder) Closure details) { option(details, name1, name2) }
        void option(String name1, String name2, String name3, @DelegatesTo(OptionSpec.Builder) Closure details) { option(details, name1, name2, name3) }
        void option(@DelegatesTo(OptionSpec.Builder) Closure details, String... names) {
            OptionSpec optionSpec = withOption(details, names)
            addOption(optionSpec)
        }
        static OptionSpec withOption(@DelegatesTo(OptionSpec.Builder) Closure details, String... names) {
            def optionSpec = OptionSpec.builder(names)
            def code = details.rehydrate(optionSpec, this, this)
            code.resolveStrategy = Closure.DELEGATE_ONLY
            code()
            optionSpec.build()
        }
        void positional(@DelegatesTo(PositionalParamSpec.Builder) Closure details) {
            def positionalSpec = PositionalParamSpec.builder()
            def code = details.rehydrate(positionalSpec, this, this)
            code.resolveStrategy = Closure.DELEGATE_ONLY
            code()
            addPositional positionalSpec.build()
        }
        void usage(@DelegatesTo(CommandLine.Model.UsageMessageSpec) Closure details) {
            def code = details.rehydrate(commandSpec.usageMessage(), this, this)
            code.resolveStrategy = Closure.DELEGATE_ONLY
            code()
        }
    }
}
