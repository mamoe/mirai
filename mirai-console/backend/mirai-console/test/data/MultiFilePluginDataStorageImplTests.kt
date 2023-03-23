/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import net.mamoe.mirai.console.internal.data.MultiFilePluginDataStorageImpl
import net.mamoe.mirai.console.testFramework.AbstractConsoleInstanceTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.test.assertEquals

internal class MultiFilePluginDataStorageImplTests : AbstractConsoleInstanceTest() {
    @TempDir
    internal lateinit var storePath: Path

    @Serializable
    @JsonClassDiscriminator("base_type")
    internal sealed class Base // not using interface, see https://github.com/Kotlin/kotlinx.serialization/issues/2181

    @Serializable
    @SerialName("DerivedA")
    internal data class DerivedA(val valueA: Double) : Base()

    @Serializable
    @SerialName("DerivedB")
    internal data class DerivedB(val valueB: String) : Base()

    @Serializable
    @SerialName("DerivedC")
    internal object DerivedC : Base() {
        @Suppress("unused")
        const val valueC: Int = 42
    }

    private class YamlPluginData : AutoSavePluginData("test_yaml") {
        var int by value(1)
        val map: MutableMap<String, String> by value()
        val map2: MutableMap<String, MutableMap<String, String>> by value()

        companion object {
            val string = """
                int: 2
                map: 
                  key1: value1
                  key2: value2
                map2: 
                  key1: 
                    key1: value1
                    key2: value2
                  key2: 
                    key1: value1
                    key2: value2
            """.trimIndent()
        }
    }

    private class JsonPluginData : AutoSavePluginData("test_json") {
        override val saveType = PluginData.SaveType.JSON

        val baseMap: MutableMap<String, Base> by value()

        companion object {
            val string = """
                {
                    "baseMap": {
                        "A": {
                            "base_type": "DerivedA",
                            "valueA": 11.4514
                        },
                        "B": {
                            "base_type": "DerivedB",
                            "valueB": "mamoe.mirai"
                        },
                        "C": {
                            "base_type": "DerivedC"
                        }
                    }
                }
            """.trimIndent()
        }
    }

    private val dataStorage by lazy { MultiFilePluginDataStorageImpl(storePath) }

    @Test
    fun testYamlLoad() {
        val data = YamlPluginData()
        dataStorage.load(mockPlugin, data)
        dataStorage.getPluginDataFileInternal(mockPlugin, data).writeText(YamlPluginData.string)
        dataStorage.load(mockPlugin, data)

        assertEquals(2, data.int)
        assertEquals(mapOf("key1" to "value1", "key2" to "value2"), data.map)
        assertEquals(
            mapOf(
                "key1" to mapOf("key1" to "value1", "key2" to "value2"),
                "key2" to mapOf("key1" to "value1", "key2" to "value2")
            ), data.map2
        )
    }

    @Test
    fun testYamlStore() {
        val data = YamlPluginData()
        dataStorage.load(mockPlugin, data)

        data.int = 2
        data.map["key1"] = "value1"
        data.map["key2"] = "value2"
        data.map2["key1"] = mutableMapOf("key1" to "value1", "key2" to "value2")
        data.map2["key2"] = mutableMapOf("key1" to "value1", "key2" to "value2")

        dataStorage.store(mockPlugin, data)

        val file = dataStorage.getPluginDataFileInternal(mockPlugin, data)
        assertEquals(YamlPluginData.string, file.readText())
    }

    @Test
    fun testJsonLoad() {
        val data = JsonPluginData()
        dataStorage.load(mockPlugin, data)
        dataStorage.getPluginDataFileInternal(mockPlugin, data).writeText(JsonPluginData.string)
        dataStorage.load(mockPlugin, data)

        assertEquals(
            mapOf(
                "A" to DerivedA(11.4514),
                "B" to DerivedB("mamoe.mirai"),
                "C" to DerivedC
            ), data.baseMap
        )
    }

    @Test
    fun testJsonStore() {
        val data = JsonPluginData()
        dataStorage.load(mockPlugin, data)

        data.baseMap["A"] = DerivedA(11.4514)
        data.baseMap["B"] = DerivedB("mamoe.mirai")
        data.baseMap["C"] = DerivedC

        dataStorage.store(mockPlugin, data)

        val file = dataStorage.getPluginDataFileInternal(mockPlugin, data)
        assertEquals(JsonPluginData.string, file.readText())
    }
}