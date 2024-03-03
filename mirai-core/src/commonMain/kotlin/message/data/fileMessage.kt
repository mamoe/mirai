/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */


package net.mamoe.mirai.internal.message.data

import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.mamoe.mirai.contact.FileSupported
import net.mamoe.mirai.contact.file.AbsoluteFile
import net.mamoe.mirai.contact.file.AbsoluteFolder
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.asQQAndroidBot
import net.mamoe.mirai.internal.contact.file.*
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.internal.network.protocol.data.proto.Oidb0x6d8.GetFileListRspBody
import net.mamoe.mirai.internal.network.protocol.data.proto.SubMsgType0x4
import net.mamoe.mirai.internal.network.protocol.packet.chat.FileManagement
import net.mamoe.mirai.internal.network.protocol.packet.chat.OfflineFilleHandleSvr
import net.mamoe.mirai.internal.network.protocol.packet.chat.toResult
import net.mamoe.mirai.message.data.FileMessage
import net.mamoe.mirai.utils.cast
import net.mamoe.mirai.utils.warning
import kotlin.contracts.contract

@Serializable
@Suppress("ANNOTATION_ARGUMENT_MUST_BE_CONST") // bug
@SerialName(FileMessage.SERIAL_NAME)
internal data class GroupFileMessageImpl(
    override val id: String,
    @SerialName("internalId") val busId: Int,
    override val name: String,
    override val size: Long,
    @Transient val allowSend: Boolean = false,
) : FileMessage {
    override val internalId: Int
        get() = busId

    override suspend fun toAbsoluteFile(contact: FileSupported): AbsoluteFile? {
        val result = contact.bot.asQQAndroidBot().network
            .sendAndExpect(FileManagement.GetFileInfo(contact.bot.asQQAndroidBot().client, contact.id, id, busId))
            .toResult("FileMessage.toAbsoluteFile").getOrThrow()
        if (result.fileInfo == null) return null

        // Get its parent AbsoluteFolder
        // This is necessary for properties like creationTime.
        // Maybe we can optimize it in the future (i.e. make it lazy?)

        val root = contact.files.root.impl()
        val folder = if (result.fileInfo.parentFolderId == AbsoluteFolder.ROOT_FOLDER_ID) {
            root
        } else {
            val folders = ArrayList<GetFileListRspBody.Item>()
            root.impl().getItemsFlow()
                .filter { it.folderInfo != null }
                .onEach { folders.add(it) }
                .firstOrNull { it.folderInfo?.folderId == result.fileInfo.parentFolderId }
                ?.resolved(root) as AbsoluteFolderImpl?
                ?: kotlin.run {
                    for (folder in folders) {
                        CommonAbsoluteFolderImpl.getItemsFlow(
                            (contact.bot as QQAndroidBot).client,
                            contact,
                            folder.folderInfo!!.folderId
                        ).firstOrNull { it.folderInfo?.folderId == result.fileInfo.parentFolderId }
                            ?.resolved(root)?.cast<AbsoluteFolderImpl?>()?.let { return@run it }
                    }
                    root
                }
        }

        return folder.createChildFile(result.fileInfo)
    }

    override fun toString(): String = "[mirai:file:$name, id=$id, internalId=$busId, size=$size]"
}

@SerialName(FileMessage.SERIAL_NAME)
internal data class FriendFileMessageImpl(
    override val id: String,
    override val name: String,
    override val size: Long,
    val md5: ByteArray,
    @Transient val allowSend: Boolean = false,
) : FileMessage {
    override val internalId: Int
        get() = 0

    override suspend fun toAbsoluteFile(contact: FileSupported): AbsoluteFile? {
        val queryResp = contact.bot.asQQAndroidBot().network
            .sendAndExpect(
                OfflineFilleHandleSvr.FileQuery(contact.bot.asQQAndroidBot().client, id.encodeToByteArray())
            )

        if (queryResp is OfflineFilleHandleSvr.FileInfo.Failed) {
            contact.bot.logger.warning { "failed to query friend file info: ${queryResp.message}" }
            return null
        }

        val fileInfo = queryResp as OfflineFilleHandleSvr.FileInfo.Success
        return AbsoluteFriendFileImpl(
            contact,
            fileInfo.fileUuid.decodeToString(),
            fileInfo.filename,
            fileInfo.ownerUin,
            fileInfo.expiryTime,
            fileInfo.fileSize,
            fileInfo.fileMd5,
            fileInfo.fileSha1,
        )
    }

    internal fun toSubMsg0x4() = SubMsgType0x4.MsgBody(
        msgNotOnlineFile = ImMsgBody.NotOnlineFile(
            fileType = 0,
            fileUuid = id.encodeToByteArray(),
            fileMd5 = md5,
            fileName = name.encodeToByteArray(),
            fileSize = size,
            subcmd = 1
        )
    )

    override fun toString(): String = "[mirai:file:$name, id=$id, size=$size]"
}