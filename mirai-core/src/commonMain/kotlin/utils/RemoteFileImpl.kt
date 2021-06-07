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
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.isOperator
import net.mamoe.mirai.internal.asQQAndroidBot
import net.mamoe.mirai.internal.contact.groupCode
import net.mamoe.mirai.internal.message.FileMessageImpl
import net.mamoe.mirai.internal.network.highway.Highway
import net.mamoe.mirai.internal.network.highway.ResourceKind
import net.mamoe.mirai.internal.network.protocol
import net.mamoe.mirai.internal.network.protocol.data.proto.*
import net.mamoe.mirai.internal.network.protocol.packet.chat.FileManagement
import net.mamoe.mirai.internal.network.protocol.packet.chat.toResult
import net.mamoe.mirai.internal.network.protocol.packet.sendAndExpect
import net.mamoe.mirai.internal.utils.io.serialization.toByteArray
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.FileMessage
import net.mamoe.mirai.message.data.sendTo
import net.mamoe.mirai.utils.*
import net.mamoe.mirai.utils.RemoteFile.Companion.ROOT_PATH
import java.util.*
import kotlin.contracts.contract

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
    val id: String, // fileId or folderId
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
    val md5: ByteArray, // for file only
) {
    companion object {
        val root = RemoteFileInfo(
            "/", false, "/", "/", "", 0, 0, 0, 0, 0, 0, EMPTY_BYTE_ARRAY, EMPTY_BYTE_ARRAY
        )
    }
}

internal fun RemoteFile.checkIsImpl(): RemoteFileImpl {
    contract { returns() implies (this@checkIsImpl is RemoteFileImpl) }
    return this as? RemoteFileImpl ?: error("RemoteFile must not be implemented manually.")
}

