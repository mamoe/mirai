/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("ClassName")

package net.mamoe.mirai.mock.internal.absolutefile

import net.mamoe.mirai.contact.FileSupported
import net.mamoe.mirai.contact.file.AbsoluteFolder
import net.mamoe.mirai.contact.file.RemoteFiles
import net.mamoe.mirai.mock.txfs.TxFileSystem

internal class MockRemoteFiles(
    override val contact: FileSupported,
    val fileSystem: TxFileSystem,
) : RemoteFiles {
    override val root: AbsoluteFolder = MRF_AbsoluteFolderRoot(this)
}

internal class MRF_AbsoluteFolderRoot(files: MockRemoteFiles) : MockAbsoluteFolder(files) {
    override var contentsCount: Int
        get() = 0
        set(value) {}

    override suspend fun refreshed(): AbsoluteFolder = MRF_AbsoluteFolderRoot(files)
    override val parent: AbsoluteFolder? get() = null
    override val id: String get() = "/"
    override var name: String
        get() = "/"
        set(value) {}
    override var absolutePath: String
        get() = "/"
        set(value) {}
    override val isFile: Boolean get() = false
    override val isFolder: Boolean get() = true
    override val uploadTime: Long get() = 0
    override var lastModifiedTime: Long
        get() = 0
        set(value) {}
    override val uploaderId: Long get() = 0
    override suspend fun exists(): Boolean = true
    override suspend fun renameTo(newName: String): Boolean = false
    override suspend fun delete(): Boolean = false
    override suspend fun refresh(): Boolean = true
}
