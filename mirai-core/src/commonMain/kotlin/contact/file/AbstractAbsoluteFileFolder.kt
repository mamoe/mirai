/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("MemberVisibilityCanBePrivate")

package net.mamoe.mirai.internal.contact.file

import net.mamoe.mirai.contact.FileSupported
import net.mamoe.mirai.contact.PermissionDeniedException
import net.mamoe.mirai.contact.file.AbsoluteFile
import net.mamoe.mirai.contact.file.AbsoluteFileFolder
import net.mamoe.mirai.contact.file.AbsoluteFolder
import net.mamoe.mirai.internal.asQQAndroidBot
import net.mamoe.mirai.internal.network.protocol.packet.chat.FileManagement
import net.mamoe.mirai.internal.network.protocol.packet.chat.toResult
import net.mamoe.mirai.internal.utils.FileSystem
import net.mamoe.mirai.utils.cast
import net.mamoe.mirai.utils.isSameType

internal fun AbstractAbsoluteFileFolder.api(): AbsoluteFileFolder = this.cast()
internal fun AbsoluteFileFolder.impl(): AbstractAbsoluteFileFolder = this.cast()
internal fun AbsoluteFile.impl(): AbsoluteFileImpl = this.cast()
internal fun AbsoluteFolder.impl(): AbsoluteFolderImpl = this.cast()

internal val AbsoluteFolder?.idOrRoot get() = this?.id ?: AbsoluteFolder.ROOT_FOLDER_ID

internal val AbstractAbsoluteFileFolder.parentOrRoot get() = parent ?: contact.files.root
internal val AbstractAbsoluteFileFolder.root get() = contact.files.root

/**
 * @see AbsoluteFileFolder
 */
internal abstract class AbstractAbsoluteFileFolder(
    // overriding AbsFileFolder
    val contact: FileSupported,
    var parent: AbsoluteFolder?,
    val id: String, // uuid-like
    var name: String,
    val uploadTime: Long,
    val uploaderId: Long,
    var lastModifiedTime: Long,
    // end

    val busId: Int, // protocol internal
) {
    protected inline val bot get() = contact.bot.asQQAndroidBot()
    protected inline val client get() = bot.client

    protected abstract fun checkPermission(operationHint: String)

    fun throwPermissionDeniedException(operationHint: String): Nothing {
        throw PermissionDeniedException("Permission denied: '$operationHint' on file '${this.api().absolutePath}' requires an operator permission.")
    }

    protected fun parentOrFail() = parent ?: error("Cannot rename the root folder.")

    ///////////////////////////////////////////////////////////////////////////
    // overriding AbsFileFolder
    ///////////////////////////////////////////////////////////////////////////

    protected abstract val isFile: Boolean
    protected abstract val isFolder: Boolean

    suspend fun renameTo(newName: String): Boolean {
        FileSystem.checkLegitimacy(newName)
        parentOrFail()
        checkPermission("renameTo")

        val result = bot.network.sendAndExpect(
            if (isFile) {
                FileManagement.RenameFile(client, contact.id, busId, id, parent.idOrRoot, newName)
            } else {
                FileManagement.RenameFolder(client, contact.id, id, newName)
            }
        )

        result.toResult("AbstractAbsoluteFileFolder.renameTo") {
            when (it) {
                0 -> {
                    name = newName
                    return true
                }
                1 -> return false
                else -> false
            }
        }.getOrThrow()

        error("unreachable")
    }

    suspend fun delete(): Boolean {
        checkPermission("delete")
        val result = if (isFile) {
            bot.network.sendAndExpect(FileManagement.DeleteFile(client, contact.id, busId, id, parent.idOrRoot))
        } else {
            // natively 'recursive'
            bot.network.sendAndExpect(FileManagement.DeleteFolder(client, contact.id, id))
        }.toResult("AbstractAbsoluteFileFolder.delete", checkResp = false).getOrThrow()

        return when (result.int32RetCode) {
            -36 -> throwPermissionDeniedException("delete")
            0 -> true
            else -> {
                // files not exists or other errors.
                false
            }
        }
    }

    @Suppress("DuplicatedCode")
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (!isSameType(this, other)) return false

        if (contact != other.contact) return false
        if (parent != other.parent) return false
        if (id != other.id) return false
        if (name != other.name) return false
        if (uploadTime != other.uploadTime) return false
        if (uploaderId != other.uploaderId) return false
        if (lastModifiedTime != other.lastModifiedTime) return false
        if (busId != other.busId) return false
        if (isFile != other.isFile) return false
        if (isFolder != other.isFolder) return false

        return true
    }

    override fun hashCode(): Int {
        var result = contact.hashCode()
        result = 31 * result + (parent?.hashCode() ?: 0)
        result = 31 * result + id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + uploadTime.hashCode()
        result = 31 * result + uploaderId.hashCode()
        result = 31 * result + lastModifiedTime.hashCode()
        result = 31 * result + busId
        result = 31 * result + isFile.hashCode()
        result = 31 * result + isFolder.hashCode()
        return result
    }
}