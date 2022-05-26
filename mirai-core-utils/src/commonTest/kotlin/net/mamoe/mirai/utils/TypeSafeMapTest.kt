/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

import net.mamoe.yamlkt.Yaml
import net.mamoe.yamlkt.YamlBuilder
import kotlin.test.Test
import kotlin.test.assertEquals

internal class TypeSafeMapTest {

    private val myKey = TypeKey<String>("test")
    private val myNullableKey = TypeKey<String?>("testNullable")
    private val myNullableKey2 = TypeKey<String?>("testNullable2")
    private val myKey2 = TypeKey<CharSequence>("test2")

    @Test
    fun `can set get`() {
        val map = createMutableTypeSafeMap()
        map[myKey] = "str"
        map[myKey2] = "str2"
        assertEquals(2, map.size)
        assertEquals("str", map[myKey])
        assertEquals("str2", map[myKey2])

    }

    @Test
    fun `test nulls`() {
        val map = createMutableTypeSafeMap()
        map[myNullableKey] = null
        map[myNullableKey2] = "str2"
        assertEquals(2, map.size)
        assertEquals(null, map[myNullableKey])
        assertEquals("str2", map[myNullableKey2])
    }

    @Test
    fun `key is inlined`() {
        val map = createMutableTypeSafeMap()
        map[TypeKey<String>("test")] = "str"
        map[TypeKey<String>("test")] = "str2"
        assertEquals(1, map.size)
        assertEquals("str2", map[TypeKey("test")])
    }

    @Test
    fun `can toMap`() {
        val map = createMutableTypeSafeMap()
        map[myKey] = "str"
        map[myKey2] = "str2"
        assertEquals(2, map.size)

        val map1 = map.toMapBoxed()

        assertEquals(2, map1.size)
        assertEquals("str", map1[myKey])
        assertEquals("str2", map1[myKey2])
    }

    @Test
    fun `test serialization`() {
        val map = createMutableTypeSafeMap()
        map[myKey] = "str"
        map[myKey2] = "str2"
        assertEquals(2, map.size)

        val map1 = map.toMap()

        // Json does not support reflective serialization, so we use Yaml in JSON format
        val yaml = Yaml {
            classSerialization = YamlBuilder.MapSerialization.FLOW_MAP
            mapSerialization = YamlBuilder.MapSerialization.FLOW_MAP
            listSerialization = YamlBuilder.ListSerialization.FLOW_SEQUENCE
            stringSerialization = YamlBuilder.StringSerialization.DOUBLE_QUOTATION
            encodeDefaultValues = true
        }

        val string = yaml.encodeToString(map1)
        println(string) // { "test2": "str2" ,"test": "str" }

        val result = createMutableTypeSafeMap(Yaml.decodeMapFromString(string).cast())
        assertEquals(map, result)
    }
}