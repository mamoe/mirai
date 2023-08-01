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
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.message.data.OfflineShortVideoImpl
import net.mamoe.mirai.internal.network.highway.Highway
import net.mamoe.mirai.internal.network.highway.ResourceKind
import net.mamoe.mirai.internal.network.protocol.data.proto.PttShortVideo
import net.mamoe.mirai.internal.network.protocol.packet.chat.video.PttCenterSvr
import net.mamoe.mirai.internal.utils.io.serialization.loadAs
import net.mamoe.mirai.internal.utils.io.serialization.writeProtoBuf
import net.mamoe.mirai.message.data.ShortVideo
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.childScopeContext
import kotlin.contracts.contract
import kotlin.coroutines.CoroutineContext

internal abstract class AbstractContact(
    final override val bot: QQAndroidBot,
    parentCoroutineContext: CoroutineContext,
) : Contact {
    final override val coroutineContext: CoroutineContext = parentCoroutineContext.childScopeContext()

    override suspend fun uploadShortVideo(thumbnail: ExternalResource, video: ExternalResource): ShortVideo {
        // TODO: add interrupt uploading video event, just like upload image
        // TODO: check mp4 file
        // TODO: check contact is group or friend

        val uploadResp = bot.network.sendAndExpect(
            PttCenterSvr.GroupShortVideoUpReq(
                client = bot.client,
                contact = this,
                thumbnailFileMd5 = thumbnail.md5,
                thumbnailFileSize = thumbnail.size,
                videoFileMd5 = video.md5,
                videoFileSize = video.size
            )
        )

        if (uploadResp is PttCenterSvr.GroupShortVideoUpReq.Response.FileExists) {
            return OfflineShortVideoImpl(
                uploadResp.fileId,
                video.md5,
                video.size,
                "mp4", // TODO: support more formats to upload
                thumbnail.md5,
                thumbnail.size,
                1280, // TODO: read thumbnail image width
                720 // TODO: read thumbnail image height
            )
        }

        val resource = buildPacket {
            thumbnail.input().use { it.copyTo(this) }
            video.input().use { it.copyTo(this) }
        }.readBytes().toExternalResource("mp4")

        val highwayResp = Highway.uploadResourceBdh(
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
                        videoFileMd5 = video.md5,
                        videoFileSize = video.size
                    )
                )
            }.readBytes(),
            encrypt = true
        )

        resource.close()

        if (highwayResp.extendInfo == null) {
            error("highway upload short video failed, extendInfo is null.")
        }

        val highwayUploadResp = highwayResp.extendInfo!!.loadAs(PttShortVideo.PttShortVideoUploadResp.serializer())

        return OfflineShortVideoImpl(
            highwayUploadResp.fileid,
            video.md5,
            video.size,
            "mp4", // TODO: support more formats to upload
            thumbnail.md5,
            thumbnail.size,
            1280, // TODO: read thumbnail image width
            720 // TODO: read thumbnail image height
        )
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