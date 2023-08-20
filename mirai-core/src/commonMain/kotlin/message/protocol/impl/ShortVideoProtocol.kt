/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message.protocol.impl

import net.mamoe.mirai.internal.message.data.AbstractShortVideoWithThumbnail
import net.mamoe.mirai.internal.message.data.OfflineShortVideoImpl
import net.mamoe.mirai.internal.message.data.OnlineShortVideoImpl
import net.mamoe.mirai.internal.message.data.OnlineShortVideoMsgInternal
import net.mamoe.mirai.internal.message.protocol.MessageProtocol
import net.mamoe.mirai.internal.message.protocol.ProcessorCollector
import net.mamoe.mirai.internal.message.protocol.decode.MessageDecoder
import net.mamoe.mirai.internal.message.protocol.decode.MessageDecoderContext
import net.mamoe.mirai.internal.message.protocol.encode.MessageEncoder
import net.mamoe.mirai.internal.message.protocol.encode.MessageEncoderContext
import net.mamoe.mirai.internal.message.protocol.serialization.MessageSerializer
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.message.data.MessageContent
import net.mamoe.mirai.message.data.ShortVideo
import net.mamoe.mirai.message.data.SingleMessage

internal class ShortVideoProtocol : MessageProtocol() {
    override fun ProcessorCollector.collectProcessorsImpl() {
        add(Decoder())
        add(Encoder())

        MessageSerializer.superclassesScope(ShortVideo::class, MessageContent::class, SingleMessage::class) {
            add(MessageSerializer(OfflineShortVideoImpl::class, OfflineShortVideoImpl.serializer()))
            add(MessageSerializer(OnlineShortVideoImpl::class, OnlineShortVideoImpl.serializer()))
        }
    }

    private class Decoder : MessageDecoder {
        override suspend fun MessageDecoderContext.process(data: ImMsgBody.Elem) {
            val videoFile = data.videoFile ?: return
            markAsConsumed()

            collect(OnlineShortVideoMsgInternal(videoFile))
        }
    }

    private class Encoder : MessageEncoder<AbstractShortVideoWithThumbnail> {
        override suspend fun MessageEncoderContext.process(data: AbstractShortVideoWithThumbnail) {
            markAsConsumed()

            collect(ImMsgBody.Elem(text = ImMsgBody.Text("你的 QQ 暂不支持查看视频短片，请期待后续版本。")))

            val thumbWidth = if (data.thumbnail.width == null || data.thumbnail.width == 0) 1280 else data.thumbnail.width!!
            val thumbHeight = if (data.thumbnail.height == null || data.thumbnail.height == 0) 720 else data.thumbnail.height!!

            collect(
                ImMsgBody.Elem(
                    videoFile = ImMsgBody.VideoFile(
                        fileUuid = data.videoId.encodeToByteArray(),
                        fileMd5 = data.fileMd5,
                        fileName = data.filename.encodeToByteArray(),
                        fileFormat = FORMAT.firstOrNull { it.first == data.fileFormat }?.second ?: 3,
                        fileTime = 10,
                        fileSize = data.fileSize.toInt(),
                        thumbWidth = thumbWidth,
                        thumbHeight = thumbHeight,
                        thumbFileMd5 = data.thumbnail.md5,
                        thumbFileSize = data.thumbnail.size.toInt(),
                        busiType = 0,
                        fromChatType = -1,
                        toChatType = -1,
                        boolSupportProgressive = true,
                        fileWidth = thumbWidth,
                        fileHeight = thumbHeight
                    )
                )
            )
        }

    }

     internal companion object {
         internal val FORMAT: List<Pair<String, Int>> = listOf(
             "ts" to 1,
             "avi" to 2,
             "mp4" to 3,
             "wmv" to 4,
             "mkv" to 5,
             "rmvb" to 6,
             "rm" to 7,
             "afs" to 8,
             "mov" to 9,
             "mod" to 10,
             "mts" to 11
         )
     }
}