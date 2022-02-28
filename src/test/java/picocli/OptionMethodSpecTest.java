package picocli;

import org.junit.*;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;

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

import static org.junit.Assert.*;
import static picocli.CommandLine.*;

public class OptionMethodSpecTest {

    // allows tests to set any kind of properties they like, without having to individually roll them back
    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");

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

    @Test
    public void testInterfaceIsInstantiated() {
        CommandLine cmd = new CommandLine(Primitives.class);
        assertTrue(((Object) cmd.getCommand()) instanceof Primitives);
    }

    @Test
    public void testPrimitiveWithoutDefaultValues() {
        CommandLine cmd = new CommandLine(Primitives.class);
        cmd.parseArgs();
        Primitives primitives = cmd.getCommand();
        assertFalse(primitives.aBoolean());
        assertEquals(0, primitives.aByte());
        assertEquals((short) 0, primitives.aShort());
        assertEquals(0, primitives.anInt());
        assertEquals(0, primitives.aLong());
        assertEquals(0, primitives.aFloat(), 0.0001);
        assertEquals(0, primitives.aDouble(), 0.0001);
    }

    @Test
    public void testPrimitivesWithDefaultValues() {
        CommandLine cmd = new CommandLine(PrimitivesWithDefault.class);
        cmd.parseArgs();
        PrimitivesWithDefault primitives = cmd.getCommand();
        assertTrue(primitives.aBoolean());
        assertEquals(11, primitives.aByte());
        assertEquals((short) 12, primitives.aShort());
        assertEquals(13, primitives.anInt());
        assertEquals(14, primitives.aLong());
        assertEquals(15.5f, primitives.aFloat(), 0.0001);
        assertEquals(16.6d, primitives.aDouble(), 0.0001);
    }

