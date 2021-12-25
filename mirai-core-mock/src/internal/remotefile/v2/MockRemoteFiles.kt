/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("ClassName")

package net.mamoe.mirai.mock.internal.remotefile.v2

import kotlinx.coroutines.flow.Flow
import net.mamoe.mirai.contact.FileSupported
import net.mamoe.mirai.contact.file.AbsoluteFile
import net.mamoe.mirai.contact.file.AbsoluteFileFolder
import net.mamoe.mirai.contact.file.AbsoluteFolder
import net.mamoe.mirai.contact.file.RemoteFiles
import net.mamoe.mirai.mock.txfs.TxFileSystem
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.JavaFriendlyAPI
import net.mamoe.mirai.utils.ProgressionCallback
import java.util.stream.Stream

internal class MockRemoteFiles(
    override val contact: FileSupported,
    val fileSystem: TxFileSystem,
) : RemoteFiles {
    override val root: AbsoluteFolder = MRF_AbsoluteFolderRoot(this)
}

internal class MRF_AbsoluteFolderRoot(
    val files: MockRemoteFiles,
) : AbsoluteFolder {
    override val contentsCount: Int get() = 0
    override suspend fun refreshed(): AbsoluteFolder = this

    override val contact: FileSupported get() = files.contact
    override val parent: AbsoluteFolder? get() = null
    override val id: String get() = "/"
    override val name: String get() = "/"
    override val absolutePath: String get() = "/"
    override val isFile: Boolean get() = false
    override val isFolder: Boolean get() = true
    override val uploadTime: Long get() = 0
    override val lastModifiedTime: Long get() = 0
    override val uploaderId: Long get() = 0
    override suspend fun exists(): Boolean = true
    override suspend fun renameTo(newName: String): Boolean = false
    override suspend fun delete(): Boolean = false
    override suspend fun refresh(): Boolean = true
    override fun toString(): String = absolutePath


    override suspend fun folders(): Flow<AbsoluteFolder> {
        TODO("Not yet implemented")
    }

    @JavaFriendlyAPI
    override suspend fun foldersStream(): Stream<AbsoluteFolder> {
        TODO("Not yet implemented")
    }

    override suspend fun files(): Flow<AbsoluteFile> {
        TODO("Not yet implemented")
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
        TODO("Not yet implemented")
    }

    override suspend fun resolveFolder(name: String): AbsoluteFolder? {
        TODO("Not yet implemented")
    }

    override suspend fun resolveFolderById(id: String): AbsoluteFolder? {
        TODO("Not yet implemented")
    }

    override suspend fun resolveFileById(id: String, deep: Boolean): AbsoluteFile? {
        TODO("Not yet implemented")
    }

    override suspend fun resolveFiles(path: String): Flow<AbsoluteFile> {
        TODO("Not yet implemented")
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
        TODO("Not yet implemented")
    }
}
