# Picocli Legacy Tests

This project runs the tests for the main `picocli` project in a Java 5 environment.

While the directory structure may suggest that this is a subproject of the `picocli` project, it is actually a standalone project, not a module.

This project does not publish any artifacts.

Note to self: do the following before running this build:

```
: start command prompt if we are running in Powershell
cmd

: ensure we are not using the OneDrive user home directory
set JAVA_OPTS=-Duser.home=C:\Users\remko\.m2\repository

: build the project once with Java 8 so that all dependencies are downloaded and cached
cd ..
gradlew assemble

: now build the project with Java 5, 6 and 7
cd picocli-legacy-tests
set JAVA_HOME=C:\apps\jdk1.5.0_22
gradlew clean build
set JAVA_HOME=C:\apps\jdk1.6.0_45
gradlew clean build
```
