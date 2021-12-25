/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package net.mamoe.mirai.mock.internal.txfs

import net.mamoe.mirai.mock.internal.remotefile.length
import net.mamoe.mirai.mock.txfs.TxFileDisk
import net.mamoe.mirai.mock.txfs.TxFileSystem
import net.mamoe.mirai.mock.txfs.TxRemoteFile
import net.mamoe.mirai.mock.txfs.TxRemoteFileInfo
import net.mamoe.mirai.utils.*
import java.nio.file.Path
import java.nio.file.attribute.FileTime
import java.util.*
import java.util.concurrent.ConcurrentLinkedDeque
import kotlin.io.path.*
import net.mamoe.mirai.internal.utils.FileSystem as MiraiFileSystem

private fun allocateNewPath(base: Path): Path {
    while (true) {
        val p = base.resolve(UUID.randomUUID().toString())
        if (!p.exists()) return p
    }
}

private fun checkFileName(name: String) {
    MiraiFileSystem.checkLegitimacy(name)
    if (name.contains('/')) error("$name contains '/'")
    if (name.isEmpty()) error("Empty name")
}

internal class TxFileDiskImpl(
    internal val storage: Path
) : TxFileDisk {
    internal val fs: MutableCollection<TxFileSystem> = ConcurrentLinkedDeque()

    override val availableSystems: Sequence<TxFileSystem> = Sequence { fs.iterator() }

    override fun newFsSystem(): TxFileSystem = TxFileSystemImpl(this)
}

internal class TxFileSystemImpl(
    override val disk: TxFileDiskImpl,
) : TxFileSystem {
    internal val storage: Path = allocateNewPath(disk.storage)

    internal fun resolvePath(id: String): Path = when {
        id.isEmpty() || id == "/" -> storage.resolve("root")
        id[0] == '/' -> storage.resolve(id.substring(1))
        else -> error("file not exists: $id")
    }

    internal fun fileDetails(id: String): Path? = when {
        id.isEmpty() || id == "/" -> storage.resolve("details/root")
        id[0] == '/' -> {
            storage
                .resolve("details")
                .resolve(id.substring(1))
        }
        else -> null
    }

    internal fun resolveName(id: String): String = when {
        id.isEmpty() || id == "/" -> ""
        id[0] == '/' -> {
            val nameMapping = fileDetails(id)?.resolve("name")
            if (nameMapping == null) null
            else if (nameMapping.isFile) {
                nameMapping.readText()
            } else null
        }
        else -> null
    } ?: id.substringAfterLast('/')

    fun resolveParent(id: String): TxFileImpl {
        val details = fileDetails(id) ?: return root
        val parent = details.resolve("parent")
        if (parent.isFile) {
            return resolveById(parent.readText()) ?: root
        }
        return root
    }

    init {
        storage.mkdirs()
        storage.resolve("details/root").mkdirs()
        storage.resolve("root").mkdirs()
        overrideDetails(fileDetails("/")!!, name = "", creator = 0, createTime = 0)
        disk.fs.add(this)
    }

    override val root = TxFileImpl(this, "/")

    override fun resolveById(id: String): TxFileImpl? {
        if (id == "/" || id.isEmpty()) return root
        if (id[0] != '/') return null
        if (MiraiFileSystem.isLegal(id) && id.count { it == '/' } == 1) {
            return TxFileImpl(this, id).takeIf { it.toPath.exists() }
        }
        return null
    }

    override fun findByPath(path: String): Sequence<TxRemoteFile> {
        return root.findByPath(
            MiraiFileSystem.normalize(path)
                .removePrefix("/")
                .split('/')
                .toMutableList()
        )
    }

    fun findDirByName(base: TxFileImpl, name: String): TxFileImpl? {
        return (base.listFiles() ?: return null)
            .filter { it.isDirectory }
            .filter { it.name == name }
            .firstOrNull()?.cast()
    }

    fun uploadFile(
        name: String,
        content: ExternalResource,
        uploader: Long,
        id: String,
        toPath: Path
    ): TxFileImpl {
        val path = allocateNewPath(storage)
        val fid = '/' + path.name

        path.outputStream().buffered().use { output ->
            content.inputStream().use { resource -> resource.copyTo(output) }
        }

        toPath.resolve(path.name).createFile()

        val details = fileDetails(fid)!!
        details.mkdirs()
        overrideDetails(details, id, name, uploader, currentTimeMillis())

        return TxFileImpl(this, fid)
    }

    fun overrideDetails(
        details: Path,
        parent: String? = null,
        name: String? = null,
        creator: Long = -1L,
        createTime: Long = -1L,
    ) {
        if (parent != null) {
            details.resolve("parent").writeText(parent)
        }
        if (name != null) {
            details.resolve("name").writeText(name)
        }
        if (creator != -1L) {
            details.resolve("creator").writeBytes(creator.toByteArray())
        }
        if (createTime != -1L) {
            details.resolve("createTime").writeBytes(createTime.toByteArray())
        }
    }

    fun mkdir(id: String, name: String, creator: Long, toPath: Path): TxFileImpl {
        if (id != "/") error("Creating 2nd directories, TxFileSystem current not support")

        val path = allocateNewPath(storage)
        val fid = '/' + path.name
        path.mkdir()

        toPath.resolve(path.name).createFile()
        val details = fileDetails(fid)!!
        details.mkdirs()
        overrideDetails(details, id, name, creator, currentTimeMillis())

        return TxFileImpl(this, fid)
    }

    fun resolveAbsPath(id: String): String {
        if (id == "/") return "/"

        val details = fileDetails(id) ?: return "<not exists>"
        val fileNamePath = details.resolve("name")
        val fileName = fileNamePath.takeIf { it.isFile }?.readText() ?: "<not exists>"
        val parentPath = details.resolve("parent")
        if (!parentPath.isFile) {
            return fileName
        }
        val pid = parentPath.readText()
        val pabs = resolveAbsPath(pid)
        if (pabs.endsWith("/")) return "$pabs$fileName"
        return "$pabs/$fileName"
    }
}

