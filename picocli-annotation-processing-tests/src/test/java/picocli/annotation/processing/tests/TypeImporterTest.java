package picocli.annotation.processing.tests;

import org.junit.Test;

import static org.junit.Assert.*;

public class TypeImporterTest {

    String[][] data = {
            {"java.util.Map<java.lang.reflect.Constructor<?>, java.lang.String[]>", "Map<Constructor<?>, String[]>"},
            {"my.pkg.MethodAnalysisClassVisitor<T, java.lang.reflect.Constructor<? extends java.math.BigDecimal>>", "MethodAnalysisClassVisitor<T, Constructor<? extends BigDecimal>>"},
            {"my.pkg.util.IterableToCollectionSelector<E, T\textends\njava.util.Iterable<?\textends\nE>, C extends my.pkg.util.Condition1<? super E>, R extends java.util.Collection<E>>",
                    "IterableToCollectionSelector<E, T\textends\nIterable<?\textends\nE>, C extends Condition1<? super E>, R extends Collection<E>>"},
            {"java.util.List<my.pkg.XsuperT>", "List<XsuperT>"},
            {"int", "int"},
            {"void", "void"},
    };

    @Test
    public void getImportedName() {
        TypeImporter importer = new TypeImporter("a.b.c");
        for (String[] tuple : data) {
            String actual = importer.getImportedName(tuple[0]);
            assertEquals(tuple[1], actual);
        }
    }

    @Test
    public void createImportDeclaration() {
        TypeImporter importer = new TypeImporter("a.b.c");
        for (String[] tuple : data) {
            importer.getImportedName(tuple[0]);
        }
        String expected = String.format("" +
                "%n" +
                "import java.lang.reflect.Constructor;%n" +
                "import java.math.BigDecimal;%n" +
                "import java.util.Collection;%n" +
                "import java.util.Iterable;%n" +
                "import java.util.List;%n" +
                "import java.util.Map;%n" +
                "import my.pkg.MethodAnalysisClassVisitor;%n" +
                "import my.pkg.XsuperT;%n" +
                "import my.pkg.util.Condition1;%n" +
                "import my.pkg.util.IterableToCollectionSelector;");
        assertEquals(expected, importer.createImportDeclaration());
    }

    @Test
    public void createImportDeclaration1() {
        TypeImporter importer = new TypeImporter("my.pkg");
        for (String[] tuple : data) {
            importer.getImportedName(tuple[0]);
        }
        String expected = "" +
                "\n" +
                "import java.lang.reflect.Constructor;\n" +
                "import java.math.BigDecimal;\n" +
                "import java.util.Collection;\n" +
                "import java.util.Iterable;\n" +
                "import java.util.List;\n" +
                "import java.util.Map;\n" +
                "import my.pkg.util.Condition1;\n" +
                "import my.pkg.util.IterableToCollectionSelector;";
        assertEquals(expected, importer.createImportDeclaration("\n"));
    }
}