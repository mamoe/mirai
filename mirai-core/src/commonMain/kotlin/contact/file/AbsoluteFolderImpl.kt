/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.contact.file

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.*
import net.mamoe.mirai.contact.FileSupported
import net.mamoe.mirai.contact.file.AbsoluteFile
import net.mamoe.mirai.contact.file.AbsoluteFileFolder
import net.mamoe.mirai.contact.file.AbsoluteFolder
import net.mamoe.mirai.contact.isOperator
import net.mamoe.mirai.internal.contact.GroupImpl
import net.mamoe.mirai.internal.contact.file.RemoteFilesImpl.Companion.findFileByPath
import net.mamoe.mirai.internal.message.flags.AllowSendFileMessage
import net.mamoe.mirai.internal.network.QQAndroidClient
import net.mamoe.mirai.internal.network.components.ClockHolder.Companion.clock
import net.mamoe.mirai.internal.network.highway.Highway
import net.mamoe.mirai.internal.network.highway.ResourceKind
import net.mamoe.mirai.internal.network.protocol
import net.mamoe.mirai.internal.network.protocol.data.proto.*
import net.mamoe.mirai.internal.network.protocol.packet.chat.FileManagement
import net.mamoe.mirai.internal.network.protocol.packet.chat.toResult
import net.mamoe.mirai.internal.utils.FileSystem
import net.mamoe.mirai.internal.utils.io.serialization.toByteArray
import net.mamoe.mirai.utils.*

internal fun Oidb0x6d8.GetFileListRspBody.Item.resolved(parent: CommonAbsoluteFolderImpl): AbsoluteFileFolder? {
    val item = this
    return when {
        item.fileInfo != null -> {
            parent.createChildFile(item.fileInfo)
        }
        item.folderInfo != null -> {
            parent.createChildFolder(item.folderInfo)
        }
        else -> null
    }
}

internal fun CommonAbsoluteFolderImpl.createChildFolder(
    folderInfo: GroupFileCommon.FolderInfo
): AbsoluteFolderImpl = AbsoluteFolderImpl(
    contact = contact,
    parent = this,
    id = folderInfo.folderId,
    name = folderInfo.folderName,
    uploadTime = folderInfo.createTime.toLongUnsigned(),
    uploaderId = folderInfo.createUin,
    lastModifiedTime = folderInfo.modifyTime.toLongUnsigned(),
    contentsCount = folderInfo.totalFileCount
)

internal fun CommonAbsoluteFolderImpl.createChildFile(
    info: GroupFileCommon.FileInfo
): AbsoluteFileImpl = AbsoluteFileImpl(
    contact = contact,
    parent = this,
    id = info.fileId,
    name = info.fileName,
    uploadTime = info.uploadTime.toLongUnsigned(),
    lastModifiedTime = info.modifyTime.toLongUnsigned(),
    uploaderId = info.uploaderUin,
    expiryTime = info.deadTime.toLongUnsigned(),
    size = info.fileSize,
    sha1 = info.sha,
    md5 = info.md5,
    busId = info.busId
)

internal expect class AbsoluteFolderImpl(
    contact: FileSupported, parent: AbsoluteFolder?, id: String, name: String,
    uploadTime: Long, uploaderId: Long, lastModifiedTime: Long,
    contentsCount: Int,
) : CommonAbsoluteFolderImpl

