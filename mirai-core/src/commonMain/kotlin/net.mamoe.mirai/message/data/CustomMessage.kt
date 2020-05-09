/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:OptIn(MiraiInternalAPI::class)
package net.mamoe.mirai.message.data

import kotlinx.io.core.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.protobuf.ProtoId
import net.mamoe.mirai.utils.*


/**
 * 自定义消息
 *
 * 它不会显示在消息文本中, 也不会被其他客户端识别.
 * 只有 mirai 才能识别这些消息.
 *
 * 目前在回复时无法通过 [originalMessage] 获取自定义类型消息
 *
 * @sample samples.CustomMessageIdentifier 实现示例
 *
 * @see CustomMessageMetadata 自定义消息元数据
 */
@MiraiExperimentalAPI
sealed class CustomMessage : SingleMessage {
    /**
     * 获取这个消息的工厂
     */
    abstract fun getFactory(): Factory<out CustomMessage>

    /**
     * 序列化和反序列化此消息的工厂, 将会自动注册.
     * 应实现为 `object`.
     *
     * @see JsonSerializerFactory 使用 [Json] 作为序列模式的 [Factory]
     * @see ProtoBufSerializerFactory 使用 [ProtoBuf] 作为序列模式的 [Factory]
     */
    @MiraiExperimentalAPI
    abstract class Factory<M : CustomMessage>(
        /**
         * 此类型消息的名称.
         * 在发往服务器时使用此名称.
         * 应确保唯一且不变.
         */
        final override val typeName: String
    ) : Message.Key<M> {

        init {
            @Suppress("LeakingThis")
            register(this)
        }

        /**
         * 序列化此消息.
         */
        @Throws(Exception::class)
        abstract fun serialize(message: @UnsafeVariance M): ByteArray

        /**
         * 从 [input] 读取此消息.
         */
        @Throws(Exception::class)
        abstract fun deserialize(input: ByteArray): @UnsafeVariance M
    }

    /**
     * 使用 [ProtoBuf] 作为序列模式的 [Factory].
     * 推荐使用此工厂
     */
    abstract class ProtoBufSerializerFactory<M : CustomMessage>(typeName: String) :
        Factory<M>(typeName) {

        /**
         * 得到 [M] 的 [KSerializer].
         */
        abstract fun serializer(): KSerializer<M>

        override fun serialize(message: M): ByteArray = ProtoBuf.dump(serializer(), message)
        override fun deserialize(input: ByteArray): M = ProtoBuf.load(serializer(), input)
    }

    /**
     * 使用 [Json] 作为序列模式的 [Factory]
     * 推荐在调试时使用此工厂
     */
    abstract class JsonSerializerFactory<M : CustomMessage>(typeName: String) :
        Factory<M>(typeName) {

        /**
         * 得到 [M] 的 [KSerializer].
         */
        abstract fun serializer(): KSerializer<M>

        @OptIn(UnstableDefault::class)
        open val json = Json(JsonConfiguration.Default)

        override fun serialize(message: M): ByteArray = json.stringify(serializer(), message).toByteArray()
        override fun deserialize(input: ByteArray): M = json.parse(serializer(), String(input))
    }

    companion object Key : Message.Key<CustomMessage> {
        override val typeName: String get() = "CustomMessage"
        private val factories: LockFreeLinkedList<Factory<*>> = LockFreeLinkedList()

        internal fun register(factory: Factory<out CustomMessage>) {
            factories.removeIf { it::class == factory::class }
            val exist = factories.asSequence().firstOrNull { it.typeName == factory.typeName }
            if (exist != null) {
                error("CustomMessage.Factory typeName ${factory.typeName} is already registered by ${exist::class.qualifiedName}")
            }
            factories.addLast(factory)
        }

        @Serializable
        class CustomMessageFullData(
            @ProtoId(1) val miraiVersionFlag: Int,
            @ProtoId(2) val typeName: String,
            @ProtoId(3) val data: ByteArray
        )

        class CustomMessageFullDataDeserializeInternalException(cause: Throwable?) : RuntimeException(cause)
        class CustomMessageFullDataDeserializeUserException(val body: ByteArray, cause: Throwable?) :
            RuntimeException(cause)

        internal fun deserialize(fullData: ByteReadPacket): CustomMessage? {
            val msg = kotlin.runCatching {
                val length = fullData.readInt()
                if (fullData.remaining != length.toLong()) {
                    return null
                }
                ProtoBuf.load(CustomMessageFullData.serializer(), fullData.readBytes(length))
            }.getOrElse {
                throw CustomMessageFullDataDeserializeInternalException(it)
            }
            return kotlin.runCatching {
                when (msg.miraiVersionFlag) {
                    1 -> factories.asSequence().firstOrNull { it.typeName == msg.typeName }?.deserialize(msg.data)
                    else -> null
                }
            }.getOrElse {
                throw CustomMessageFullDataDeserializeUserException(msg.data, it)
            }
        }

        internal fun <M : CustomMessage> serialize(factory: Factory<M>, message: M): ByteArray = buildPacket {
            ProtoBuf.dump(CustomMessageFullData.serializer(), CustomMessageFullData(
                miraiVersionFlag = 1,
                typeName = factory.typeName,
                data = factory.serialize(message)
            )).let { data ->
                writeInt(data.size)
                writeFully(data)
            }
        }.readBytes()
    }
}

/**
 * 自定义消息元数据.
 *
 * **实现方法**:
 * 1. 实现一个类继承 [CustomMessageMetadata], 添加 `@Serializable` (来自 `kotlinx.serialization`)
 * 2. 添加伴生对象, 继承 [CustomMessage.ProtoBufSerializerFactory] 或 [CustomMessage.JsonSerializerFactory], 或 [CustomMessage.Factory]
 * 3. 在需要解析消息前调用一次伴生对象以注册
 *
 * @see CustomMessage 查看更多信息
 * @see ConstrainSingle 可实现此接口以保证消息链中只存在一个元素
 */
@MiraiExperimentalAPI
abstract class CustomMessageMetadata : CustomMessage(), MessageMetadata {
    companion object Key : Message.Key<CustomMessageMetadata> {
        override val typeName: String get() = "CustomMessageMetadata"
    }

    open fun customToString(): ByteArray = customToStringImpl(this.getFactory())

    final override fun toString(): String =
        "[mirai:custom:${getFactory().typeName}:${String(customToString())}]"
}


@Suppress("NOTHING_TO_INLINE")
internal inline fun <T : CustomMessageMetadata> T.customToStringImpl(factory: CustomMessage.Factory<*>): ByteArray {
    @Suppress("UNCHECKED_CAST")
    return (factory as CustomMessage.Factory<T>).serialize(this)
}