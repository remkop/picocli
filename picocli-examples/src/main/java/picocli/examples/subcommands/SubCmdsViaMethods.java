package picocli.examples.subcommands;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Spec;
import java.util.Locale;

@Command(name = "ISOCodeResolve", subcommands = { CommandLine.HelpCommand.class },
         description = "Resolve ISO country codes (ISO-3166-1) or language codes (ISO 639-1 or -2)")
public class SubCmdsViaMethods implements Runnable {
    @Spec CommandSpec spec;
    
    @Command(name = "country", description = "Resolve ISO country code (ISO-3166-1, Alpha-2 code)")
    void country(@Parameters(arity = "1..*", paramLabel = "<country code>",
                 description = "country code(s) to be resolved") String[] countryCodes) {
        for (String code : countryCodes) {
            System.out.println(String.format("%s: %s", code.toUpperCase(), new Locale("", code).getDisplayCountry()));
        }
    }

    @Command(name = "language", description = "Resolve ISO language code (ISO 639-1 or -2, two/three letters)")
    void language(@Parameters(arity = "1..*", paramLabel = "<language code>",
                  description = "language code(s) to be resolved") String[] languageCodes) {
        for (String code : languageCodes) {
            System.out.println(String.format("%s: %s", code.toLowerCase(), new Locale(code).getDisplayLanguage()));
        }
    }

    @Override
    public void run() {
        throw new ParameterException(spec.commandLine(), "Specify a subcommand");
    }

    public static void main(String[] args) {
        CommandLine cmd = new CommandLine(new SubCmdsViaMethods());
        if (args.length == 0) {
            cmd.usage(System.out);
        }
        else {
            cmd.execute(args);
        }
    }
}
