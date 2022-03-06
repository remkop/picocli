
module info.picocli.examples.module.simple {
    requires info.picocli;
    exports picocli.examples.module.simple;

    opens picocli.examples.module.simple to info.picocli;
}
