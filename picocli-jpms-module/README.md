<p align="center"><img src="https://picocli.info/images/logo/horizontal-400x150.png" alt="picocli" height="150px"></p>

# Picocli JPMS Module

This subproject generates a [modular jar](https://openjdk.java.net/projects/jigsaw/spec/sotms/#module-artifacts) `picocli-jpms-module-${version}.jar` for applications that want to use picocli while taking full advantage of the Java Platform Module System (JPMS) introduced in Java 9.

This jar is generated in addition to the `picocli-${version}.jar` artifact. Starting from picocli 4.0, `picocli-${version}.jar` will no longer be an [automatic module](https://openjdk.java.net/projects/jigsaw/spec/sotms/#automatic-modules).

## Contents of picocli-jpms-module

* a `module-info.class` in the root of the jar.
* the classes in the `picocli` package.
* excludes (does not contain) any classes in the `picocli.groovy` package, so this module has no dependency on Groovy.

We considered making the `picocli-jpms-module` artifact a 
modular multi-release jar with the `module-info.class` file located in `META-INF/versions/9`,
in the hope that this would avoid problems for some older tools,
which incorrectly process the module descriptor as if it were a normal Java class.

However, putting the `module-info.class` file in `META-INF/versions/9` seems to have its own problems, as discussed [here](https://github.com/remkop/picocli/issues/674).

I'm open to changing this, based on community feedback. Let us know what works or does not work for you!

# Using picocli with Java 9 modules

Applications that use Java 9's modules need to configure their module to allow picocli reflective access to the annotated classes and fields. 

Often applications want the annotated classes and fields to be **private**; there should be no need to make them part of the exported API of your module just to allow picocli to access them. The below settings make this possible. 

Example `module-info.java`:
```
module com.yourorg.yourapp {
    requires info.picocli;

    // Open this package for reflection to external frameworks.
    opens your.package.using.picocli;

    // or: limit access to picocli only
    opens other.package.using.picocli to info.picocli;
}
```
Note that neither package is `exported`, so other modules cannot accidentally compile against types in these packages.

Alternatively:
```
// open all packages in the module to reflective access
open module com.yourorg.yourapp {
    requires info.picocli;
}
```