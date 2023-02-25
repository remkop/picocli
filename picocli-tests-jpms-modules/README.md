# JPMS Module Tests

This subproject is not part of the build of the main picocli project.
In order to test the JPMS modules, it uses the module path instead of the classpath,
and it was easier to make it a separate project with its own `settings.gradle` file.

This project has two subprojects:

* The `app` subproject contains a simple CLI app, packaged as a JPMS modular application.
  The application does not do anything useful, it is for testing purposes.

* The `app-it` subproject contains integration tests for the `app` subproject.

To run this project:

```bash
cd picocli-tests-jpms-modules
../gradlew check
```
