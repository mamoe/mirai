/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.data

import kotlinx.serialization.json.Json
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.console.plugin.jvm.SimpleJvmPluginDescription
import net.mamoe.mirai.console.util.ConsoleInternalAPI
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

@OptIn(ConsoleInternalAPI::class)
internal class PluginDataTest {

    object MyPlugin : KotlinPlugin(
        SimpleJvmPluginDescription(
            "1", "2"
        )
    )

    class MyPluginData : AutoSavePluginData() {
        var int by value(1)
        val map: MutableMap<String, String> by value()
        val map2: MutableMap<String, MutableMap<String, String>> by value()
    }

    private val jsonPrettyPrint = Json { prettyPrint = true }
    private val json = Json {}

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
}
