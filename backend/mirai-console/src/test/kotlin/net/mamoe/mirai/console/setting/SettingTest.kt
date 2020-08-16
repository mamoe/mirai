/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.setting

import kotlinx.serialization.json.Json
import net.mamoe.mirai.console.util.ConsoleInternalAPI
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

@OptIn(ConsoleInternalAPI::class)
internal class SettingTest {

    class MySetting : AbstractSetting() {
        var int by value(1)
        val map by value<MutableMap<String, String>>()
        val map2 by value<MutableMap<String, MutableMap<String, String>>>()

        @ConsoleInternalAPI
        override fun onValueChanged(value: Value<*>) {

        }

        override fun setStorage(storage: SettingStorage) {
        }
    }

    private val jsonPrettyPrint = Json { prettyPrint = true }
    private val json = Json {}

    @Test
    fun testStringify() {
        val setting = MySetting()

        var string = json.encodeToString(setting.updaterSerializer, Unit)
        assertEquals("""{"int":1,"map":{},"map2":{}}""", string)

        setting.int = 2

        string = json.encodeToString(setting.updaterSerializer, Unit)
        assertEquals("""{"int":2,"map":{},"map2":{}}""", string)
    }

    @Test
    fun testParseUpdate() {
        val setting = MySetting()

        assertEquals(1, setting.int)

        json.decodeFromString(
            setting.updaterSerializer, """
                {"int":3,"map":{},"map2":{}}
            """.trimIndent()
        )

        assertEquals(3, setting.int)
    }

    @Test
    fun testNestedParseUpdate() {
        val setting = MySetting()

        fun delegation() = setting.map

        val refBefore = setting.map
        fun reference() = refBefore

        assertEquals(mutableMapOf(), delegation()) // delegation

        json.decodeFromString(
            setting.updaterSerializer, """
                {"int":1,"map":{"t":"test"},"map2":{}}
            """.trimIndent()
        )

        assertEquals(mapOf("t" to "test").toString(), delegation().toString())
        assertEquals(mapOf("t" to "test").toString(), reference().toString())

        assertSame(reference(), delegation()) // check shadowing
    }

    @Test
    fun testDeepNestedParseUpdate() {
        val setting = MySetting()

        fun delegation() = setting.map2

        val refBefore = setting.map2
        fun reference() = refBefore

        assertEquals(mutableMapOf(), delegation()) // delegation

        json.decodeFromString(
            setting.updaterSerializer, """
                {"int":1,"map":{},"map2":{"t":{"f":"test"}}}
            """.trimIndent()
        )

        assertEquals(mapOf("t" to mapOf("f" to "test")).toString(), delegation().toString())
        assertEquals(mapOf("t" to mapOf("f" to "test")).toString(), reference().toString())

        assertSame(reference(), delegation()) // check shadowing
    }

    @Test
    fun testDeepNestedTrackingParseUpdate() {
        val setting = MySetting()

        setting.map2["t"] = mutableMapOf()

        fun delegation() = setting.map2["t"]!!

        val refBefore = setting.map2["t"]!!
        fun reference() = refBefore

        assertEquals(mutableMapOf(), delegation()) // delegation

        json.decodeFromString(
            setting.updaterSerializer, """
                {"int":1,"map":{},"map2":{"t":{"f":"test"}}}
            """.trimIndent()
        )

        assertEquals(mapOf("f" to "test").toString(), delegation().toString())
        assertEquals(mapOf("f" to "test").toString(), reference().toString())

        assertSame(reference(), delegation()) // check shadowing
    }
}
