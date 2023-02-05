# Picocli Legacy Tests

This project builds the main `picocli` project and runs its tests in a Java 5 environment.

## This project is for testing only
The build for this project compiles the Java source files in the `../src/java` and `../test/java` folders of the parent project, and then runs the tests.
While the directory structure may suggest that this is a subproject of the `picocli` project, it is actually a standalone project, and not included as a module in the main build.

This project does not publish any artifacts.

## Gradle version
This project uses an old version of Gradle (1.12); I believe this is the most recent version of Gradle that runs on Java 5. (Later versions require at least Java 6.)

## HTTPS problems on Java 5
I was unable to automate downloading the Gradle 1.12 binary via HTTPS when running the Gradle Wrapper on Java 5; there seemed to be some issue with TLS versions; I could not get HTTPS to work on Java 5.

A similar issue exists with test dependencies: while picocli itself does not have any dependencies, the test classes do have some dependencies.
Maven Central and other repositories require downloads to use HTTPS and disallow plain HTTP.
I have not been able to use HTTPS when running on Java 5.
This causes the build to fail; Gradle reports errors when trying to download test dependencies and aborts the build.

## Binaries included in project
To work around these problems, I ended up including some binaries in this project:

* the Gradle Wrapper included in this project also has a copy of the Gradle 1.12 binary distribution
* the test dependency binaries are included in the `lib` directory of this project

I realize this is uncouth, inelegant and a generally barbarous thing to do. I am a sinner, please forgive me.

## Usage
This project assumes that you have Java 5 installed.

If you have multiple versions of Java installed, then before running this build, set the `JAVA_HOME` environment variable to the directory where Java 5 is installed:

```
: (on Windows)
: start command prompt if we are running in Powershell
cmd
cd picocli-tests-java567

: now build the project with Java 5, 6 and 7
set JAVA_HOME=C:\apps\jdk1.5.0_22
gradlew clean build --no-daemon

set JAVA_HOME=C:\apps\jdk1.6.0_45
gradlew clean build --no-daemon

set JAVA_HOME=C:\apps\jdk1.7.0_80
gradlew clean build --no-daemon
```

## Usage from CI Continuous Integration builds

See the `../.github/workflows/ci.yml` workflow for a concrete example, specifically the `build-java-6-7` section.
