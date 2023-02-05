# Picocli Codegen Tests for Java 8+

This subproject contains tests for the annotation processor in the `picocli-codegen` module.
These tests use the (excellent and recommended) [google/compile-testing](https://github.com/google/compile-testing) library, which requires Java 8.

The `picocli-codegen` module itself only requires Java 6, and I found it convenient to split off the annotation processor tests into a separate subproject.

This module does not publish any artifacts.

