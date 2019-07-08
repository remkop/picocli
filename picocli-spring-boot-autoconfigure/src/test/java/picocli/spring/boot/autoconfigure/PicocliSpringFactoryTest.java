package picocli.spring.boot.autoconfigure;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.After;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import picocli.CommandLine;
import picocli.CommandLine.IFactory;
import picocli.spring.PicocliSpringFactory;

import static org.junit.Assert.*;

public class PicocliSpringFactoryTest {

    private AnnotationConfigApplicationContext context;

    @After
    public void tearDown() {
        if (this.context != null) {
            this.context.close();
        }
    }

    @Ignore
    @Test
    public void defaultPicocliSpringFactory() {
        load(EmptyConfiguration.class);
        IFactory factory = this.context.getBean(PicocliSpringFactory.class);
        assertNotNull(factory);
        assertTrue(factory instanceof PicocliSpringFactory);
        CommandLine cmd = new CommandLine(new MyCommand(), factory);
        cmd.parseArgs();
    }

    @Ignore
    @Test
    public void defaultFactory() {
        load(EmptyConfiguration.class);
        IFactory factory = this.context.getBean(IFactory.class);
        assertNotNull(factory);
        assertTrue(factory instanceof PicocliSpringFactory);
    }

    @Test
    public void configuredFactory() {
        load(CommandConfiguration.class);
        IFactory factory = this.context.getBean(IFactory.class);
        assertNotNull(factory);
        assertTrue(factory instanceof PicocliSpringFactory);
    }

    @Configuration
    static class EmptyConfiguration {}

    @Configuration
    @Import(PicocliSpringFactory.class)
    static class CommandConfiguration {

        @Autowired
        IFactory factory;

        @Bean
        CommandLine commandLine() {
            return new CommandLine(new MyCommand(), factory);
        }
    }

    @CommandLine.Command
    static class MyCommand {}

    private void load(Class<?> config, String... environment) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
        //EnvironmentTestUtils.addEnvironment(applicationContext, environment);
        applicationContext.register(config);
        applicationContext.refresh();
        this.context = applicationContext;
    }

}