/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.testFramework.test

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.protobuf.ProtoNumber
import net.mamoe.mirai.internal.test.AbstractTest
import net.mamoe.mirai.internal.testFramework.Platform
import net.mamoe.mirai.internal.testFramework.desensitizer.Desensitizer
import net.mamoe.mirai.internal.testFramework.rules.DisabledOnPlatform
import net.mamoe.mirai.internal.utils.io.ProtocolStruct
import net.mamoe.yamlkt.Yaml
import net.mamoe.yamlkt.YamlBuilder
import kotlin.test.Test
import kotlin.test.assertEquals

internal class RecordingNoticeProcessorTest : AbstractTest() {

    @Serializable
    data class MyProtocolStruct(
        val value: String
    ) : ProtocolStruct

    @Test
    @DisabledOnPlatform(Platform.Android::class) // resources not available
    fun `test plain desensitization`() {
        val text = loadDesensitizationRules()
        val desensitizer = Desensitizer.create(Yaml.decodeFromString(text))


        assertEquals(
            """
            "111": s1av12sad3
            "222": s1av12sad3
        """.trim(),
            desensitizer.desensitize(
                """
            "123456789": s1av12sad3
            "987654321": s1av12sad3
        """.trim()
            )
        )
    }


    @Serializable
    data class TestProto(
        @ProtoNumber(1) val proto: Proto
    ) : ProtocolStruct {
        @Serializable
        data class Proto(
            @ProtoNumber(1) val int: Int
        )
    }

    @Serializable
    data class ByteArrayWrapper(
        val value: ByteArray
    )

    val format = Yaml {
        // one-line
        classSerialization = YamlBuilder.MapSerialization.FLOW_MAP
        mapSerialization = YamlBuilder.MapSerialization.FLOW_MAP
        listSerialization = YamlBuilder.ListSerialization.FLOW_SEQUENCE
        stringSerialization = YamlBuilder.StringSerialization.DOUBLE_QUOTATION
        encodeDefaultValues = false
    }


    @Test
    @DisabledOnPlatform(Platform.Android::class)
    fun `test long as byte array desensitization`() {
        val text = loadDesensitizationRules()
        val desensitizer = Desensitizer.create(Yaml.decodeFromString(text))

        val proto = TestProto(TestProto.Proto(123456789))

        assertEquals(
            TestProto(TestProto.Proto(111)),
            format.decodeFromString(
                TestProto.serializer(),
                desensitizer.desensitize(format.encodeToString(TestProto.serializer(), proto))
            )
        )
    }

    private fun loadDesensitizationRules() =
        ((Thread.currentThread().contextClassLoader ?: this::class.java.classLoader)
            .getResource("recording/configs/test.desensitization.yml")!!).readText()
}