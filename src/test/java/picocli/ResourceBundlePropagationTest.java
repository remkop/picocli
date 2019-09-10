package picocli;

import java.util.ResourceBundle;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import picocli.CommandLine.Command;

@Command(name = "leaf", description = "I'm a leaf")
class Leaf {}

@Command(name = "branch", description = "I'm a branch", subcommands = {Leaf.class})
class Branch {}

@Command(name = "trunk", description = "I'm a trunk", subcommands = {Branch.class})
class Trunk {}

@Command(name = "root", description = "I'm a root", subcommands = {Trunk.class})
class Root {}

public class ResourceBundlePropagationTest {
	@Test
	public void testPropagation() {
		CommandLine rootCommand = new CommandLine(new Root());
		rootCommand.setResourceBundle(ResourceBundle.getBundle("picocli.ResourceBundlePropagationTest"));
		CommandLine leafCommand = rootCommand
				.getSubcommands().get("trunk")
				.getSubcommands().get("branch")
				.getSubcommands().get("leaf");
		String actual = leafCommand.getUsageMessage();
		String expected = String.format(
				"my name is leaf%n" +
				"Usage: root trunk branch leaf%n" +
				"my parent is branch, I'm a leaf%n" +
				"lorem ipsum%n");
		assertEquals(expected, actual);
	}
}
