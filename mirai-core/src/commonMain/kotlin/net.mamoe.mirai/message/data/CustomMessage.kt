/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:OptIn(MiraiInternalAPI::class)

package net.mamoe.mirai.message.data

import kotlinx.io.core.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.protobuf.ProtoNumber
import net.mamoe.mirai.utils.*
import net.mamoe.mirai.utils.internal.checkOffsetAndLength
import kotlin.jvm.JvmOverloads


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
public sealed class CustomMessage : SingleMessage {
    /**
     * 获取这个消息的工厂
     */
    public abstract fun getFactory(): Factory<out CustomMessage>

    /**
     * 序列化和反序列化此消息的工厂, 将会自动注册.
     * 应实现为 `object`.
     *
     * @see JsonSerializerFactory 使用 [Json] 作为序列模式的 [Factory]
     * @see ProtoBufSerializerFactory 使用 [ProtoBuf] 作为序列模式的 [Factory]
     */
    @MiraiExperimentalAPI
    public abstract class Factory<M : CustomMessage>(
        /**
         * 此类型消息的名称.
         * 在发往服务器时使用此名称.
         * 应确保唯一且不变.
         */
        public final override val typeName: String
    ) : Message.Key<M> {

        init {
            @Suppress("LeakingThis")
            register(this)
        }

        /**
         * 序列化此消息.
         */
        @Throws(Exception::class)
        public abstract fun dump(message: @UnsafeVariance M): ByteArray

        /**
         * 从 [input] 读取此消息.
         */
        @Throws(Exception::class)
        public abstract fun load(input: ByteArray): @UnsafeVariance M
    }

    /**
     * 使用 [ProtoBuf] 作为序列模式的 [Factory].
     * 推荐使用此工厂
     */
    public abstract class ProtoBufSerializerFactory<M : CustomMessage>(typeName: String) :
        Factory<M>(typeName) {

        /**
         * 得到 [M] 的 [KSerializer].
         */
        public abstract fun serializer(): KSerializer<M>

        public override fun dump(message: M): ByteArray = ProtoBuf.encodeToByteArray(serializer(), message)
        public override fun load(input: ByteArray): M = ProtoBuf.decodeFromByteArray(serializer(), input)
    }

    /**
     * 使用 [Json] 作为序列模式的 [Factory]
     * 推荐在调试时使用此工厂
     */
    public abstract class JsonSerializerFactory<M : CustomMessage>(typeName: String) :
        Factory<M>(typeName) {

        /**
         * 得到 [M] 的 [KSerializer].
         */
        public abstract fun serializer(): KSerializer<M>

        public open val json: Json = Json.Default

        public override fun dump(message: M): ByteArray = json.encodeToString(serializer(), message).toByteArray()
        public override fun load(input: ByteArray): M = json.decodeFromString(serializer(), String(input))
    }

    public companion object Key : Message.Key<CustomMessage> {
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
        private class CustomMessageFullData(
            @ProtoNumber(1) val miraiVersionFlag: Int,
            @ProtoNumber(2) val typeName: String,
            @ProtoNumber(3) val data: ByteArray
        )

        public class CustomMessageFullDataDeserializeInternalException(cause: Throwable?) : RuntimeException(cause)
        public class CustomMessageFullDataDeserializeUserException(public val body: ByteArray, cause: Throwable?) :
            RuntimeException(cause)

        internal fun load(fullData: ByteReadPacket): CustomMessage? {
            val msg = kotlin.runCatching {
                val length = fullData.readInt()
                if (fullData.remaining != length.toLong()) {
                    return null
                }
                ProtoBuf.decodeFromByteArray(CustomMessageFullData.serializer(), fullData.readBytes(length))
            }.getOrElse {
                throw CustomMessageFullDataDeserializeInternalException(it)
            }
            return kotlin.runCatching {
                when (msg.miraiVersionFlag) {
                    1 -> factories.asSequence().firstOrNull { it.typeName == msg.typeName }?.load(msg.data)
                    else -> null
                }
            }.getOrElse {
                throw CustomMessageFullDataDeserializeUserException(msg.data, it)
            }
        }

        internal fun <M : CustomMessage> dump(factory: Factory<M>, message: M): ByteArray = buildPacket {
            ProtoBuf.encodeToByteArray(
                CustomMessageFullData.serializer(), CustomMessageFullData(
                    miraiVersionFlag = 1,
                    typeName = factory.typeName,
                    data = factory.dump(message)
                )
            ).let { data ->
                writeInt(data.size)
                writeFully(data)
            }
        }.readBytes()
    }
}

/**
 * 序列化这个消息
 */
@MiraiExperimentalAPI
@SinceMirai("1.1.0")
public fun <T : CustomMessage> T.toByteArray(): ByteArray {
    @Suppress("UNCHECKED_CAST")
    return (this.getFactory() as CustomMessage.Factory<T>).dump(this)
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
public abstract class CustomMessageMetadata : CustomMessage(), MessageMetadata {
    public companion object Key : Message.Key<CustomMessageMetadata> {
        override val typeName: String get() = "CustomMessageMetadata"
    }

    public open fun customToString(): ByteArray = customToStringImpl(this.getFactory())

    final override fun toString(): String =
        "[mirai:custom:${getFactory().typeName}:${String(customToString())}]"
}


@Suppress("NOTHING_TO_INLINE")
internal inline fun <T : CustomMessageMetadata> T.customToStringImpl(factory: CustomMessage.Factory<*>): ByteArray {
    @Suppress("UNCHECKED_CAST")
    return (factory as CustomMessage.Factory<T>).dump(this)
}

@JvmOverloads
@Suppress("DuplicatedCode") // false positive. foreach is not common to UByteArray and ByteArray
internal fun ByteArray.toUHexString(
    separator: String = " ",
    offset: Int = 0,
    length: Int = this.size - offset
): String {
    this.checkOffsetAndLength(offset, length)
    if (length == 0) {
        return ""
    }
    val lastIndex = offset + length
    return buildString(length * 2) {
        this@toUHexString.forEachIndexed { index, it ->
            if (index in offset until lastIndex) {
                var ret = it.toUByte().toString(16).toUpperCase()
                if (ret.length == 1) ret = "0$ret"
                append(ret)
                if (index < lastIndex - 1) append(separator)
            }
        }
    }
}
