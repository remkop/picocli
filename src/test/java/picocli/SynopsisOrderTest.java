package picocli;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.SystemErrRule;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static picocli.TestUtil.usageString;

public class SynopsisOrderTest {
	@Rule
	public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");
	@Rule
	public final SystemErrRule systemErrRule = new SystemErrRule().enableLog().muteForSuccessfulTests();

	@Command()
	class SynopsisOrder {
		class Group {
			@CommandLine.Option(names = "--option1", required = true)
			String value1;
		}

		class AllGroups {
			@CommandLine.ArgGroup(exclusive = false, multiplicity = "1", order = 1) Group group;
			@CommandLine.Option(names = "--option2", required = true, order = 2) String value2;
		}

		@ArgGroup(exclusive = true, multiplicity = "1")
		AllGroups allGroups;
	}

	@Test
	public void testSynopsisOrderForArgGroup() {
		String result = usageString(new SynopsisOrder(), Help.Ansi.OFF);
		assertEquals(format("" +
				"Usage: <main class> (--option1=<value1> | (--option2=<value2>))%n" +
				"      --option1=<value1>%n" +
				"      --option2=<value2>%n"), result);
	}
}
