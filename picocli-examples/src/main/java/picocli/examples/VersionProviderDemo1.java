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
package picocli.examples;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.IVersionProvider;
import picocli.CommandLine.Option;

import java.net.URL;
import java.util.Properties;

/**
 * Demonstrates a {@link IVersionProvider} implementation that reads version information from a
 * {@code /version.txt} file that is expected to be in the root of the classpath.
 * <p>
 * The following gradle build snippet can be used to generate such a version.txt file and include it in the generated jar:
 * </p>
 * <pre>
 * def generatedResources = "$buildDir/generated-resources/main"
 * sourceSets {
 *     main {
 *         //register an output folder on the main SourceSet:
 *         output.dir(generatedResources, builtBy: 'generateVersionTxt')
 *         //it is now a part of the 'main' classpath and will be a part of the jar
 *     }
 * }
 *
 * //a task that generates the resources:
 * task generateVersionTxt {
 *     description 'Creates a version.txt file with build info that is added to the root of the jar'
 *     doLast {
 *         new File(generatedResources).mkdirs()
 *         def generated = new File(generatedResources, "version.txt")
 *         generated.text = """
 *         Version: $rootProject.version
 *         Buildtime: ${new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())}
 *         Application-name: $rootProject.name $project.name
 *         """
 *     }
 * }
 * </pre>
 */
@Command(versionProvider = VersionProviderDemo1.PropertiesVersionProvider.class)
public class VersionProviderDemo1 implements Runnable {

    @Option(names = {"-V", "--version"}, versionHelp = true, description = "Print version info from examples.jar/version.txt and exit")
    boolean versionRequested;

    public void run() {
        CommandLine.usage(this, System.out);
    }

    public static void main(String[] args) {
        new CommandLine(new VersionProviderDemo1()).execute(args);
    }

    /**
     * {@link IVersionProvider} implementation that returns version information from a {@code /version.txt} file in the classpath.
     */
    static class PropertiesVersionProvider implements IVersionProvider {
        public String[] getVersion() throws Exception {
            URL url = getClass().getResource("/version.txt");
            if (url == null) {
                return new String[] {"No version.txt file found in the classpath. Is examples.jar in the classpath?"};
            }
            Properties properties = new Properties();
            properties.load(url.openStream());
            return new String[] {
                    properties.getProperty("Application-name") + " version \"" + properties.getProperty("Version") + "\"",
                    "Built: " + properties.getProperty("Buildtime"),
            };
        }
    }
}
