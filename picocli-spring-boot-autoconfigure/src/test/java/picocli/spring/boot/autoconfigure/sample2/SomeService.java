package picocli.spring.boot.autoconfigure.sample2;

public class SomeService {
    private final ServiceDependency dependency;

    public SomeService(ServiceDependency dependency) {
        this.dependency = dependency;
    }

    public String service() {
        return dependency.provideSomething();
    }
}
