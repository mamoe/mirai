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
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.internal.asQQAndroidBot
import net.mamoe.mirai.internal.message.RefinableMessage
import net.mamoe.mirai.internal.message.RefineContext
import net.mamoe.mirai.internal.message.RefineContextKey
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.internal.network.protocol.packet.chat.video.PttCenterSvr
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageSourceKind
import net.mamoe.mirai.message.data.OnlineShortVideo
import net.mamoe.mirai.utils.cast
import net.mamoe.mirai.utils.isSameType
import net.mamoe.mirai.utils.toUHexString

/**
 * refine to [OnlineShortVideoImpl]
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
            else -> return null // don't process stranger's video message
        }.cast<Contact>()
        val sender = when (sourceKind) {
            net.mamoe.mirai.message.data.MessageSourceKind.FRIEND -> bot.getFriend(fromId)
            net.mamoe.mirai.message.data.MessageSourceKind.GROUP -> {
                val group = bot.getGroup(groupId)
                checkNotNull(group).members[fromId]
            }

            else -> return null // don't process stranger's video message
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
            shortVideoDownloadReq.urlV4
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

@Suppress("DuplicatedCode")
@SerialName(OnlineShortVideo.SERIAL_NAME)
@Serializable
internal class OnlineShortVideoImpl(
    override val fileId: String,
    override val fileMd5: ByteArray,
    override val urlForDownload: String
) : OnlineShortVideo {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (!isSameType(this, other)) return false

        if (fileId != other.fileId) return false
        if (urlForDownload != other.urlForDownload) return false
        if (!fileMd5.contentEquals(other.fileMd5)) return false

        return true
    }

    override fun toString(): String {
        return "[mirai:svideo:$fileId, md5=${fileMd5.toUHexString("")}]"
    }

    override fun contentToString(): String {
        return "[视频]"
    }

    override fun hashCode(): Int {
        var result = fileId.hashCode()
        result = 31 * result + fileMd5.contentHashCode()
        result = 31 * result + urlForDownload.hashCode()
        return result
    }
}