/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:OptIn(LowLevelApi::class)
@file:Suppress(
    "NOTHING_TO_INLINE",
)

package net.mamoe.mirai.internal.contact

import kotlinx.atomicfu.AtomicInt
import kotlinx.atomicfu.atomic
import net.mamoe.mirai.LowLevelApi
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.data.FriendInfo
import net.mamoe.mirai.event.events.FriendMessagePostSendEvent
import net.mamoe.mirai.event.events.FriendMessagePreSendEvent
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.contact.info.FriendInfoImpl
import net.mamoe.mirai.internal.message.OfflineAudioImpl
import net.mamoe.mirai.internal.network.highway.*
import net.mamoe.mirai.internal.network.protocol.data.proto.Cmd0x346
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.internal.network.protocol.packet.chat.voice.PttStore
import net.mamoe.mirai.internal.network.protocol.packet.chat.voice.audioCodec
import net.mamoe.mirai.internal.network.protocol.packet.list.FriendList
import net.mamoe.mirai.internal.utils.C2CPkgMsgParsingCache
import net.mamoe.mirai.internal.utils.io.serialization.loadAs
import net.mamoe.mirai.internal.utils.io.serialization.toByteArray
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.OfflineAudio
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.recoverCatchingSuppressed
import net.mamoe.mirai.utils.toByteArray
import net.mamoe.mirai.utils.toUHexString
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.coroutines.CoroutineContext

internal fun net.mamoe.mirai.internal.network.protocol.data.jce.FriendInfo.toMiraiFriendInfo(): FriendInfoImpl =
    FriendInfoImpl(
        friendUin,
        nick,
        remark
    )

@OptIn(ExperimentalContracts::class)
internal inline fun Friend.checkIsFriendImpl(): FriendImpl {
    contract {
        returns() implies (this@checkIsFriendImpl is FriendImpl)
    }
    check(this is FriendImpl) { "A Friend instance is not instance of FriendImpl. Your instance: ${this::class.qualifiedName}" }
    return this
}

internal class FriendImpl(
    bot: QQAndroidBot,
    parentCoroutineContext: CoroutineContext,
    internal val friendInfo: FriendInfo,
) : Friend, AbstractUser(bot, parentCoroutineContext, friendInfo) {
    @Suppress("unused") // bug
    val lastMessageSequence: AtomicInt = atomic(-1)
    val friendPkgMsgParsingCache = C2CPkgMsgParsingCache()
    override suspend fun delete() {
        check(bot.friends[this.id] != null) {
            "Friend ${this.id} had already been deleted"
        }
        bot.network.run {
            FriendList.DelFriend.invoke(bot.client, this@FriendImpl).sendAndExpect().also {
                check(it.isSuccess) { "delete friend failed: ${it.resultCode}" }
            }
        }
    }


    private val handler: FriendSendMessageHandler by lazy { FriendSendMessageHandler(this) }

    @Suppress("DuplicatedCode")
    override suspend fun sendMessage(message: Message): MessageReceipt<Friend> {
        return handler.sendMessageImpl(message, ::FriendMessagePreSendEvent, ::FriendMessagePostSendEvent)
    }

    override fun toString(): String = "Friend($id)"

    override suspend fun uploadAudio(resource: ExternalResource): OfflineAudio = bot.network.run {
        var audio: OfflineAudioImpl? = null
        kotlin.runCatching {
            val resp = Highway.uploadResourceBdh(
                bot = bot,
                resource = resource,
                kind = ResourceKind.PRIVATE_AUDIO,
                commandId = 26,
                extendInfo = PttStore.C2C.createC2CPttStoreBDHExt(bot, this@FriendImpl.uin, resource)
                    .toByteArray(Cmd0x346.ReqBody.serializer())
            )
            // resp._miraiContentToString("UV resp")
            val c346resp = resp.extendInfo!!.loadAs(Cmd0x346.RspBody.serializer())
            if (c346resp.msgApplyUploadRsp == null) {
                error("Upload failed")
            }
            audio = OfflineAudioImpl(
                filename = "${resource.md5.toUHexString("")}.amr",
                fileMd5 = resource.md5,
                fileSize = resource.size,
                codec = resource.audioCodec,
                originalPtt = ImMsgBody.Ptt(
                    fileType = 4,
                    srcUin = bot.uin,
                    fileUuid = c346resp.msgApplyUploadRsp.uuid,
                    fileMd5 = resource.md5,
                    fileName = resource.md5 + ".amr".toByteArray(),
                    fileSize = resource.size.toInt(),
                    boolValid = true,
                )
            )
        }.recoverCatchingSuppressed {
            when (val resp = PttStore.GroupPttUp(bot.client, bot.id, id, resource).sendAndExpect<Any>()) {
                is PttStore.GroupPttUp.Response.RequireUpload -> {
                    tryServersUpload(
                        bot,
                        resp.uploadIpList.zip(resp.uploadPortList),
                        resource.size,
                        ResourceKind.GROUP_AUDIO,
                        ChannelKind.HTTP
                    ) { ip, port ->
                        Mirai.Http.postPtt(ip, port, resource, resp.uKey, resp.fileKey)
                    }
                    audio = OfflineAudioImpl(
                        filename = "${resource.md5.toUHexString("")}.amr",
                        fileMd5 = resource.md5,
                        fileSize = resource.size,
                        codec = resource.audioCodec,
                        originalPtt = ImMsgBody.Ptt(
                            fileType = 4,
                            srcUin = bot.uin,
                            fileUuid = resp.fileId.toByteArray(),
                            fileMd5 = resource.md5,
                            fileName = resource.md5 + ".amr".toByteArray(),
                            fileSize = resource.size.toInt(),
                            boolValid = true,
                        )
                    )
                }
            }
        }.getOrThrow()

        return audio!!
    }
}
