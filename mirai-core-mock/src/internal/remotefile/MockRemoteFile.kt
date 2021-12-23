/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress(
    "INVISIBLE_REFERENCE",
    "INVISIBLE_MEMBER",
    "DEPRECATION",
    "DEPRECATION_ERROR",
    "OverridingDeprecatedMember"
)

package net.mamoe.mirai.mock.internal.remotefile

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.FileSupported
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.isOperator
import net.mamoe.mirai.internal.message.FileMessageImpl
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.FileMessage
import net.mamoe.mirai.mock.utils.mock
import net.mamoe.mirai.utils.*
import java.nio.file.Path
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.fileSize
import kotlin.io.path.getLastModifiedTime
import kotlin.io.path.isDirectory

// Fs Root
internal class MockRemoteFileRoot(
    override val contact: FileSupported
) : RemoteFile {
    override val id: String? get() = null
    override val name: String get() = ""
    override val path: String get() = "/"
    override val parent: RemoteFile? get() = null

    internal val fsTable = ConcurrentHashMap<String, FileInfo>()

    override suspend fun delete(): Boolean =
        throw UnsupportedOperationException("Deleting root folder")

    override suspend fun exists(): Boolean = true
    override suspend fun getDownloadInfo(): RemoteFile.DownloadInfo? = null
    override suspend fun getInfo(): RemoteFile.FileInfo? = null
    override suspend fun isFile(): Boolean = false
    override suspend fun length(): Long = 0
    override suspend fun mkdir(): Boolean = false

    @OptIn(JavaFriendlyAPI::class)
    override suspend fun listFiles(): Flow<RemoteFile> = listFilesIterator(false).asFlow()

    @JavaFriendlyAPI
    override suspend fun listFilesIterator(lazy: Boolean): Iterator<RemoteFile> {
        return fsTable.values.asSequence()
            .filter { it.parent == null }
            .map { MockRemoteFile(it.name, it.id, this, this) }
            .iterator()
    }

    override fun toString(): String {
        return "MockFileSystemRoot[/]"
    }

    override fun resolve(relative: String): RemoteFile {
        val r = relative.removePrefix("/")
        if (r.contains('/')) {
            return MockRemoteFile(r.substringBefore('/'), null, this, this)
                .resolve(r.substringAfter('/'))
        }
        return MockRemoteFile(relative, null, this, this)
    }

    override fun resolve(relative: RemoteFile): RemoteFile {
        return MockRemoteFile(relative.name, relative.id, this, this)
    }

    override suspend fun resolveById(id: String, deep: Boolean): RemoteFile? {
        val info = fsTable[id] ?: return null
        if (!deep) {
            if (info.parent != null) return null
        }
        fun findP(): RemoteFile {
            if (info.parent == null) return this
            val pinfo = fsTable[id] ?: return this
            return MockRemoteFile(pinfo.name, pinfo.id, this, this)
        }
        return MockRemoteFile(info.name, info.id, findP(), this)
    }

    override fun resolveSibling(relative: String): RemoteFile = resolve(relative)
    override fun resolveSibling(relative: RemoteFile): RemoteFile = resolve(relative)

    override suspend fun toMessage(): FileMessage? = null

    override suspend fun upload(resource: ExternalResource, callback: RemoteFile.ProgressionCallback?): FileMessage {
        throw UnsupportedOperationException("Uploading as a folder")
    }

    @MiraiExperimentalApi
    override suspend fun uploadAndSend(resource: ExternalResource): MessageReceipt<Contact> {
        throw UnsupportedOperationException("Uploading as a folder")
    }

    override suspend fun isDirectory(): Boolean = true
    override suspend fun moveTo(target: RemoteFile): Boolean {
        throw UnsupportedOperationException("Moving root folder")
    }

    override suspend fun renameTo(name: String): Boolean {
        throw UnsupportedOperationException("Renaming folder")
    }
}

