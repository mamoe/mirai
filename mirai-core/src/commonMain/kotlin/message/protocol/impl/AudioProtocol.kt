/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message.protocol.impl

import net.mamoe.mirai.internal.message.data.OfflineAudioImpl
import net.mamoe.mirai.internal.message.data.OnlineAudioImpl
import net.mamoe.mirai.internal.message.protocol.MessageProtocol
import net.mamoe.mirai.internal.message.protocol.ProcessorCollector
import net.mamoe.mirai.internal.message.protocol.decode.MessageDecoder
import net.mamoe.mirai.internal.message.protocol.decode.MessageDecoderContext
import net.mamoe.mirai.internal.message.protocol.serialization.MessageSerializer
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.toLongUnsigned

internal class AudioProtocol : MessageProtocol() {
    override fun ProcessorCollector.collectProcessorsImpl() {
        add(Decoder())

        MessageSerializer.superclassesScope(
            OnlineAudio::class,
            Audio::class,
            MessageContent::class,
            SingleMessage::class
        ) {
            add(MessageSerializer(OnlineAudioImpl::class, OnlineAudioImpl.serializer()))
        }
        MessageSerializer.superclassesScope(
            OfflineAudio::class,
            Audio::class,
            MessageContent::class,
            SingleMessage::class
        ) {
            add(MessageSerializer(OfflineAudioImpl::class, OfflineAudioImpl.serializer()))
        }
        MessageSerializer.superclassesScope(MessageContent::class, SingleMessage::class) {
            @Suppress("DEPRECATION_ERROR")
            add(
                MessageSerializer(
                    net.mamoe.mirai.message.data.Voice::class,
                    net.mamoe.mirai.message.data.Voice.serializer()
                )
            )
        }
    }

    private class Decoder : MessageDecoder {
        override suspend fun MessageDecoderContext.process(data: ImMsgBody.Elem) {
            val originalMsg = runCatching { attributes[MessageDecoderContext.CONTAINING_MSG] }
                .getOrNull() ?: return

            val ptt = originalMsg.msgBody.richText.ptt ?: return
            collect(OnlineAudioImpl(
                filename = ptt.fileName.decodeToString(),
                fileMd5 = ptt.fileMd5,
                fileSize = ptt.fileSize.toLongUnsigned(),
                codec = AudioCodec.fromId(ptt.format),
                url = ptt.downPara.decodeToString(),
                length = ptt.time.toLongUnsigned(),
                originalPtt = ptt,
            ))
        }
    }
}