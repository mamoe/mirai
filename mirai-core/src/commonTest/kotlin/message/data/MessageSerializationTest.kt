/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import kotlinx.serialization.serializer
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.internal.test.AbstractTest
import net.mamoe.mirai.message.MessageSerializers
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.cast
import net.mamoe.mirai.utils.mapToByteArray
import net.mamoe.mirai.utils.structureToString
import kotlin.test.*

internal class MessageSerializationTest : AbstractTest() {
    @Suppress("DEPRECATION_ERROR")
    private val module
        get() = MessageSerializers.serializersModule
    private val format
        get() = Json {
            serializersModule = module
            useArrayPolymorphism = false
            ignoreUnknownKeys = true
        }

    private inline fun <reified T : Any> T.serialize(serializer: KSerializer<T> = module.serializer()): String {
        return format.encodeToString(serializer, this)
    }

    private inline fun <reified T : Any> String.deserialize(serializer: KSerializer<T> = module.serializer()): T {
        return format.decodeFromString(serializer, this)
    }

    private inline fun <reified T : Any> testSerialization(t: T, serializer: KSerializer<T> = module.serializer()) {
        val deserialized = kotlin.runCatching {
            println("Testing ${t::class.simpleName} with serializer $serializer")
            val serialized = t.serialize(serializer)
            println("Result: ${serializer.descriptor.serialName}  $serialized")
            serialized.deserialize(serializer)
        }.getOrElse {
            throw IllegalStateException("Failed to serialize $t", it)
        }
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
        Face(Face.AI_NI),
        RockPaperScissors.PAPER,
        UnsupportedMessageImpl(ImMsgBody.Elem())
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

        VipFace(VipFace.AiXin, 1),
        PokeMessage.BaoBeiQiu,
        MarketFaceImpl(ImMsgBody.MarketFace()),
        SimpleServiceMessage(1, "SSM"),
        LightApp("lightApp"),
        image.flash(),
        image.toForwardMessage(1L, "test"),
        MusicShare(MusicKind.NeteaseCloudMusic, "123", "123", "123", "123", "123", "123"),
        MessageOrigin(SimpleServiceMessage(1, "content"), "resource id", MessageOriginKind.LONG),
        ShowImageFlag,
        Dice(1),
        FileMessageImpl("id", 2, "name", 1)
    )

    @Serializable
    data class W(
        val m: FileMessage
    )

    @Test
    fun `test FileMessage serialization`() {
        val w = W(FileMessageImpl("id", 2, "name", 1))
        println(w.serialize(W.serializer()))
        assertEquals(w, w.serialize(W.serializer()).deserialize(W.serializer()))
    }

    @Test
    fun `test Image serialization`() {
        val string = image.serialize()
        val element = string.deserialize<JsonElement>()
        element as JsonObject
        assertEquals(string.deserialize(), image)

        val image2 = Image(image.imageId) {
            type = ImageType.GIF
            width = 123
            height = 456
        }
        val string2 = image2.serialize()
        val element2 = string2.deserialize<JsonElement>()
        element2 as JsonObject
        assertEquals(element2["imageType"]?.jsonPrimitive?.content, image2.imageType.name)
        assertEquals(element2["width"]?.jsonPrimitive?.int, image2.width)
        assertEquals(element2["height"]?.jsonPrimitive?.int, image2.height)
        val decoded: Image = string2.deserialize()
        assertEquals(decoded.imageId, image2.imageId)
        assertEquals(decoded.imageType, image2.imageType)
        assertEquals(decoded.width, image2.width)
        assertEquals(decoded.height, image2.height)

        val string3 = """
        {
            "imageType": "GIF",
            "width": 123,
            "height": 456,
            "imageId": "${image.imageId}"
         }
        """.trimIndent()
        val decoded2: Image = string3.deserialize()
        assertEquals(decoded2.imageId, image2.imageId)
        assertEquals(decoded2.imageType, image2.imageType)
        assertEquals(decoded2.width, image2.width)
        assertEquals(decoded2.height, image2.height)
    }