internal class MockRemoteFile(
    override val name: String,
    override val id: String?,
    override val parent: RemoteFile,
    val root: MockRemoteFileRoot,
) : RemoteFile {
    override val contact: FileSupported get() = root.contact
    private fun resloveFsInfo(): FileInfo? {
        if (id != null) {
            return root.fsTable[id]
        }
        val st1 = root.fsTable.values.asSequence()
            .filter { it.name == name }
        val st2 = if (parent is MockRemoteFileRoot) {
            st1.filter { it.parent == null }
        } else {
            val pid = parent.id
            if (pid == null) {
                st1.filter {
                    val fp = root.fsTable[it.parent ?: return@filter false] ?: return@filter false
                    fp.name == parent.name
                }
            } else {
                st1.filter { it.parent == parent.id }
            }
        }
        return st2.firstOrNull()
    }

    override suspend fun isFile(): Boolean = resloveFsInfo()?.isFile ?: false

    override suspend fun length(): Long {
        val info = resloveFsInfo() ?: return 0
        return runBIO {
            contact.bot.mock().tmpFsServer.fsSystem.getPath(info.id).fileSize()
        }
    }

    override suspend fun getInfo(): RemoteFile.FileInfo? {
        val info = resloveFsInfo() ?: return null
        return info.toRF(root)
    }

    override suspend fun exists(): Boolean = resloveFsInfo() != null

    override fun toString(): String {
        return "MockRemoteFile[$path]"
    }

    override fun resolve(relative: String): RemoteFile {
        return MockRemoteFile(
            relative, null, this, root
        )
    }

    override fun resolve(relative: RemoteFile): RemoteFile {
        return MockRemoteFile(
            relative.name, relative.id, this, root
        )
    }

    override suspend fun resolveById(id: String, deep: Boolean): RemoteFile? {
        if (id == this.id) return this
        val fsinf = root.fsTable[id] ?: return null
        if (fsinf.parent == null) return null
        if (this.id != null) {
            if (fsinf.parent != this.id) return null
        } else {
            val fsinfp = root.fsTable[fsinf.parent] ?: return null
            if (fsinfp.name != this.name) return null
        }
        return MockRemoteFile(
            fsinf.name, fsinf.id, this, root
        )
    }

    override fun resolveSibling(relative: String): RemoteFile = root.resolve(relative)
    override fun resolveSibling(relative: RemoteFile): RemoteFile = root.resolve(relative)

    override suspend fun delete(): Boolean {
        val info = resloveFsInfo() ?: return false
        if (!isOperable(info)) return false
        root.fsTable.remove(info.id)
        if (info.isDir) {
            root.fsTable.values.removeIf { it.parent == info.id }
        }
        return true
    }

    override suspend fun renameTo(name: String): Boolean {
        val info = resloveFsInfo() ?: return false
        if (!isOperable(info)) return false
        info.name = name
        return true
    }

    override suspend fun moveTo(target: RemoteFile): Boolean {
        if (target.contact != this.contact) error("Not support move file to other group")

        val info = resloveFsInfo() ?: return false
        if (!isOperable(info)) return false

        info.name = target.name
        val targetParent = target.parent

        if (targetParent == null || targetParent is MockRemoteFileRoot) {
            info.parent = null
        } else if (targetParent === this) {
            // noop
        } else {
            targetParent as MockRemoteFile
            if (target.id != null) info.parent = target.id
            else {
                val parentFileInfo = root.fsTable.values.asSequence()
                    .filter { it.name == target.name }
                    .filter { it.isDir }
                    .filter { it.parent == null }
                    .firstOrNull()
                if (parentFileInfo == null) {
                    return false
                } else {
                    info.parent = parentFileInfo.id
                }
            }
        }

        return true
    }

    override suspend fun mkdir(): Boolean {
        if (!contact.isOperable()) return false
        if (parent !is MockRemoteFileRoot) return false
        val duplicated = root.fsTable.values.any {
            it.isDir && it.name == this.name
        }
        if (duplicated) return false
        val fsInfo = FileInfo(
            id = newDirId(),
            name = this.name,
            isDir = true,
            uploaderId = contact.bot.id,
            uploadTime = 0,
            sha1 = EMPTY_BYTE_ARRAY,
            md5 = EMPTY_BYTE_ARRAY,
            parent = null,
        )
        root.fsTable[fsInfo.id] = fsInfo
        return true
    }

    @OptIn(JavaFriendlyAPI::class)
    override suspend fun listFiles(): Flow<RemoteFile> = listFilesIterator(false).asFlow()

    @JavaFriendlyAPI
    override suspend fun listFilesIterator(lazy: Boolean): Iterator<RemoteFile> {
        val info = resloveFsInfo() ?: return emptyList<RemoteFile>().iterator()
        return root.fsTable.values
            .filter { it.parent == info.id }
            .map { MockRemoteFile(it.name, it.id, this, root) }
            .iterator()
    }

    override suspend fun toMessage(): FileMessage? {
        val info0 = resloveFsInfo()?.toRF(root) ?: return null
        return FileMessageImpl(
            info0.id, 0, name, info0.length
        )
    }

    override suspend fun upload(resource: ExternalResource, callback: RemoteFile.ProgressionCallback?): FileMessage {
        val pinfo = if (parent === root) {
            null
        } else {
            findParent() ?: error("Parent $parent not exists")
        }

        callback?.onBegin(this, resource)
        val sha1 = resource.sha1.clone()
        val md5 = resource.md5.clone()
        val size = resource.size

        val fid = contact.bot.mock().tmpFsServer.uploadFile(resource)
        callback?.onSuccess(this, resource)

        val finfo = FileInfo(
            fid,
            this.name,
            false,
            contact.bot.id,
            currentTimeSeconds(),
            sha1,
            md5,
            pinfo?.id
        )
        root.fsTable[fid] = finfo

        return FileMessageImpl(
            fid, 0, name, size
        )
    }

    @MiraiExperimentalApi
    override suspend fun uploadAndSend(resource: ExternalResource): MessageReceipt<Contact> {
        return contact.sendMessage(upload(resource))
    }

    override suspend fun getDownloadInfo(): RemoteFile.DownloadInfo? {
        val info = resloveFsInfo() ?: return null
        if (info.isDir) return null
        return RemoteFile.DownloadInfo(
            filename = info.name,
            id = info.id,
            path = info.solvePath(root),
            url = contact.bot.mock().tmpFsServer.getHttpUrl(info.id),
            sha1 = info.sha1.clone(),
            md5 = info.md5.clone(),
        )
    }

    override val path: String
        get() = when (parent) {
            is MockRemoteFileRoot -> "/$name"
            else -> "${parent.path}/$name"
        }
}

