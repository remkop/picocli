package picocli;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.SystemErrRule;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help;
import picocli.CommandLine.Option;

import static java.lang.String.format;
import static org.junit.Assert.*;
import static picocli.TestUtil.usageString;

// https://github.com/remkop/picocli/issues/964
public class OrderedArgGroupSynopsisTest {
	@Rule
	public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");
	@Rule
	public final SystemErrRule systemErrRule = new SystemErrRule().enableLog().muteForSuccessfulTests();

	@Command(sortOptions = false, sortSynopsis = false)
	class SynopsisOrder {
		class GroupWithOneOption {
			@Option(names = "--option1", required = true)
			String value1;
		}

		class AllGroups {
			@ArgGroup(exclusive = false, multiplicity = "1", order = 1) GroupWithOneOption group;
			@Option(names = "--option2", required = true, order = 2) String value2;
		}

		@ArgGroup(exclusive = true, multiplicity = "1")
		AllGroups allGroups;
	}

    //@Ignore("Requires #964")
	@Test
	public void testSynopsisOrderForArgGroup() {
		String result = usageString(new SynopsisOrder(), Help.Ansi.OFF);
		assertEquals(format("" +
				"Usage: <main class> (--option1=<value1> | --option2=<value2>)%n" +
				"      --option1=<value1>%n" +
				"      --option2=<value2>%n"), result);
	}
}
