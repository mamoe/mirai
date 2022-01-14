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
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.internal.utils.io.serialization.toByteArray
import net.mamoe.mirai.message.data.UnsupportedMessage
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.utils.copy

@SerialName(UnsupportedMessage.SERIAL_NAME)
@Serializable(UnsupportedMessageImpl.Serializer::class)
internal data class UnsupportedMessageImpl(
    val structElem: ImMsgBody.Elem,
) : UnsupportedMessage {
    override val struct: ByteArray by lazy { structElem.toByteArray(ImMsgBody.Elem.serializer()) }
    override fun toString(): String = content
    override fun hashCode(): Int {
        return struct.contentHashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is UnsupportedMessageImpl) return false
        if (other.structElem == this.structElem) return true
        return other.struct.contentEquals(this.struct)
    }

    object Serializer : KSerializer<UnsupportedMessageImpl> {
        override val descriptor: SerialDescriptor =
            UnsupportedMessage.Serializer.descriptor.copy(UnsupportedMessage.SERIAL_NAME)

        override fun deserialize(decoder: Decoder): UnsupportedMessageImpl =
            UnsupportedMessage.Serializer.deserialize(decoder) as UnsupportedMessageImpl

        override fun serialize(encoder: Encoder, value: UnsupportedMessageImpl) =
            UnsupportedMessage.Serializer.serialize(encoder, value)
    }
}