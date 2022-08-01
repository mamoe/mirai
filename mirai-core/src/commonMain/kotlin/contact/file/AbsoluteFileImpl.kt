/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.contact.file

import io.ktor.utils.io.core.*
import net.mamoe.mirai.contact.FileSupported
import net.mamoe.mirai.contact.file.AbsoluteFile
import net.mamoe.mirai.contact.file.AbsoluteFolder
import net.mamoe.mirai.internal.message.data.FileMessageImpl
import net.mamoe.mirai.internal.network.protocol.packet.chat.FileManagement
import net.mamoe.mirai.internal.network.protocol.packet.chat.toResult
import net.mamoe.mirai.message.data.FileMessage
import net.mamoe.mirai.utils.isSameClass
import net.mamoe.mirai.utils.toUHexString

internal class AbsoluteFileImpl(
    contact: FileSupported,
    parent: AbsoluteFolder?,
    id: String,
    name: String,
    uploadTime: Long,
    lastModifiedTime: Long,
    uploaderId: Long,

    override var expiryTime: Long,
    override val size: Long, // when file is changed, its id will also be changed, so no need to be var
    override val sha1: ByteArray,
    override val md5: ByteArray,

    busId: Int,
) : AbsoluteFile, AbstractAbsoluteFileFolder(
    contact, parent, id, name, uploadTime, uploaderId, lastModifiedTime,
    busId
) {
    override fun checkPermission(operationHint: String) {
        // TODO: 30/10/2021  checkPermission: 群可以设置允许任何人上传而目前没有检测这个属性, 因此不能实现权限判定

//        if (uploaderId == bot.id) return
//        if (contact is GroupImpl && !contact.botPermission.isOperator()) throwPermissionDeniedException(operationHint)
//        return
    }

    override val isFile: Boolean get() = true
    override val isFolder: Boolean get() = false

    override val absolutePath: String
        get() {
            val parent = parent
            return when {
                parent == null || parent.name == "/" -> "/$name"
                else -> "${parent.absolutePath}/$name"
            }
        }

    override suspend fun exists(): Boolean {
        return bot.network.sendAndExpect(
            FileManagement.GetFileInfo(
                client,
                groupCode = contact.id,
                busId = busId,
                fileId = id
            )
        ).toResult("AbsoluteFileImpl.exists", checkResp = false)
            .getOrThrow()
            .fileInfo != null
    }


    override suspend fun moveTo(folder: AbsoluteFolder): Boolean {
        if (folder.contact != this.contact) {
            error("Cross-group file operation is not yet supported.")
        }
        if (folder.absolutePath == this.parentOrRoot.absolutePath) return true
        checkPermission("moveTo")


        val result =
            bot.network.sendAndExpect(
                FileManagement.MoveFile(
                    client,
                    contact.id,
                    busId,
                    id,
                    parent.idOrRoot,
                    folder.idOrRoot
                )
            )
                .toResult("AbsoluteFileImpl.moveTo", checkResp = false)
                .getOrThrow()

        return when (result.int32RetCode) {
            -36 -> throwPermissionDeniedException("moveTo")
            0 -> {
                parent = folder
                true
            }
            else -> {
                false
            }
        }
//        } else {
//            return FileManagement.RenameFolder(client, contact.id, id, name).sendAndExpect(bot)
//                .toResult("RemoteFile.moveTo", checkResp = false).getOrThrow().int32RetCode == 0
//        }
    }

    override suspend fun getUrl(): String? {
        // Known error
        // java.lang.IllegalStateException: Failed AbsoluteFileImpl.getUrl, result=-303, msg=param error: bus_id
        // java.lang.IllegalStateException: Failed AbsoluteFileImpl.getUrl, result=-103, msg=GetFileAttrAction file not exist

        val resp = bot.network.sendAndExpect(
            FileManagement.RequestDownload(
                client,
                groupCode = contact.id,
                busId = busId,
                fileId = id
            )
        ).toResult("AbsoluteFileImpl.getUrl").getOrElse { return null }


        return "http://${resp.downloadIp}/ftn_handler/${resp.downloadUrl.toUHexString("")}/?fname=" +
                id.toByteArray().toUHexString("")
    }

    override fun toMessage(): FileMessage {
        return FileMessageImpl(id, busId, name, size)
    }

    override suspend fun refresh(): Boolean {
        val new = refreshed() ?: return false
        this.parent = new.parent
        this.expiryTime = new.expiryTime
        this.name = new.name
        this.lastModifiedTime = new.lastModifiedTime
        return true
    }

    override fun toString(): String = "AbsoluteFile(name=$name, absolutePath=$absolutePath, id=$id)"

    override suspend fun refreshed(): AbsoluteFile? {
        val result = bot.network.sendAndExpect(FileManagement.GetFileInfo(client, contact.id, id, busId))
            .toResult("AbsoluteFile.refreshed")
            .getOrNull()?.fileInfo
            ?: return null

        return if (result.parentFolderId == this.parentOrRoot.id) {
            this.parentOrRoot.impl().createChildFile(result)
        } else {
            null
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AbsoluteFileImpl || !isSameClass(this, other)) return false
        if (!super.equals(other)) return false

        if (expiryTime != other.expiryTime) return false
        if (size != other.size) return false
        if (!sha1.contentEquals(other.sha1)) return false
        if (!md5.contentEquals(other.md5)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + expiryTime.hashCode()
        result = 31 * result + size.hashCode()
        result = 31 * result + sha1.contentHashCode()
        result = 31 * result + md5.contentHashCode()
        return result
    }
}