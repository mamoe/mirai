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
import kotlinx.serialization.Transient
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.internal.asQQAndroidBot
import net.mamoe.mirai.internal.message.RefinableMessage
import net.mamoe.mirai.internal.message.RefineContext
import net.mamoe.mirai.internal.message.RefineContextKey
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.internal.network.protocol.packet.chat.video.PttCenterSvr
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.cast
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

        val sourceKind = refineContext[MessageSourceKind] ?: return null
        val fromId = refineContext[FromId] ?: return null
        val groupId = refineContext[GroupIdOrZero] ?: return null

        val contact = when (sourceKind) {
            net.mamoe.mirai.message.data.MessageSourceKind.FRIEND -> bot.getFriend(fromId)
            net.mamoe.mirai.message.data.MessageSourceKind.GROUP -> bot.getGroup(groupId)
            else -> return null // TODO: ignore processing stranger's video message
        }.cast<Contact>()
        val sender = when (sourceKind) {
            net.mamoe.mirai.message.data.MessageSourceKind.FRIEND -> bot.getFriend(fromId)
            net.mamoe.mirai.message.data.MessageSourceKind.GROUP -> {
                val group = bot.getGroup(groupId)
                checkNotNull(group).members[fromId]
            }

            else -> return null // TODO: ignore processing stranger's video message
        }.cast<User>()

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
            throw IllegalStateException("failed to query short video download attributes.")

        if (!shortVideoDownloadReq.fileMd5.contentEquals(videoFile.fileMd5))
            throw IllegalStateException(
                "queried short video download attributes doesn't match the requests. " +
                        "message provides: ${videoFile.fileMd5.toUHexString("")}, " +
                        "queried result: ${shortVideoDownloadReq.fileMd5.toUHexString("")}"
            )

        return OnlineShortVideoImpl(
            videoFile.fileUuid.decodeToString(),
            shortVideoDownloadReq.fileMd5,
            videoFile.fileName.decodeToString(),
            videoFile.fileSize.toLong(),
            videoFile.fileFormat.toVideoFormat(),
            shortVideoDownloadReq.urlV4,
            videoFile.thumbFileMd5,
            videoFile.thumbFileSize.toLong(),
            videoFile.thumbWidth,
            videoFile.thumbHeight
        )
    }


    override fun toString(): String {
        TODO("Not yet implemented")
    }

    override fun contentToString(): String {
        TODO("Not yet implemented")
    }

    companion object {
        val MessageSourceKind = RefineContextKey<MessageSourceKind>("MessageSourceKind")
        val FromId = RefineContextKey<Long>("FromId")
        val GroupIdOrZero = RefineContextKey<Long>("GroupIdOrZero")
    }
}

internal abstract class AbstractShortVideoWithThumbnail : ShortVideo {
    abstract val thumbMd5: ByteArray
    abstract val thumbSize: Long
    abstract val thumbWidth: Int?
    abstract val thumbHeight: Int?
}

@Suppress("DuplicatedCode")
@SerialName(OnlineShortVideo.SERIAL_NAME)
@Serializable
internal class OnlineShortVideoImpl(
    override val videoId: String,
    override val fileMd5: ByteArray,
    override val fileName: String,
    override val fileSize: Long,
    override val fileFormat: String,
    override val urlForDownload: String,
    override val thumbMd5: ByteArray,
    override val thumbSize: Long,
    @Transient override val thumbWidth: Int = 0,
    @Transient override val thumbHeight: Int = 0
) : OnlineShortVideo, AbstractShortVideoWithThumbnail() {

    override fun toString(): String {
        return "[mirai:svideo:$fileName.$fileFormat, video=${fileMd5.toUHexString("")}, videoSize=${fileSize}, " +
                "thumbnail=${thumbMd5.toUHexString("")}, thumbnailSize=${thumbSize}]"
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
        if (fileName != other.fileName) return false
        if (fileSize != other.fileSize) return false
        if (fileFormat != other.fileFormat) return false
        if (urlForDownload != other.urlForDownload) return false
        if (!thumbMd5.contentEquals(other.thumbMd5)) return false
        if (thumbSize != other.thumbSize) return false
        if (thumbWidth != other.thumbWidth) return false
        if (thumbHeight != other.thumbHeight) return false

        return true
    }

    override fun hashCode(): Int {
        var result = videoId.hashCode()
        result = 31 * result + fileMd5.contentHashCode()
        result = 31 * result + fileName.hashCode()
        result = 31 * result + fileSize.hashCode()
        result = 31 * result + fileFormat.hashCode()
        result = 31 * result + urlForDownload.hashCode()
        result = 31 * result + thumbMd5.contentHashCode()
        result = 31 * result + thumbSize.hashCode()
        result = 31 * result + thumbWidth
        result = 31 * result + thumbHeight
        return result
    }
}

@Serializable
internal class OfflineShortVideoImpl(
    override val videoId: String,
    override val fileName: String,
    override val fileMd5: ByteArray,
    override val fileSize: Long,
    override val fileFormat: String,
    override val thumbMd5: ByteArray,
    override val thumbSize: Long,
    @Transient override val thumbWidth: Int = 0,
    @Transient override val thumbHeight: Int = 0
) : OfflineShortVideo, AbstractShortVideoWithThumbnail() {

    /**
     * offline short video uses
     */
    override fun toString(): String {
        return "[mirai:svideo:$fileName.$fileFormat, video=${fileMd5.toUHexString("")}, " +
                "videoSize=${fileSize}, thumbnail=${thumbMd5.toUHexString("")}, thumbnailSize=${thumbSize}]"
    }

    override fun contentToString(): String {
        return "[视频]"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OfflineShortVideoImpl

        if (videoId != other.videoId) return false
        if (fileName != other.fileName) return false
        if (!fileMd5.contentEquals(other.fileMd5)) return false
        if (fileSize != other.fileSize) return false
        if (fileFormat != other.fileFormat) return false
        if (!thumbMd5.contentEquals(other.thumbMd5)) return false
        if (thumbSize != other.thumbSize) return false
        if (thumbWidth != other.thumbWidth) return false
        if (thumbHeight != other.thumbHeight) return false

        return true
    }

    override fun hashCode(): Int {
        var result = videoId.hashCode()
        result = 31 * result + fileName.hashCode()
        result = 31 * result + fileMd5.contentHashCode()
        result = 31 * result + fileSize.hashCode()
        result = 31 * result + fileFormat.hashCode()
        result = 31 * result + thumbMd5.contentHashCode()
        result = 31 * result + thumbSize.hashCode()
        result = 31 * result + thumbWidth
        result = 31 * result + thumbHeight
        return result
    }
}

private fun Int.toVideoFormat() = when (this) {
    1 -> "ts"
    2 -> "avi"
    3 -> "mp4"
    4 -> "wmv"
    5 -> "mkv"
    6 -> "rmvb"
    7 -> "rm"
    8 -> "afs"
    9 -> "mov"
    10 -> "mod"
    11 -> "mts"
    else -> "mirai" // unknown to default
}