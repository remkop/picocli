package picocli;

import org.junit.Test;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.lang.reflect.Method;

import static org.junit.Assert.*;

public class CommandAnnotationMethodTest
{

    static class State {

        private String state;

        public String getState() {
            return state;
        }

        public void setState(final String state)
        {
            this.state = state;
        }
    }

    static class Context {

        private State state;

        // No default constructor, so factory needs to be used
        public Context(State state) {
            this.state = state;
        }

        @Command(name = "context")
        public void context(@Option(names = "-c") String context) {
            state.setState(context);
        }
    }

    static class StateFactory implements CommandLine.IFactory
    {

        private State state;

        public StateFactory(State state) {
            this.state = state;
        }

        public <K> K create(final Class<K> cls)
          throws Exception
        {
            try
            {
                return cls.getConstructor(State.class).newInstance(state);
            }
            catch (final Exception e)
            {
                return cls.getConstructor().newInstance();
            }
        }
    }

    @Test
    public void testUseFactoryForMethodCommands() {

        State state = new State();
        state.setState("some application state");

        assertEquals(state.getState(), "some application state");

        Method contextMethod = CommandLine.getCommandMethods(Context.class, "context").get(0);

        CommandLine commandLine = new CommandLine(contextMethod, new StateFactory(state));
        commandLine.setExecutionStrategy(new CommandLine.RunLast());
        commandLine.execute("-c", "new application state");

        assertEquals(state.getState(), "new application state");
    }

}