internal abstract class CommonAbsoluteFolderImpl(
    contact: FileSupported, parent: AbsoluteFolder?, id: String, name: String,
    uploadTime: Long, uploaderId: Long, lastModifiedTime: Long,
    override var contentsCount: Int,
) : AbstractAbsoluteFileFolder(
    contact,
    parent, id, name, uploadTime, uploaderId, lastModifiedTime, 0
), AbsoluteFolder {
    override fun checkPermission(operationHint: String) {
        // 目录权限不受 '允许任何人上传' 设置的影响
        if (contact is GroupImpl && !contact.botPermission.isOperator()) throwPermissionDeniedException(operationHint)
        return
    }

    override val isFile: Boolean get() = false
    override val isFolder: Boolean get() = true

    override val absolutePath: String
        get() {
            val parent = parent
            return when {
                parent == null || this.id == "/" -> "/"
                parent.parent == null || parent.id == "/" -> "/$name"
                else -> "${parent.absolutePath}/$name"
            }
        }

    companion object {
        suspend fun getItemsFlow(
            client: QQAndroidClient,
            contact: FileSupported,
            folderId: String
        ): Flow<Oidb0x6d8.GetFileListRspBody.Item> {
            return flow {
                var index = 0
                while (true) {
                    val list =
                        client.bot.network.sendAndExpect(
                            FileManagement.GetFileList(
                                client,
                                groupCode = contact.id,
                                folderId = folderId,
                                startIndex = index
                            )
                        ).toResult("AbsoluteFolderImpl.getFilesFlow").getOrThrow()
                    index += list.itemList.size

                    if (list.int32RetCode != 0) return@flow
                    if (list.itemList.isEmpty()) return@flow

                    emitAll(list.itemList.asFlow())
                }
            }
        }

        suspend fun uploadNewFileImpl(
            folder: AbsoluteFolderImpl,
            filepath: String,
            content: ExternalResource,
            callback: ProgressionCallback<AbsoluteFile, Long>?
        ): AbsoluteFile {
            if (filepath.isBlank()) throw IllegalArgumentException("filename cannot be blank.")
            // TODO: 12/10/2021 checkPermission for AbsoluteFolderImpl.upload

            content.withAutoClose {
                val resp =
                    folder.bot.network.sendAndExpect(
                        FileManagement.RequestUpload(
                            folder.client,
                            groupCode = folder.contact.id,
                            folderId = folder.id,
                            resource = content,
                            filename = filepath
                        )
                    ).toResult("AbsoluteFolderImpl.upload").getOrThrow()

                when (resp.int32RetCode) {
                    -36 -> folder.throwPermissionDeniedException("uploadNewFile")
                }

                val file = AbsoluteFileImpl(
                    contact = folder.contact,
                    parent = folder,
                    id = resp.fileId,
                    name = filepath,
                    uploadTime = folder.bot.clock.server.currentTimeSeconds(),
                    lastModifiedTime = folder.bot.clock.server.currentTimeSeconds(),
                    expiryTime = 0,
                    uploaderId = folder.bot.id,
                    size = content.size,
                    sha1 = content.sha1,
                    md5 = content.md5,
                    busId = resp.busId
                )

                if (resp.boolFileExist) {
                    // resp.boolFileExist:
                    //      服务器是否存在相同的内容, 只是用来判断可不可以跳过上传
                    //      当为 true 时跳过上传, 但仍然需要完成 `sendMessage(FileMessage)` 才是正常逻辑
                    callback?.onBegin(file, content)
                    val result = kotlin.runCatching {
                        folder.contact.sendMessage(AllowSendFileMessage + file.toMessage())
                    }.map { content.size }
                    callback?.onFinished(file, content, result)
                    return file
                }

                val ext = GroupFileUploadExt(
                    u1 = 100,
                    u2 = 1,
                    entry = GroupFileUploadEntry(
                        business = ExcitingBusiInfo(
                            busId = resp.busId,
                            senderUin = folder.bot.id,
                            receiverUin = folder.contact.id, // TODO: 2021/3/1 code or uin?
                            groupCode = folder.contact.id,
                        ),
                        fileEntry = ExcitingFileEntry(
                            fileSize = content.size,
                            md5 = content.md5,
                            sha1 = content.sha1,
                            fileId = resp.fileId.toByteArray(),
                            uploadKey = resp.checkKey,
                        ),
                        clientInfo = ExcitingClientInfo(
                            clientType = 2,
                            appId = folder.client.protocol.id.toString(),
                            terminalType = 2,
                            clientVer = "9e9c09dc",
                            unknown = 4,
                        ),
                        fileNameInfo = ExcitingFileNameInfo(filepath),
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

                callback?.onBegin(file, content)

                kotlin.runCatching {
                    Highway.uploadResourceBdh(
                        bot = folder.bot,
                        resource = content,
                        kind = ResourceKind.GROUP_FILE,
                        commandId = 71,
                        extendInfo = ext,
                        dataFlag = 0,
                        callback = if (callback == null) null else fun(it: Long) {
                            callback.onProgression(file, content, it)
                        }
                    )
                }.let { result0 ->
                    val result = result0.onSuccessCatching {
                        folder.contact.sendMessage(AllowSendFileMessage + file.toMessage())
                    }
                    callback?.onFinished(file, content, result.map { content.size })
                }

                return file
            }
        }
    }

    suspend fun getItemsFlow(): Flow<Oidb0x6d8.GetFileListRspBody.Item> = Companion.getItemsFlow(client, contact, id)

    protected fun Oidb0x6d8.GetFileListRspBody.Item.resolve(): AbsoluteFileFolder? =
        resolved(this@CommonAbsoluteFolderImpl)

    override suspend fun folders(): Flow<AbsoluteFolder> {
        return getItemsFlow().filter { it.folderInfo != null }.map { it.resolve() as AbsoluteFolder }
    }

    override suspend fun files(): Flow<AbsoluteFile> {
        return getItemsFlow().filter { it.fileInfo != null }.map { it.resolve() as AbsoluteFile }
    }

    override suspend fun children(): Flow<AbsoluteFileFolder> {
        return getItemsFlow().mapNotNull { it.resolve() }
    }

    override suspend fun createFolder(name: String): AbsoluteFolder {
        if (name.isBlank()) throw IllegalArgumentException("folder name cannot be blank.")
        checkPermission("createFolder")
        FileSystem.checkLegitimacy(name)

        // server only support nesting depth level of 1 so we don't need to check the name

        val result = bot.network.sendAndExpect(FileManagement.CreateFolder(client, contact.id, this.id, name))
            .toResult("AbsoluteFolderImpl.mkdir", checkResp = false)
            .getOrThrow() // throw protocol errors

        /*
        2021-10-30 13:06:33 D/soutv: unnamed = CreateFolderRspBody#-941698272 {
            folderInfo=FolderInfo#1879610684 {
                    createTime=0x617D3548(1635595592)
                    createUin=xxx
                    folderId=/49a18e46-cf24-4362-b0d0-13235c0e7862
                    folderName=myFolder
                    modifyTime=0x617D3548(1635595592)
                    modifyUin=xxx
                    parentFolderId=/
                    usedSpace=0x0000000000000000(0)
            }
            retMsg=ok
        }
         */

        /*
        2021-10-30 13:03:44 D/soutv: unnamed = CreateFolderRspBody#-941698272 {
            clientWording=只允许群主和管理员操作
            int32RetCode=0xFFFFFFDC(-36)
            retMsg=not group admin
        }
         */

        /*
        2021-10-30 13:10:32 D/soutv: unnamed = CreateFolderRspBody#-941698272 {
            clientWording=同名文件夹已存在
            int32RetCode=0xFFFFFEC7(-313)
            retMsg=folder name has exist
        }
         */

        return when (result.int32RetCode) {
            -36 -> throwPermissionDeniedException("createFolder")
            -313 -> this.resolveFolder(name) // already exists
            0 -> {
                if (result.folderInfo != null) {
                    this.createChildFolder(result.folderInfo)
                } else {
                    this.resolveFolder(name)
                }
            }
            else -> {
                // unexpected errors
                error("Failed to create folder '$name': ${result.int32RetCode} ${result.clientWording}.")
            }
        } ?: error("Failed to create folder '$name': server returned success but failed to find folder.")
    }

    override suspend fun resolveFolder(name: String): AbsoluteFolder? {
        if (name.isBlank()) throw IllegalArgumentException("folder name cannot be blank.")
        if (!FileSystem.isLegal(name)) return null
        if (name[0] == '/') {
            return root.resolveFolder(name.substring(1))
        }
        return getItemsFlow().firstOrNull { it.folderInfo?.folderName == name }?.resolve() as AbsoluteFolder?
    }

    override suspend fun resolveFolderById(id: String): AbsoluteFolder? {
        if (name.isBlank()) throw IllegalArgumentException("folder id cannot be blank.")
        if (!FileSystem.isLegal(id)) return null
        if (id == AbsoluteFolder.ROOT_FOLDER_ID) return root // special case, not ambiguous — '/' always refers to root.
        if (this.id != AbsoluteFolder.ROOT_FOLDER_ID) return null // reserved for future

        // All folder ids start with '/'.
        // Currently, only root folders can have children folders,
        // and we don't know how the folderIds can be changed,
        // so we force the receiver to be root for now, to provide forward-compatibility.
        return root.impl().getItemsFlow().firstOrNull { it.folderInfo?.folderId == id }?.resolve() as AbsoluteFolder?
    }

    override suspend fun resolveFileById(id: String, deep: Boolean): AbsoluteFile? {
        if (id == "/" || id.isEmpty()) throw IllegalArgumentException("Illegal file id: $id")
        getItemsFlow().filter { it.fileInfo?.fileId == id }.map { it.resolve() as AbsoluteFile }.firstOrNull()
            ?.let { return it }

        if (!deep) return null

        return folders().map { it.resolveFileById(id, deep) }.firstOrNull { it != null }
    }

    override suspend fun resolveFiles(path: String): Flow<AbsoluteFile> {
        if (path.isBlank()) throw IllegalArgumentException("path cannot be blank.")
        if (!FileSystem.isLegal(path)) return emptyFlow()

        if (path[0] == '/') {
            return root.resolveFiles(path.substring(1))
        }

        if (!path.contains('/')) {
            return getItemsFlow().filter { it.fileInfo?.fileName == path }.map { it.resolve() as AbsoluteFile }
        }

        return resolveFolder(path.substringBefore('/'))?.resolveFiles(path.substringAfter('/')) ?: emptyFlow()
    }

    override suspend fun resolveAll(path: String): Flow<AbsoluteFileFolder> {
        if (path.isBlank()) throw IllegalArgumentException("path cannot be blank.")
        if (!FileSystem.isLegal(path)) return emptyFlow()
        if (path[0] == '/') {
            return root.resolveAll(path.substring(1))
        }
        if (!path.contains('/')) {
            return getItemsFlow().mapNotNull { it.resolve() }
        }

        return resolveFolder(path.substringBefore('/'))?.resolveAll(path.substringAfter('/')) ?: emptyFlow()
    }

    override suspend fun uploadNewFile(
        filepath: String,
        content: ExternalResource,
        callback: ProgressionCallback<AbsoluteFile, Long>?
    ): AbsoluteFile {
        val (actualFolder, actualFilename) = findFileByPath(filepath)
        return uploadNewFileImpl(actualFolder.impl(), actualFilename, content, callback)
    }

    override suspend fun exists(): Boolean {
        return parentOrFail().folders().firstOrNull { it.id == this.id } != null
    }

    override suspend fun refresh(): Boolean {
        val new = refreshed() ?: return false
        this.name = new.name
        this.lastModifiedTime = new.lastModifiedTime
        this.contentsCount = new.contentsCount
        return true
    }

    override fun toString(): String = "AbsoluteFolder(name=$name, absolutePath=$absolutePath, id=$id)"

    override suspend fun refreshed(): AbsoluteFolder? = parentOrRoot.folders().firstOrNull { it.id == this.id }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (!isSameType(this, other)) return false
        if (!super.equals(other)) return false

        if (contentsCount != other.contentsCount) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + contentsCount.hashCode()
        return result
    }
}
