package picocli.spring.boot.autoconfigure;

import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.IFactory;
import picocli.spring.PicocliSpringFactory;

import static org.junit.Assert.*;

public class PicocliAutoConfigurationTest {

    private AnnotationConfigApplicationContext context;

    @After
    public void tearDown() {
        if (this.context != null) {
            this.context.close();
        }
    }

    @Test
    public void defaultPicocliSpringFactory() {
        load(EmptyConfiguration.class);
        IFactory factory = this.context.getBean(PicocliSpringFactory.class);
        assertNotNull(factory);
        assertTrue(factory instanceof PicocliSpringFactory);
        CommandLine cmd = new CommandLine(new MyCommand(), factory);
        cmd.parseArgs();
    }

    @Test
    public void defaultFactory() {
        load(EmptyConfiguration.class);
        IFactory factory = this.context.getBean(IFactory.class);
        assertNotNull(factory);
        assertTrue(factory instanceof PicocliSpringFactory);
    }

    @Test
    public void testEnableAutoConfigurationRequired() {
        load(EmptyNoAutoConfiguration.class);
        try {
            this.context.getBean(IFactory.class);
            fail("Expected exception");
        } catch (NoSuchBeanDefinitionException ok) {
            assertEquals("No qualifying bean of type 'picocli.CommandLine$IFactory' available", ok.getMessage());
        }
    }

    @Test
    public void testEnableAutoConfigurationImplRequired() {
        load(EmptyNoAutoConfiguration.class);
        try {
            this.context.getBean(PicocliSpringFactory.class);
            fail("Expected exception");
        } catch (NoSuchBeanDefinitionException ok) {
            assertEquals("No qualifying bean of type 'picocli.spring.PicocliSpringFactory' available", ok.getMessage());
        }
    }

    @Test
    public void configuredFactory() {
        load(CommandIFactoryConfiguration.class);
        IFactory factory = this.context.getBean(IFactory.class);
        assertNotNull(factory);
        assertTrue(factory instanceof PicocliSpringFactory);
    }

    @Test
    public void configuredFactoryImpl() {
        load(CommandPicocliSpringFactoryConfiguration.class);
        IFactory factory = this.context.getBean(IFactory.class);
        assertNotNull(factory);
        assertTrue(factory instanceof PicocliSpringFactory);
    }

    @Configuration
    @EnableAutoConfiguration
    static class EmptyConfiguration {}

    @Configuration
    static class EmptyNoAutoConfiguration {}

    @Configuration
    @EnableAutoConfiguration
    static class CommandIFactoryConfiguration {

        @Autowired
        IFactory factory;
    }

    @Configuration
    @EnableAutoConfiguration
    static class CommandPicocliSpringFactoryConfiguration {

        @Autowired
        PicocliSpringFactory factory;
    }

    @Component
    @Command
    static class MyCommand {}

    private void load(Class<?> config, String... environment) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
        //EnvironmentTestUtils.addEnvironment(applicationContext, environment);
        applicationContext.register(config);
        applicationContext.refresh();
        this.context = applicationContext;
    }
}
