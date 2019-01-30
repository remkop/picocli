package picocli.examples.annotatedmethods;

import picocli.CommandLine.ParameterException;
import picocli.CommandLine.PicocliException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import static picocli.CommandLine.Option;

public class AnnotatedClassMethodOptions {

    static class Primitives {
        boolean aBoolean;
        byte aByte;
        short aShort;
        int anInt;
        long aLong;
        float aFloat;
        double aDouble;

        @Option(names = "-b") void setBoolean(boolean val) { aBoolean = val; }
        @Option(names = "-y") void setByte(byte val) { aByte = val; }
        @Option(names = "-s") void setShort(short val) { aShort = val; }
        @Option(names = "-i") void setInt(int val) { anInt = val; }
        @Option(names = "-l") void setLong(long val) { aLong = val; }
        @Option(names = "-f") void setFloat(float val) { aFloat = val; }
        @Option(names = "-d") void setDouble(double val) { aDouble = val; }
    }

    static class PrimitivesWithDefault {
        boolean aBoolean;
        byte aByte;
        short aShort;
        int anInt;
        long aLong;
        float aFloat;
        double aDouble;

        @Option(names = "-b", defaultValue = "true") void setBoolean(boolean val) { aBoolean = val; }
        @Option(names = "-y", defaultValue = "11") void setByte(byte val) { aByte = val; }
        @Option(names = "-s", defaultValue = "12") void setShort(short val) { aShort = val; }
        @Option(names = "-i", defaultValue = "13") void setInt(int val) { anInt = val; }
        @Option(names = "-l", defaultValue = "14") void setLong(long val) { aLong = val; }
        @Option(names = "-f", defaultValue = "15.5") void setFloat(float val) { aFloat = val; }
        @Option(names = "-d", defaultValue = "16.6") void setDouble(double val) { aDouble = val; }
    }

    static class ObjectsWithDefaults {
        Boolean aBoolean;
        Byte aByte;
        Short aShort;
        Integer anInt;
        Long aLong;
        Float aFloat;
        Double aDouble;
        BigDecimal aBigDecimal;
        String aString;
        List<String> aList;
        Map<Integer, Double> aMap;
        SortedSet<Short> aSet;

        @Option(names = "-b", defaultValue = "true") void setBoolean(Boolean val) { aBoolean = val; }
        @Option(names = "-y", defaultValue = "123") void setByte(Byte val) { aByte = val; }
        @Option(names = "-s", defaultValue = "11") void setShort(Short val) { aShort = val; }
        @Option(names = "-i", defaultValue = "12") void setInt(Integer val) { anInt = val; }
        @Option(names = "-l", defaultValue = "13") void setLong(Long val) { aLong = val; }
        @Option(names = "-f", defaultValue = "14.4") void setFloat(Float val) { aFloat = val; }
        @Option(names = "-d", defaultValue = "15.5") void setDouble(Double val) { aDouble = val; }

        @Option(names = "-bigint", defaultValue = "16.6") void setBigDecimal(BigDecimal val) { aBigDecimal = val; }
        @Option(names = "-string", defaultValue = "abc") void setString(String val) { aString = val; }
        @Option(names = "-list", defaultValue = "a,b,c", split = ",") void setList(List<String> val) { aList = val; }

        @Option(names = "-map", defaultValue = "1=1,2=2,3=3", split = ",")
        void setMap(Map<Integer, Double> val) { aMap = val; }

        @Option(names = "-set", defaultValue = "1,2,3", split = ",")
        void setSortedSet(SortedSet<Short> val) { aSet = val; }
    }

    static class Objects {
        Boolean aBoolean;
        Byte aByte;
        Short aShort;
        Integer anInt;
        Long aLong;
        Float aFloat;
        Double aDouble;
        BigInteger aBigInteger;
        String aString;
        List<String> aList;
        Map<Integer, Double> aMap;
        SortedSet<Short> aSet;

        @Option(names = "-b") void setBoolean(Boolean val) { aBoolean = val; }
        @Option(names = "-y") void setByte(Byte val) { aByte = val; }
        @Option(names = "-s") void setShort(Short val) { aShort = val; }
        @Option(names = "-i") void setInt(Integer val) { anInt = val; }
        @Option(names = "-l") void setLong(Long val) { aLong = val; }
        @Option(names = "-f") void setFloat(Float val) { aFloat = val; }
        @Option(names = "-d") void setDouble(Double val) { aDouble = val; }

        @Option(names = "-bigint") void setBigInteger(BigInteger val) { aBigInteger = val; }
        @Option(names = "-string") void setString(String val) { aString = val; }
        @Option(names = "-list")   void setList(List<String> val) { aList = val; }

        @Option(names = "-map")
        void setMap(Map<Integer, Double> val) { aMap = val; }

        @Option(names = "-set")
        void setSortedSet(SortedSet<Short> val) { aSet = val; }
    }
}
