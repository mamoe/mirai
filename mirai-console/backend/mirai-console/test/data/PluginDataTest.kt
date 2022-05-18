/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.data

import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import net.mamoe.mirai.console.internal.data.MultiFilePluginDataStorageImpl
import net.mamoe.mirai.console.testFramework.AbstractConsoleInstanceTest
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.SingleMessage
import net.mamoe.mirai.message.data.messageChainOf
import net.mamoe.mirai.utils.mapPrimitive
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

internal class PluginDataTest : AbstractConsoleInstanceTest() {
    @TempDir
    lateinit var tempDir: Path

    class MyPluginData : AutoSavePluginData("test") {
        var int by value(1)
        val map: MutableMap<String, String> by value()
        val map2: MutableMap<String, MutableMap<String, String>> by value()
    }

    @Suppress("unused")
    private val jsonPrettyPrint = Json {
        prettyPrint = true
        encodeDefaults = true
    }
    private val json = Json { encodeDefaults = true }

    @Test
    fun testStringify() {
        val data = MyPluginData()

        var string = json.encodeToString(data.updaterSerializer, Unit)
        assertEquals("""{"int":1,"map":{},"map2":{}}""", string)

        data.int = 2

        string = json.encodeToString(data.updaterSerializer, Unit)
        assertEquals("""{"int":2,"map":{},"map2":{}}""", string)
    }

    @Test
    fun testParseUpdate() {
        val data = MyPluginData()

        assertEquals(1, data.int)

        json.decodeFromString(
            data.updaterSerializer, """
                {"int":3,"map":{},"map2":{}}
            """.trimIndent()
        )

        assertEquals(3, data.int)
    }

    @Test
    fun testNestedParseUpdate() {
        val data = MyPluginData()

        fun delegation() = data.map

        val refBefore = data.map
        fun reference() = refBefore

        assertEquals(mutableMapOf(), delegation()) // delegation

        json.decodeFromString(
            data.updaterSerializer, """
                {"int":1,"map":{"t":"test"},"map2":{}}
            """.trimIndent()
        )

        assertEquals(mapOf("t" to "test").toString(), delegation().toString())
        assertEquals(mapOf("t" to "test").toString(), reference().toString())

        assertSame(reference(), delegation()) // check shadowing
    }

    @Test
    fun testDeepNestedParseUpdate() {
        val data = MyPluginData()

        fun delegation() = data.map2

        val refBefore = data.map2
        fun reference() = refBefore

        assertEquals(mutableMapOf(), delegation()) // delegation

        json.decodeFromString(
            data.updaterSerializer, """
                {"int":1,"map":{},"map2":{"t":{"f":"test"}}}
            """.trimIndent()
        )

        assertEquals(mapOf("t" to mapOf("f" to "test")).toString(), delegation().toString())
        assertEquals(mapOf("t" to mapOf("f" to "test")).toString(), reference().toString())

        assertSame(reference(), delegation()) // check shadowing
    }

    @Test
    fun testDeepNestedTrackingParseUpdate() {
        val data = MyPluginData()

        data.map2["t"] = mutableMapOf()

        fun delegation() = data.map2["t"]!!

        val refBefore = data.map2["t"]!!
        fun reference() = refBefore

        assertEquals(mutableMapOf(), delegation()) // delegation

        json.decodeFromString(
            data.updaterSerializer, """
                {"int":1,"map":{},"map2":{"t":{"f":"test"}}}
            """.trimIndent()
        )

        assertEquals(mapOf("f" to "test").toString(), delegation().toString())
        assertEquals(mapOf("f" to "test").toString(), reference().toString())

        assertSame(reference(), delegation()) // check shadowing
    }


    class SupportsMessageChain : AutoSavePluginData("test") {
        val chain: MessageChain by value(messageChainOf(PlainText("str")))
    }

    @Test
    fun `supports message chain`() {
        assertEquals(
            """
            chain: 
              - type: PlainText
                value: 
                  content: str
        """.trimIndent(), serializePluginData(SupportsMessageChain())
        )
        serializeAndRereadPluginData(SupportsMessageChain())
    }

