<p align="center"><img src="https://picocli.info/images/logo/horizontal-400x150.png" alt="picocli" height="150px"></p>


# Picocli Code Generation

Picocli Code Generation contains tools for generating source code, documentation and configuration files 
for picocli-based applications.


## ReflectionConfigGenerator Tool

`ReflectionConfigGenerator` generates a JSON String with the program elements that will be accessed reflectively in a picocli-based application, in order to compile this application ahead-of-time into a native executable with GraalVM.

The output of `ReflectionConfigGenerator` is intended to be passed to the `-H:ReflectionConfigurationFiles=/path/to/reflectconfig` option of the `native-image` GraalVM utility. This allows picocli-based applications to be compiled to a native image.

See [Picocli on GraalVM: Blazingly Fast Command Line Apps](https://github.com/remkop/picocli/wiki/Picocli-on-GraalVM:-Blazingly-Fast-Command-Line-Apps) for details.