internal class TxFileImpl(
    override val system: TxFileSystemImpl,
    override val id: String,
) : TxRemoteFile {
    internal val toPath: Path get() = system.resolvePath(id)
    override val exists: Boolean get() = toPath.exists()
    override val isFile: Boolean get() = toPath.isFile
    override val isDirectory: Boolean get() = toPath.isDirectory()
    override val name: String get() = system.resolveName(id)
    override val path: String get() = system.resolveAbsPath(id)
    override val parent: TxFileImpl get() = system.resolveParent(id)
    override val size: Long
        get() {
            val pt = toPath
            if (pt.isFile) return pt.length()
            return 0
        }

    override fun listFiles(): Sequence<TxRemoteFile>? {
        val pt = toPath
        if (!pt.isDirectory()) {
            return null
        }
        return pt.listDirectoryEntries().asSequence().filter {
            it.exists()
        }.map { TxFileImpl(system, '/' + it.name) }
    }

    override fun delete(): Boolean {
        if (!toPath.deleteIfExists()) return false
        val details = system.fileDetails(id) ?: return false
        system.resolvePath(details.resolve("parent").readText())
            .resolve(id.substring(1))
            .deleteIfExists()
        details.deleteRecursively()
        return true
    }

    override fun rename(name: String): Boolean {
        checkFileName(name)
        if (id.isEmpty() || id == "/") return false
        val details = system.fileDetails(id) ?: return false
        details.resolve("name").writeText(name)
        return true
    }

    override fun moveTo(path: TxRemoteFile) {
        path.cast<TxFileImpl>()
        if (path.system !== this.system) error("Cross file system moving")

        if (!path.isDirectory) error("Remote file $path not exists")
        if (id == "/") error("Moving root")

        // TODO: 移动到自己的子目录

        val details = system.fileDetails(id) ?: error("Moving ghost file: $id")

        val currentParent = parent
        currentParent.toPath.resolve(id.substring(1)).deleteIfExists()

        details.resolve("parent").writeText(path.id)
        path.toPath.resolve(id.substring(1)).createFile()
    }

    override fun resolveNativePath(): Path {
        val pt = toPath
        if (!pt.isFile) error("file not exists: $this <$pt>")
        return pt
    }

    override fun asExternalResource(): ExternalResource {
        val pt = toPath
        if (!pt.isFile) error("file not exists: $pt")
        TODO("https://github.com/mamoe/mirai/pull/1637")
    }

    override fun uploadFile(name: String, content: ExternalResource, uploader: Long): TxFileImpl {
        content.withAutoClose {
            checkFileName(name)
            val storage = toPath
            if (storage.isFile) error("Uploading file to a file")
            if (!storage.isDirectory()) error("$this not exists")

            return system.uploadFile(name, content, uploader, id, toPath)
        }
    }

    override fun mksubdir(name: String, creator: Long): TxRemoteFile {
        checkFileName(name)
        return system.mkdir(id, name, creator, toPath)
    }

    override var fileInfo: TxRemoteFileInfo
        get() {
            val details = system.fileDetails(id) ?: error("File not exists")
            if (!details.isDirectory()) {
                error("File not exists")
            }
            // parent, name, creator, createTime
            return TxRemoteFileInfo(
                creator = details.resolve("creator").readBytes().toLong(),
                createTime = details.resolve("createTime").readBytes().toLong(),
                lastUpdateTime = toPath.getLastModifiedTime().toMillis(),
            )
        }
        set(value) {
            val details = system.fileDetails(id) ?: error("File not exists")
            if (!details.isDirectory()) {
                error("File not exists")
            }
            details.resolve("creator").writeBytes(value.creator.toByteArray())
            details.resolve("createTime").writeBytes(value.createTime.toByteArray())
            toPath.setLastModifiedTime(FileTime.fromMillis(value.lastUpdateTime))
        }

    override fun toString(): String = "$path := $id"

    override fun equals(other: Any?): Boolean {
        if (other !is TxFileImpl) return false
        if (other.system !== system) return false
        return other.id == this.id
    }

    override fun hashCode(): Int {
        return id.hashCode() + system.hashCode()
    }

    fun findByPath(path: MutableList<String>): Sequence<TxRemoteFile> {
        if (path.isEmpty()) error("Empty path")
        val nxt = path.removeAt(0)
        if (nxt.isEmpty()) error("Empty subpath")
        if (path.isEmpty()) return listFiles()?.filter { it.name == nxt } ?: emptySequence()

        return system.findDirByName(this, nxt)?.findByPath(path) ?: emptySequence()
    }
}
