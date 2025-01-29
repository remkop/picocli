package picocli;

import java.util.ServiceLoader;

/**
 * A listener to observe all executed commands and customize the user object if needed.
 * <p/>
 * Comma separate list of {@link CommandListener CommandListeners} can be provided
 * with the {@code -Dpicocli.commandListeners} system property or via SPI ({@link ServiceLoader}).
 */
public interface CommandListener {

    /**
     * This method is invoked after setting the value on the command if the option is provided
     * as a command line argument.
     */
    default void argumentOptionSet(Object command, String optionName) {
    }

    /**
     * This method is invoked after setting the value on the command if the option isn't provided
     * as a command line argument and the default value was used instead.
     */
    default void defaultOptionSet(Object command, String optionName) {
    }

    /**
     * This method is before executing the command.
     */
    default void onExecute(Object command) {
    }
}
