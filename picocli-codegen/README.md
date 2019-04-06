<p align="center"><img src="https://picocli.info/images/logo/horizontal-400x150.png" alt="picocli" height="150px"></p>


# Picocli Code Generation

Picocli Code Generation contains tools for generating source code, documentation and configuration files 
for picocli-based applications.


## ReflectionConfigGenerator Tool for AOT Compilation to Native Image on GraalVM

`ReflectionConfigGenerator` generates a JSON String with the program elements that will be accessed reflectively in a picocli-based application, in order to compile this application ahead-of-time into a native executable with GraalVM.

The output of `ReflectionConfigGenerator` is intended to be passed to the `-H:ReflectionConfigurationFiles=/path/to/reflectconfig` option of the `native-image` GraalVM utility. This allows picocli-based applications to be compiled to a native image.

See [Picocli on GraalVM: Blazingly Fast Command Line Apps](https://github.com/remkop/picocli/wiki/Picocli-on-GraalVM:-Blazingly-Fast-Command-Line-Apps) for details.

### Generating GraalVM Reflection Configuration During the Build

Below shows some examples of configuring your build to generate a GraalVM reflection configuration file with the `ReflectionConfigGenerator` tool during the build.

Note that the `--output` option allows you to specify the path to the file to write the configuration to.
When this option is omitted, the output is sent to standard out.
 
The `ReflectionConfigGenerator` tool accepts any number of fully qualified class names of command classes
(classes with picocli annotations like `@Command`, `@Option` and `@Parameters`).
The resulting configuration file will contain entries for the reflected elements of all specified classes.

#### Maven

For Maven, add an `exec:java` goal to generate a Graal reflection configuration file with the `ReflectionConfigGenerator` tool.
This example uses the `process-classes` phase of the build, there are [alternatives](http://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html).

Note that the `picocli-codegen` module is only added as a dependency for the `exec` plugin, so it does not need to be added to the project dependencies.

```xml
<build>
  <plugins>
    <plugin>
      <groupId>org.codehaus.mojo</groupId>
      <artifactId>exec-maven-plugin</artifactId>
      <version>1.6.0</version>
      <executions>
        <execution>
          <id>generateGraalReflectionConfig</id>
          <phase>process-classes</phase>
          <goals>
            <goal>java</goal>
          </goals>
        </execution>
      </executions>
      <configuration>
        <includeProjectDependencies>true</includeProjectDependencies>
        <includePluginDependencies>true</includePluginDependencies>
        <mainClass>picocli.codegen.aot.graalvm.ReflectionConfigGenerator</mainClass>
        <arguments>
          <argument>--output=target/cli-reflect.json</argument>
          <argument>com.your.package.YourCommand1</argument>
          <argument>com.your.package.YourCommand2</argument>
        </arguments>
      </configuration>
      <dependencies>
        <dependency>
          <groupId>info.picocli</groupId>
          <artifactId>picocli-codegen</artifactId>
          <version>3.9.6</version>
          <type>jar</type>
        </dependency>
      </dependencies>
    </plugin>
  </plugins>
</build>
```

#### Gradle

For Gradle, add a custom configuration for the `picocli-codegen` module to your `gradle.build`.
This allows us to add this module to the classpath of our custom task without adding it as a dependency to the "standard" build.

```gradle
configurations {
    generateConfig
}
dependencies {
    compile 'info.picocli:picocli:3.9.6'
    generateConfig 'info.picocli:picocli-codegen:3.9.6'
}
```

Then, add a custom task to run the `ReflectionConfigGenerator` tool.
This example generates the file during the `assemble` lifecycle task, there are [alternatives](https://docs.gradle.org/current/userguide/java_plugin.html#sec:java_tasks).

```gradle
task(generateGraalReflectionConfig, dependsOn: 'classes', type: JavaExec) {
    main = 'picocli.codegen.aot.graalvm.ReflectionConfigGenerator'
    classpath = configurations.generateConfig + sourceSets.main.runtimeClasspath
    def outputFile = new File(project.buildDir, 'cli-reflect.json')
    args = ["--output=$outputFile", 'com.your.package.YourCommand1', 'com.your.package.YourCommand2']
}
assemble.dependsOn generateGraalReflectionConfig
```