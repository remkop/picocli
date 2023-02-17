package picocli.examples.annotatedmethods;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import static picocli.CommandLine.*;

public class InvalidAnnotatedInterfaceMethodOptions {

    // Invalid picocli annotation on interface fields
    interface InvalidAnnotatedStringOrPrimitiveFields {
        @Option(names = "-i")
        int anInt = 0;

        @Option(names = "-s")
        String aString = null;
    }

    // Invalid picocli annotation on interface field
    interface InvalidAnnotatedMutableFields {
        @Option(names = "-s")
        final List<String> aList = new ArrayList<String>();
    }
}
