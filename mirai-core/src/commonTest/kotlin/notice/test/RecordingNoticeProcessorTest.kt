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
import net.mamoe.mirai.internal.MockBot
import net.mamoe.mirai.internal.network.components.AbstractPipelineContext
import net.mamoe.mirai.internal.network.components.ProcessResult
import net.mamoe.mirai.internal.notice.Desensitizer
import net.mamoe.mirai.internal.notice.RecordingNoticeProcessor
import net.mamoe.mirai.internal.test.AbstractTest
import net.mamoe.mirai.internal.utils.io.ProtocolStruct
import net.mamoe.mirai.utils.MutableTypeSafeMap
import net.mamoe.mirai.utils.TypeSafeMap
import net.mamoe.yamlkt.Yaml
import kotlin.test.Test
import kotlin.test.assertEquals

internal class RecordingNoticeProcessorTest : AbstractTest() {

    class MyContext(attributes: TypeSafeMap) : AbstractPipelineContext(MockBot(), attributes) {
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

        val serialize = RecordingNoticeProcessor.serialize(context, struct)
        println(serialize)
        val deserialized = RecordingNoticeProcessor.deserialize(serialize)

        assertEquals(context.attributes, deserialized.attributes)
        assertEquals(struct, deserialized.struct)
    }

    @Test
    fun `can read desensitization config`() {
        val text = Thread.currentThread().contextClassLoader.getResource("recording/configs/test.desensitization.yml")!!
            .readText()
        val desensitizer = Desensitizer.create(Yaml.decodeFromString(text))
        assertEquals(
            mapOf(
                "123456789" to "111",
                "987654321" to "111"
            ), desensitizer.rules
        )
    }

    @Test
    fun `test desensitization`() {
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
}