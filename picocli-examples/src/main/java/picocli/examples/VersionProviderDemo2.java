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

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Demonstrates a {@link IVersionProvider} implementation that reads version information from the picocli-x.x.jar
 * file's {@code /META-INF/MANIFEST.MF} file.
 */
@Command(versionProvider = VersionProviderDemo2.ManifestVersionProvider.class)
public class VersionProviderDemo2 implements Runnable {

    @Option(names = {"-V", "--version"}, versionHelp = true, description = "Print version info from the picocli-x.x.jar file's /META-INF/MANIFEST.MF and exit")
    boolean versionRequested;

    public void run() {
        CommandLine.usage(this, System.out);
    }

    public static void main(String[] args) {
        new CommandLine(new VersionProviderDemo2()).execute(args);
    }

    /**
     * {@link IVersionProvider} implementation that returns version information from the picocli-x.x.jar file's {@code /META-INF/MANIFEST.MF} file.
     */
    static class ManifestVersionProvider implements IVersionProvider {
        public String[] getVersion() throws Exception {
            Enumeration<URL> resources = CommandLine.class.getClassLoader().getResources("META-INF/MANIFEST.MF");
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                try {
                    Manifest manifest = new Manifest(url.openStream());
                    if (isApplicableManifest(manifest)) {
                        Attributes attr = manifest.getMainAttributes();
                        return new String[] { get(attr, "Implementation-Title") + " version \"" +
                                get(attr, "Implementation-Version") + "\"" };
                    }
                } catch (IOException ex) {
                    return new String[] { "Unable to read from " + url + ": " + ex };
                }
            }
            return new String[0];
        }

        private boolean isApplicableManifest(Manifest manifest) {
            Attributes attributes = manifest.getMainAttributes();
            return "picocli".equals(get(attributes, "Implementation-Title"));
        }

        private static Object get(Attributes attributes, String key) {
            return attributes.get(new Attributes.Name(key));
        }
    }
}
