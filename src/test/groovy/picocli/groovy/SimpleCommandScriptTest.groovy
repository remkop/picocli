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
 * @author Jim White
 * @author Remko Popma
 * @since 2.0
 */

@groovy.transform.BaseScript(PicocliBaseScript)
import groovy.transform.Field
import picocli.CommandLine


@CommandLine.Parameters
@Field List<String> parameters

@CommandLine.Option(names = ["-cp", "--codepath"])
@Field List<String> codepath = []

//// Override the default of using the 'args' binding for our test.
//String[] getScriptArguments() {
//   [ "--codepath", "/usr/x.jar", "placeholder", "-cp", "/bin/y.jar", "another" ] as String[]
//}

println parameters

println codepath

assert parameters == ['placeholder', 'another']
assert codepath == ['/usr/x.jar', '/bin/y.jar']

[777]

