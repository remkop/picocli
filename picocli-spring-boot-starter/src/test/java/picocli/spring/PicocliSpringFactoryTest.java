package picocli.spring;

import org.junit.After;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import picocli.CommandLine;
import picocli.CommandLine.IFactory;
import picocli.CommandLine.ParseResult;
import picocli.spring.boot.autoconfigure.sample.MyCommand;
import picocli.spring.boot.autoconfigure.sample.MySpringApp;

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
}
