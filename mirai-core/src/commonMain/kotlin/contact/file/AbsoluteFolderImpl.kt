/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.contact.file

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.FileSupported
import net.mamoe.mirai.contact.file.AbsoluteFile
import net.mamoe.mirai.contact.file.AbsoluteFileFolder
import net.mamoe.mirai.contact.file.AbsoluteFolder
import net.mamoe.mirai.internal.network.components.ClockHolder.Companion.clock
import net.mamoe.mirai.internal.network.highway.Highway
import net.mamoe.mirai.internal.network.highway.ResourceKind
import net.mamoe.mirai.internal.network.protocol
import net.mamoe.mirai.internal.network.protocol.data.proto.*
import net.mamoe.mirai.internal.network.protocol.packet.chat.FileManagement
import net.mamoe.mirai.internal.network.protocol.packet.chat.toResult
import net.mamoe.mirai.internal.network.protocol.packet.sendAndExpect
import net.mamoe.mirai.internal.utils.FileSystem
import net.mamoe.mirai.internal.utils.io.serialization.toByteArray
import net.mamoe.mirai.utils.*
import java.util.stream.Stream
import kotlin.streams.asStream

internal class AbsoluteFolderImpl(
    contact: FileSupported, parent: AbsoluteFolder?, id: String, name: String,
    uploadTime: Long, uploaderId: Long, lastModifiedTime: Long,
    override var contentsCount: Int,
) : AbstractAbsoluteFileFolder(
    contact,
    parent, id, name, uploadTime, uploaderId, lastModifiedTime, 0
), AbsoluteFolder {
    override val isFile: Boolean get() = false
    override val isFolder: Boolean get() = true

    private suspend fun getItemsFlow(): Flow<Oidb0x6d8.GetFileListRspBody.Item> {
        return flow {
            var index = 0
            while (true) {
                val list = FileManagement.GetFileList(
                    client,
                    groupCode = contact.id,
                    folderId = id,
                    startIndex = index
                ).sendAndExpect(bot).toResult("AbsoluteFolderImpl.getFilesFlow").getOrThrow()
                index += list.itemList.size

                if (list.int32RetCode != 0) return@flow
                if (list.itemList.isEmpty()) return@flow

                emitAll(list.itemList.asFlow())
            }
        }
    }

    @JavaFriendlyAPI
    private suspend fun getItemsSequence(): Sequence<Oidb0x6d8.GetFileListRspBody.Item> {
        return sequence {
            var index = 0
            while (true) {
                val list = runBlocking {
                    FileManagement.GetFileList(
                        client,
                        groupCode = contact.id,
                        folderId = id,
                        startIndex = index
                    ).sendAndExpect(bot)
                }.toResult("AbsoluteFolderImpl.getFilesFlow").getOrThrow()
                index += list.itemList.size

                if (list.int32RetCode != 0) return@sequence
                if (list.itemList.isEmpty()) return@sequence

                yieldAll(list.itemList)
            }
        }
    }

    private fun Oidb0x6d8.GetFileListRspBody.Item.resolve(): AbsoluteFileFolder? {
        val item = this
        return when {
            item.fileInfo != null -> {
                val i = item.fileInfo
                AbsoluteFileImpl(
                    contact = contact,
                    parent = this@AbsoluteFolderImpl,
                    id = i.fileId,
                    name = i.fileName,
                    uploadTime = i.uploadTime.toLongUnsigned(),
                    lastModifiedTime = i.modifyTime.toLongUnsigned(),
                    uploaderId = i.uploaderUin,
                    expiryTime = i.deadTime.toLongUnsigned(),
                    size = i.fileSize,
                    sha1 = i.sha,
                    md5 = i.md5,
                    busId = i.busId
                )
            }
            item.folderInfo != null -> {
                val i = item.folderInfo
                AbsoluteFolderImpl(
                    contact = contact,
                    parent = this@AbsoluteFolderImpl,
                    id = i.folderId,
                    name = i.folderName,
                    uploadTime = i.createTime.toLongUnsigned(),
                    uploaderId = i.createUin,
                    lastModifiedTime = i.modifyTime.toLongUnsigned(),
                    contentsCount = i.totalFileCount
                )
            }
            else -> null
        }
    }

    override suspend fun folders(): Flow<AbsoluteFolder> {
        return getItemsFlow().filter { it.folderInfo != null }.map { it.resolve() as AbsoluteFolder }
    }

    @JavaFriendlyAPI
    override suspend fun foldersStream(): Stream<AbsoluteFolder> {
        return getItemsSequence().filter { it.folderInfo != null }.map { it.resolve() as AbsoluteFolder }.asStream()
    }

    override suspend fun files(): Flow<AbsoluteFile> {
        return getItemsFlow().filter { it.fileInfo != null }.map { it.resolve() as AbsoluteFile }
    }

    @JavaFriendlyAPI
    override suspend fun filesStream(): Stream<AbsoluteFile> {
        return getItemsSequence().filter { it.fileInfo != null }.map { it.resolve() as AbsoluteFile }.asStream()
    }

    override suspend fun children(): Flow<AbsoluteFileFolder> {
        return getItemsFlow().map { it.resolve() as AbsoluteFile }
    }

    @JavaFriendlyAPI
    override suspend fun childrenStream(): Stream<AbsoluteFileFolder> {
        return getItemsSequence().map { it.resolve() as AbsoluteFile }.asStream()
    }

    override suspend fun createFolder(name: String): AbsoluteFolder {
        checkPermission()
        FileSystem.checkLegitimacy(name)

        FileManagement.CreateFolder(client, contact.id, this.id, name)
            .sendAndExpect(bot).toResult("RemoteFile.mkdir", checkResp = false).getOrThrow()

        return this.resolveFolder(name) ?: error("Failed to ")
    }

    override suspend fun resolveFolder(name: String): AbsoluteFolder? {
        if (!FileSystem.isLegal(name)) return null
        if (name.isEmpty()) return this
        return getItemsFlow().firstOrNull { it.folderInfo?.folderName == name }?.resolve() as AbsoluteFolder?
    }

    override suspend fun resolveFiles(path: String): Flow<AbsoluteFile> {
        if (!FileSystem.isLegal(path)) return emptyFlow()
        if (path.isEmpty()) return emptyFlow()

        if (!path.contains('/')) {
            return getItemsFlow().filter { it.fileInfo?.fileName == path }.map { it.resolve() as AbsoluteFile }
        }

        return resolveFolder(path.substringBefore('/'))?.resolveFiles(path.substringAfter('/')) ?: emptyFlow()
    }

    @OptIn(JavaFriendlyAPI::class)
    override suspend fun resolveFilesStream(path: String): Stream<AbsoluteFile> {
        if (!FileSystem.isLegal(path)) return Stream.empty()
        if (path.isEmpty()) return Stream.empty()

        if (!path.contains('/')) {
            return getItemsSequence().filter { it.fileInfo?.fileName == path }.map { it.resolve() as AbsoluteFile }
                .asStream()
        }

        return resolveFolder(path.substringBefore('/'))?.resolveFilesStream(path.substringAfter('/')) ?: Stream.empty()
    }

    override suspend fun resolveAll(path: String): Flow<AbsoluteFileFolder> {
        if (!FileSystem.isLegal(path)) return emptyFlow()
        if (path.isEmpty()) return emptyFlow()
        if (!path.contains('/')) {
            return getItemsFlow().mapNotNull { it.resolve() }
        }

        return resolveFolder(path.substringBefore('/'))?.resolveAll(path.substringAfter('/')) ?: emptyFlow()
    }

    @JavaFriendlyAPI
    override suspend fun resolveAllStream(path: String): Stream<AbsoluteFileFolder> {
        if (!FileSystem.isLegal(path)) return Stream.empty()
        if (path.isEmpty()) return Stream.empty()
        if (!path.contains('/')) {
            return getItemsSequence().mapNotNull { it.resolve() }.asStream()
        }

        return resolveFolder(path.substringBefore('/'))?.resolveAllStream(path.substringAfter('/')) ?: Stream.empty()
    }

    override suspend fun uploadNewFile(
        filename: String,
        content: ExternalResource,
        callback: ProgressionCallback<AbsoluteFile, Long>?
    ): AbsoluteFile = content.withAutoClose {
        val resp = FileManagement.RequestUpload(
            client,
            groupCode = contact.id,
            folderId = this.id,
            resource = content,
            filename = filename
        ).sendAndExpect(bot).toResult("RemoteFiles.upload").getOrThrow()

        val file = AbsoluteFileImpl(
            contact = contact,
            parent = this,
            id = resp.fileId,
            name = filename,
            uploadTime = bot.clock.server.currentTimeSeconds(),
            lastModifiedTime = bot.clock.server.currentTimeSeconds(),
            expiryTime = 0,
            uploaderId = bot.id,
            size = content.size,
            sha1 = content.sha1,
            md5 = content.md5,
            busId = resp.busId
        )

        if (resp.boolFileExist) {
            return file
        }

        val ext = GroupFileUploadExt(
            u1 = 100,
            u2 = 1,
            entry = GroupFileUploadEntry(
                business = ExcitingBusiInfo(
                    busId = resp.busId,
                    senderUin = bot.id,
                    receiverUin = contact.id, // TODO: 2021/3/1 code or uin?
                    groupCode = contact.id,
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
                    appId = client.protocol.id.toString(),
                    terminalType = 2,
                    clientVer = "9e9c09dc",
                    unknown = 4,
                ),
                fileNameInfo = ExcitingFileNameInfo(filename),
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
                bot = bot,
                resource = content,
                kind = ResourceKind.GROUP_FILE,
                commandId = 71,
                extendInfo = ext,
                dataFlag = 0,
                callback = if (callback == null) null else fun(it: Long) {
                    callback.onProgression(file, content, it)
                }
            )
        }.let {
            callback?.onFinished(file, content, it.map { content.size })
        }

        return file
    }

    override suspend fun exists(): Boolean {
        // TODO: 2021/10/1 try optimize exists
        return parentOrFail().files().firstOrNull { it.id == this.id } != null
    }

    override suspend fun refresh(): Boolean {
        val new = refreshed() ?: return false
        this.name = new.name
        this.lastModifiedTime = new.lastModifiedTime
        this.contentsCount = new.contentsCount
        return true
    }

    override suspend fun refreshed(): AbsoluteFolder? = parentOrRoot.folders().firstOrNull { it.id == this.id }.cast()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as AbsoluteFolderImpl

        if (contentsCount != other.contentsCount) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + contentsCount.hashCode()
        return result
    }
}