# Contributing to picocli

:+1::star_struck: Wow, thanks for taking the time to contribute! :tada::+1:

The following is a set of guidelines for contributing to [picocli](https://github.com/remkop/picocli). These are mostly guidelines, not rules. Use your best judgment, and feel free to propose changes to this document in a pull request.

## <a name="questions"></a> It is okay to raise an issue to ask a question
If you have a question about picocli, feel free to raise an issue for it.

## <a name="helpful"></a> Helpful problem reports look like this
A helpful issue (problem report) is one that saves the maintainer time.
Try to include these elements:

* How to reproduce the problem
* What is the expected behaviour
* What do you actually see

If you can provide code that reproduces the problem, great!
If this code is in the form of a failing unit test, even better!

## <a name="issue_or_pr"></a> Issues or pull requests?
As a rule of thumb, it may be good to raise an issue first before providing a pull request.

It is a good idea to check we all have the same understanding that there actually is a problem to solve, and for complex pull requests it may save time when we discuss in advance what shape the solution should take.

That said, for bugfixes and documentation fixes, reporting the issue and providing a pull request to fix it in one PR is perfectly fine.
When in doubt, maybe raise an issue first.


## <a name="pr_conventions"></a> Conventions for pull requests
If there is a corresponding GitHub issue, please mention the issue number in the pull request title.

Ideally prefix commit comments with either the pull request number, or the associated GitHub issue number.


## <a name="java_version"></a> Java version
The project is built with Java 8, and different artifacts target different versions of Java.
See the table below.

Artifact | Target Java Version
------------ | -------------
`picocli` | Java 5
`picocli-codegen` | Java 6
`picocli-groovy` | Java 5
`picocli-shell-jline2` | Java 5
`picocli-shell-jline3` | Java 8
`picocli-spring-boot-starter` | Java 8

Please be aware that pull requests can only use language features that are supported in the above version of Java.

## <a name="build"> Building

```
git clone https://github.com/remkop/picocli.git
cd picocli
gradlew publishToMavenLocal
```

That should publish `picocli-4.7.0-SNAPSHOT` to your local .m2 Maven cache. You can then try this in a project that uses the `info.picocli:picocli:4.7.0-SNAPSHOT` dependency.
