/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message.protocol.impl

import net.mamoe.mirai.internal.message.protocol.*
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.message.data.MusicShare
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.content

internal class MusicShareProtocol : MessageProtocol() {
    override fun ProcessorCollector.collectProcessorsImpl() {
        add(Encoder())
//        add(Decoder())
    }

    private class Encoder : MessageEncoder<MusicShare> {
        override suspend fun MessageEncoderContext.process(data: MusicShare) {
            markAsConsumed()
            // 只有在 QuoteReply 的 source 里才会进行 MusicShare 转换, 因此可以转 PT.
            // 发送消息时会被特殊处理
            processAlso(PlainText(data.content))
        }
    }

    private class Decoder : MessageDecoder {
        override suspend fun MessageDecoderContext.process(data: ImMsgBody.Elem) {

        }

    }
}