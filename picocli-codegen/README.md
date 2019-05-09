<p align="center"><img src="https://picocli.info/images/logo/horizontal-400x150.png" alt="picocli" height="150px"></p>


# Picocli Code Generation

Picocli Code Generation contains tools for generating source code, documentation and configuration files 
for picocli-based applications.


## Tools for Configuring GraalVM Native Image Builds

The `picocli-codegen` module contains the following tools to assist with AOT compilation to GraalVM native image builds:

* ReflectionConfigGenerator
* ResourceConfigGenerator
* DynamicProxyConfigGenerator

The generated configuration files can be supplied to the `native-image` tool via command line options like `-H:ReflectionConfigurationFiles=/path/to/reflect-config.json`,
or alternatively by placing them in a `META-INF/native-image/` directory on the class path, for example, in a JAR file used in the image build.
This directory (or any of its subdirectories) is searched for files with the names `reflect-config.json`, `proxy-config.json` and `resource-config.json`,
which are then automatically included in the build. Not all of those files must be present.
When multiple files with the same name are found, all of them are included.

See also the SubstrateVM [configuration documentation](https://github.com/oracle/graal/blob/master/substratevm/CONFIGURE.md).

### ReflectionConfigGenerator

GraalVM has <a href="https://github.com/oracle/graal/blob/master/substratevm/REFLECTION.md">limited support for Java
reflection</a> and it needs to know ahead of time the reflectively accessed program elements.

`ReflectionConfigGenerator` generates a JSON String with the program elements that will be accessed reflectively in a picocli-based application, in order to compile this application ahead-of-time into a native executable with GraalVM.

The output of `ReflectionConfigGenerator` is intended to be passed to the `-H:ReflectionConfigurationFiles=/path/to/reflect-config.json` option of the `native-image` GraalVM utility,
or placed in a `META-INF/native-image/` subdirectory of the JAR. 

This allows picocli-based applications to be compiled to a native image.

See [Picocli on GraalVM: Blazingly Fast Command Line Apps](https://github.com/remkop/picocli/wiki/Picocli-on-GraalVM:-Blazingly-Fast-Command-Line-Apps) for details.

#### Generating Reflection Configuration During the Build

The `--output` option can be used to specify the path of the file to write the configuration to.
When this option is omitted, the output is sent to standard out.
 
The `ReflectionConfigGenerator` tool accepts any number of fully qualified class names of command classes
(classes with picocli annotations like `@Command`, `@Option` and `@Parameters`).
The resulting configuration file will contain entries for the reflected elements of all specified classes.

##### Maven

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
          <argument>--output=target/reflect-config.json</argument>
          <argument>com.your.package.YourCommand1</argument>
          <argument>com.your.package.YourCommand2</argument>
        </arguments>
      </configuration>
      <dependencies>
        <dependency>
          <groupId>info.picocli</groupId>
          <artifactId>picocli-codegen</artifactId>
          <version>4.0.0-alpha-2</version>
          <type>jar</type>
        </dependency>
      </dependencies>
    </plugin>
  </plugins>
</build>
```

##### Gradle

For Gradle, add a custom configuration for the `picocli-codegen` module to your `gradle.build`.
This allows us to add this module to the classpath of our custom task without adding it as a dependency to the "standard" build.

```gradle
configurations {
    generateConfig
}
dependencies {
    compile 'info.picocli:picocli:4.0.0-alpha-2'
    generateConfig 'info.picocli:picocli-codegen:4.0.0-alpha-2'
}
```

Then, add a custom task to run the `ReflectionConfigGenerator` tool.
This example generates the file during the `assemble` lifecycle task, there are [alternatives](https://docs.gradle.org/current/userguide/java_plugin.html#sec:java_tasks).

```gradle
task(generateGraalReflectionConfig, dependsOn: 'classes', type: JavaExec) {
    main = 'picocli.codegen.aot.graalvm.ReflectionConfigGenerator'
    classpath = configurations.generateConfig + sourceSets.main.runtimeClasspath
    def outputFile = "${buildDir}/resources/main/META-INF/native-image/${project.group}/${project.name}/reflect-config.json"
    args = ["--output=$outputFile", 'com.your.package.YourCommand1', 'com.your.package.YourCommand2']
}
assemble.dependsOn generateGraalReflectionConfig
```


### ResourceConfigGenerator

The GraalVM native-image builder by default will not integrate any of the
<a href="https://github.com/oracle/graal/blob/master/substratevm/RESOURCES.md">classpath resources</a> into the image it creates.

`ResourceConfigGenerator` generates a JSON String with the resource bundles and other classpath resources
that should be included in the Substrate VM native image.

The output of `ResourceConfigGenerator` is intended to be passed to the `-H:ResourceConfigurationFiles=/path/to/reflect-config.json` option of the `native-image` GraalVM utility,
or placed in a `META-INF/native-image/` subdirectory of the JAR. 

This allows picocli-based native image applications to access these resources.

#### Generating Resource Configuration During the Build

The `--output` option can be used to specify the path of the file to write the configuration to.
When this option is omitted, the output is sent to standard out.
 
The `ResourceConfigGenerator` tool accepts any number of fully qualified class names of command classes
(classes with picocli annotations like `@Command`, `@Option` and `@Parameters`).
The resulting configuration file will contain entries for the resource bundles used in any of the specified commands or their subcommands.

The `--bundle` option can be used to specify the base name of additional resource bundle(s) to be included in the image.

The `--pattern` option can be used to specify Java regular expressions that match additional resource(s) to be included in the image.


##### Maven

For Maven, add an `exec:java` goal to generate a Graal resource configuration file with the `ResourceConfigGenerator` tool.
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
          <id>generateGraalResourceConfig</id>
          <phase>process-classes</phase>
          <goals>
            <goal>java</goal>
          </goals>
        </execution>
      </executions>
      <configuration>
        <includeProjectDependencies>true</includeProjectDependencies>
        <includePluginDependencies>true</includePluginDependencies>
        <mainClass>picocli.codegen.aot.graalvm.ResourceConfigGenerator</mainClass>
        <arguments>
          <argument>--output=target/resource-config.json</argument>
          <argument>com.your.package.YourCommand1</argument>
          <argument>com.your.package.YourCommand2</argument>
        </arguments>
      </configuration>
      <dependencies>
        <dependency>
          <groupId>info.picocli</groupId>
          <artifactId>picocli-codegen</artifactId>
          <version>4.0.0-alpha-2</version>
          <type>jar</type>
        </dependency>
      </dependencies>
    </plugin>
  </plugins>
</build>
```

##### Gradle

For Gradle, add a custom configuration for the `picocli-codegen` module to your `gradle.build`.
This allows us to add this module to the classpath of our custom task without adding it as a dependency to the "standard" build.

```gradle
configurations {
    generateConfig
}
dependencies {
    compile 'info.picocli:picocli:4.0.0-alpha-2'
    generateConfig 'info.picocli:picocli-codegen:4.0.0-alpha-2'
}
```

Then, add a custom task to run the `ResourceConfigGenerator` tool.
This example generates the file during the `assemble` lifecycle task, there are [alternatives](https://docs.gradle.org/current/userguide/java_plugin.html#sec:java_tasks).

```gradle
task(generateGraalResourceConfig, dependsOn: 'classes', type: JavaExec) {
    main = 'picocli.codegen.aot.graalvm.ResourceConfigGenerator'
    classpath = configurations.generateConfig + sourceSets.main.runtimeClasspath
    def outputFile = "${buildDir}/resources/main/META-INF/native-image/${project.group}/${project.name}/resource-config.json"
    args = ["--output=$outputFile", 'com.your.package.YourCommand1', 'com.your.package.YourCommand2']
}
assemble.dependsOn generateGraalResourceConfig
```

### DynamicProxyConfigGenerator

Substrate VM doesn't provide machinery for generating and interpreting bytecodes at run time. Therefore all dynamic proxy classes 
<a href="https://github.com/oracle/graal/blob/master/substratevm/DYNAMIC_PROXY.md">need to be generated</a> at native image build time.

`DynamicProxyConfigGenerator` generates a JSON String with the fully qualified interface names for which
dynamic proxy classes should be generated at native image build time.

The output of `DynamicProxyConfigGenerator` is intended to be passed to the `-H:DynamicProxyConfigurationFiles=/path/to/proxy-config.json` option of the `native-image` GraalVM utility,
or placed in a `META-INF/native-image/` subdirectory of the JAR.

This allows picocli-based native image applications that use `@Command`-annotated interfaces with
`@Option` and `@Parameters`-annotated methods to define options and positional parameters.

#### Generating Dynamic Proxy Configuration During the Build

The `--output` option can be used to specify the path of the file to write the configuration to.
When this option is omitted, the output is sent to standard out.
 
The `DynamicProxyConfigGenerator` tool accepts any number of fully qualified class names of command classes
(classes with picocli annotations like `@Command`, `@Option` and `@Parameters`).
The resulting configuration file will contain entries for the resource bundles used in any of the specified commands or their subcommands.

The `--interface` option can be used to specify the fully qualified class names of additional interfaces to generate dynamic proxy classes for in the native image.


##### Maven

For Maven, add an `exec:java` goal to generate a Graal proxy configuration file with the `DynamicProxyConfigGenerator` tool.
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
          <id>generateGraalDynamicProxyConfig</id>
          <phase>process-classes</phase>
          <goals>
            <goal>java</goal>
          </goals>
        </execution>
      </executions>
      <configuration>
        <includeProjectDependencies>true</includeProjectDependencies>
        <includePluginDependencies>true</includePluginDependencies>
        <mainClass>picocli.codegen.aot.graalvm.DynamicProxyConfigGenerator</mainClass>
        <arguments>
          <argument>--output=target/proxy-config.json</argument>
          <argument>com.your.package.YourCommand1</argument>
          <argument>com.your.package.YourCommand2</argument>
        </arguments>
      </configuration>
      <dependencies>
        <dependency>
          <groupId>info.picocli</groupId>
          <artifactId>picocli-codegen</artifactId>
          <version>4.0.0-alpha-2</version>
          <type>jar</type>
        </dependency>
      </dependencies>
    </plugin>
  </plugins>
</build>
```

##### Gradle

For Gradle, add a custom configuration for the `picocli-codegen` module to your `gradle.build`.
This allows us to add this module to the classpath of our custom task without adding it as a dependency to the "standard" build.

```gradle
configurations {
    generateConfig
}
dependencies {
    compile 'info.picocli:picocli:4.0.0-alpha-2'
    generateConfig 'info.picocli:picocli-codegen:4.0.0-alpha-2'
}
```

Then, add a custom task to run the `DynamicProxyConfigGenerator` tool.
This example generates the file during the `assemble` lifecycle task, there are [alternatives](https://docs.gradle.org/current/userguide/java_plugin.html#sec:java_tasks).

```gradle
task(generateGraalDynamicProxyConfig, dependsOn: 'classes', type: JavaExec) {
    main = 'picocli.codegen.aot.graalvm.DynamicProxyConfigGenerator'
    classpath = configurations.generateConfig + sourceSets.main.runtimeClasspath
    def outputFile = "${buildDir}/resources/main/META-INF/native-image/${project.group}/${project.name}/proxy-config.json"
    args = ["--output=$outputFile", 'com.your.package.YourCommand1', 'com.your.package.YourCommand2']
}
assemble.dependsOn generateGraalDynamicProxyConfig
```
