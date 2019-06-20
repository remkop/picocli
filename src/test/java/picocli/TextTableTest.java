package picocli;


import static org.junit.Assert.assertEquals;
import static picocli.CommandLine.Help.Column.Overflow.SPAN;
import static picocli.CommandLine.Help.Column.Overflow.WRAP;
import static picocli.CommandLine.Help.TextTable.forColumns;

import org.junit.Test;

public class TextTableTest
{

  private final String key = "<query>";
  private final String newline = String.format("%n");
  private final String value = "Shows results of SQL <query>\n"
                               + "The query itself can contain the variables ${table}, ${columns} "
                               + "and ${tabletype}, or system properties referenced as ${<system-property-name>}\n"
                               + "Queries without any variables are executed exactly once\n"
                               + "Queries with variables are executed once for each table, "
                               + "with the variables substituted";

  @Test
  public void autoSplit()
  {
    CommandLine.Help.TextTable textTable = emptyTable();

    // What we want to do:
    textTable.addRowValues(key, value);
    assertEquals(" <query>        Shows results of SQL <query>\n"
                 + "The query itself can contain the\n"
                 + "                  variables ${table}, ${columns} and ${tabletype}, or system\n"
                 + "                  properties referenced as ${<system-property-name>}\n"
                 + "Queries\n"
                 + "                  without any variables are executed exactly once\n"
                 + "Queries with\n"
                 + "                  variables are executed once for each table, with the\n"
                 + "                  variables substituted\n",
                 normalizeNewlines(textTable));
  }

  @Test
  public void manualSplit()
  {
    CommandLine.Help.TextTable textTable = emptyTable();

    // what we actually need to do to deal with the embedded newlines
    CommandLine.Help.Ansi.Text name = CommandLine.Help.Ansi.AUTO.text(key);
    CommandLine.Help.Ansi.Text description = CommandLine.Help.Ansi.AUTO
      .text(value);

    // split the description
    CommandLine.Help.Ansi.Text[] lines = description.splitLines();

    // first line
    textTable.addRowValues(name, lines[0]);

    // remaining lines
    CommandLine.Help.Ansi.Text EMPTY = CommandLine.Help.Ansi.OFF.text("");
    for (int i = 1; i < lines.length; i++)
    {
      textTable.addRowValues(EMPTY, lines[i]);
    }

    assertEquals(" <query>        Shows results of SQL <query>\n"
                 + "                The query itself can contain the variables ${table}, ${columns}\n"
                 + "                  and ${tabletype}, or system properties referenced as\n"
                 + "                  ${<system-property-name>}\n"
                 + "                Queries without any variables are executed exactly once\n"
                 + "                Queries with variables are executed once for each table, with\n"
                 + "                  the variables substituted\n",
                 normalizeNewlines(textTable));
  }

  private CommandLine.Help.TextTable emptyTable()
  {
    return forColumns(CommandLine.Help.Ansi.OFF,
                      new CommandLine.Help.Column(15, 1, SPAN),
                      new CommandLine.Help.Column(65, 1, WRAP));
  }

  private String normalizeNewlines(final CommandLine.Help.TextTable textTable)
  {
    return textTable.toString().replaceAll(newline, "\n");
  }

}
