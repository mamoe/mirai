/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.mock.internal.remotefile.v2

import net.mamoe.mirai.contact.FileSupported
import net.mamoe.mirai.contact.file.AbsoluteFile
import net.mamoe.mirai.contact.file.AbsoluteFolder
import net.mamoe.mirai.message.data.FileMessage

internal class MockAbsoluteFile(
    override val sha1: ByteArray,
    override val md5: ByteArray,
    override val contact: FileSupported,
    override val parent: AbsoluteFolder?,
    override val id: String,
    override val name: String,
    override val absolutePath: String,
    override val expiryTime: Long = 0L,
    override val size: Long = 0,
    override val isFile: Boolean = true,
    override val isFolder: Boolean = false,
    override val uploadTime: Long = 0,
    override val lastModifiedTime: Long = 0,
    override val uploaderId: Long = 0
) : AbsoluteFile {
    override suspend fun moveTo(folder: AbsoluteFolder): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun getUrl(): String? {
        TODO("Not yet implemented")
    }

    override fun toMessage(): FileMessage {
        TODO("Not yet implemented")
    }

    override suspend fun refreshed(): AbsoluteFile? {
        TODO("Not yet implemented")
    }

    override suspend fun exists(): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun renameTo(newName: String): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun delete(): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun refresh(): Boolean {
        TODO("Not yet implemented")
    }

    override fun toString(): String = "MockAbsoluteFile(id=$id,absolutePath=$absolutePath,name=$name)"
}