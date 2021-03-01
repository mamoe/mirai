/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.utils

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.contact.checkBotPermission
import net.mamoe.mirai.internal.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.internal.asQQAndroidBot
import net.mamoe.mirai.internal.network.protocol.data.proto.Oidb0x6d8
import net.mamoe.mirai.internal.network.protocol.packet.chat.FileManagement
import net.mamoe.mirai.internal.network.protocol.packet.chat.toResult
import net.mamoe.mirai.internal.network.protocol.packet.sendAndExpect
import net.mamoe.mirai.utils.*
import java.util.*

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
    val uuid: String, // fileId or folderId
    val isFile: Boolean,
    val path: String,
    val name: String,
    val parentFolderId: String,
    val size: Long,
    val busId: Int, // for file only
    val creatorId: Long, //ownerUin, createUin
    val createTime: Long, // uploadTime, createTime
    val modifyTime: Long,
    val downloadTimes: Int,
    val sha: ByteArray, // for file only
    val sha3: ByteArray,
    val md5: ByteArray, // for file only
) {
    companion object {
        val root = RemoteFileInfo(
            "", false, "/", "/", "", 0, 0, 0, 0, 0, 0, EMPTY_BYTE_ARRAY, EMPTY_BYTE_ARRAY, EMPTY_BYTE_ARRAY
        )
    }
}

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
            if (path == "/") return null
            val s = path.substringBeforeLast('/')
            return RemoteFileImpl(contact, if (s.isEmpty()) "/" else s)
        }

    private suspend fun getFileFolderInfo(): RemoteFileInfo? {
        val parent = parent ?: return RemoteFileInfo.root
        parent as RemoteFileImpl
        val info = parent.getFilesFlow()
            .filter { it.folderInfo?.folderName == this.name || it.fileInfo?.fileName == this.name }.firstOrNull()
            ?: return null
        return when {
            info.folderInfo != null -> info.folderInfo.run {
                RemoteFileInfo(
                    uuid = folderId,
                    isFile = false,
                    path = path,
                    name = folderName,
                    parentFolderId = parentFolderId,
                    size = 0,
                    busId = 0,
                    creatorId = createUin,
                    createTime = createTime.toLongUnsigned(),
                    modifyTime = modifyTime.toLongUnsigned(),
                    downloadTimes = 0,
                    sha = EMPTY_BYTE_ARRAY,
                    sha3 = EMPTY_BYTE_ARRAY,
                    md5 = EMPTY_BYTE_ARRAY,
                )
            }
            info.fileInfo != null -> info.fileInfo.run {
                RemoteFileInfo(
                    uuid = fileId,
                    isFile = true,
                    path = path,
                    name = fileName,
                    parentFolderId = parentFolderId,
                    size = fileSize,
                    busId = busId,
                    creatorId = uploaderUin,
                    createTime = uploadTime.toLongUnsigned(),
                    modifyTime = modifyTime.toLongUnsigned(),
                    downloadTimes = downloadTimes,
                    sha = sha,
                    sha3 = sha3,
                    md5 = md5,
                )
            }
            else -> null
        }
    }

    private fun RemoteFileInfo?.checkExists(thisPath: String): RemoteFileInfo {
        if (this == null) throw IllegalStateException("Remote path '$thisPath' does not exist.")
        return this
    }

    override suspend fun isFile(): Boolean = this.getFileFolderInfo().checkExists(this.path).isFile
    override suspend fun length(): Long = this.getFileFolderInfo().checkExists(this.path).size
    override suspend fun exists(): Boolean = this.getFileFolderInfo() != null

    private suspend fun getFilesFlow(): Flow<Oidb0x6d8.GetFileListRspBody.Item> {
        return flow {
            var index = 0
            while (true) {
                val list = FileManagement.GetFileList(
                    client,
                    groupCode = contact.id,
                    folderId = path,
                    startIndex = index
                ).sendAndExpect(bot).toResult("RemoteFile.listFiles").getOrThrow()
                index += list.itemList.size

                if (list.int32RetCode != 0) return@flow
                if (list.itemList.isEmpty()) return@flow

                emitAll(list.itemList.asFlow())
            }
        }
    }

    private fun Oidb0x6d8.GetFileListRspBody.Item.resolveToFile(): RemoteFile? {
        val item = this
        return when {
            item.fileInfo != null -> {
                resolve(item.fileInfo.fileName)
            }
            item.folderInfo != null -> {
                resolve(item.folderInfo.folderName)
            }
            else -> null
        }
    }

    override suspend fun listFiles(): Flow<RemoteFile> {
        return getFilesFlow().mapNotNull { item ->
            item.resolveToFile()
        }
    }

    @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
    @OptIn(JavaFriendlyAPI::class)
    override suspend fun listFilesIterator(lazy: Boolean): Iterator<RemoteFile> {
        if (!lazy) return listFiles().toList().iterator()

        return object : Iterator<RemoteFile> {
            private val queue = ArrayDeque<Oidb0x6d8.GetFileListRspBody.Item>(1)

            @Volatile
            private var index = 0
            private var ended = false

            private suspend fun updateItems() {
                val list = FileManagement.GetFileList(
                    client,
                    groupCode = contact.id,
                    folderId = path,
                    startIndex = index
                ).sendAndExpect(bot).toResult("RemoteFile.listFiles").getOrThrow()
                if (list.int32RetCode != 0 || list.itemList.isEmpty()) {
                    ended = true
                    return
                }
                index += list.itemList.size
                for (item in list.itemList) {
                    if (item.fileInfo != null || item.folderInfo != null) queue.add(item)
                }
            }

            override fun hasNext(): Boolean {
                if (queue.isEmpty() && !ended) runBlocking { updateItems() }
                return queue.isNotEmpty()
            }

            override fun next(): RemoteFile {
                return queue.removeFirst().resolveToFile()!!
            }
        }
    }

    override fun resolve(relative: String): RemoteFile {
        return RemoteFileImpl(contact, this.path, relative)
    }

    override fun resolveSibling(relative: String): RemoteFile {
        val parent = this.parent
        if (parent == null) {
            if (fs.normalize(relative) != "/") error("Remote path '/' does not have sibling paths.")
            return RemoteFileImpl(contact, "/")
        }
        return RemoteFileImpl(contact, parent.path, relative)
    }

    override suspend fun delete(recursively: Boolean): Boolean {
        if (isFile()) {
            val info = getFileFolderInfo().checkExists(path)
            contact.checkBotPermission(MemberPermission.ADMINISTRATOR)
            return FileManagement.Delete(
                client,
                groupCode = contact.id,
                busId = info.busId,
                fileId = info.uuid,
                parentFolderId = info.parentFolderId,
            ).sendAndExpect(bot).toResult("RemoteFile.delete").getOrThrow().int32RetCode == 0
        } else {
            if (recursively) {
                this.listFiles().collect { child ->
                    child.delete(recursively = true)
                }
                return this.delete(false)
            } else {
                // TODO: 2021/3/1 check delete folder, tentative implementation
                val info = getFileFolderInfo().checkExists(path)
                return FileManagement.Delete(
                    client,
                    groupCode = contact.id,
                    busId = info.busId,
                    fileId = info.uuid,
                    parentFolderId = info.parentFolderId,
                ).sendAndExpect(bot).toResult("RemoteFile.delete").getOrThrow().int32RetCode == 0
            }
        }
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

//    override suspend fun writeSession(resource: ExternalResource): FileUploadSession {
//    }

    override suspend fun getDownloadInfo(): RemoteFile.DownloadInfo {
        val info = getFileFolderInfo().checkExists(path)
        if (!info.isFile) error("Remote path $path does not refer to a file.")
        val resp = FileManagement.RequestDownload(
            client,
            groupCode = contact.id,
            busId = info.busId,
            fileId = info.uuid
        ).sendAndExpect(bot).toResult("RemoteFile.getDownloadInfo").getOrThrow()
        check(resp.int32RetCode == 0) {
            "Failed RemoteFile.getDownloadInfo, code=${resp.int32RetCode}, msg=${resp.retMsg}"
        }

        return RemoteFile.DownloadInfo(
            filename = name,
            path = path,
            url = "http://${resp.downloadIp}/ftn_handler/${resp.downloadUrl.toUHexString("")}/?fname=" +
                    info.uuid.toByteArray().toUHexString(""),
//            cookie = resp.cookieVal,
            sha = info.sha,
            sha3 = info.sha3,
            md5 = info.md5
        )
    }

    override fun toString(): String = path
}

//internal class FileUploadSessionImpl : FileUploadSession, CoroutineScope {
//    override val onProgression: SharedFlow<Long>
//        get() = TODO("Not yet implemented")
//
//    override suspend fun downloadTo(out: OutputStream) {
//        TODO("Not yet implemented")
//    }
//
//    override suspend fun downloadTo(file: RandomAccessFile) {
//        TODO("Not yet implemented")
//    }
//
//    override fun inputStream(): InputStream {
//        TODO("Not yet implemented")
//    }
//
//    override val coroutineContext: CoroutineContext
//        get() = TODO("Not yet implemented")
//
//
//}