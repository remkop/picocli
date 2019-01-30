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

public class AnnotatedInterfaceMethodOptions {

    interface Primitives {
        @Option(names = "-b")
        boolean aBoolean();

        @Option(names = "-y")
        byte aByte();

        @Option(names = "-s")
        short aShort();

        @Option(names = "-i")
        int anInt();

        @Option(names = "-l")
        long aLong();

        @Option(names = "-f")
        float aFloat();

        @Option(names = "-d")
        double aDouble();
    }

    interface PrimitivesWithDefault {
        @Option(names = "-b", defaultValue = "true")
        boolean aBoolean();

        @Option(names = "-y", defaultValue = "11")
        byte aByte();

        @Option(names = "-s", defaultValue = "12")
        short aShort();

        @Option(names = "-i", defaultValue = "13")
        int anInt();

        @Option(names = "-l", defaultValue = "14")
        long aLong();

        @Option(names = "-f", defaultValue = "15.5")
        float aFloat();

        @Option(names = "-d", defaultValue = "16.6")
        double aDouble();
    }

    interface Objects {
        @Option(names = "-b")
        Boolean aBoolean();

        @Option(names = "-y")
        Byte aByte();

        @Option(names = "-s")
        Short aShort();

        @Option(names = "-i")
        Integer anInt();

        @Option(names = "-l")
        Long aLong();

        @Option(names = "-f")
        Float aFloat();

        @Option(names = "-d")
        Double aDouble();

        @Option(names = "-bigint")
        BigInteger aBigInteger();

        @Option(names = "-string")
        String aString();

        @Option(names = "-list")
        List<String> getList();

        @Option(names = "-map")
        Map<Integer, Double> getMap();

        @Option(names = "-set")
        SortedSet<Short> getSortedSet();
    }

    interface ObjectsWithDefault {
        @Option(names = "-b", defaultValue = "true")
        Boolean aBoolean();

        @Option(names = "-y", defaultValue = "123")
        Byte aByte();

        @Option(names = "-s", defaultValue = "11")
        Short aShort();

        @Option(names = "-i", defaultValue = "12")
        Integer anInt();

        @Option(names = "-l", defaultValue = "13")
        Long aLong();

        @Option(names = "-f", defaultValue = "14.4")
        Float aFloat();

        @Option(names = "-d", defaultValue = "15.5")
        Double aDouble();

        @Option(names = "-bigint", defaultValue = "16.6")
        BigDecimal aBigDecimal();

        @Option(names = "-string", defaultValue = "abc")
        String aString();

        @Option(names = "-list", defaultValue = "a,b,c", split = ",")
        List<String> getList();

        @Option(names = "-map", defaultValue = "1=1,2=2,3=3", split = ",")
        Map<Integer, Double> getMap();

        @Option(names = "-set", defaultValue = "1,2,3", split = ",")
        SortedSet<Short> getSortedSet();
    }

}
