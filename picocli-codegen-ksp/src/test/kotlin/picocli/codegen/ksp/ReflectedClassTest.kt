package picocli.codegen.ksp

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ReflectedClassTest {

    @Test
    fun `empty class produces minimal JSON`() {
        val json = ReflectedClass("com.example.MyCommand").toJson()

        assertTrue(json.contains("\"name\" : \"com.example.MyCommand\""))
        assertTrue(json.contains("\"allDeclaredConstructors\" : true"))
        assertTrue(json.contains("\"allPublicConstructors\" : true"))
        assertTrue(json.contains("\"allDeclaredMethods\" : true"))
        assertTrue(json.contains("\"allPublicMethods\" : true"))
        assertFalse(json.contains("fields"))
        assertFalse(json.contains("methods"))
    }

    @Test
    fun `class with mutable field has no allowWrite`() {
        val cls = ReflectedClass("com.example.Cmd").addField("verbose", isFinal = false)
        val json = cls.toJson()

        assertTrue(json.contains("\"name\" : \"verbose\""))
        assertFalse(json.contains("allowWrite"))
    }

    @Test
    fun `class with final field has allowWrite true`() {
        val cls = ReflectedClass("com.example.Cmd").addField("port", isFinal = true)
        val json = cls.toJson()

        assertTrue(json.contains("\"name\" : \"port\""))
        assertTrue(json.contains("\"allowWrite\" : true"))
    }

    @Test
    fun `class with method serialises parameter types`() {
        val cls = ReflectedClass("com.example.Cmd")
            .addMethod("setVerbose", listOf("boolean"))
        val json = cls.toJson()

        assertTrue(json.contains("\"name\" : \"setVerbose\""))
        assertTrue(json.contains("\"parameterTypes\" : [\"boolean\"]"))
    }

    @Test
    fun `class with no-arg method serialises empty parameterTypes array`() {
        val cls = ReflectedClass("com.example.Cmd").addMethod("call", emptyList())
        val json = cls.toJson()

        assertTrue(json.contains("\"parameterTypes\" : []"))
    }

    @Test
    fun `nested class uses dollar sign separator`() {
        val cls = ReflectedClass("com.example.Outer\$Inner")
        val json = cls.toJson()

        assertTrue(json.contains("com.example.Outer\$Inner"))
    }

    @Test
    fun `fields are sorted alphabetically`() {
        val cls = ReflectedClass("com.example.Cmd")
            .addField("zzz", false)
            .addField("aaa", false)
            .addField("mmm", false)
        val json = cls.toJson()

        val aaaIdx = json.indexOf("\"aaa\"")
        val mmmIdx = json.indexOf("\"mmm\"")
        val zzzIdx = json.indexOf("\"zzz\"")
        assertTrue(aaaIdx < mmmIdx && mmmIdx < zzzIdx, "Fields should be sorted alphabetically")
    }

    @Test
    fun `reflected field toJson without allowWrite`() {
        val f = ReflectedField("verbose", isFinal = false)
        assertEquals("{\"name\" : \"verbose\"}", f.toJson())
    }

    @Test
    fun `reflected field toJson with allowWrite`() {
        val f = ReflectedField("port", isFinal = true)
        assertEquals("{\"name\" : \"port\", \"allowWrite\" : true}", f.toJson())
    }

    @Test
    fun `reflected method toJson with multiple params`() {
        val m = ReflectedMethod("connect", listOf("java.lang.String", "int"))
        assertEquals("{\"name\" : \"connect\", \"parameterTypes\" : [\"java.lang.String\", \"int\"]}", m.toJson())
    }
}
