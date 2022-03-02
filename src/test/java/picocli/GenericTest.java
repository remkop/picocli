package picocli;

import org.junit.Test;
import picocli.CommandLine.MissingTypeConverterException;
import picocli.CommandLine.UseDefaultConverter;
import picocli.CommandLine.Option;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static picocli.CommandLine.ITypeConverter;
import static picocli.CommandLine.Parameters;

public class GenericTest {

    static final class GenericValue<T> {
        private final T value;

        public GenericValue(T value) {
            this.value = value;
        }

        public T getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "GenericValue{" +
                    "value=" + value +
                    '}';
        }
    }

    static final class GenericValueConverter implements ITypeConverter<GenericValue<?>> {
        public GenericValue<?> convert(String value) throws Exception {
            if (value.startsWith("s")) {
                return new GenericValue<String>(value);
            }
            return new GenericValue<Integer>(Integer.parseInt(value));
        }
    }

    @Test
    public void testListOfGenericClasses() {
        class App {
            @Parameters(arity = "0..*", converter = GenericValueConverter.class)
            List<GenericValue<?>> values;
        }
        App app = CommandLine.populateCommand(new App(), "sOne", "15");
        assertEquals(2, app.values.size());
        assertEquals(String.class, app.values.get(0).value.getClass());
        assertEquals("sOne", app.values.get(0).value);
        assertEquals(Integer.class, app.values.get(1).value.getClass());
        assertEquals(15, app.values.get(1).value);
    }

    @Test
    public void testListOfGenericClassesNoConverter() {
        class NoConverterApp {
            @Parameters(arity = "0..*")
            List<GenericValue<?>> values;
        }
        try {
            CommandLine.populateCommand(new NoConverterApp(), "sOne", "15");
            fail("Expected exception");
        } catch (MissingTypeConverterException ex) {
            String j8errMsg = "No TypeConverter registered for picocli.GenericTest$GenericValue of field java.util.List<picocli.GenericTest.picocli.GenericTest$GenericValue<?>> picocli.GenericTest$1NoConverterApp.values";
            String j11errMsg = "No TypeConverter registered for picocli.GenericTest$GenericValue of field java.util.List<picocli.GenericTest$GenericValue<?>> picocli.GenericTest$1NoConverterApp.values";
            assertTrue(ex.getMessage(),j8errMsg.equals(ex.getMessage()) || j11errMsg.equals(ex.getMessage()));
        }
    }

    @Test
    public void testListOfGenericWithGlobalConverter() {
        class NoConverterApp {
            @Parameters(arity = "0..*")
            List<GenericValue<?>> values;
        }
        @SuppressWarnings("rawtypes")
        class GenericConverter implements ITypeConverter<GenericValue> {
            @SuppressWarnings("unchecked")
            public GenericValue convert(String value) throws Exception {
                return new GenericValue("abc");
            }
        }
        NoConverterApp app = new NoConverterApp();
        new CommandLine(app).registerConverter(GenericValue.class, new GenericConverter()).parseArgs("xyz");
        assertEquals(1, app.values.size());
        assertEquals(String.class, app.values.get(0).value.getClass());
        assertEquals("abc", app.values.get(0).value);
    }

    @Test
    public void testMapOfGenericClasses() {
        class App {
            @Option(names = "-D", converter = {UseDefaultConverter.class, GenericValueConverter.class})
            Map<String, GenericValue<?>> values;
        }
        App app = CommandLine.populateCommand(new App(), "-Dkey1=sOne", "-Dkey2=15");
        assertEquals(2, app.values.size());
        assertEquals(String.class, app.values.get("key1").value.getClass());
        assertEquals("sOne", app.values.get("key1").value);
        assertEquals(Integer.class, app.values.get("key2").value.getClass());
        assertEquals(15, app.values.get("key2").value);
    }

    @Test
    public void testSingleValueGenericWildcardClass() {
        class App {
            @Parameters(converter = GenericValueConverter.class)
            GenericValue<?> value;
        }
        App app = CommandLine.populateCommand(new App(), "sTest");
        assertEquals(String.class, app.value.value.getClass());
        assertEquals("sTest", app.value.value);
    }

    @Test
    public void testSingleValueGenericConcreteClass() {
        class App {
            @Parameters(converter = GenericValueConverter.class)
            GenericValue<Integer> value;
        }
        App app = CommandLine.populateCommand(new App(), "3");
        assertEquals(Integer.class, app.value.value.getClass());
        assertEquals(Integer.valueOf(3), app.value.value);
    }

}
