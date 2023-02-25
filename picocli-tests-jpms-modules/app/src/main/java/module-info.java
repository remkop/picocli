module picocli.test_jpms.modular_app {
    exports picocli.test_jpms.modular_app;

    requires info.picocli;
    requires system.lambda; // for test

    // Open this package for reflection to external frameworks.
    opens picocli.test_jpms.modular_app;

    // THE BELOW DOES NOT WORK!
    // The below syntax results in MissingResourceException: Can't find bundle for base name XXX
    //    opens picocli.test_jpms.modular_app to info.picocli;  // AVOID THIS
}