internal class RemoteFileImpl(
    contact: Group,
    override val path: String, // absolute
) : RemoteFile {
    private val contactRef by contact.weakRef()
    override val contact get() = contactRef ?: error("RemoteFile is closed due to Contact closed.")

    constructor(contact: Group, parent: String, name: String) : this(contact, fs.normalize(parent, name))

    override var id: String? = null

    override val name: String
        get() = path.substringAfterLast('/')

    private val bot get() = contact.bot.asQQAndroidBot()
    private val client get() = bot.client

    override val parent: RemoteFileImpl?
        get() {
            if (path == ROOT_PATH) return null
            val s = path.substringBeforeLast('/')
            return RemoteFileImpl(contact, s.ifEmpty { ROOT_PATH })
        }

    /**
     * Prefer id matching.
     */
    private suspend fun Flow<Oidb0x6d8.GetFileListRspBody.Item>.findMatching(): Oidb0x6d8.GetFileListRspBody.Item? {
        var nameMatching: Oidb0x6d8.GetFileListRspBody.Item? = null

        val idMatching = firstOrNull {
            if (it.name == this@RemoteFileImpl.name) {
                nameMatching = it
            }
            it.id == this@RemoteFileImpl.id
        }

        return idMatching ?: nameMatching
    }

    private suspend fun getFileFolderInfo(): RemoteFileInfo? {
        val parent = parent ?: return RemoteFileInfo.root
        val info = parent.getFilesFlow()
            .filter { it.name == this.name }
            .findMatching()
            ?: return null
        return when {
            info.folderInfo != null -> info.folderInfo.run {
                RemoteFileInfo(
                    id = folderId,
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
                    md5 = EMPTY_BYTE_ARRAY,
                )
            }
            info.fileInfo != null -> info.fileInfo.run {
                RemoteFileInfo(
                    id = fileId,
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
                    md5 = md5,
                )
            }
            else -> null
        }
    }

    private fun RemoteFileInfo?.checkExists(thisPath: String, kind: String = "Remote path"): RemoteFileInfo {
        if (this == null) throw IllegalStateException("$kind '$thisPath' does not exist.")
        return this
    }

    override suspend fun isFile(): Boolean = this.getFileFolderInfo().checkExists(this.path).isFile
    override suspend fun length(): Long = this.getFileFolderInfo().checkExists(this.path).size
    override suspend fun exists(): Boolean = this.getFileFolderInfo() != null
    override suspend fun getInfo(): RemoteFile.FileInfo? {
        return getFileFolderInfo()?.run {
            RemoteFile.FileInfo(
                name = name,
                id = id,
                path = path,
                length = size,
                downloadTimes = downloadTimes,
                uploaderId = creatorId,
                uploadTime = createTime,
                lastModifyTime = modifyTime,
                sha1 = sha,
                md5 = md5,
            )
        }
    }

    private suspend fun getFilesFlow(): Flow<Oidb0x6d8.GetFileListRspBody.Item> {
        val info = getFileFolderInfo() ?: return emptyFlow()

        return flow {
            var index = 0
            while (true) {
                val list = FileManagement.GetFileList(
                    client,
                    groupCode = contact.id,
                    folderId = info.id,
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
        }?.also {
            it.id = item.id
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

    override fun resolve(relative: String) = RemoteFileImpl(contact, this.path, relative)
    override fun resolve(relative: RemoteFile): RemoteFileImpl {
        if (relative.checkIsImpl().contact !== this.contact) error("`relative` must be obtained from the same Group as `this`.")

        return resolve(relative.path).also { it.id = relative.id }
    }

    override suspend fun resolveById(id: String, deep: Boolean): RemoteFile? {
        if (this.id == id) return this
        val dirs = mutableListOf<Oidb0x6d8.GetFileListRspBody.Item>()
        getFilesFlow().mapNotNull { item ->
            when {
                item.id == id -> item.resolveToFile()
                deep && item.folderInfo != null -> {
                    dirs.add(item)
                    null
                }
                else -> null
            }
        }.firstOrNull()?.let { return it }
        for (dir in dirs) {
            dir.resolveToFile()?.resolveById(id, deep)?.let { return it }
        }
        return null
    }

    override fun resolveSibling(relative: String): RemoteFileImpl {
        val parent = this.parent
        if (parent == null) {
            if (fs.normalize(relative) == ROOT_PATH) error("Root path does not have sibling paths.")
            return RemoteFileImpl(contact, ROOT_PATH)
        }
        return RemoteFileImpl(contact, parent.path, relative)
    }

    override fun resolveSibling(relative: RemoteFile): RemoteFileImpl {
        if (relative.checkIsImpl().contact !== this.contact) error("`relative` must be obtained from the same Group as `this`.")

        return resolveSibling(relative.path).also { it.id = relative.id }
    }

    private fun RemoteFileInfo.isOperable(): Boolean =
        creatorId == bot.id || contact.botPermission.isOperator()

    private fun isBotOperator(): Boolean = contact.botPermission.isOperator()

    override suspend fun delete(): Boolean {
        val info = getFileFolderInfo() ?: return false
        if (!info.isOperable()) return false
        return when {
            info.isFile -> {
                FileManagement.DeleteFile(
                    client,
                    groupCode = contact.id,
                    busId = info.busId,
                    fileId = info.id,
                    parentFolderId = info.parentFolderId,
                ).sendAndExpect(bot).toResult("RemoteFile.delete", checkResp = false).getOrThrow().int32RetCode == 0
            }
//            recursively -> {
//                this.listFiles().collect { child ->
//                    child.delete()
//                }
//                this.delete()
//            }
            else -> {
                // natively 'recursive'
                FileManagement.DeleteFolder(
                    client, contact.id, info.id
                ).sendAndExpect(bot).toResult("RemoteFile.delete").getOrThrow().int32RetCode == 0
            }
        }
    }

    override suspend fun renameTo(name: String): Boolean {
        if (path == ROOT_PATH && name != ROOT_PATH) return false

        val normalized = fs.normalize(name)
        if (normalized.contains('/')) throw IllegalArgumentException("'/' is not allowed in file or directory names. Given: '$name'.")

        val info = getFileFolderInfo() ?: return false
        if (!info.isOperable()) return false
        return if (info.isFile) {
            FileManagement.RenameFile(client, contact.id, info.busId, info.id, info.parentFolderId, normalized)
        } else {
            FileManagement.RenameFolder(client, contact.id, info.id, normalized)
        }.sendAndExpect(bot).toResult("RemoteFile.renameTo", checkResp = false).getOrThrow().int32RetCode == 0
    }

    /**
     * null means not exist
     */
    private suspend fun getIdSmart(): String? {
        if (path == ROOT_PATH) return ROOT_PATH
        return this.id ?: this.getFileFolderInfo()?.id
    }

    override suspend fun moveTo(target: RemoteFile): Boolean {
        if (target.checkIsImpl().contact != this.contact) {
            // TODO: 2021/3/4 cross-group file move

            //                target.mkdir()
//                val targetFolderId = target.getIdSmart() ?: return false
//                this.listFiles().mapNotNull { it.checkIsImpl().getFileFolderInfo() }.collect {
//                    FileManagement.MoveFile(client, contact.id, it.busId, it.id, it.parentFolderId, targetFolderId)
//                        .sendAndExpect(bot).toResult("RemoteFile.moveTo", checkResp = false).getOrThrow()
//
//                    // TODO: 2021/3/3 batch packets
//                }
//                this.delete() // it is now empty

            error("Cross-group file operation is not yet supported.")
        }
        if (target.path == this.path) return true
        if (target.parent?.path == this.path) return false
        val info = getFileFolderInfo() ?: return false
        if (!info.isOperable()) return false
        return if (info.isFile) {
            val newParentId = target.parent?.checkIsImpl()?.getIdSmart() ?: return false
            FileManagement.MoveFile(client, contact.id, info.busId, info.id, info.parentFolderId, newParentId)
                .sendAndExpect(bot).toResult("RemoteFile.moveTo", checkResp = false).getOrThrow().int32RetCode == 0
        } else {
            return FileManagement.RenameFolder(client, contact.id, info.id, target.name).sendAndExpect(bot)
                .toResult("RemoteFile.moveTo", checkResp = false).getOrThrow().int32RetCode == 0
        }
    }


    override suspend fun mkdir(): Boolean {
        if (path == ROOT_PATH) return false
        if (!isBotOperator()) return false

        val parentFolderId: String = parent?.getIdSmart() ?: return false

        return FileManagement.CreateFolder(client, contact.id, parentFolderId, this.name)
            .sendAndExpect(bot).toResult("RemoteFile.mkdir", checkResp = false).getOrThrow().int32RetCode == 0
    }

    private suspend fun upload0(
        resource: ExternalResource,
        callback: RemoteFile.ProgressionCallback?
    ): Oidb0x6d6.UploadFileRspBody? {
        val parent = parent ?: return null
        val parentInfo = parent.getFileFolderInfo() ?: return null
        val resp = FileManagement.RequestUpload(
            client,
            groupCode = contact.id,
            folderId = parentInfo.id,
            resource = resource,
            filename = this.name
        ).sendAndExpect(bot).toResult("RemoteFile.upload").getOrThrow()
        if (resp.boolFileExist) {
            return resp
        }

        val ext = GroupFileUploadExt(
            u1 = 100,
            u2 = 1,
            entry = GroupFileUploadEntry(
                business = ExcitingBusiInfo(
                    busId = resp.busId,
                    senderUin = bot.id,
                    receiverUin = contact.groupCode, // TODO: 2021/3/1 code or uin?
                    groupCode = contact.groupCode,
                ),
                fileEntry = ExcitingFileEntry(
                    fileSize = resource.size,
                    md5 = resource.md5,
                    sha1 = resource.sha1,
                    fileId = resp.fileId.toByteArray(),
                    uploadKey = resp.checkKey,
                ),
                clientInfo = ExcitingClientInfo(
                    clientType = 2,
                    appId = client.protocol.id.toString(),
                    terminalType = 2,
                    clientVer = "9e9c09dc",
                    unknown = 4,
                ),
                fileNameInfo = ExcitingFileNameInfo(this.name),
                host = ExcitingHostConfig(
                    hosts = listOf(
                        ExcitingHostInfo(
                            url = ExcitingUrlInfo(
                                unknown = 1,
                                host = resp.uploadIpLanV4.firstOrNull()
                                    ?: resp.uploadIpLanV6.firstOrNull()
                                    ?: resp.uploadIp,
                            ),
                            port = resp.uploadPort,
                        ),
                    ),
                ),
            ),
            u3 = 0,
        ).toByteArray(GroupFileUploadExt.serializer())

        callback?.onBegin(this, resource)

        kotlin.runCatching {
            Highway.uploadResourceBdh(
                bot = bot,
                resource = resource,
                kind = ResourceKind.GROUP_FILE,
                commandId = 71,
                extendInfo = ext,
                dataFlag = 0,
                callback = if (callback == null) null else fun(it: Long) {
                    callback.onProgression(this, resource, it)
                }
            )
        }.fold(
            onSuccess = {
                callback?.onSuccess(this, resource)
            },
            onFailure = {
                callback?.onFailure(this, resource, it)
            }
        )

        return resp
    }

    override suspend fun upload(
        resource: ExternalResource,
        callback: RemoteFile.ProgressionCallback?
    ): FileMessage {
        val resp = upload0(resource, null) ?: error("Failed to upload file.")
        return FileMessageImpl(
            resp.fileId, resp.busId, name, resource.size
        )
    }

    override suspend fun uploadAndSend(resource: ExternalResource): MessageReceipt<Contact> {
        @Suppress("DEPRECATION")
        return upload(resource).sendTo(contact)
    }

//    override suspend fun writeSession(resource: ExternalResource): FileUploadSession {
//    }

    override suspend fun getDownloadInfo(): RemoteFile.DownloadInfo? {
        val info = getFileFolderInfo() ?: return null
        if (!info.isFile) return null
        val resp = FileManagement.RequestDownload(
            client,
            groupCode = contact.id,
            busId = info.busId,
            fileId = info.id
        ).sendAndExpect(bot).toResult("RemoteFile.getDownloadInfo").getOrThrow()

        return RemoteFile.DownloadInfo(
            filename = name,
            id = info.id,
            path = path,
            url = "http://${resp.downloadIp}/ftn_handler/${resp.downloadUrl.toUHexString("")}/?fname=" +
                    info.id.toByteArray().toUHexString(""),
            sha1 = info.sha,
            md5 = info.md5
        )
    }

    override fun toString(): String = path

    override suspend fun toMessage(): FileMessage? {
        val info = getFileFolderInfo() ?: return null
        if (!info.isFile) return null
        return FileMessageImpl(info.id, info.busId, name, info.size)
    }
}
