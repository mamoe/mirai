/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.mock.internal.remotefile.v2

import kotlinx.coroutines.flow.*
import net.mamoe.mirai.contact.FileSupported
import net.mamoe.mirai.contact.file.AbsoluteFile
import net.mamoe.mirai.contact.file.AbsoluteFileFolder
import net.mamoe.mirai.contact.file.AbsoluteFolder
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.JavaFriendlyAPI
import net.mamoe.mirai.utils.ProgressionCallback
import java.util.stream.Stream
import kotlin.streams.asStream

internal open class MockAbsoluteFolder(
    val files: MockRemoteFiles,
    override val parent: AbsoluteFolder? = null,
    override val id: String = "/",
    override val name: String = "/",
    override val absolutePath: String = "/",
    override val contact: FileSupported = files.contact,
    override val isFile: Boolean = false,
    override val isFolder: Boolean = true,
    override val uploadTime: Long = 0L,
    override val lastModifiedTime: Long = 0L,
    override val uploaderId: Long = 0L,
    override val contentsCount: Int = 0
) : AbsoluteFolder {
    private var _exists = true
    override suspend fun refreshed(): AbsoluteFolder? =
        (parent ?: files.root).folders().firstOrNull { it.id == id }

    private fun currentTxRF() =
        if (absolutePath == "/") files.fileSystem.root else files.fileSystem.findByPath(absolutePath).first()

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

    override suspend fun children(): Flow<AbsoluteFileFolder> {
        TODO("Not yet implemented")
    }

    @JavaFriendlyAPI
    override suspend fun childrenStream(): Stream<AbsoluteFileFolder> {
        TODO("Not yet implemented")
    }

    override suspend fun createFolder(name: String): AbsoluteFolder {
        if (name.isBlank()) throw IllegalArgumentException("folder name cannot be blank.")
        checkLegitimacy(name)
        currentTxRF().mksubdir(name, 0L)
        return resolveFolder(name)!!
    }

    override suspend fun resolveFolder(name: String): AbsoluteFolder? {
        checkLegitimacy(name)
        if (name.isBlank()) throw IllegalArgumentException("folder path cannot be blank")
        val n = name.removePrefix("/").removeSuffix("/")
        val a = absolutePath.removeSuffix("/")
        val f = files.fileSystem.findByPath("$a/$n").firstOrNull() ?: return null
        return f.toMockAbsFolder(files)
    }

    override suspend fun resolveFolderById(id: String): AbsoluteFolder? {
        if (name.isBlank()) throw IllegalArgumentException("folder id cannot be blank.")
        if (!isLegal(id)) return null
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
        return folders().map { it.resolveFileById(id, deep) }.firstOrNull()
    }

    override suspend fun resolveFiles(path: String): Flow<AbsoluteFile> {
        if (path.isBlank()) throw IllegalArgumentException("path cannot be blank.")
        if (isLegal(path)) return emptyFlow()
        if (path[0] == '/') return files.root.resolveFiles(path.removePrefix("/"))
        if (path.contains("/")) return resolveFolder(path.substringBefore("/"))?.resolveFiles(path.substringAfter("/"))
            ?: emptyFlow()
        return flow {
            files.fileSystem.findByPath(absolutePath).map {
                it.toMockAbsFile(files)
            }.asFlow()
        }
    }

    @JavaFriendlyAPI
    override suspend fun resolveFilesStream(path: String): Stream<AbsoluteFile> {
        if (path.isBlank()) throw IllegalArgumentException("path cannot be blank.")
        if (isLegal(path)) return Stream.empty()
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
        checkLegitimacy(path)
        val p = if (path.startsWith("/")) path
        else "${absolutePath.removeSuffix("/")}/$path"
        return files.fileSystem.findByPath(p).map {
            if (it.isDirectory) it.toMockAbsFolder(files)
            else it.toMockAbsFile(files)
        }.asFlow()
    }

    @JavaFriendlyAPI
    override suspend fun resolveAllStream(path: String): Stream<AbsoluteFileFolder> {
        TODO("Not yet implemented")
    }

    override suspend fun uploadNewFile(
        filepath: String, content: ExternalResource, callback: ProgressionCallback<AbsoluteFile, Long>?
    ): AbsoluteFile {
        val folderName = filepath.removePrefix("/").substringBeforeLast("/")
        val folder =
            if (filepath.removePrefix("/").contains("/")) resolveFolder(folderName) ?: createFolder(folderName)
            else this
        val f = files.fileSystem.findByPath(folder.absolutePath).first()
            .uploadFile(filepath.substringAfterLast("/"), content, 0L)
        return f.toMockAbsFile(files, content.md5, content.sha1)
    }

    override suspend fun exists(): Boolean = _exists

    override suspend fun renameTo(newName: String): Boolean {
        TODO("Not yet implemented")
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
        TODO("Not yet implemented")
    }

    override fun toString(): String = "MockAbsoluteFolder(id=$id,absolutePath=$absolutePath,name=$name"
}