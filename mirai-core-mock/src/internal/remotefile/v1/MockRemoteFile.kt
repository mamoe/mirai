/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("DEPRECATION")

package net.mamoe.mirai.mock.internal.remotefile.v1

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.FileSupported
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.isOperator
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.FileMessage
import net.mamoe.mirai.mock.txfs.TxFileSystem
import net.mamoe.mirai.mock.txfs.TxRemoteFile
import net.mamoe.mirai.mock.utils.mock
import net.mamoe.mirai.utils.*
import kotlin.io.path.inputStream

internal class MockRemoteFileRoot(
    override val contact: FileSupported,
    val fileSystem: TxFileSystem,
) : RemoteFile {
    constructor(contact: FileSupported) : this(
        contact,
        contact.bot.mock().tmpFsServer.fsDisk.newFsSystem()
    )

    override val name: String get() = ""
    override val id: String? get() = null
    override val path: String get() = RemoteFile.ROOT_PATH
    override val parent: RemoteFile? get() = null

    override fun toString(): String = path

    override suspend fun isFile(): Boolean = false
    override suspend fun length(): Long = 0
    override suspend fun toMessage(): FileMessage? = null
    override suspend fun getDownloadInfo(): RemoteFile.DownloadInfo? = null
    override suspend fun exists(): Boolean = true
    override suspend fun isDirectory(): Boolean = true
    override suspend fun delete(): Boolean = false
    override suspend fun renameTo(name: String): Boolean = false
    override suspend fun moveTo(target: RemoteFile): Boolean = false
    override suspend fun mkdir(): Boolean = false

    override suspend fun getInfo(): RemoteFile.FileInfo {
        return RemoteFile.FileInfo(
            name = "",
            id = "/",
            path = "/",
            length = 0,
            downloadTimes = 0,
            uploaderId = 0,
            uploadTime = 0,
            lastModifyTime = fileSystem.root.fileInfo.lastUpdateTime,
            sha1 = byteArrayOf(),
            md5 = byteArrayOf(),
        )
    }


    override fun resolve(relative: String): RemoteFile {
        return MockRemoteFileSub(
            this,
            relative.substringAfter('/'),
            null,
            relative.removePrefix("/"),
            this
        )
    }

    override fun resolve(relative: RemoteFile): RemoteFile {
        return resolve(relative.path)
    }

    override suspend fun resolveById(id: String, deep: Boolean): RemoteFile? {
        val native = fileSystem.resolveById(id) ?: return null
        if (!deep && native !== fileSystem.root) return null
        return toSubRF(native)
    }

    override fun resolveSibling(relative: String): RemoteFile = error("Root path does not have sibling paths.")
    override fun resolveSibling(relative: RemoteFile): RemoteFile = error("Root path does not have sibling paths.")

    fun toSubRF(native: TxRemoteFile): MockRemoteFileSub {
        val pt = native.parent
        return MockRemoteFileSub(
            root = this,
            name = native.name,
            id = id,
            path = native.path,
            parent = when {
                pt === fileSystem.root -> this
                else -> toSubRF(pt)
            }
        )
    }

    private fun listFilesSeq(): Sequence<RemoteFile> {
        return fileSystem.root.listFiles()!!
            .map { toSubRF(it) }
    }

    override suspend fun listFiles(): Flow<RemoteFile> {
        return listFilesSeq().asFlow()
    }

    @JavaFriendlyAPI
    override suspend fun listFilesIterator(lazy: Boolean): Iterator<RemoteFile> {
        return listFilesSeq().iterator()
    }


    override suspend fun upload(resource: ExternalResource, callback: RemoteFile.ProgressionCallback?): FileMessage {
        error("Cannot upload a file as a directory")
    }

    @MiraiExperimentalApi
    override suspend fun uploadAndSend(resource: ExternalResource): MessageReceipt<Contact> {
        error("Cannot upload a file as a directory")
    }

}

