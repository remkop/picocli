package picocli;


import static org.junit.Assert.assertEquals;
import static picocli.CommandLine.Help.Column.Overflow.SPAN;
import static picocli.CommandLine.Help.Column.Overflow.WRAP;
import static picocli.CommandLine.Help.TextTable.forColumns;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;

public class TextTableTest {

    // allows tests to set any kind of properties they like, without having to individually roll them back
    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");

    private static final String newline = String.format("%n");

    @Test
    public void addRowValues() {
        CommandLine.Help.TextTable textTable = emptyTable();
        textTable.addRowValues("<query>",
                "Shows results of SQL <query>\n"
                        + "The query itself can contain the variables ${table}, ${columns} "
                        + "and ${tabletype}, or system properties referenced as ${<system-property-name>}\n"
                        + "Queries without any variables are executed exactly once\n"
                        + "Queries with variables are executed once for each table, "
                        + "with the variables substituted");

        assertEquals(" <query>        Shows results of SQL <query>\n"
                        + "                The query itself can contain the variables ${table}, ${columns}\n"
                        + "                  and ${tabletype}, or system properties referenced as\n"
                        + "                  ${<system-property-name>}\n"
                        + "                Queries without any variables are executed exactly once\n"
                        + "                Queries with variables are executed once for each table, with\n"
                        + "                  the variables substituted\n",
                normalizeNewlines(textTable));
    }

    @Test
    public void addRowValues_nulls() {
        CommandLine.Help.TextTable textTable = emptyTable();
        textTable.addRowValues("key", null);
        textTable.addRowValues(null, "value");

        assertEquals(" key\n                value\n", normalizeNewlines(textTable));
    }

    @SuppressWarnings("deprecation")
    private CommandLine.Help.TextTable emptyTable() {
        return forColumns(CommandLine.Help.Ansi.OFF,
                new CommandLine.Help.Column(15, 1, SPAN),
                new CommandLine.Help.Column(65, 1, WRAP));
    }

    private String normalizeNewlines(final CommandLine.Help.TextTable textTable) {
        return textTable.toString().replaceAll(newline, "\n");
    }

}
