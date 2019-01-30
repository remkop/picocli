package picocli.codegen.util;

import java.io.IOException;
import java.io.InputStream;
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
            return slurp(Resources.class.getResource(resource).openStream());
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public static String slurp(InputStream in) {
        Scanner s = new Scanner(in).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    private Resources() {
        // utility class; don't instantiate
    }
}
