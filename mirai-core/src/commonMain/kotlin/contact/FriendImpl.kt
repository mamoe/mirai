/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:OptIn(LowLevelApi::class)
@file:Suppress(
    "NOTHING_TO_INLINE",
)

package net.mamoe.mirai.internal.contact

import io.ktor.utils.io.core.*
import kotlinx.coroutines.launch
import net.mamoe.mirai.LowLevelApi
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.contact.file.AbsoluteFile
import net.mamoe.mirai.contact.file.RemoteFiles
import net.mamoe.mirai.contact.friendgroup.FriendGroup
import net.mamoe.mirai.contact.roaming.RoamingMessages
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.FriendMessagePostSendEvent
import net.mamoe.mirai.event.events.FriendMessagePreSendEvent
import net.mamoe.mirai.event.events.FriendRemarkChangeEvent
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.contact.file.AbsoluteFriendFileImpl
import net.mamoe.mirai.internal.contact.info.FriendInfoImpl
import net.mamoe.mirai.internal.contact.roaming.RoamingMessagesImplFriend
import net.mamoe.mirai.internal.message.data.OfflineAudioImpl
import net.mamoe.mirai.internal.message.flags.AllowSendFileMessage
import net.mamoe.mirai.internal.message.protocol.outgoing.FriendMessageProtocolStrategy
import net.mamoe.mirai.internal.message.protocol.outgoing.MessageProtocolStrategy
import net.mamoe.mirai.internal.network.components.HttpClientProvider
import net.mamoe.mirai.internal.network.highway.*
import net.mamoe.mirai.internal.network.protocol
import net.mamoe.mirai.internal.network.protocol.data.proto.Cmd0x346
import net.mamoe.mirai.internal.network.protocol.data.proto.ExcitingBusiInfo
import net.mamoe.mirai.internal.network.protocol.data.proto.ExcitingClientInfo
import net.mamoe.mirai.internal.network.protocol.data.proto.ExcitingFileEntry
import net.mamoe.mirai.internal.network.protocol.data.proto.ExcitingFileNameInfo
import net.mamoe.mirai.internal.network.protocol.data.proto.FileUploadEntry
import net.mamoe.mirai.internal.network.protocol.data.proto.FileUploadExt
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.internal.network.protocol.packet.chat.OfflineFilleHandleSvr
import net.mamoe.mirai.internal.network.protocol.packet.chat.voice.PttStore
import net.mamoe.mirai.internal.network.protocol.packet.chat.voice.audioCodec
import net.mamoe.mirai.internal.network.protocol.packet.list.FriendList
import net.mamoe.mirai.internal.network.protocol.packet.summarycard.ChangeFriendRemark
import net.mamoe.mirai.internal.utils.io.serialization.loadAs
import net.mamoe.mirai.internal.utils.io.serialization.toByteArray
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.FileMessage
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.OfflineAudio
import net.mamoe.mirai.spi.AudioToSilkService
import net.mamoe.mirai.utils.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.coroutines.CoroutineContext

internal fun net.mamoe.mirai.internal.network.protocol.data.jce.FriendInfo.toMiraiFriendInfo(): FriendInfoImpl =
    FriendInfoImpl(
        friendUin,
        nick,
        remark,
        groupId.toInt(),
    )

@OptIn(ExperimentalContracts::class)
internal inline fun Friend.impl(): FriendImpl {
    contract {
        returns() implies (this@impl is FriendImpl)
    }
    check(this is FriendImpl) { "A Friend instance is not instance of FriendImpl. Your instance: ${this::class.qualifiedName}" }
    return this
}

