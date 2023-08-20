/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.contact

import io.ktor.utils.io.core.*
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.BeforeShortVideoUploadEvent
import net.mamoe.mirai.event.events.EventCancelledException
import net.mamoe.mirai.event.events.ShortVideoUploadEvent
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.message.data.OfflineShortVideoImpl
import net.mamoe.mirai.internal.message.data.ShortVideoThumbnail
import net.mamoe.mirai.internal.message.image.calculateImageInfo
import net.mamoe.mirai.internal.network.highway.Highway
import net.mamoe.mirai.internal.network.highway.ResourceKind
import net.mamoe.mirai.internal.network.protocol.data.proto.PttShortVideo
import net.mamoe.mirai.internal.network.protocol.packet.chat.video.PttCenterSvr
import net.mamoe.mirai.internal.utils.io.serialization.loadAs
import net.mamoe.mirai.internal.utils.io.serialization.writeProtoBuf
import net.mamoe.mirai.message.data.ShortVideo
import net.mamoe.mirai.internal.utils.CombinedExternalResource
import net.mamoe.mirai.utils.*
import kotlin.contracts.contract
import kotlin.coroutines.CoroutineContext

internal abstract class AbstractContact(
    final override val bot: QQAndroidBot,
    parentCoroutineContext: CoroutineContext,
) : Contact {
    final override val coroutineContext: CoroutineContext = parentCoroutineContext.childScopeContext()

    override suspend fun uploadShortVideo(
        thumbnail: ExternalResource,
        video: ExternalResource,
        fileName: String?
    ): ShortVideo = thumbnail.withAutoClose {
        video.withAutoClose {
            if (this !is Group && this !is Friend) {
                throw UnsupportedOperationException("short video can only upload to friend or group.")
            }

            if (video.formatName != "mp4") {
                throw UnsupportedOperationException("video format ${video.formatName} is not supported.")
            }

            if (BeforeShortVideoUploadEvent(this, thumbnail, video).broadcast().isCancelled) {
                throw EventCancelledException("cancelled by BeforeShortVideoUploadEvent")
            }

            // local uploaded offline short video uses video file md5 as its file name by default
            val videoName = fileName ?: video.md5.toUHexString("")

            val uploadResp = bot.network.sendAndExpect(
                PttCenterSvr.GroupShortVideoUpReq(
                    client = bot.client,
                    contact = this,
                    thumbnailFileMd5 = thumbnail.md5,
                    thumbnailFileSize = thumbnail.size,
                    videoFileName = videoName,
                    videoFileMd5 = video.md5,
                    videoFileSize = video.size,
                    videoFileFormat = video.formatName
                )
            )

            // get thumbnail image width and height
            val thumbnailInfo = thumbnail.calculateImageInfo()

            // fast path
            if (uploadResp is PttCenterSvr.GroupShortVideoUpReq.Response.FileExists) {
                return OfflineShortVideoImpl(
                    uploadResp.fileId,
                    videoName,
                    video.md5,
                    video.size,
                    video.formatName,
                    ShortVideoThumbnail(
                        thumbnail.md5,
                        thumbnail.size,
                        thumbnailInfo.width,
                        thumbnailInfo.height
                    )
                ).also {
                    ShortVideoUploadEvent.Succeed(this, thumbnail, video, it).broadcast()
                }
            }

            val highwayRespExt = CombinedExternalResource(thumbnail, video).use { resource ->
                Highway.uploadResourceBdh(
                    bot = bot,
                    resource = resource,
                    kind = ResourceKind.SHORT_VIDEO,
                    commandId = 25,
                    extendInfo = buildPacket {
                        writeProtoBuf(
                            PttShortVideo.PttShortVideoUploadReq.serializer(),
                            PttCenterSvr.GroupShortVideoUpReq.buildShortVideoFileInfo(
                                client = bot.client,
                                contact = this@AbstractContact,
                                thumbnailFileMd5 = thumbnail.md5,
                                thumbnailFileSize = thumbnail.size,
                                videoFileName = videoName,
                                videoFileMd5 = video.md5,
                                videoFileSize = video.size,
                                videoFileFormat = video.formatName
                            )
                        )
                    }.readBytes(),
                    encrypt = true
                ).extendInfo
            }

            if (highwayRespExt == null) {
                ShortVideoUploadEvent.Failed(
                    this,
                    thumbnail,
                    video,
                    -1,
                    "highway upload short video failed, extendInfo is null."
                ).broadcast()
                error("highway upload short video failed, extendInfo is null.")
            }

            val highwayUploadResp = highwayRespExt.loadAs(PttShortVideo.PttShortVideoUploadResp.serializer())

            OfflineShortVideoImpl(
                highwayUploadResp.fileid,
                videoName,
                video.md5,
                video.size,
                video.formatName,
                ShortVideoThumbnail(
                    thumbnail.md5,
                    thumbnail.size,
                    thumbnailInfo.width,
                    thumbnailInfo.height
                )
            ).also {
                ShortVideoUploadEvent.Succeed(this, thumbnail, video, it).broadcast()
            }
        }
    }
}

internal val Contact.userIdOrNull: Long? get() = if (this is User) this.id else null
internal val Contact.groupIdOrNull: Long? get() = if (this is Group) this.id else null
internal val Contact.groupUinOrNull: Long? get() = if (this is Group) this.uin else null
internal val ContactOrBot.uin: Long
    get() = when (this) {
        is Group -> uin
        is User -> uin
        is OtherClient -> bot.uin
        is Bot -> id
        else -> this.id
    }

internal fun Contact.impl(): AbstractContact {
    contract { returns() implies (this@impl is AbstractContact) }
    return this as AbstractContact
}