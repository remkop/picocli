package picocli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import picocli.CommandLine.Command;
import picocli.CommandLine.IDefaultValueProvider;
import picocli.CommandLine.Model.ArgSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

public class CommandLineDefaultProviderTest {

  static class TestDefaultProvider implements IDefaultValueProvider {
    public String defaultValue(ArgSpec argSpec) {
      return "Default provider string value";
    }
  }

  @Command(defaultValueProvider = TestDefaultProvider.class)
  static class App {
    @Option(names = "-a")
    private String optionStringFieldWithoutDefaultNorInitialValue;
    @Option(names = "-b", defaultValue = "Annotated default value")
    private String optionStringFieldWithAnnotatedDefault;
    @Option(names = "-c")
    private String optionStringFieldWithInitDefault = "Initial default value";

    @Parameters(arity = "0..1")
    private String paramStringFieldWithoutDefaultNorInitialValue;
    @Parameters(arity = "0..1", defaultValue = "Annotated default value")
    private String paramStringFieldWithAnnotatedDefault;
    @Parameters(arity = "0..1")
    private String paramStringFieldWithInitDefault = "Initial default value";

    private String stringForSetterDefault;
    @Option(names = "-d", defaultValue = "Annotated setter default value")
    void setString(String val) { stringForSetterDefault = val; }
  }

  @Command(name = "sub")
  static class Sub {
    @Option(names = "-a")
    private String optionStringFieldWithoutDefaultNorInitialValue;
    @Option(names = "-b", defaultValue = "Annotated default value")
    private String optionStringFieldWithAnnotatedDefault;
    @Option(names = "-c")
    private String optionStringFieldWithInitDefault = "Initial default value";

    @Parameters(arity = "0..1")
    private String paramStringFieldWithoutDefaultNorInitialValue;
    @Parameters(arity = "0..1", defaultValue = "Annotated default value")
    private String paramStringFieldWithAnnotatedDefault;
    @Parameters(arity = "0..1")
    private String paramStringFieldWithInitDefault = "Initial default value";

    private String stringForSetterDefault;
    @Option(names = "-d", defaultValue = "Annotated setter default value")
    void setString(String val) { stringForSetterDefault = val; }
  }


  @Test
  public void testCommandDefaultProviderByAnnotation() {
    CommandLine cmd = new CommandLine(App.class);
    cmd.parse();

    App app = cmd.getCommand();
    // if no default defined on the option, command default provider should be used
    assertEquals("Default provider string value",app.optionStringFieldWithoutDefaultNorInitialValue);
    assertEquals("Default provider string value",app.paramStringFieldWithoutDefaultNorInitialValue);
    // if a default is defined on the option either by annotation or by initial value, it must
    // override the default provider.
    assertEquals("Annotated default value",app.optionStringFieldWithAnnotatedDefault);
    assertEquals("Annotated default value",app.paramStringFieldWithAnnotatedDefault);

    assertEquals("Initial default value",app.optionStringFieldWithInitDefault);
    assertEquals("Initial default value",app.paramStringFieldWithInitDefault);

    assertEquals("Annotated setter default value",app.stringForSetterDefault);
  }

  @Test
  public void testDefaultProviderPropagatedToSubCommand() {
    CommandLine cmd = new CommandLine(App.class);

    cmd.addSubcommand("sub", new CommandLine(Sub.class));

    CommandLine subCommandLine = cmd.getSubcommands().get("sub");
    cmd.setDefaultValueProvider(new TestDefaultProvider());

    assertNotNull(subCommandLine.getCommandSpec().defaultValueProvider());
    assertEquals(TestDefaultProvider.class, subCommandLine.getCommandSpec().defaultValueProvider().getClass());
  }

  @Test
  public void testCommandDefaultProviderSetting() {

    CommandLine cmd = new CommandLine(App.class);
    cmd.setDefaultValueProvider(new TestDefaultProvider());
    cmd.parse();

    App app = cmd.getCommand();
    // if no default defined on the option, command default provider should be used
    assertEquals("Default provider string value",app.optionStringFieldWithoutDefaultNorInitialValue);
  }
}
