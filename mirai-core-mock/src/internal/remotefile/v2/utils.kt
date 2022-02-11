/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.mock.internal.remotefile.v2

import net.mamoe.mirai.contact.file.AbsoluteFile
import net.mamoe.mirai.contact.file.AbsoluteFolder
import net.mamoe.mirai.mock.txfs.TxRemoteFile

internal fun isLegal(path: String): Boolean {
    return path.firstOrNull { it in """:*?"<>|""" } == null
}

internal fun checkLegitimacy(path: String) {
    val char = path.firstOrNull { it in """:*?"<>|""" }
    if (char != null) {
        throw IllegalArgumentException("""Chars ':*?"<>|' are not allowed in path. RemoteFile path contains illegal char: '$char'. path='$path'""")
    }
}

internal fun TxRemoteFile.toMockAbsFolder(files: MockRemoteFiles): AbsoluteFolder {
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

internal fun TxRemoteFile.toMockAbsFile(
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