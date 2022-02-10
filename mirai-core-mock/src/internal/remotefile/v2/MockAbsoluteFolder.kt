/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.mock.internal.remotefile.v2

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import net.mamoe.mirai.contact.FileSupported
import net.mamoe.mirai.contact.file.AbsoluteFile
import net.mamoe.mirai.contact.file.AbsoluteFileFolder
import net.mamoe.mirai.contact.file.AbsoluteFolder
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.JavaFriendlyAPI
import net.mamoe.mirai.utils.ProgressionCallback
import java.util.stream.Stream

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
    override suspend fun refreshed(): AbsoluteFolder? {
        TODO("Not yet implemented")
    }

    private fun currentTxRF() =
        if (absolutePath == "/") files.fileSystem.root else files.fileSystem.findByPath(absolutePath).first()

    override suspend fun folders(): Flow<AbsoluteFolder> = flow {
        currentTxRF().listFiles()?.forEach {
            println(it)
            if (it.isDirectory) emit(resolveFolder(it.name)!!)
        }
    }

    @JavaFriendlyAPI
    override suspend fun foldersStream(): Stream<AbsoluteFolder> {
        TODO("Not yet implemented")
    }

    override suspend fun files(): Flow<AbsoluteFile> = flow {
        // note: listFiles 应该只返回不重复的文件, 准备在txFile里支持重复文件:每个文件名后面带有递增序号以支持重复的文件
        currentTxRF().listFiles()?.forEach { txRFs ->
            if (txRFs.isFile) {
                resolveFiles(txRFs.name).collect {
                    emit(it)
                }
            }
        }
    }

    @JavaFriendlyAPI
    override suspend fun filesStream(): Stream<AbsoluteFile> {
        TODO("Not yet implemented")
    }

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
        val n = name.removePrefix("/").removeSuffix("/")
        val a = absolutePath.removeSuffix("/")
        val f = files.fileSystem.findByPath("$a/$n").firstOrNull()
        if (f != null)
            return MockAbsoluteFolder(files, this, f.id, f.name, "$a/$n")
        return null
    }

    override suspend fun resolveFolderById(id: String): AbsoluteFolder? {
        TODO("Not yet implemented")
    }

    override suspend fun resolveFileById(id: String, deep: Boolean): AbsoluteFile? {
        TODO("Not yet implemented")
    }

    override suspend fun resolveFiles(path: String): Flow<AbsoluteFile> {
        checkLegitimacy(path)
        val f = if (path[0] == '/') // abs
            files.fileSystem.findByPath(path)
        else {
            val n = path.removePrefix("/")
            val a = absolutePath.removeSuffix("/")
            files.fileSystem.findByPath("$a/$n")
        }
        return flow {
            f.forEach {
                // TODO get md5 and sha1
                emit(
                    MockAbsoluteFile(
                        byteArrayOf(),
                        byteArrayOf(),
                        files.contact,
                        this@MockAbsoluteFolder,
                        it.id,
                        it.name,
                        "${absolutePath.removeSuffix("/")}/${it.name}"
                    )
                )
            }
        }
    }

    @JavaFriendlyAPI
    override suspend fun resolveFilesStream(path: String): Stream<AbsoluteFile> {
        TODO("Not yet implemented")
    }

    override suspend fun resolveAll(path: String): Flow<AbsoluteFileFolder> {
        TODO("Not yet implemented")
    }

    @JavaFriendlyAPI
    override suspend fun resolveAllStream(path: String): Stream<AbsoluteFileFolder> {
        TODO("Not yet implemented")
    }

    override suspend fun uploadNewFile(
        filepath: String,
        content: ExternalResource,
        callback: ProgressionCallback<AbsoluteFile, Long>?
    ): AbsoluteFile {
        val folderName = filepath.removePrefix("/").substringBeforeLast("/")
        val folder = if (filepath.removePrefix("/").contains("/"))
            resolveFolder(folderName) ?: createFolder(folderName)
        else
            this
        val f = files.fileSystem.findByPath(folder.absolutePath).first()
            .uploadFile(filepath.substringAfterLast("/"), content, 0L)
        return MockAbsoluteFile(
            content.sha1,
            content.md5,
            files.contact,
            files.root.resolveFolder(filepath.substringBeforeLast("/")),
            f.id,
            f.name,
            f.path
        )
    }

    override suspend fun exists(): Boolean = files.fileSystem.resolveById(id) != null

    override suspend fun renameTo(newName: String): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun delete(): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun refresh(): Boolean {
        TODO("Not yet implemented")
    }

    override fun toString(): String = "MockAbsoluteFolder(id=$id,absolutePath=$absolutePath,name=$name"
}