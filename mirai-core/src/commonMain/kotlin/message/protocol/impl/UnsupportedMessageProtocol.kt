/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message.protocol.impl

import net.mamoe.mirai.internal.message.data.UnsupportedMessageImpl
import net.mamoe.mirai.internal.message.protocol.*
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody

internal class UnsupportedMessageProtocol : MessageProtocol(priority = 100u) {
    override fun ProcessorCollector.collectProcessorsImpl() {
        add(Decoder())
        add(Encoder())
    }

    private class Decoder : MessageDecoder {
        override suspend fun MessageDecoderContext.process(data: ImMsgBody.Elem) {
            val struct = UnsupportedMessageImpl(data).takeIf { it.struct.isNotEmpty() } ?: return
            collect(struct)
        }
    }

    private class Encoder : MessageEncoder<UnsupportedMessageImpl> {
        override suspend fun MessageEncoderContext.process(data: UnsupportedMessageImpl) {
            collect(data.structElem)
        }
    }
}