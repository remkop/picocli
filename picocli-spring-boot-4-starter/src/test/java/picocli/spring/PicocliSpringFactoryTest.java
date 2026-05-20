package picocli.spring;

import org.junit.After;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import picocli.CommandLine;
import picocli.CommandLine.IFactory;
import picocli.CommandLine.ParseResult;
import picocli.spring.boot.autoconfigure.sample.MyCommand;
import picocli.spring.boot.autoconfigure.sample.MySpringApp;

import java.lang.reflect.Field;
import java.util.Arrays;

import static org.junit.Assert.*;

public class PicocliSpringFactoryTest {

    private AnnotationConfigApplicationContext context;

    @After
    public void tearDown() {
        if (this.context != null) {
            this.context.close();
        }
    }

    @Test
    public void defaultPicocliSpringFactory() {
        load(MySpringApp.class);
        IFactory factory = this.context.getBean(IFactory.class);
        assertNotNull(factory);
        assertTrue(factory instanceof PicocliSpringFactory);
    }

    @Test
    public void testParseTopLevelCommand() {
        load(MySpringApp.class);
        IFactory factory = this.context.getBean(IFactory.class);
        MyCommand userObject = this.context.getBean(MyCommand.class);
        CommandLine cmd = new CommandLine(userObject, factory);
        cmd.parseArgs("-x", "abc", "xyz");
        assertEquals("abc", userObject.x);
        assertEquals(Arrays.asList("xyz"), userObject.positionals);
    }

    @Test
    public void testParseSubCommand() {
        load(MySpringApp.class);
        IFactory factory = this.context.getBean(IFactory.class);
        MyCommand userObject = this.context.getBean(MyCommand.class);
        CommandLine cmd = new CommandLine(userObject, factory);
        ParseResult parseResult = cmd.parseArgs("sub", "-y", "abc", "xyz");
        assertNull(userObject.x);
        assertNull(userObject.positionals);

        assertTrue(parseResult.hasSubcommand());
        MyCommand.Sub sub = (MyCommand.Sub) parseResult.subcommand().commandSpec().userObject();
        assertEquals("abc", sub.y);
        assertEquals(Arrays.asList("xyz"), sub.positionals);
    }

    @Test
    public void testParseSubSubCommand() {
        load(MySpringApp.class);
        IFactory factory = this.context.getBean(IFactory.class);
        MyCommand userObject = this.context.getBean(MyCommand.class);
        CommandLine cmd = new CommandLine(userObject, factory);
        ParseResult parseResult = cmd.parseArgs("sub", "subsub", "-z", "abc");
        assertNull(userObject.x);
        assertNull(userObject.positionals);

        assertTrue(parseResult.hasSubcommand());
        assertTrue(parseResult.subcommand().hasSubcommand());
        MyCommand.SubSub subsub = (MyCommand.SubSub) parseResult.subcommand().subcommand().commandSpec().userObject();
        assertEquals("abc", subsub.z);
        assertEquals("something", subsub.service.service());
    }

    private void load(Class<?> config, String... environment) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
        //EnvironmentTestUtils.addEnvironment(applicationContext, environment);
        applicationContext.register(config);
        applicationContext.refresh();
        this.context = applicationContext;
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullAppContext() {
        new PicocliSpringFactory(null);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullAppContextNullFallbackFactory() {
        new PicocliSpringFactory(null, null);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullAppContextValidFallbackFactory() {
        new PicocliSpringFactory(null, CommandLine.defaultFactory());
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorValidAppContextNullFallbackFactory() {
        load(MySpringApp.class);
        new PicocliSpringFactory(context, null);
    }

    @Test
    public void testConstructorAppContextUsesDefaultFactory() throws Exception {
        load(MySpringApp.class);
        PicocliSpringFactory factory = new PicocliSpringFactory(context);
        IFactory fallbackFactory = extractFallbackFactory(factory);
        assertNotNull(fallbackFactory);
        assertEquals("picocli.CommandLine$DefaultFactory", fallbackFactory.getClass().getName());
    }

    @Test
    public void testConstructorAppContextFactoryUsesSpecifiedFactory() throws Exception {
        load(MySpringApp.class);
        IFactory myFactory = new IFactory() {
            @Override public <K> K create(Class<K> cls) {return null;}
        };
        PicocliSpringFactory factory = new PicocliSpringFactory(context, myFactory);
        IFactory fallbackFactory = extractFallbackFactory(factory);
        assertNotNull(fallbackFactory);
        assertSame(myFactory, fallbackFactory);
    }

    private IFactory extractFallbackFactory(PicocliSpringFactory factory) throws NoSuchFieldException, IllegalAccessException {
        Field fallbackFactoryField = PicocliSpringFactory.class.getDeclaredField("fallbackFactory");
        fallbackFactoryField.setAccessible(true);
        IFactory result = (IFactory) fallbackFactoryField.get(factory);
        return result;
    }
}
