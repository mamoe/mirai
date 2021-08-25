/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.notice.test

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.protobuf.ProtoNumber
import net.mamoe.mirai.internal.MockBot
import net.mamoe.mirai.internal.network.components.AbstractNoticePipelineContext
import net.mamoe.mirai.internal.network.components.ProcessResult
import net.mamoe.mirai.internal.notice.Desensitizer
import net.mamoe.mirai.internal.notice.Desensitizer.Companion.generateAndDesensitize
import net.mamoe.mirai.internal.test.AbstractTest
import net.mamoe.mirai.internal.utils.codegen.ConstructorCallCodegenFacade
import net.mamoe.mirai.internal.utils.io.ProtocolStruct
import net.mamoe.mirai.utils.MutableTypeSafeMap
import net.mamoe.mirai.utils.TypeSafeMap
import net.mamoe.yamlkt.Yaml
import net.mamoe.yamlkt.YamlBuilder
import kotlin.test.Test
import kotlin.test.assertEquals

internal class RecordingNoticeProcessorTest : AbstractTest() {

    class MyContext(attributes: TypeSafeMap) : AbstractNoticePipelineContext(MockBot(), attributes) {
        override suspend fun processAlso(data: ProtocolStruct, attributes: TypeSafeMap): ProcessResult {
            throw UnsupportedOperationException()
        }
    }

    @Serializable
    data class MyProtocolStruct(
        val value: String
    ) : ProtocolStruct

    @Test
    fun `can serialize and deserialize reflectively`() {
        val context = MyContext(MutableTypeSafeMap(mapOf("test" to "value")))
        val struct = MyProtocolStruct("vvv")

        val serialize = ConstructorCallCodegenFacade.generateAndDesensitize(struct)
        assertEquals(
            """
                net.mamoe.mirai.internal.notice.test.RecordingNoticeProcessorTest.MyProtocolStruct(
                value="vvv",
                )
            """.trimIndent(),
            serialize
        )
//        val deserialized = KotlinScriptExternalDependencies
//
//        assertEquals(context.attributes, deserialized.attributes)
//        assertEquals(struct, deserialized.struct)
    }

    @Test
    fun `test plain desensitization`() {
        val text = Thread.currentThread().contextClassLoader.getResource("recording/configs/test.desensitization.yml")!!
            .readText()
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
    fun `test long as byte array desensitization`() {
        val text = Thread.currentThread().contextClassLoader.getResource("recording/configs/test.desensitization.yml")!!
            .readText()
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
}