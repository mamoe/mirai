/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.message.source.identity

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

public object MessageIdentitySerializer : KSerializer<MessageIdentity> {
    private val delegate = RawMessageIdentity.serializer()

    override val descriptor: SerialDescriptor
        get() = delegate.descriptor

    override fun deserialize(decoder: Decoder): MessageIdentity {
        return delegate.deserialize(decoder)
    }

    override fun serialize(encoder: Encoder, value: MessageIdentity) {
        delegate.serialize(encoder, value.convertToRawMessageIdentity())
    }
}

public object FullyMessageIdentitySerializer : KSerializer<FullMessageIdentity> {
    private val delegate = RawFullMessageIdentity.serializer()

    override val descriptor: SerialDescriptor
        get() = delegate.descriptor

    override fun deserialize(decoder: Decoder): FullMessageIdentity {
        return delegate.deserialize(decoder)
    }

    override fun serialize(encoder: Encoder, value: FullMessageIdentity) {
        delegate.serialize(encoder, value.convertToRawFullMessageIdentity())
    }
}