package picocli.examples.validation;

import static picocli.CommandLine.Command;
import static picocli.CommandLine.Option;
import static picocli.CommandLine.Parameters;

// https://github.com/remkop/picocli/issues/1387

@Command
public class InvalidFinal {

    // Options

    @Option(names = {"--boolean"})
    private final boolean invalidBoolean = false;

    @Option(names = {"--valid-boolean"})
    private final boolean validBoolean;

    @Option(names = {"--byte"})
    private final byte invalidByte = 0;

    @Option(names = {"--valid-byte"})
    private final byte validByte;

    @Option(names = {"--short"})
    private final short invalidShort = 123;

    @Option(names = {"--valid-short"})
    private final short validShort;

    @Option(names = {"--int"})
    private final int invalidInt = -1;

    @Option(names = {"--valid-int"})
    private final int validInt;

    @Option(names = {"--long"})
    private final long invalidLong = -2L;

    @Option(names = {"--valid-long"})
    private final long validLong;

    @Option(names = {"--char"})
    private final char invalidChar = 'c';

    @Option(names = {"--valid-char"})
    private final char validChar;

    @Option(names = {"--float"})
    private final float invalidFloat = 1.0f;

    @Option(names = {"--valid-float"})
    private final float validFloat;

    @Option(names = {"--double"})
    private final double invalidDouble = 1.0;

    @Option(names = {"--valid-double"})
    private final double validDouble;

    @Option(names = {"--string"})
    private final String invalidString = "foo";

    @Option(names = {"--valid-string"})
    private final String validString;

    // Parameters

    @Parameters(index = "0")
    private final boolean invalidBooleanParam = false;

    @Parameters(index = "1")
    private final boolean validBooleanParam;

    @Parameters(index = "2")
    private final byte invalidByteParam = 0;

    @Parameters(index = "3")
    private final byte validByteParam;

    @Parameters(index = "4")
    private final short invalidShortParam = 123;

    @Parameters(index = "5")
    private final short validShortParam;

    @Parameters(index = "6")
    private final int invalidIntParam = -1;

    @Parameters(index = "7")
    private final int validIntParam;

    @Parameters(index = "8")
    private final long invalidLongParam = -2L;

    @Parameters(index = "9")
    private final long validLongParam;

    @Parameters(index = "10")
    private final char invalidCharParam = 'c';

    @Parameters(index = "11")
    private final char validCharParam;

    @Parameters(index = "12")
    private final float invalidFloatParam = 1.0f;

    @Parameters(index = "13")
    private final float validFloatParam;

    @Parameters(index = "14")
    private final double invalidDoubleParam = 1.0;

    @Parameters(index = "15")
    private final double validDoubleParam;

    @Parameters(index = "16")
    private final String invalidStringParam = "bar";

    @Parameters(index = "17")
    private final String validStringParam;

}
