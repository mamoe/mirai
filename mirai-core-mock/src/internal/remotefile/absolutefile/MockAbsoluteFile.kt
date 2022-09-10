/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */
@file:Suppress("invisible_member", "INVISIBLE_REFERENCE")

package net.mamoe.mirai.mock.internal.remotefile.absolutefile

import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import net.mamoe.mirai.contact.FileSupported
import net.mamoe.mirai.contact.file.AbsoluteFile
import net.mamoe.mirai.contact.file.AbsoluteFolder
import net.mamoe.mirai.internal.message.data.FileMessageImpl
import net.mamoe.mirai.message.data.FileMessage
import net.mamoe.mirai.mock.internal.remotefile.remotefile.MockRemoteFile
import net.mamoe.mirai.mock.resserver.MockServerRemoteFile
import net.mamoe.mirai.mock.utils.mock

internal class MockAbsoluteFile(
    override val sha1: ByteArray,
    override val md5: ByteArray,
    private val files: MockRemoteFiles,
    override var parent: AbsoluteFolder?,
    override val id: String,
    override var name: String,
    override var absolutePath: String,
    override val contact: FileSupported = files.contact,
    override var expiryTime: Long = 0L,
    override val size: Long = 0,
    override val isFile: Boolean = true,
    override val isFolder: Boolean = false,
    override val uploadTime: Long = 0,
    override var lastModifiedTime: Long = 0,
    override val uploaderId: Long = 0
) : AbsoluteFile {
    @Volatile
    private var _exists = true
    override suspend fun moveTo(folder: AbsoluteFolder): Boolean {
        if (!exists()) return false
        files.fileSystem.resolveById(id)!!.moveTo(files.fileSystem.resolveById(folder.id)!!)
        this.parent = folder
        refresh()
        return true
    }

    override suspend fun getUrl(): String =
        files.contact.bot.mock().tmpResourceServer.resolveHttpUrlByPath(
            files.fileSystem.resolveById(id)!!.resolveNativePath()
        ).toString()

    override fun toMessage(): FileMessage {
        //todo busId
        return FileMessageImpl(id, 0, name, size)
    }

    override suspend fun refreshed(): AbsoluteFile? =
        parent!!.files().filter { it.id == id }.firstOrNull()


    private fun canModify(resolved: MockServerRemoteFile): Boolean {
        return MockRemoteFile.canModify(resolved, contact)
    }

    override suspend fun exists(): Boolean = _exists

    override suspend fun renameTo(newName: String): Boolean {
        if (!exists()) return false
        val resolved = files.fileSystem.resolveById(id) ?: return false
        if (!canModify(resolved)) return false
        if (resolved.rename(newName)) {
            refresh()
            return true
        }
        return false
    }

    override suspend fun delete(): Boolean {
        if (!exists()) return false
        val resolved = files.fileSystem.resolveById(id) ?: return false
        if (!canModify(resolved)) return false
        if (resolved.delete()) {
            _exists = false
            return true
        }
        return false
    }

    override suspend fun refresh(): Boolean {
        val new = refreshed()
        if (new == null) {
            _exists = false
            return false
        }
        _exists = true
        this.parent = new.parent
        this.expiryTime = new.expiryTime
        this.name = new.name
        this.lastModifiedTime = new.lastModifiedTime
        this.absolutePath = new.absolutePath
        return true
    }

    override fun toString(): String = "MockAbsoluteFile(id=$id,absolutePath=$absolutePath,name=$name)"
    override fun equals(other: Any?): Boolean =
        other != null && other is AbsoluteFile && other.id == id

    override fun hashCode(): Int {
        // from absoluteFileImpl
        var result = super.hashCode()
        result = 31 * result + expiryTime.hashCode()
        result = 31 * result + size.hashCode()
        result = 31 * result + sha1.contentHashCode()
        result = 31 * result + md5.contentHashCode()
        return result
    }
}