    @Serializable
    data class RichWrapper(
        val richMessage: RichMessage
    )

    @Test
    fun `test polymorphic serialization`() {
        val string = format.encodeToString(RichWrapper.serializer(), RichWrapper(SimpleServiceMessage(1, "content")))
        println(string)
        var element = format.parseToJsonElement(string)
        element as JsonObject
        element = element["richMessage"] as JsonObject
        assertEquals("SimpleServiceMessage", element["type"]?.cast<JsonPrimitive>()?.content)
        assertEquals("content", element["content"]?.cast<JsonPrimitive>()?.content)
        assertEquals(1, element["serviceId"]?.cast<JsonPrimitive>()?.content?.toInt())
    }

    @Serializable
    data class Wrapper(
        val message: @Polymorphic SingleMessage
    )

    @Test
    fun `test ShowImageFlag serialization`() {
        val string = format.encodeToString(Wrapper.serializer(), Wrapper(ShowImageFlag))
        println(string)
        var element = format.parseToJsonElement(string)
        element as JsonObject
        element = element["message"] as JsonObject
        assertEquals("ShowImageFlag", element["type"]?.cast<JsonPrimitive>()?.content)
        assertTrue { element.size == 1 }
        assertEquals(ShowImageFlag, format.decodeFromString(Wrapper.serializer(), string).message)
    }

    @Test
    fun `test contextual serialization`() {
        for (message in testMessageContentInstances) {
            testSerialization(
                message,
                module.getContextual(message::class)?.cast()
                    ?: error("No contextual serializer found for ${message::class}")
            )
        }
        for (message in testConstrainSingleMessageInstances) {
            testSerialization(
                message,
                module.getContextual(message::class)?.cast()
                    ?: error("No contextual serializer found for ${message::class}")
            )
        }
    }

    @Test
    fun `test serialize message chain`() {
        val chain = testMessageContentInstances.toMessageChain() + emptySource
        println(chain.serialize()) // [["net.mamoe.mirai.message.data.PlainText",{"content":"test"}],["net.mamoe.mirai.message.data.At",{"target":123456,"display":""}],["net.mamoe.mirai.message.data.AtAll",{}],["net.mamoe.mirai.internal.message.OfflineGroupImage",{"imageId":"{01E9451B-70ED-EAE3-B37C-101F1EEBF5B5}.mirai"}]]

        testSerialization(chain)
    }

    @Test
    fun `test MessageSource serializable from issue 1273`() {
        // #1273

        val a = """
            {"kind":"GROUP","botId":692928873,"ids":[44],"internalIds":[-933057735],"time":1621607925,"fromId":1930893235,"targetId":1067474509,"originalMessage":[{"type":"Image","imageId":"{47B45B11-1491-3E85-E816-467029444C3F}.jpg"}]}
        """.trimIndent()
        val j = Json {
            serializersModule = module
            ignoreUnknownKeys = true
        }
        val source = j.decodeFromString(MessageSerializers.serializersModule.serializer<MessageSource>(), a)
        println(source.structureToString())
        assertEquals(
            expected = Mirai.buildMessageSource(692928873, MessageSourceKind.GROUP) {
                id(44)
                internalId(-933057735)
                time(1621607925)
                sender(1930893235)
                target(1067474509)
                messages {
                    +Image("{47B45B11-1491-3E85-E816-467029444C3F}.jpg")
                }
            },
            actual = source
        )
    }

    @Serializable
    data class AudioTestStandard(
        val online: OnlineAudio,
        val offline: OfflineAudio,
        val onlineAsRec: Audio,
        val offlineAsRec: Audio,
    )