    @Test
    public void testPrimitives() {
        CommandLine cmd = new CommandLine(Primitives.class);
        cmd.parseArgs("-b -y1 -s2 -i3 -l4 -f5 -d6".split(" "));
        Primitives primitives = cmd.getCommand();
        assertTrue(primitives.aBoolean());
        assertEquals(1, primitives.aByte());
        assertEquals(2, primitives.aShort());
        assertEquals(3, primitives.anInt());
        assertEquals(4, primitives.aLong());
        assertEquals(5, primitives.aFloat(), 0.0001);
        assertEquals(6, primitives.aDouble(), 0.0001);
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

    @Test
    public void testObjectsWithoutDefaultValues() {
        CommandLine cmd = new CommandLine(Objects.class);
        cmd.parseArgs();
        Objects objects = cmd.getCommand();
        assertNull(objects.aBoolean());
        assertNull(objects.aByte());
        assertNull(objects.aShort());
        assertNull(objects.anInt());
        assertNull(objects.aLong());
        assertNull(objects.aFloat());
        assertNull(objects.aDouble());
        assertNull(objects.aBigInteger());
        assertNull(objects.aString());
        assertNull(objects.getList());
        assertNull(objects.getMap());
        assertNull(objects.getSortedSet());
    }

    @Test
    public void testObjectsWithDefaultValues() {
        CommandLine cmd = new CommandLine(ObjectsWithDefault.class);
        cmd.parseArgs();
        ObjectsWithDefault objects = cmd.getCommand();
        assertTrue(objects.aBoolean());
        assertEquals(Byte.valueOf((byte) 123), objects.aByte());
        assertEquals(Short.valueOf((short) 11), objects.aShort());
        assertEquals(Integer.valueOf(12), objects.anInt());
        assertEquals(Long.valueOf(13), objects.aLong());
        assertEquals(14.4f, objects.aFloat(), 0.0001);
        assertEquals(15.5d, objects.aDouble(), 0.0001);
        assertEquals(new BigDecimal("16.6"), objects.aBigDecimal());
        assertEquals("abc", objects.aString());
        assertEquals(Arrays.asList("a", "b", "c"), objects.getList());
        Map<Integer, Double> map = new HashMap<Integer, Double>();
        map.put(1, 1.0);
        map.put(2, 2.0);
        map.put(3, 3.0);
        assertEquals(map, objects.getMap());
        assertEquals(new TreeSet<Short>(Arrays.asList((short)1, (short)2, (short)3)), objects.getSortedSet());
    }

    @Test
    public void testObjects() {
        CommandLine cmd = new CommandLine(Objects.class);
        cmd.parseArgs("-b -y1 -s2 -i3 -l4 -f5 -d6 -bigint=7 -string abc -list a -list b -map 1=2.0 -set 33 -set 22".split(" "));
        Objects objects = cmd.getCommand();
        assertTrue(objects.aBoolean());
        assertEquals(Byte.valueOf((byte) 1), objects.aByte());
        assertEquals(Short.valueOf((short) 2), objects.aShort());
        assertEquals(Integer.valueOf(3), objects.anInt());
        assertEquals(Long.valueOf(4), objects.aLong());
        assertEquals(5f, objects.aFloat(), 0.0001);
        assertEquals(6d, objects.aDouble(), 0.0001);
        assertEquals(BigInteger.valueOf(7), objects.aBigInteger());
        assertEquals("abc", objects.aString());
        assertEquals(Arrays.asList("a", "b"), objects.getList());
        Map<Integer, Double> map = new HashMap<Integer, Double>();
        map.put(1, 2.0);
        assertEquals(map, objects.getMap());
        Set<Short> set = new TreeSet<Short>();
        set.add((short) 22);
        set.add((short) 33);
        assertEquals(set, objects.getSortedSet());
    }

    @Test
    public void testObjectsWithoutDefaultAreNotReset() {
        //TestUtil.setTraceLevel(CommandLine.TraceLevel.DEBUG);
        String[] args = "-b -y1 -s2 -i3 -l4 -f5 -d6 -bigint=7 -string abc -list a -list b -map 1=2.0 -set 33 -set 22".split(" ");
        CommandLine cmd = new CommandLine(Objects.class);
        cmd.parseArgs(args);
        Objects objects = cmd.getCommand();
        assertTrue(objects.aBoolean());
        assertEquals(Byte.valueOf((byte) 1), objects.aByte());
        assertEquals(Short.valueOf((short) 2), objects.aShort());
        assertEquals(Integer.valueOf(3), objects.anInt());
        assertEquals(Long.valueOf(4), objects.aLong());
        assertEquals(5f, objects.aFloat(), 0.0001);
        assertEquals(6d, objects.aDouble(), 0.0001);
        assertEquals(BigInteger.valueOf(7), objects.aBigInteger());
        assertEquals("abc", objects.aString());
        assertEquals(Arrays.asList("a", "b"), objects.getList());
        Map<Integer, Double> map = new HashMap<Integer, Double>();
        map.put(1, 2.0);
        assertEquals(map, objects.getMap());
        Set<Short> set = new TreeSet<Short>();
        set.add((short) 22);
        set.add((short) 33);
        assertEquals(set, objects.getSortedSet());

//        cmd.parseArgs(); // no args: no invocations, still same value
//        assertTrue(objects.aBoolean());
//        assertEquals(Byte.valueOf((byte) 1), objects.aByte());
//        assertEquals(Short.valueOf((short) 2), objects.aShort());
//        assertEquals(Integer.valueOf(3), objects.anInt());
//        assertEquals(Long.valueOf(4), objects.aLong());
//        assertEquals(5f, objects.aFloat(), 0.0001);
//        assertEquals(6d, objects.aDouble(), 0.0001);
//        assertEquals(BigInteger.valueOf(7), objects.aBigInteger());
//        assertEquals("abc", objects.aString());
//        assertEquals(Arrays.asList("a", "b"), objects.getList());
//        /*Map<Integer, Double>*/ map = new HashMap<Integer, Double>();
//        map.put(1, 2.0);
//        assertEquals(map, objects.getMap());
//        /*Set<Short>*/ set = new TreeSet<Short>();
//        set.add((short) 22);
//        set.add((short) 33);
//        assertEquals(set, objects.getSortedSet());

        cmd.parseArgs(); // no args: reset to initial value
        assertNull(objects.aBoolean());
        assertNull(objects.aByte());
        assertNull(objects.aShort());
        assertNull(objects.anInt());
        assertNull(objects.aLong());
        assertNull(objects.aFloat());
        assertNull(objects.aDouble());
        assertNull(objects.aBigInteger());
        assertNull(objects.aString());
        assertNull(objects.getList());
        assertNull(objects.getMap());
        assertNull(objects.getSortedSet());
    }

    interface ObjectsWithNullDefaults {
        @Option(names = "-b", defaultValue = Option.NULL_VALUE)
        Boolean aBoolean();

        @Option(names = "-y", defaultValue = Option.NULL_VALUE)
        Byte aByte();

        @Option(names = "-s", defaultValue = Option.NULL_VALUE)
        Short aShort();

        @Option(names = "-i", defaultValue = Option.NULL_VALUE)
        Integer anInt();

        @Option(names = "-l", defaultValue = Option.NULL_VALUE)
        Long aLong();

        @Option(names = "-f", defaultValue = Option.NULL_VALUE)
        Float aFloat();

        @Option(names = "-d", defaultValue = Option.NULL_VALUE)
        Double aDouble();

        @Option(names = "-bigint", defaultValue = Option.NULL_VALUE)
        BigInteger aBigInteger();

        @Option(names = "-string", defaultValue = Option.NULL_VALUE)
        String aString();

        @Option(names = "-list", defaultValue = Option.NULL_VALUE, split = ",")
        List<String> getList();

        @Option(names = "-map", defaultValue = Option.NULL_VALUE, split = ",")
        Map<Integer, Double> getMap();

        @Option(names = "-set", defaultValue = Option.NULL_VALUE, split = ",")
        SortedSet<Short> getSortedSet();
    }

    @Test
    public void testObjectsWithNullDefaultAreReset() {
        String[] args = "-b -y1 -s2 -i3 -l4 -f5 -d6 -bigint=7 -string abc -list a -list b -map 1=2.0 -set 33 -set 22".split(" ");
        CommandLine cmd = new CommandLine(ObjectsWithNullDefaults.class);
        cmd.parseArgs(args);
        ObjectsWithNullDefaults objects = cmd.getCommand();
        assertTrue(objects.aBoolean());
        assertEquals(Byte.valueOf((byte) 1), objects.aByte());
        assertEquals(Short.valueOf((short) 2), objects.aShort());
        assertEquals(Integer.valueOf(3), objects.anInt());
        assertEquals(Long.valueOf(4), objects.aLong());
        assertEquals(5f, objects.aFloat(), 0.0001);
        assertEquals(6d, objects.aDouble(), 0.0001);
        assertEquals(BigInteger.valueOf(7), objects.aBigInteger());
        assertEquals("abc", objects.aString());
        assertEquals(Arrays.asList("a", "b"), objects.getList());
        Map<Integer, Double> map = new HashMap<Integer, Double>();
        map.put(1, 2.0);
        assertEquals(map, objects.getMap());
        Set<Short> set = new TreeSet<Short>();
        set.add((short) 22);
        set.add((short) 33);
        assertEquals(set, objects.getSortedSet());

        cmd.parseArgs(); // no args: reset
        assertNull(objects.aBoolean());
        assertNull(objects.aByte());
        assertNull(objects.aShort());
        assertNull(objects.anInt());
        assertNull(objects.aLong());
        assertNull(objects.aFloat());
        assertNull(objects.aDouble());
        assertNull(objects.aBigInteger());
        assertNull(objects.aString());
        assertNull(objects.getList());
        assertNull(objects.getMap());
        assertNull(objects.getSortedSet());
    }

    interface InvalidAnnotatedStringOrPrimitiveFields {
        @Option(names = "-i")
        int anInt = 0;

        @Option(names = "-s")
        String aString = null;
    }

    @Test
    public void testInvalidAnnotatedFieldsOnInterface() {
        try {
            new CommandLine(InvalidAnnotatedStringOrPrimitiveFields.class);
            fail("Expected exception");
        } catch (InitializationException ok) {
            assertEquals("Invalid picocli annotation on interface field", ok.getMessage());
        }
    }

    interface InvalidAnnotatedMutableFields {
        @Option(names = "-s")
        final List<String> aList = new ArrayList<String>();
    }

    @Test
    public void testAnnotatedMutableFieldsOnInterfaceAreValid() {
        try {
            CommandLine cmd = new CommandLine(InvalidAnnotatedMutableFields.class);
            cmd.parseArgs("-s a -s b -s c".split(" "));
            fail("Expected exception");
        } catch (InitializationException ok) {
            assertEquals("Invalid picocli annotation on interface field", ok.getMessage());
        }
    }

    @Test
    public void testPopulateSpec() {
        Objects objects = CommandLine.populateSpec(Objects.class, "-b -y1 -s2 -i3 -l4 -f5 -d6 -bigint=7 -string abc -list a -list b -map 1=2.0 -set 33 -set 22".split(" "));
        assertTrue(objects.aBoolean());
        assertEquals(Byte.valueOf((byte) 1), objects.aByte());
        assertEquals(Short.valueOf((short) 2), objects.aShort());
        assertEquals(Integer.valueOf(3), objects.anInt());
        assertEquals(Long.valueOf(4), objects.aLong());
        assertEquals(5f, objects.aFloat(), 0.0001);
        assertEquals(6d, objects.aDouble(), 0.0001);
        assertEquals(BigInteger.valueOf(7), objects.aBigInteger());
        assertEquals("abc", objects.aString());
        assertEquals(Arrays.asList("a", "b"), objects.getList());
        Map<Integer, Double> map = new HashMap<Integer, Double>();
        map.put(1, 2.0);
        assertEquals(map, objects.getMap());
        Set<Short> set = new TreeSet<Short>();
        set.add((short) 22);
        set.add((short) 33);
        assertEquals(set, objects.getSortedSet());
    }
}
