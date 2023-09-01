/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.contact.file

import net.mamoe.mirai.contact.FileSupported
import net.mamoe.mirai.contact.file.AbsoluteFile
import net.mamoe.mirai.contact.file.AbsoluteFolder
import net.mamoe.mirai.internal.asQQAndroidBot
import net.mamoe.mirai.internal.message.data.FriendFileMessageImpl
import net.mamoe.mirai.internal.network.protocol.packet.chat.OfflineFilleHandleSvr
import net.mamoe.mirai.message.data.FileMessage
import net.mamoe.mirai.utils.currentTimeSeconds
import net.mamoe.mirai.utils.warning

internal class AbsoluteFriendFileImpl(
    override var contact: FileSupported,
    override var id: String,
    override var name: String,
    override var uploaderId: Long,

    override var expiryTime: Long,
    override val size: Long,
    override val sha1: ByteArray,
    override val md5: ByteArray,
) : AbsoluteFile {
    private inline val bot get() = contact.bot.asQQAndroidBot()
    private inline val client get() = bot.client

    override var parent: AbsoluteFolder? = null

    override val absolutePath: String
        get() = "/"

    override val isFile: Boolean
        get() = true

    override val isFolder: Boolean
        get() = false

    override var lastModifiedTime: Long = 0

    override suspend fun moveTo(folder: AbsoluteFolder): Boolean {
        throw UnsupportedOperationException("AbsoluteFile.moveTo is not implemented in FriendFile.")
    }

    override suspend fun getUrl(): String? {
        val resp = bot.network.sendAndExpect(OfflineFilleHandleSvr.ApplyDownload(client, id.encodeToByteArray()))
        return if (resp is OfflineFilleHandleSvr.ApplyDownload.Response.Success) {
            resp.url
        } else {
            null
        }
    }

    override fun toMessage(): FileMessage {
        return FriendFileMessageImpl(id, name, size, false)
    }

    override suspend fun refreshed(): AbsoluteFile? {
        val queryResp = bot.network.sendAndExpect(OfflineFilleHandleSvr.FileQuery(client, id.encodeToByteArray()))

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
    override suspend fun exists(): Boolean {
        val queryResp = bot.network.sendAndExpect(OfflineFilleHandleSvr.FileQuery(client, id.encodeToByteArray()))

        if (queryResp is OfflineFilleHandleSvr.FileInfo.Failed) {
            contact.bot.logger.warning { "failed to query friend file info: ${queryResp.message}" }
            return false
        }

        val fileInfo = queryResp as OfflineFilleHandleSvr.FileInfo.Success
        return fileInfo.expiryTime >= currentTimeSeconds()
    }

    override suspend fun renameTo(newName: String): Boolean {
        throw UnsupportedOperationException("AbsoluteFile.renameTo is not implemented in FriendFile.")
    }

    override suspend fun delete(): Boolean {
        throw UnsupportedOperationException("AbsoluteFile.delete is not implemented in FriendFile.")
    }

    override suspend fun refresh(): Boolean {
        return refreshed() == null
    }

    override fun toString(): String {
        return "AbsoluteFriendFile(name=$name, id=$id)"
    }

    override var uploadTime: Long = 0
}