    @Test
    fun `test Audio standard`() {
        val origin = AudioTestStandard(
            OnlineAudioImpl("name", byteArrayOf(), 1, AudioCodec.SILK, "url", 2, null),
            OfflineAudio("test", byteArrayOf(), 1, AudioCodec.SILK, null),

            OnlineAudioImpl("name", byteArrayOf(), 1, AudioCodec.SILK, "url", 2, null),
            OfflineAudio("test", byteArrayOf(), 1, AudioCodec.SILK, null),
        )

        assertEquals(
            AudioCodec.SILK.id,
            format.encodeToJsonElement(origin).jsonObject["offline"]!!.jsonObject["codec"]!!.jsonPrimitive.content.toInt()
        ) // use custom serializer

        assertEquals(
            AudioCodec.SILK.id,
            format.encodeToJsonElement(origin).jsonObject["online"]!!.jsonObject["codec"]!!.jsonPrimitive.content.toInt()
        ) // use custom serializer

        assertEquals(
            "OnlineAudio",
            format.encodeToJsonElement(origin).jsonObject["online"]!!.jsonObject["type"]!!.jsonPrimitive.content
        )
        assertEquals(
            "OfflineAudio",
            format.encodeToJsonElement(origin).jsonObject["offline"]!!.jsonObject["type"]!!.jsonPrimitive.content
        )

        assertEquals(
            "OnlineAudio",
            format.encodeToJsonElement(origin).jsonObject["onlineAsRec"]!!.jsonObject["type"]!!.jsonPrimitive.content
        )
        assertEquals(
            "OfflineAudio",
            format.encodeToJsonElement(origin).jsonObject["offlineAsRec"]!!.jsonObject["type"]!!.jsonPrimitive.content
        )

        val result = origin.serialize().deserialize<AudioTestStandard>()

        assertEquals(origin.online::class, result.online::class)
        assertEquals(origin.offline::class, result.offline::class)
        assertEquals(origin.onlineAsRec::class, result.onlineAsRec::class)
        assertEquals(origin.offlineAsRec::class, result.offlineAsRec::class)

        assertEquals(origin.online, result.online)
        assertEquals(origin.offline, result.offline)
        assertEquals(origin.onlineAsRec, result.onlineAsRec)
        assertEquals(origin.offlineAsRec, result.offlineAsRec)

        assertEquals(origin, result)
    }

    @Serializable
    data class AudioTestWithPtt(
        val online: OnlineAudio,
        val offline: OfflineAudio,
        val onlineAsRec: Audio,
        val offlineAsRec: Audio,
    )

    @Test
    fun `test Audio with ptt`() {
        val origin = AudioTestWithPtt(
            OnlineAudioImpl("name", byteArrayOf(), 1, AudioCodec.SILK, "url", 2, ImMsgBody.Ptt(1)),
            OfflineAudio("test", byteArrayOf(), 1, AudioCodec.SILK, byteArrayOf(1, 2)),

            OnlineAudioImpl("name", byteArrayOf(), 1, AudioCodec.SILK, "url", 2, ImMsgBody.Ptt(1)),
            OfflineAudio("test", byteArrayOf(), 1, AudioCodec.SILK, byteArrayOf(1, 2)),
        )

        val result = origin.serialize().deserialize<AudioTestWithPtt>()

        assertEquals(origin.online::class, result.online::class)
        assertEquals(origin.offline::class, result.offline::class)
        assertEquals(origin.onlineAsRec::class, result.onlineAsRec::class)
        assertEquals(origin.offlineAsRec::class, result.offlineAsRec::class)

        assertEquals(origin.online, result.online)
        assertEquals(origin.offline, result.offline)
        assertEquals(origin.onlineAsRec, result.onlineAsRec)
        assertEquals(origin.offlineAsRec, result.offlineAsRec)

        assertEquals(origin, result)
    }

    @Test
    fun `test Audio extraData`() {
        val origin = OnlineAudioImpl("name", byteArrayOf(), 1, AudioCodec.SILK, "url", 2, ImMsgBody.Ptt(1))
        val result = format.parseToJsonElement(origin.serialize()).jsonObject

        assertNotNull(origin.extraData)
        assertContentEquals(
            origin.extraData,
            result["extraData"]?.jsonArray?.mapToByteArray { it.jsonPrimitive.int.toByte() })
    }
}