    class SupportsPolymorphicCorrectly : AutoSavePluginData("test") {
        val singleMessage: SingleMessage by value(PlainText("str"))
        val plainText: PlainText by value(PlainText("str"))
    }

    @Test
    fun `supports polymorphic correctly`() {
        assertEquals(
            """
            singleMessage: 
              type: PlainText
              value: 
                content: str
            plainText: 
              content: str
        """.trimIndent(), serializePluginData(SupportsPolymorphicCorrectly())
        )
        serializeAndRereadPluginData(SupportsPolymorphicCorrectly())
    }

    class SupportsSerializersModule : AutoSavePluginData("test") {
        override val serializersModule: SerializersModule = SerializersModule {
            contextual(MyClass::class, myClassSerializer)
        }

        val v: MyClass by value(MyClass("test"))

        data class MyClass(
            val str: String
        )

        companion object {
            private val myClassSerializer = String.serializer().mapPrimitive("MyClass",
                { MyClass(it) },
                { it.str }
            )
        }
    }

    @Test
    fun `supports serializers module`() {
        assertEquals(
            """
            v: test
        """.trimIndent(), serializePluginData(SupportsSerializersModule())
        )
        serializeAndRereadPluginData(SupportsSerializersModule())
    }


    private fun serializePluginData(data: PluginData): String {
        val storage = MultiFilePluginDataStorageImpl(tempDir)
        storage.store(mockPlugin, data)
        return storage.getPluginDataFileInternal(mockPlugin, data).readText()
    }

    private fun serializeAndRereadPluginData(data: PluginData) {
        val storage = MultiFilePluginDataStorageImpl(tempDir)
        storage.store(mockPlugin, data)
        val serialized = storage.getPluginDataFileInternal(mockPlugin, data).readText()
        storage.load(mockPlugin, data)
        assertEquals(serialized, storage.getPluginDataFileInternal(mockPlugin, data).readText())
    }


    class DefaultValueForArray : AutoSavePluginData("save") {
        val byteArray: ByteArray by value()
        val booleanArray: BooleanArray by value()
        var shortArray: ShortArray by value()
        val intArray: IntArray by value()
        val longArray: LongArray by value()
        val floatArray: FloatArray by value()
        val doubleArray: DoubleArray by value()
        val charArray: CharArray by value()

        var stringArray: Array<String> by value()
        var longObjectArray: Array<Long> by value()
    }

    @Test
    fun `default value for array`() {
        val instance = DefaultValueForArray()
        assertEquals(
            """
                byteArray: []
                booleanArray: []
                shortArray: []
                intArray: []
                longArray: []
                floatArray: []
                doubleArray: []
                charArray: []
                stringArray: []
                longObjectArray: []
            """.trimIndent(),
            serializePluginData(instance)
        )
        instance.shortArray = shortArrayOf(1)
        instance.stringArray = arrayOf("1234")
        println(instance.findBackingFieldValueNode(instance::longObjectArray))
        instance.longObjectArray = arrayOf(1234)
        assertEquals(
            """
                byteArray: []
                booleanArray: []
                shortArray: 
                  - 1
                intArray: []
                longArray: []
                floatArray: []
                doubleArray: []
                charArray: []
                stringArray: 
                  - 1234
                longObjectArray: 
                  - 1234
            """.trimIndent(),
            serializePluginData(instance)
        )
        serializeAndRereadPluginData(instance)
    }

    class DefaultValueForCollections : AutoSavePluginData("save") {
        val map: Map<String, String> by value()
        val mapAny: Map<String, Any> by value()
        val hashMapAny: HashMap<String, Any> by value()
        val linkedHashMapAny: LinkedHashMap<String, Any> by value()

        val list: List<String> by value()
        val listAny: List<Any> by value()

        val set: Set<String> by value()
        val setAny: Set<Any> by value()
    }

    @Test
    fun `default value for collections`() {
        val instance = DefaultValueForCollections()
        assertEquals(
            """
                map: {}

                mapAny: {}

                hashMapAny: {}

                linkedHashMapAny: {}

                list: []
                listAny: []
                set: []
                setAny: []
            """.trimIndent(),
            serializePluginData(instance)
        )
        serializeAndRereadPluginData(instance)
    }
}