internal class FriendImpl(
    bot: QQAndroidBot,
    parentCoroutineContext: CoroutineContext,
    override val info: FriendInfoImpl,
) : Friend, AbstractUser(bot, parentCoroutineContext, info) {
    override var nick: String by info::nick

    override var remark: String
        get() = info.remark
        set(value) {
            val old = info.remark
            info.remark = value
            launch {
                bot.network.sendWithoutExpect(ChangeFriendRemark(bot.client, this@FriendImpl.id, value))
                FriendRemarkChangeEvent(this@FriendImpl, old, value).broadcast()
            }
        }

    override val friendGroup: FriendGroup
        get() = bot.friendGroups[info.friendGroupId] ?: bot.friendGroups[0]!!

    private val messageProtocolStrategy: MessageProtocolStrategy<FriendImpl> = FriendMessageProtocolStrategy(this)

    override val files: RemoteFiles
        get() = throw UnsupportedOperationException("file system is not supported by Friend, please use uploadFile instead.")

    @Suppress("DEPRECATION_ERROR")
    @Deprecated("Please use files instead.", replaceWith = ReplaceWith("files.root"), level = DeprecationLevel.ERROR)
    override val filesRoot: RemoteFile
        get() = throw UnsupportedOperationException("file system is not supported by Friend, please use uploadFile instead.")

    override suspend fun uploadFile(
        filename: String,
        content: ExternalResource,
        callback: ProgressionCallback<AbsoluteFile, Long>?
    ): FileMessage {
        val md5 = content.md5
        val sha1 = content.sha1
        val size = content.size

        val appUpResp = bot.network.sendAndExpect(
            OfflineFilleHandleSvr.ApplyUploadV3(bot.client, this, filename, size, md5, sha1)
        )

        if (appUpResp is OfflineFilleHandleSvr.ApplyUploadV3.Response.Failed) {
            throw IllegalStateException(appUpResp.message)
        }

        val fileUuid = when (appUpResp) {
            is OfflineFilleHandleSvr.ApplyUploadV3.Response.FileExists -> appUpResp.fileUuid
            is OfflineFilleHandleSvr.ApplyUploadV3.Response.RequireUpload -> appUpResp.fileUuid
            else -> error("unreachable!")
        }
        val file = AbsoluteFriendFileImpl(
            this,
            fileUuid.decodeToString(),
            filename,
            bot.id,
            0,
            content.size,
            content.sha1,
            content.md5
        )

        if (appUpResp is OfflineFilleHandleSvr.ApplyUploadV3.Response.RequireUpload) {
            val ext = FileUploadExt(
                u1 = 100,
                u2 = 2,
                entry = FileUploadEntry(
                    business = ExcitingBusiInfo(
                        busId = 3,
                        senderUin = bot.uin,
                        receiverUin = uin,
                        groupCode = 0,
                    ),
                    fileEntry = ExcitingFileEntry(
                        fileSize = content.size,
                        md5 = content.md5,
                        sha1 = content.sha1,
                        fileId = appUpResp.fileUuid,
                        uploadKey = appUpResp.uploadKey,
                    ),
                    clientInfo = ExcitingClientInfo(
                        clientType = 2,
                        appId = bot.client.protocol.id.toString(),
                        terminalType = 2,
                        clientVer = "d92615c5",
                        unknown = 4,
                    ),
                    fileNameInfo = ExcitingFileNameInfo(filename)
                ),
                u3 = 0,
                u200 = null
            )

            Highway.uploadResourceBdh(
                bot = bot,
                resource = content,
                kind = ResourceKind.FRIEND_FILE,
                commandId = 69,
                extendInfo = ext.toByteArray(FileUploadExt.serializer()),
                dataFlag = 0,
                callback = if (callback == null) null else fun(it: Long) {
                    callback.onProgression(file, content, it)
                }
            )

            callback?.onFinished(file, content, Result.success(content.size))
        }

        val upSuccResp = bot.network.sendAndExpect(OfflineFilleHandleSvr.UploadSucc(bot.client, this, fileUuid))
        if (upSuccResp is OfflineFilleHandleSvr.FileInfo.Failed) {
            throw IllegalStateException(upSuccResp.message)
        }

        sendMessage(AllowSendFileMessage + file.toMessage())
        return file.toMessage()
    }

    override suspend fun delete() {
        check(bot.friends[id] != null) {
            "Friend $id had already been deleted"
        }
        bot.network.sendAndExpect(FriendList.DelFriend.invoke(bot.client, this@FriendImpl), 5000, 2).let {
            check(it.isSuccess) { "delete friend failed: ${it.resultCode}" }
        }
    }

    override suspend fun sendMessage(message: Message): MessageReceipt<Friend> {
        return sendMessageImpl(
            message,
            messageProtocolStrategy,
            ::FriendMessagePreSendEvent,
            ::FriendMessagePostSendEvent.cast()
        )
    }

    override fun toString(): String = "Friend($id)"

    override suspend fun uploadAudio(resource: ExternalResource): OfflineAudio = AudioToSilkService.instance.convert(
        resource
    ).useAutoClose { res ->

        var audio: OfflineAudioImpl? = null
        kotlin.runCatching {
            val resp = Highway.uploadResourceBdh(
                bot = bot,
                resource = res,
                kind = ResourceKind.PRIVATE_AUDIO,
                commandId = 26,
                extendInfo = PttStore.C2C.createC2CPttStoreBDHExt(bot, this@FriendImpl.uin, res)
                    .toByteArray(Cmd0x346.ReqBody.serializer())
            )
            // resp._miraiContentToString("UV resp")
            val c346resp = resp.extendInfo!!.loadAs(Cmd0x346.RspBody.serializer())
            if (c346resp.msgApplyUploadRsp == null) {
                error("Upload failed")
            }
            audio = OfflineAudioImpl(
                filename = "${res.md5.toUHexString("")}.amr",
                fileMd5 = res.md5,
                fileSize = res.size,
                codec = res.audioCodec,
                originalPtt = ImMsgBody.Ptt(
                    fileType = 4,
                    srcUin = bot.uin,
                    fileUuid = c346resp.msgApplyUploadRsp.uuid,
                    fileMd5 = res.md5,
                    fileName = res.md5 + ".amr".toByteArray(),
                    fileSize = res.size.toInt(),
                    boolValid = true,
                )
            )
        }.recoverCatchingSuppressed {
            when (val resp = bot.network.sendAndExpect(PttStore.GroupPttUp(bot.client, bot.id, id, res))) {
                is PttStore.GroupPttUp.Response.RequireUpload -> {
                    tryServersUpload(
                        bot,
                        resp.uploadIpList.zip(resp.uploadPortList),
                        res.size,
                        ResourceKind.GROUP_AUDIO,
                        ChannelKind.HTTP
                    ) { ip, port ->
                        bot.components[HttpClientProvider].getHttpClient()
                            .postPtt(ip, port, res, resp.uKey, resp.fileKey)
                    }
                    audio = OfflineAudioImpl(
                        filename = "${res.md5.toUHexString("")}.amr",
                        fileMd5 = res.md5,
                        fileSize = res.size,
                        codec = res.audioCodec,
                        originalPtt = ImMsgBody.Ptt(
                            fileType = 4,
                            srcUin = bot.uin,
                            fileUuid = resp.fileId.toByteArray(),
                            fileMd5 = res.md5,
                            fileName = res.md5 + ".amr".toByteArray(),
                            fileSize = res.size.toInt(),
                            boolValid = true,
                        )
                    )
                }
            }
        }.getOrThrow()

        return audio!!
    }

    override val roamingMessages: RoamingMessages by lazy { RoamingMessagesImplFriend(this) }
}
