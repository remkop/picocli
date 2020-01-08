package picocli.annotation.processing.tests;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public final class Resources {

    public static List<String> slurpAll(String... resources) {
        List<String> result = new ArrayList<String>();
        for (String resource : resources) {
            result.add(slurp(resource));
        }
        return result;
    }

    public static String slurp(String resource) {
        try {
            URL url = Resources.class.getResource(resource);
            if (url == null) {
                throw new FileNotFoundException(resource);
            }
            return slurp(url.openStream());
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public static String slurp(InputStream in) {
        Scanner s = new Scanner(in).useDelimiter("\\A");
        return s.hasNext()
                ? s.next().replaceAll("\r\n", "\n").replaceAll("\n", System.getProperty("line.separator"))
                : "";
    }

    private Resources() {
        // utility class; don't instantiate
    }
}