private fun RemoteFile.findParent(): FileInfo? {
    if (this is MockRemoteFileRoot) return null
    this as MockRemoteFile

    if (parent is MockRemoteFileRoot) return null
    val parent = this.parent.cast<MockRemoteFile>()

    if (parent.id != null) return root.fsTable[parent.id]

    return root.fsTable.values.asSequence()
        .filter { it.isDir && it.name == parent.name }
        .firstOrNull()
}

private fun newDirId(): String = "d-${currentTimeMillis()}-${UUID.randomUUID()}"

internal fun MockRemoteFile.isOperable(info: FileInfo): Boolean {
    if (info.uploaderId == contact.bot.id) return true
    return contact.isOperable()
}

internal fun Contact.isOperable(): Boolean {
    val contact = this

    if (contact is Group) {
        if (contact.botPermission.isOperator()) {
            return true
        }
    }
    return false
}

internal class FileInfo(
    val id: String,
    var name: String,
    val isDir: Boolean,
    val uploaderId: Long,
    val uploadTime: Long,
    val sha1: ByteArray,
    val md5: ByteArray,
    var parent: String?, // null for root
)

internal val FileInfo.isFile: Boolean get() = !isDir
internal fun FileInfo.solvePath(fsroot: MockRemoteFileRoot): String {
    if (parent == null) return "/$name"
    val fp = fsroot.fsTable[parent] ?: return "/<unknown>/$name"
    return "/${fp.name}/$name"
}

internal fun FileInfo.solveFsPath(fsroot: MockRemoteFileRoot): Path {
    return fsroot.contact.bot.mock().tmpFsServer.fsSystem.getPath(id)
}

internal fun Path.length(): Long {
    if (isDirectory()) return 0
    return fileSize()
}

internal fun FileInfo.toRF(fsroot: MockRemoteFileRoot): RemoteFile.FileInfo {
    val fspath = solveFsPath(fsroot)
    return RemoteFile.FileInfo(
        name = name,
        id = id,
        path = solvePath(fsroot),
        length = if (isDir) 0 else fspath.length(),
        downloadTimes = 5,
        uploaderId = uploaderId,
        uploadTime = uploadTime,
        lastModifyTime = fspath.getLastModifiedTime().toMillis(),
        sha1 = sha1.clone(),
        md5 = md5.clone(),
    )
}
