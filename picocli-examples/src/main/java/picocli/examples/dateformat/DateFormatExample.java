package picocli.examples.dateformat;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Callable;

@Command(name = "datefmt")
public class DateFormatExample implements Callable<Integer> {

    @Option(names = "--date")
    Date date;

    @Override
    public Integer call() throws Exception {
        System.out.printf("date=%s%n", date);
        return null;
    }

    public static void main(String[] ignored) {
        new CommandLine(new DateFormatExample()).execute("--date=2020-01-01");
        System.out.println();

        new CommandLine(new DateFormatExample())
                .registerConverter(Date.class, s -> new SimpleDateFormat("MMM.dd.yyyy", Locale.ITALIAN).parse(s))
                .execute("--date=Gennaio.01.2020"); //Gennaio=January in Italian

    }
}
