/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.message.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.internal.message.MarketFaceImpl
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.message.data.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class MessageSerializationTest {
    @Suppress("DEPRECATION_ERROR")
    private val module get() = Message.Serializer.serializersModule
    private val format
        get() = Json {
            serializersModule = module
            useArrayPolymorphism = false // ?
        }

    private inline fun <reified T : Any> T.serialize(serializer: KSerializer<T> = module.serializer()): String {
        return format.encodeToString(serializer, this)
    }

    private inline fun <reified T : Any> String.deserialize(serializer: KSerializer<T> = module.serializer()): T {
        return format.decodeFromString(serializer, this)
    }

    private inline fun <reified T : Any> testSerialization(t: T, serializer: KSerializer<T> = module.serializer()) {
        val deserialized = t.serialize(serializer).deserialize(serializer)
        assertEquals(
            t,
            deserialized,
            message = "serialized string:   ${t.serialize(serializer)}\ndeserialized string: ${
                deserialized.serialize(
                    serializer
                )
            }\n"
        )
    }


    private val image = Image("{01E9451B-70ED-EAE3-B37C-101F1EEBF5B5}.mirai")
    private val testMessageContentInstances: Array<out MessageContent> = arrayOf(
        PlainText("test"),
        At(123456),
        AtAll,
        image,
        image.toForwardMessage(1L, "test"),
        VipFace(VipFace.AiXin, 1),
        PokeMessage.BaoBeiQiu,
        Face(Face.AI_NI),
        MarketFaceImpl(ImMsgBody.MarketFace()),
        image.flash(),
    )

    private val emptySource = Mirai.constructMessageSource(
        1L,
        MessageSourceKind.FRIEND,
        1,
        2,
        intArrayOf(1),
        1,
        intArrayOf(1),
        messageChainOf()
    )

    @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
    private val testConstrainSingleMessageInstances: Array<out ConstrainSingle> = arrayOf(
        LongMessage("content", "resId"),
        Mirai.constructMessageSource(
            1L,
            MessageSourceKind.FRIEND,
            1,
            2,
            intArrayOf(1),
            1,
            intArrayOf(1),
            messageChainOf(emptySource, image)
        ),
    )

    companion object {
        @BeforeAll
        @JvmStatic
        fun init() {
            Mirai
        }
    }

    @Test
    fun `test serialize each message contents`() {
        for (message in testMessageContentInstances) {
            testSerialization(message, module.serializer(message.javaClass))
        }
        for (message in testConstrainSingleMessageInstances) {
            testSerialization(message, module.serializer(message.javaClass))
        }
    }

    @Test
    fun `test serialize message chain`() {
        val chain = testMessageContentInstances.asMessageChain()
        println(chain.serialize()) // [["net.mamoe.mirai.message.data.PlainText",{"content":"test"}],["net.mamoe.mirai.message.data.At",{"target":123456,"display":""}],["net.mamoe.mirai.message.data.AtAll",{}],["net.mamoe.mirai.internal.message.OfflineGroupImage",{"imageId":"{01E9451B-70ED-EAE3-B37C-101F1EEBF5B5}.mirai"}]]

        testSerialization(chain)
    }
}