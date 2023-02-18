module picocli.test_jpms.modular_app {
    exports picocli.test_jpms.modular_app;

    requires info.picocli;
    requires system.lambda; // for test

    // Open this package for reflection to external frameworks.
    //opens picocli.test.jpms.modular.app;

    // or: limit access to picocli only
    opens picocli.test_jpms.modular_app to info.picocli;
}
