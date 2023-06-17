/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */


@file:Suppress("NOTHING_TO_INLINE")
@file:JvmMultifileClass
@file:JvmName("MessageUtils")

package net.mamoe.mirai.message.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.mamoe.mirai.IMirai
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.message.data.visitor.MessageVisitor
import net.mamoe.mirai.utils.*

/**
 * mirai 尚未支持的消息类型.
 *
 * [UnsupportedMessage] 可以发送, 接收, 或序列化保存
 * @since 2.6
 */
@SerialName(UnsupportedMessage.SERIAL_NAME)
@Serializable(UnsupportedMessage.Serializer::class)
@NotStableForInheritance
public interface UnsupportedMessage : MessageContent {
    override fun contentToString(): String =
        "[不支持的消息#${struct.contentHashCode()}]" // to produce 'stable' and reliable text

    /**
     * 原生消息数据
     */
    public val struct: ByteArray

    @MiraiInternalApi
    override fun <D, R> accept(visitor: MessageVisitor<D, R>, data: D): R {
        return visitor.visitUnsupportedMessage(this, data)
    }

    public companion object {
        public const val SERIAL_NAME: String = "UnsupportedMessage"

        /**
         * 创建 [UnsupportedMessage]
         * @see IMirai.createUnsupportedMessage
         */
        @JvmStatic
        public fun create(struct: ByteArray): UnsupportedMessage = Mirai.createUnsupportedMessage(struct)
    }

    public object Serializer : KSerializer<UnsupportedMessage> by Surrogate.serializer().map(
        resultantDescriptor = Surrogate.serializer().descriptor,
        deserialize = { Mirai.createUnsupportedMessage(struct.hexToBytes()) },
        serialize = { Surrogate(struct.toUHexString("")) }
    ) {
        @Suppress("RemoveRedundantQualifierName")
        @Serializable
        @SerialName(UnsupportedMessage.SERIAL_NAME)
        private class Surrogate(
            val struct: String // hex
        )
    }
}

/**
 * 创建 [UnsupportedMessage]
 * @since 2.6
 * @see UnsupportedMessage.create
 */
@JvmSynthetic
public inline fun UnsupportedMessage(struct: ByteArray): UnsupportedMessage = UnsupportedMessage.create(struct)