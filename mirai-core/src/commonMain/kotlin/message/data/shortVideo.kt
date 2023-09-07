/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.getMember
import net.mamoe.mirai.internal.asQQAndroidBot
import net.mamoe.mirai.internal.message.RefinableMessage
import net.mamoe.mirai.internal.message.RefineContext
import net.mamoe.mirai.internal.message.RefineContextKey
import net.mamoe.mirai.internal.message.protocol.impl.ShortVideoProtocol
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.internal.network.protocol.packet.chat.video.PttCenterSvr
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.toUHexString

/**
 * receive from pipeline and refine to [OnlineShortVideoImpl]
 */
internal class OnlineShortVideoMsgInternal(
    private val videoFile: ImMsgBody.VideoFile
) : RefinableMessage {

    override fun tryRefine(bot: Bot, context: MessageChain, refineContext: RefineContext): Message? {
        return null
    }

    override suspend fun refine(bot: Bot, context: MessageChain, refineContext: RefineContext): Message? {
        bot.asQQAndroidBot()

        val sourceKind = refineContext[RefineContextKey.MessageSourceKind] ?: return null
        val fromId = refineContext[RefineContextKey.FromId] ?: return null
        val groupId = refineContext[RefineContextKey.GroupIdOrZero] ?: return null

        val contact = when (sourceKind) {
            MessageSourceKind.FRIEND -> bot.getFriend(fromId) ?: error("Cannot find friend $fromId.")
            MessageSourceKind.GROUP -> bot.getGroup(groupId) ?: error("Cannot find group $groupId.")
            else -> return null // ignore processing stranger's video message
        }
        val sender = when (sourceKind) {
            MessageSourceKind.FRIEND -> bot.getFriend(fromId) ?: error("Cannot find friend $fromId.")
            MessageSourceKind.GROUP -> {
                val group = bot.getGroup(groupId) ?: error("Cannot find group $groupId.")
                group.getMember(fromId) ?: error("Cannot find member $fromId of group $groupId.")
            }
            else -> return null // ignore processing stranger's video message
        }

        val shortVideoDownloadReq = bot.network.sendAndExpect(
            PttCenterSvr.ShortVideoDownReq(
                bot.client,
                contact,
                sender,
                videoFile.fileUuid.decodeToString(),
                videoFile.fileMd5
            )
        )

        if (shortVideoDownloadReq !is PttCenterSvr.ShortVideoDownReq.Response.Success)
            throw IllegalStateException("Failed to query short video download attributes.")

        if (!shortVideoDownloadReq.fileMd5.contentEquals(videoFile.fileMd5))
            throw IllegalStateException(
                "Queried short video download attributes doesn't match the requests. " +
                        "message provides: ${videoFile.fileMd5.toUHexString("")}, " +
                        "queried result: ${shortVideoDownloadReq.fileMd5.toUHexString("")}"
            )

        val format = ShortVideoProtocol.FORMAT
            .firstOrNull { it.second == videoFile.fileFormat }?.first
            ?: ExternalResource.DEFAULT_FORMAT_NAME

        return OnlineShortVideoImpl(
            videoFile.fileUuid.decodeToString(),
            shortVideoDownloadReq.fileMd5,
            videoFile.fileName.decodeToString(),
            videoFile.fileSize.toLong(),
            format,
            shortVideoDownloadReq.urlV4,
            ShortVideoThumbnail(
                videoFile.thumbFileMd5,
                videoFile.thumbFileSize.toLong(),
                videoFile.thumbWidth,
                videoFile.thumbHeight
            )
        )
    }


    override fun toString(): String {
        return "OnlineShortVideoMsgInternal(videoElem=$videoFile)"
    }

    override fun contentToString(): String {
        return "[视频元数据]"
    }
}

@Serializable
internal data class ShortVideoThumbnail(
    val md5: ByteArray,
    val size: Long,
    val width: Int?,
    val height: Int?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ShortVideoThumbnail

        if (!md5.contentEquals(other.md5)) return false
        if (size != other.size) return false
        if (width != other.width) return false
        if (height != other.height) return false

        return true
    }

    override fun hashCode(): Int {
        var result = md5.contentHashCode()
        result = 31 * result + size.hashCode()
        result = 31 * result + (width ?: 0)
        result = 31 * result + (height ?: 0)
        return result
    }
}

internal abstract class AbstractShortVideoWithThumbnail : ShortVideo {
    abstract val thumbnail: ShortVideoThumbnail
}

@Suppress("DuplicatedCode")
@SerialName(OnlineShortVideo.SERIAL_NAME)
@Serializable
internal class OnlineShortVideoImpl(
    override val videoId: String,
    override val fileMd5: ByteArray,
    override val filename: String,
    override val fileSize: Long,
    override val fileFormat: String,
    override val urlForDownload: String,
    override val thumbnail: ShortVideoThumbnail
) : OnlineShortVideo, AbstractShortVideoWithThumbnail() {

    override fun toString(): String {
        return "[mirai:shortvideo:$videoId, videoName=$filename.$fileFormat, videoMd5=${fileMd5.toUHexString("")}, " +
                "videoSize=${fileSize}, thumbnailMd5=${thumbnail.md5.toUHexString("")}, thumbnailSize=${thumbnail.size}]"
    }

    override fun contentToString(): String {
        return "[视频]"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OnlineShortVideoImpl

        if (videoId != other.videoId) return false
        if (!fileMd5.contentEquals(other.fileMd5)) return false
        if (filename != other.filename) return false
        if (fileSize != other.fileSize) return false
        if (fileFormat != other.fileFormat) return false
        if (urlForDownload != other.urlForDownload) return false
        if (thumbnail != other.thumbnail) return false

        return true
    }

    override fun hashCode(): Int {
        var result = videoId.hashCode()
        result = 31 * result + fileMd5.contentHashCode()
        result = 31 * result + filename.hashCode()
        result = 31 * result + fileSize.hashCode()
        result = 31 * result + fileFormat.hashCode()
        result = 31 * result + urlForDownload.hashCode()
        result = 31 * result + thumbnail.hashCode()
        return result
    }
}

@Serializable
internal class OfflineShortVideoImpl(
    override val videoId: String,
    override val filename: String,
    override val fileMd5: ByteArray,
    override val fileSize: Long,
    override val fileFormat: String,
    override val thumbnail: ShortVideoThumbnail
) : OfflineShortVideo, AbstractShortVideoWithThumbnail() {

    /**
     * offline short video uses
     */
    override fun toString(): String {
        return "[mirai:shortvideo:$videoId, videoName=$filename.$fileFormat, videoMd5=${fileMd5.toUHexString("")}, " +
                "videoSize=${fileSize}, thumbnailMd5=${thumbnail.md5.toUHexString("")}, thumbnailSize=${thumbnail.size}]"
    }

    override fun contentToString(): String {
        return "[视频]"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OfflineShortVideoImpl

        if (videoId != other.videoId) return false
        if (filename != other.filename) return false
        if (!fileMd5.contentEquals(other.fileMd5)) return false
        if (fileSize != other.fileSize) return false
        if (fileFormat != other.fileFormat) return false
        if (thumbnail != other.thumbnail) return false

        return true
    }

    override fun hashCode(): Int {
        var result = videoId.hashCode()
        result = 31 * result + filename.hashCode()
        result = 31 * result + fileMd5.contentHashCode()
        result = 31 * result + fileSize.hashCode()
        result = 31 * result + fileFormat.hashCode()
        result = 31 * result + thumbnail.hashCode()
        return result
    }
}
