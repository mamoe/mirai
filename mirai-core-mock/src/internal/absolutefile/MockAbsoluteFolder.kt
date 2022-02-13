/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */
@file: Suppress("invisible_member", "invisible_reference")
package net.mamoe.mirai.mock.internal.absolutefile

import kotlinx.coroutines.flow.*
import net.mamoe.mirai.contact.FileSupported
import net.mamoe.mirai.contact.file.AbsoluteFile
import net.mamoe.mirai.contact.file.AbsoluteFileFolder
import net.mamoe.mirai.contact.file.AbsoluteFolder
import net.mamoe.mirai.internal.utils.FileSystem
import net.mamoe.mirai.mock.txfs.TxRemoteFile
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.JavaFriendlyAPI
import net.mamoe.mirai.utils.ProgressionCallback
import java.util.stream.Stream
import kotlin.streams.asStream

private fun TxRemoteFile.toMockAbsFolder(files: MockRemoteFiles): AbsoluteFolder {
    if (this == files.fileSystem.root) return files.root
    val parent = this.parent.toMockAbsFolder(files)
    return MockAbsoluteFolder(
        files,
        parent,
        this.id,
        this.name,
        parent.absolutePath.removeSuffix("/") + "/" + this.name,
        contentsCount = this.listFiles()?.count() ?: 0
    )
}

private fun TxRemoteFile.toMockAbsFile(
    files: MockRemoteFiles,
    md5: ByteArray = byteArrayOf(),
    sha1: ByteArray = byteArrayOf()
): AbsoluteFile {
    val parent = this.parent.toMockAbsFolder(files)
    // todo md5 and sha
    return MockAbsoluteFile(
        sha1,
        md5,
        files,
        parent,
        this.id,
        this.name,
        parent.absolutePath.removeSuffix("/") + "/" + this.name
    )
}

