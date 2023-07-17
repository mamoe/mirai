/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message.protocol.impl

import net.mamoe.mirai.internal.message.data.OnlineShortVideoMsgInternal
import net.mamoe.mirai.internal.message.protocol.MessageProtocol
import net.mamoe.mirai.internal.message.protocol.ProcessorCollector
import net.mamoe.mirai.internal.message.protocol.decode.MessageDecoder
import net.mamoe.mirai.internal.message.protocol.decode.MessageDecoderContext
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody

internal class ShortVideoProtocol : MessageProtocol() {
    override fun ProcessorCollector.collectProcessorsImpl() {
        add(Decoder())
    }

    private class Decoder : MessageDecoder {
        override suspend fun MessageDecoderContext.process(data: ImMsgBody.Elem) {
            val videoFile = data.videoFile ?: return
            markAsConsumed()

            collect(OnlineShortVideoMsgInternal(videoFile))
        }
    }
}