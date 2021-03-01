/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.flow
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.internal.asQQAndroidBot
import net.mamoe.mirai.internal.network.protocol.packet.chat.FileManagement
import net.mamoe.mirai.internal.network.protocol.packet.chat.toResult
import net.mamoe.mirai.internal.network.protocol.packet.sendAndExpect
import net.mamoe.mirai.utils.*
import java.io.InputStream
import java.io.OutputStream
import java.io.RandomAccessFile
import kotlin.coroutines.CoroutineContext

private val fs = FileSystem

// internal for tests
internal object FileSystem {
    fun checkLegitimacy(path: String) {
        val char = path.firstOrNull { it in """:*?"<>|""" }
        if (char != null) {
            throw IllegalArgumentException("""Chars ':*?"<>|' are not allowed in path. RemoteFile path contains illegal char: '$char'. path='$path'""")
        }
    }

    fun normalize(path: String): String {
        checkLegitimacy(path)
        return path.replace('\\', '/')
    }

    // TODO: 2021/2/25 add tests for FS
    // net.mamoe.mirai.internal.utils.internal.utils.FileSystemTest

    fun normalize(parent: String, name: String): String {
        var nName = normalize(name)
        if (nName.startsWith('/')) return nName // absolute path then ignore parent
        nName = nName.removeSuffix("/")

        var nParent = normalize(parent)
        if (nParent == "/") return "/$nName"
        if (!nParent.startsWith('/')) nParent = "/$nParent"

        val slash = nName.indexOf('/')
        if (slash != -1) {
            nParent += '/' + nName.substring(0, slash)
            nName = nName.substring(slash + 1)
        }

        return "$nParent/$nName"
    }
}

internal class RemoteFileInfo(
    val isFile: Boolean,
    val path: String, // fileId
    val name: String,
    val size: Long,
    val busId: Int, // for file only
    val creatorId: Long, //ownerUin, createUin
    val createTime: Long, // uploadTime, createTime
    val modifyTime: Long,
    val sha: ByteArray, // for file only
    val md5: ByteArray, // for file only
    val downloadTimes: Int,
)

internal class RemoteFileImpl(
    contact: Group,
    override val path: String, // absolute
) : RemoteFile {
    private val contactRef by contact.weakRef()
    private val contact get() = contactRef ?: error("RemoteFile is closed due to Contact closed.")

    constructor(contact: Group, parent: String, name: String) : this(contact, fs.normalize(parent, name))

    override val name: String
        get() = path.substringAfterLast('/')

    private val bot get() = contact.bot.asQQAndroidBot()
    private val client get() = bot.client

    override val parent: RemoteFile?
        get() {
            val s = path.substringBeforeLast('/', "")
            if (s.isEmpty()) return null
            return RemoteFileImpl(contact, s)
        }

    private suspend fun getFileFolderInfo(): RemoteFileInfo? {
        TODO()
    }

    private fun RemoteFileInfo?.checkExists(thisPath: String): RemoteFileInfo {
        if (this == null) throw IllegalStateException("Remote path '$thisPath' does not exist.")
        return this
    }

    override suspend fun isFile(): Boolean = this.getFileFolderInfo().checkExists(this.path).isFile
    override suspend fun length(): Long = this.getFileFolderInfo().checkExists(this.path).size
    override suspend fun exists(): Boolean = this.getFileFolderInfo() != null

    override suspend fun listFiles(): Flow<RemoteFile> {
        return flow {
            var index = 0
            while (true) {
                val list = FileManagement.GetFileList(
                    client,
                    groupCode = contact.id,
                    folderId = path,
                    startIndex = index
                ).sendAndExpect(bot).toResult("get group file").getOrThrow()
                index += list.itemList.size

                if (list.int32RetCode != 0) return@flow
                if (list.itemList.isEmpty()) return@flow

                for (item in list.itemList) {
                    when {
                        item.fileInfo != null -> {
                            emit(resolve(item.fileInfo.fileName))
                        }
                        item.folderInfo != null -> {
                            emit(resolve(item.folderInfo.folderName))
                        }
                        else -> {
                        }
                    }
                }
            }
        }
    }

    @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
    @OptIn(JavaFriendlyAPI::class)
    override suspend fun listFilesIterator(): Iterator<RemoteFile> {
        TODO("Not yet implemented")
    }

    override fun resolve(relativePath: String): RemoteFile {
        return RemoteFileImpl(contact, this.path, relativePath)
    }

    override fun resolveSibling(other: String): RemoteFile {
        val parent = this.parent
        if (parent == null) {
            if (fs.normalize(other) != "/") error("Remote path '/' does not have sibling paths.")
            return RemoteFileImpl(contact, "/")
        }
        return RemoteFileImpl(contact, parent.path, other)
    }

    override suspend fun delete(recursively: Boolean): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun moveTo(target: RemoteFile): Boolean {
        TODO("Not yet implemented")
    }

    @MiraiExperimentalApi
    override suspend fun copyTo(target: RemoteFile): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun write(resource: ExternalResource) {
        TODO("Not yet implemented")
    }

    override suspend fun open(): FileDownloadSessionImpl {
        TODO("Not yet implemented")
    }

    override fun toString(): String = path
}

internal class FileDownloadSessionImpl : FileDownloadSession, CoroutineScope {
    override val onProgression: SharedFlow<Long>
        get() = TODO("Not yet implemented")

    override suspend fun downloadTo(out: OutputStream) {
        TODO("Not yet implemented")
    }

    override suspend fun downloadTo(file: RandomAccessFile) {
        TODO("Not yet implemented")
    }

    override fun inputStream(): InputStream {
        TODO("Not yet implemented")
    }

    override val coroutineContext: CoroutineContext
        get() = TODO("Not yet implemented")


}