internal open class MockAbsoluteFolder(
    internal val files: MockRemoteFiles,
    override val parent: AbsoluteFolder? = null,
    override val id: String = "/",
    override var name: String = "/",
    override var absolutePath: String = "/",
    override val contact: FileSupported = files.contact,
    override val isFile: Boolean = false,
    override val isFolder: Boolean = true,
    override val uploadTime: Long = 0L,
    override var lastModifiedTime: Long = 0L,
    override val uploaderId: Long = 0L,
    override var contentsCount: Int = 0
) : AbsoluteFolder {
    private var _exists = true
    override suspend fun refreshed(): AbsoluteFolder? = parent!!.resolveFolderById(id)

    private fun currentTxRF() = files.fileSystem.resolveById(id)!!

    override suspend fun folders(): Flow<AbsoluteFolder> =
        currentTxRF().listFiles()?.filter { it.isDirectory }?.map { it.toMockAbsFolder(files) }?.asFlow() ?: emptyFlow()


    @JavaFriendlyAPI
    override suspend fun foldersStream(): Stream<AbsoluteFolder> =
        currentTxRF().listFiles()?.filter { it.isDirectory }?.map { it.toMockAbsFolder(files) }?.asStream()
            ?: Stream.empty()

    override suspend fun files(): Flow<AbsoluteFile> =
        currentTxRF().listFiles()?.filter { it.isFile }?.map { it.toMockAbsFile(files) }?.asFlow() ?: emptyFlow()

    @JavaFriendlyAPI
    override suspend fun filesStream(): Stream<AbsoluteFile> =
        currentTxRF().listFiles()?.filter { it.isFile }?.map { it.toMockAbsFile(files) }?.asStream() ?: Stream.empty()

    override suspend fun children(): Flow<AbsoluteFileFolder> =
        files.fileSystem.resolveById(id)!!.listFiles()?.map {
            if (it.isFile) it.toMockAbsFile(files)
            else it.toMockAbsFolder(files)
        }?.asFlow() ?: emptyFlow()

    @JavaFriendlyAPI
    override suspend fun childrenStream(): Stream<AbsoluteFileFolder> =
        files.fileSystem.resolveById(id)!!.listFiles()?.map {
            if (it.isFile) it.toMockAbsFile(files)
            else it.toMockAbsFolder(files)
        }?.asStream() ?: Stream.empty()

    override suspend fun createFolder(name: String): AbsoluteFolder {
        if (name.isBlank()) throw IllegalArgumentException("folder name cannot be blank.")
        FileSystem.checkLegitimacy(name)
        currentTxRF().mksubdir(name, 0L)
        return resolveFolder(name)!!
    }

    override suspend fun resolveFolder(name: String): AbsoluteFolder? {
        FileSystem.checkLegitimacy(name)
        if (name.isBlank()) throw IllegalArgumentException("folder path cannot be blank")
        val n = name.removePrefix("/").removeSuffix("/")
        val a = absolutePath.removeSuffix("/")
        val f = files.fileSystem.findByPath("$a/$n").firstOrNull() ?: return null
        return f.toMockAbsFolder(files)
    }

    override suspend fun resolveFolderById(id: String): AbsoluteFolder? {
        if (name.isBlank()) throw IllegalArgumentException("folder id cannot be blank.")
        if (!FileSystem.isLegal(id)) return null
        if (id == files.root.id) return files.root
        if (this.id != files.root.id) return null // tx服务器只支持一层文件夹
        val f = files.fileSystem.resolveById(id) ?: return null
        if (!f.exists || !f.isDirectory) return null
        return f.toMockAbsFolder(files)
    }

    override suspend fun resolveFileById(id: String, deep: Boolean): AbsoluteFile? {
        if (id == "/" || id.isEmpty()) throw IllegalArgumentException("Illegal file id: $id")
        files().firstOrNull { it.id == id }?.let { return it }
        if (!deep) return null
        return folders().map { it.resolveFileById(id, deep) }.firstOrNull { it != null }
    }

    override suspend fun resolveFiles(path: String): Flow<AbsoluteFile> {
        if (path.isBlank()) throw IllegalArgumentException("path cannot be blank.")
        if (!FileSystem.isLegal(path)) return emptyFlow()
        if (path[0] == '/') return files.root.resolveFiles(path.removePrefix("/"))
        return files.fileSystem.findByPath(absolutePath.removeSuffix("/") + "/" + path.removePrefix("/")).map {
            it.toMockAbsFile(files)
        }.asFlow()
    }

    @JavaFriendlyAPI
    override suspend fun resolveFilesStream(path: String): Stream<AbsoluteFile> {
        if (path.isBlank()) throw IllegalArgumentException("path cannot be blank.")
        if (!FileSystem.isLegal(path)) return Stream.empty()
        if (path[0] == '/') return files.root.resolveFilesStream(path.removePrefix("/"))
        if (path.contains("/")) return resolveFolder(path.substringBefore("/"))?.resolveFilesStream(
            path.substringAfter(
                "/"
            )
        ) ?: Stream.empty()
        return files.fileSystem.findByPath(absolutePath).map {
            it.toMockAbsFile(files)
        }.asStream()
    }

    override suspend fun resolveAll(path: String): Flow<AbsoluteFileFolder> {
        if (path.isBlank()) throw IllegalArgumentException("path cannot be blank.")
        FileSystem.checkLegitimacy(path)
        val p = if (path.startsWith("/")) path
        else "${absolutePath.removeSuffix("/")}/$path"
        return files.fileSystem.findByPath(p).map {
            if (it.isDirectory) it.toMockAbsFolder(files)
            else it.toMockAbsFile(files)
        }.asFlow()
    }

    @JavaFriendlyAPI
    override suspend fun resolveAllStream(path: String): Stream<AbsoluteFileFolder> {
        if (path.isBlank()) throw IllegalArgumentException("path cannot be blank.")
        FileSystem.checkLegitimacy(path)
        val p = if (path.startsWith("/")) path
        else "${absolutePath.removeSuffix("/")}/$path"
        return files.fileSystem.findByPath(p).map {
            if (it.isDirectory) it.toMockAbsFolder(files)
            else it.toMockAbsFile(files)
        }.asStream()
    }

    override suspend fun uploadNewFile(
        filepath: String, content: ExternalResource, callback: ProgressionCallback<AbsoluteFile, Long>?
    ): AbsoluteFile {
        FileSystem.checkLegitimacy(filepath)
        val folderName = filepath.removePrefix("/").substringBeforeLast("/")
        val folder =
            if (folderName == "") files.root
            else if (filepath.removePrefix("/").contains("/")) resolveFolder(folderName) ?: createFolder(folderName)
            else this
        val f = files.fileSystem.resolveById(folder.id)!!
            .uploadFile(filepath.substringAfterLast("/"), content, 0L)
        return f.toMockAbsFile(files, content.md5, content.sha1)
    }

    override suspend fun exists(): Boolean = _exists

    override suspend fun renameTo(newName: String): Boolean {
        if (files.fileSystem.resolveById(id)!!.rename(newName)) {
            refresh()
            return true
        }
        return false
    }

    override suspend fun delete(): Boolean {
        if (!_exists) return false
        if (files.fileSystem.resolveById(id)!!.delete()) {
            _exists = false
            return true
        }
        return false
    }

    override suspend fun refresh(): Boolean {
        val new = refreshed() ?: let {
            _exists = false
            return false
        }
        this.name = new.name
        this.lastModifiedTime = new.lastModifiedTime
        this.contentsCount = new.contentsCount
        this.absolutePath = new.absolutePath
        return false
    }

    override fun toString(): String = "MockAbsoluteFolder(id=$id,absolutePath=$absolutePath,name=$name"
    override fun equals(other: Any?): Boolean =
        other != null && other is AbsoluteFolder && other.id == id

    override fun hashCode(): Int {
        // from AbsoluteFolderImpl
        var result = super.hashCode()
        result = 31 * result + contentsCount.hashCode()
        return result
    }
}