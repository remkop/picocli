package picocli;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;

import java.math.BigInteger;
import java.util.*;

import static org.junit.Assert.*;
import static picocli.CommandLine.Option;

public class CommandLineAnnotatedMethodImplTest {
    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");

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

    @Test
    public void testPrimitiveDefaultValues() {
        Primitives primitives = CommandLine.populateCommand(new Primitives());
        assertFalse(primitives.aBoolean);
        assertEquals(0, primitives.aByte);
        assertEquals((short) 0, primitives.aShort);
        assertEquals(0, primitives.anInt);
        assertEquals(0, primitives.aLong);
        assertEquals(0, primitives.aFloat, 0.0001);
        assertEquals(0, primitives.aDouble, 0.0001);
    }

    @Test
    public void testPrimitives() {
        String[] args = "-b -y1 -s2 -i3 -l4 -f5 -d6".split(" ");
        Primitives primitives = CommandLine.populateCommand(new Primitives(), args);
        assertTrue(primitives.aBoolean);
        assertEquals(1, primitives.aByte);
        assertEquals(2, primitives.aShort);
        assertEquals(3, primitives.anInt);
        assertEquals(4, primitives.aLong);
        assertEquals(5, primitives.aFloat, 0.0001);
        assertEquals(6, primitives.aDouble, 0.0001);
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

    @Test
    public void testObjectsDefaultValues() {
        Objects objects = CommandLine.populateCommand(new Objects());
        assertNull(objects.aBoolean);
        assertNull(objects.aByte);
        assertNull(objects.aShort);
        assertNull(objects.anInt);
        assertNull(objects.aLong);
        assertNull(objects.aFloat);
        assertNull(objects.aDouble);
        assertNull(objects.aBigInteger);
        assertNull(objects.aString);
        assertNull(objects.aList);
        assertNull(objects.aMap);
        assertNull(objects.aSet);
    }

    @Test
    public void testObjects() {
        String[] args = "-b -y1 -s2 -i3 -l4 -f5 -d6 -bigint=7 -string abc -list a -list b -map 1=2.0 -set 33 -set 22".split(" ");
        Objects objects = CommandLine.populateCommand(new Objects(), args);
        assertTrue(objects.aBoolean);
        assertEquals(Byte.valueOf((byte) 1), objects.aByte);
        assertEquals(Short.valueOf((short) 2), objects.aShort);
        assertEquals(Integer.valueOf(3), objects.anInt);
        assertEquals(Long.valueOf(4), objects.aLong);
        assertEquals(5f, objects.aFloat, 0.0001);
        assertEquals(6d, objects.aDouble, 0.0001);
        assertEquals(BigInteger.valueOf(7), objects.aBigInteger);
        assertEquals("abc", objects.aString);
        assertEquals(Arrays.asList("a", "b"), objects.aList);
        Map<Integer, Double> map = new HashMap<Integer, Double>();
        map.put(1, 2.0);
        assertEquals(map, objects.aMap);
        Set<Short> set = new TreeSet<Short>();
        set.add((short) 22);
        set.add((short) 33);
        assertEquals(set, objects.aSet);
    }
}
