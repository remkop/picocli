package picocli;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.PicocliException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import static org.junit.Assert.*;
import static picocli.CommandLine.Option;

public class OptionMethodImplTest {

    // allows tests to set any kind of properties they like, without having to individually roll them back
    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

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
    public void testPrimitivesWithoutDefaultValues() {
        Primitives primitives = CommandLine.populateCommand(new Primitives());
        assertFalse(primitives.aBoolean);
        assertEquals(0, primitives.aByte);
        assertEquals((short) 0, primitives.aShort);
        assertEquals(0, primitives.anInt);
        assertEquals(0, primitives.aLong);
        assertEquals(0, primitives.aFloat, 0.0001);
        assertEquals(0, primitives.aDouble, 0.0001);
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

    @Test
    public void testPrimitivesWithDefaultValues() {
        PrimitivesWithDefault primitives = CommandLine.populateCommand(new PrimitivesWithDefault());
        assertTrue(primitives.aBoolean);
        assertEquals(11, primitives.aByte);
        assertEquals((short) 12, primitives.aShort);
        assertEquals(13, primitives.anInt);
        assertEquals(14, primitives.aLong);
        assertEquals(15.5, primitives.aFloat, 0.0001);
        assertEquals(16.6, primitives.aDouble, 0.0001);
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

    @Test
    public void testObjectsWithDefaultValues() {
        CommandLine cmd = new CommandLine(ObjectsWithDefaults.class);
        cmd.parseArgs();
        ObjectsWithDefaults objects = cmd.getCommand();
        assertTrue(objects.aBoolean);
        assertEquals(Byte.valueOf((byte) 123), objects.aByte);
        assertEquals(Short.valueOf((short) 11), objects.aShort);
        assertEquals(Integer.valueOf(12), objects.anInt);
        assertEquals(Long.valueOf(13), objects.aLong);
        assertEquals(14.4f, objects.aFloat, 0.0001);
        assertEquals(15.5d, objects.aDouble, 0.0001);
        assertEquals(new BigDecimal("16.6"), objects.aBigDecimal);
        assertEquals("abc", objects.aString);
        assertEquals(Arrays.asList("a", "b", "c"), objects.aList);
        Map<Integer, Double> map = new HashMap<Integer, Double>();
        map.put(1, 1.0);
        map.put(2, 2.0);
        map.put(3, 3.0);
        assertEquals(map, objects.aMap);
        assertEquals(new TreeSet<Short>(Arrays.asList((short)1, (short)2, (short)3)), objects.aSet);
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
    public void testObjectsWithoutDefaultValues() {
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

    @Test
    public void testObjectsWithoutDefaultAreNotReset() {
        String[] args = "-b -y1 -s2 -i3 -l4 -f5 -d6 -bigint=7 -string abc -list a -list b -map 1=2.0 -set 33 -set 22".split(" ");
        Objects objects = new Objects();
        CommandLine cmd = new CommandLine(objects);
        cmd.parseArgs(args);
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

        cmd.parseArgs(); // no args: no invocations, still same value
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
        /*Map<Integer, Double>*/ map = new HashMap<Integer, Double>();
        map.put(1, 2.0);
        assertEquals(map, objects.aMap);
        /*Set<Short>*/ set = new TreeSet<Short>();
        set.add((short) 22);
        set.add((short) 33);
        assertEquals(set, objects.aSet);

//        cmd.parseArgs(); // no args: reset to initial value
//        assertNull(objects.aBoolean);
//        assertNull(objects.aByte);
//        assertNull(objects.aShort);
//        assertNull(objects.anInt);
//        assertNull(objects.aLong);
//        assertNull(objects.aFloat);
//        assertNull(objects.aDouble);
//        assertNull(objects.aBigInteger);
//        assertNull(objects.aString);
//        assertNull(objects.aList);
//        assertNull(objects.aMap);
//        assertNull(objects.aSet);
    }

    static class ObjectsWithNullDefaults {
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

        @Option(names = "-b", defaultValue = Option.NULL_VALUE) void setBoolean(Boolean val) { aBoolean = val; }
        @Option(names = "-y", defaultValue = Option.NULL_VALUE) void setByte(Byte val) { aByte = val; }
        @Option(names = "-s", defaultValue = Option.NULL_VALUE) void setShort(Short val) { aShort = val; }
        @Option(names = "-i", defaultValue = Option.NULL_VALUE) void setInt(Integer val) { anInt = val; }
        @Option(names = "-l", defaultValue = Option.NULL_VALUE) void setLong(Long val) { aLong = val; }
        @Option(names = "-f", defaultValue = Option.NULL_VALUE) void setFloat(Float val) { aFloat = val; }
        @Option(names = "-d", defaultValue = Option.NULL_VALUE) void setDouble(Double val) { aDouble = val; }

        @Option(names = "-bigint", defaultValue = Option.NULL_VALUE) void setBigInteger(BigInteger val) { aBigInteger = val; }
        @Option(names = "-string", defaultValue = Option.NULL_VALUE) void setString(String val) { aString = val; }
        @Option(names = "-list",   defaultValue = Option.NULL_VALUE, split = ",") void setList(List<String> val) { aList = val; }

        @Option(names = "-map", defaultValue = Option.NULL_VALUE, split = ",")
        void setMap(Map<Integer, Double> val) { aMap = val; }

        @Option(names = "-set", defaultValue = Option.NULL_VALUE, split = ",")
        void setSortedSet(SortedSet<Short> val) { aSet = val; }
    }

    @Test
    public void testObjectsWithNullDefaultAreReset() {
        String[] args = "-b -y1 -s2 -i3 -l4 -f5 -d6 -bigint=7 -string abc -list a -list b -map 1=2.0 -set 33 -set 22".split(" ");
        ObjectsWithNullDefaults objects = new ObjectsWithNullDefaults();
        CommandLine cmd = new CommandLine(objects);
        cmd.parseArgs(args);
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

        cmd.parseArgs(); // no args: reset
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
    public void testExceptionFromMethod() {
        class App {
            @Option(names = "--jvm")
            public void jvmException(String value) { throw new IllegalArgumentException("Boo!"); }
        }

        CommandLine parser = new CommandLine(new App());
        try {
            parser.parseArgs("--jvm", "abc");
            fail("Expected exception");
        } catch (ParameterException ex) {
            assertNotNull(ex.getCause());
            assertTrue(ex.getCause() instanceof IllegalArgumentException);
            assertEquals("Boo!", ex.getCause().getMessage());
            assertEquals("Could not invoke public void picocli.OptionMethodImplTest$1App.jvmException(java.lang.String) with abc (java.lang.IllegalArgumentException: Boo!)", ex.getMessage());
        }
    }

    @Test
    public void testParameterExceptionFromMethod() {
        class App {
            @Option(names = "--param")
            public void paramException(String value) { throw new ParameterException(new CommandLine(new App()), "Param!"); }
        }

        CommandLine parser = new CommandLine(new App());
        try {
            parser.parseArgs("--param", "abc");
            fail("Expected exception");
        } catch (ParameterException ex) {
            assertNull(ex.getCause());
            assertEquals("Param!", ex.getMessage());
        }
    }

    @Test
    public void testPicocliExceptionFromMethod() {
        class App {
            @Option(names = "--pico")
            public void picocliException(String value) { throw new PicocliException("Pico!"); }
        }

        CommandLine parser = new CommandLine(new App());
        try {
            parser.parseArgs("--pico", "abc");
            fail("Expected exception");
        } catch (ParameterException ex) {
            assertNotNull(ex.getCause());
            assertTrue(ex.getCause() instanceof PicocliException);
            assertEquals("Pico!", ex.getCause().getMessage());
            assertEquals("PicocliException: Pico! while processing argument at or before arg[1] 'abc' in [--pico, abc]: picocli.CommandLine$PicocliException: Pico!", ex.getMessage());
        }
    }
}