internal class MockRemoteFileSub(
    val root: MockRemoteFileRoot,
    override val name: String,
    override val id: String?,
    override val path: String,
    override val parent: RemoteFile,
) : RemoteFile {
    override val contact: FileSupported get() = root.contact
    private fun resolveNative(): TxRemoteFile? {

        id?.let { root.fileSystem.resolveById(it) }?.let { return it }

        return root.fileSystem.findByPath(path).firstOrNull()
    }

    override suspend fun isFile(): Boolean {
        return resolveNative()?.isFile ?: false
    }

    override suspend fun isDirectory(): Boolean {
        return resolveNative()?.isDirectory ?: false
    }

    override suspend fun length(): Long {
        return resolveNative()?.size ?: 0
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun getInfo(): RemoteFile.FileInfo? {
        val native = resolveNative() ?: return null
        return runBIO {
            RemoteFile.FileInfo(
                name = native.name,
                id = native.id,
                path = native.path,
                length = native.size,
                downloadTimes = 0,
                uploadTime = native.fileInfo.createTime,
                uploaderId = native.fileInfo.creator,
                lastModifyTime = native.fileInfo.lastUpdateTime,
                sha1 = native.resolveNativePath().inputStream().sha1(),
                md5 = native.resolveNativePath().inputStream().md5()
            )
        }
    }

    override suspend fun exists(): Boolean {
        return resolveNative() != null
    }

    override fun toString(): String = path

    override fun resolve(relative: String): RemoteFile {
        if (relative.startsWith('/')) {
            return root.resolve(relative)
        }
        return root.resolve("$path/$relative")
    }

    override fun resolve(relative: RemoteFile): RemoteFile {
        return resolve(relative.path)
    }

    override suspend fun resolveById(id: String, deep: Boolean): RemoteFile? {
        val result = root.fileSystem.resolveById(id) ?: return null
        return root.toSubRF(result)
    }

    override fun resolveSibling(relative: String): RemoteFile {
        return parent.resolve(relative)
    }

    override fun resolveSibling(relative: RemoteFile): RemoteFile {
        return parent.resolve(relative)
    }

    private fun TxRemoteFile.hasPerm(): Boolean {
        if (fileInfo.creator == contact.bot.id) return true
        when (val contact = contact) {
            is Group -> {
                if (contact.botPermission.isOperator()) return true
            }
        }
        return false
    }

    override suspend fun delete(): Boolean {
        // TODO: Perm check
        val native = resolveNative() ?: return false
        if (!native.hasPerm()) return false
        return native.delete()
    }

    override suspend fun renameTo(name: String): Boolean {
        val native = resolveNative() ?: return false
        if (!native.hasPerm()) return false
        return native.rename(name)
    }

    override suspend fun moveTo(target: RemoteFile): Boolean {
        if (target.contact !== contact) {
            error("Crossing file moving not support")
        }
        if (target === root) return false
        if (target === this) return true
        val f = resolveNative() ?: return false
        if (!f.hasPerm()) return false
        val t = when (val p = target.parent) {
            is MockRemoteFileRoot -> root.fileSystem.root
            else -> p.cast<MockRemoteFileSub>().resolveNative()
        } ?: return false
        f.moveTo(t)
        f.rename(target.name)
        return true
    }

    override suspend fun mkdir(): Boolean {
        if (parent !== root) return false
        when (val contact = contact) {
            is Group -> {
                if (!contact.botPermission.isOperator()) return false
            }
        }
        root.fileSystem.root.mksubdir(name, contact.bot.id)
        return true
    }

    private fun listFilesSeq(): Sequence<RemoteFile> {
        return resolveNative()?.listFiles().orEmpty()
            .map { root.toSubRF(it) }
    }

    override suspend fun listFiles(): Flow<RemoteFile> {
        return listFilesSeq().asFlow()
    }

    @JavaFriendlyAPI
    override suspend fun listFilesIterator(lazy: Boolean): Iterator<RemoteFile> {
        return listFilesSeq().iterator()
    }

    override suspend fun toMessage(): FileMessage? {
        val nt = resolveNative() ?: return null
        if (!nt.isFile) return null
        return FileMessage(
            id = nt.id,
            internalId = 0,
            name = nt.name,
            size = nt.size,
        )
    }

    @Suppress("OverridingDeprecatedMember")
    override suspend fun upload(resource: ExternalResource, callback: RemoteFile.ProgressionCallback?): FileMessage {

        kotlin.run perm@{
            when (val contact = contact) {
                is Group -> {
                    if (contact.botPermission.isOperator()) return@perm
                    if (contact.mock().controlPane.isAllowMemberFileUploading) return@perm

                    error("Group $contact not allow uploading files.")
                }
            }
        }

        val dir = when {
            parent === root -> root.fileSystem.root
            else -> parent.cast<MockRemoteFileSub>().resolveNative() ?: error("Parent $parent not found")
        }
        val file = dir.uploadFile(name, resource, contact.bot.id)
        return FileMessage(
            id = file.id,
            internalId = 0,
            name = file.name,
            size = file.size,
        )
    }

    @Suppress("DEPRECATION_ERROR")
    @MiraiExperimentalApi
    override suspend fun uploadAndSend(resource: ExternalResource): MessageReceipt<Contact> {
        @Suppress("DEPRECATION_ERROR")
        return contact.sendMessage(upload(resource, null))
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun getDownloadInfo(): RemoteFile.DownloadInfo? {
        val native = resolveNative() ?: return null
        return runBIO {
            RemoteFile.DownloadInfo(
                filename = native.name,
                id = native.id,
                path = native.path,
                url = contact.bot.mock().tmpFsServer.resolveHttpUrl(native.resolveNativePath()),
                sha1 = native.resolveNativePath().inputStream().sha1(),
                md5 = native.resolveNativePath().inputStream().md5()
            )
        }
    }
}
