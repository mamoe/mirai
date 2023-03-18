/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.mock.test.mock

import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.plus
import net.mamoe.mirai.message.MessageSerializers
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.mock.test.MockBotTestBase
import net.mamoe.mirai.mock.utils.randomImageContent
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

internal class MessageSerializationTest : MockBotTestBase() {
    @Suppress("DEPRECATION_ERROR")
    private val module
        get() = MessageSerializers.serializersModule
    private val format
        get() = Json {
            serializersModule = module
            useArrayPolymorphism = false
            ignoreUnknownKeys = false
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

        val msg = "serialized string:   ${t.serialize(serializer)}\ndeserialized string: ${
            deserialized.serialize(
                serializer
            )
        }\n"



        assert1(
            t,
            deserialized,
            msg
        )

        if (t is SingleMessage) {
            PolymorphicWrapperContent(t)
                .serialize(PolymorphicWrapperContent.serializer())
                .deserialize(PolymorphicWrapperContent.serializer())
                .let { assert1(t, it.message, msg) }
        }
    }

    private fun assert1(t: Any, deserialized: Any, msg: String) {
        if (deserialized is MessageSource && t is MessageSource) {
            assertSource(t, deserialized, msg)
            return
        }

        if (t is MessageChain && deserialized is MessageChain) {
            assertEquals(t.size, deserialized.size)
            val iter1 = t.iterator()
            val iter2 = deserialized.iterator()

            repeat(t.size) {
                assert1(iter1.next(), iter2.next(), msg)
            }
            assertFalse(iter1.hasNext(), msg)
            assertFalse(iter2.hasNext(), msg)
            return
        }

        assertEquals(t, deserialized, msg)
    }

    private fun assertSource(t: MessageSource, deserialized: MessageSource, msg: String) {
        assertEquals(t.kind, deserialized.kind, msg)
        assertEquals(t.botId, deserialized.botId, msg)
        assertEquals(t.fromId, deserialized.fromId, msg)
        assertEquals(t.targetId, deserialized.targetId, msg)
        assertEquals(t.time, deserialized.time, msg)
        Assertions.assertArrayEquals(t.ids, deserialized.ids, msg)
        Assertions.assertArrayEquals(t.internalIds, deserialized.internalIds, msg)
        assertEquals(t.originalMessage, deserialized.originalMessage, msg)
    }

    @Test
    fun testSerializersModulePlus() {
        MessageSerializers.serializersModule + EmptySerializersModule()
    }

    @Test
    fun testMockMessageSources() = runTest {
        testSerialization(bot.addFriend(1, "").says(""))
        testSerialization(bot.addStranger(2, "").says(""))
        bot.addGroup(3, "").let { group ->
            group.sendMessage("AWA").source.let { testSerialization(messageChainOf(it)) }
            group.addMember(6, "").says("A").let { testSerialization(it) }
        }
    }

    @Test
    fun testMockResources() = runTest {
        testSerialization(bot.uploadMockImage(Image.randomImageContent().toExternalResource().toAutoCloseable()))

        "1".toByteArray().toExternalResource().use { data0 ->
            testSerialization(bot.uploadOnlineAudio(data0))
            testSerialization(bot.asFriend.uploadAudio(data0))
        }
    }

    @Serializable
    data class PolymorphicWrapperImage(
        val message: @Polymorphic Image
    )

    @Serializable
    data class PolymorphicWrapperContent(
        val message: @Polymorphic SingleMessage
    )

    @Test
    @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "UNRESOLVED_REFERENCE")
    fun `test serialization for MockImage`() = runTest {
        val img = this@MessageSerializationTest.bot.uploadMockImage(
            Image.randomImageContent().toExternalResource().toAutoCloseable()
        )
        PolymorphicWrapperImage(img)
            .serialize(PolymorphicWrapperImage.serializer())
            .also { println(it) }
            .deserialize(PolymorphicWrapperImage.serializer())
    }

    // https://github.com/mamoe/mirai/pull/2414#issuecomment-1386253123
    @Test
    fun `test 2414-1386253123`() = runTest {
        // event
        val img = this@MessageSerializationTest.bot.uploadMockImage(
            Image.randomImageContent().toExternalResource().toAutoCloseable()
        )
        val msg = buildMessageChain {
            add("imgUploaded")
            add(img)
        }

        val s = format.encodeToString(msg)
        println(s)
        println(format.decodeFromString<MessageChain>(s))
        testSerialization(msg)

    }
}
