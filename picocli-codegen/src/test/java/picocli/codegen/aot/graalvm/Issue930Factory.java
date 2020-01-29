package picocli.codegen.aot.graalvm;

import picocli.CommandLine;
import picocli.CommandLine.IFactory;

public class Issue930Factory implements IFactory {
    IFactory fallback = CommandLine.defaultFactory();

    @SuppressWarnings("unchecked")
    @Override
    public <K> K create(Class<K> cls) throws Exception {
        if (cls == Issue930Example.class) {
            return (K) new Issue930Example("a test");
        }
        return fallback.create(cls);
    }
}