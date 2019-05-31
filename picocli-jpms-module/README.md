# Picocli JPMS Module

This subproject does not produce any artifacts,
but generates a `picocli-jpms-module/build/classes/java/main/META-INF/versions/9/module-info.class` file,
for inclusion in the main `picocli-${version}.jar` artifact.
 
Starting from picocli 4.0, `picocli-${version}.jar` will no longer be an [automatic module](https://openjdk.java.net/projects/jigsaw/spec/sotms/#automatic-modules),
but will be a [modular multi-release jar](https://openjdk.java.net/jeps/238#Modular-multi-release-JAR-files).


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