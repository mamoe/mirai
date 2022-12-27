/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("DEPRECATION", "DEPRECATION_ERROR", "INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package net.mamoe.mirai.mock.internal.remotefile.remotefile

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.FileSupported
import net.mamoe.mirai.contact.PermissionDeniedException
import net.mamoe.mirai.contact.isOperator
import net.mamoe.mirai.internal.message.data.FileMessageImpl
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.FileMessage
import net.mamoe.mirai.mock.contact.MockGroup
import net.mamoe.mirai.mock.resserver.MockServerFileSystem
import net.mamoe.mirai.mock.resserver.MockServerRemoteFile
import net.mamoe.mirai.mock.utils.mock
import net.mamoe.mirai.utils.*
import kotlin.io.path.inputStream

internal class RootRemoteFile(
    val fileSystem: MockServerFileSystem,
    override val contact: FileSupported,
) : RemoteFile {
    override val name: String get() = ""
    override val id: String get() = "/"
    override val path: String get() = "/"
    override val parent: RemoteFile get() = this

    override suspend fun isFile(): Boolean = false
    override suspend fun length(): Long = 0
    override suspend fun getInfo(): RemoteFile.FileInfo = fileSystem.root.fileInfo.let { inf ->
        RemoteFile.FileInfo(
            name = "/",
            path = "/",
            id = "/",
            length = 0,
            downloadTimes = 0,
            uploaderId = inf.creator,
            uploadTime = inf.createTime,
            lastModifyTime = inf.lastUpdateTime,
            sha1 = byteArrayOf(),
            md5 = byteArrayOf(),
        )
    }

    override suspend fun exists(): Boolean = true
    override fun toString(): String = "MockRemoteFile[ROOT, contact=$contact]"

    override fun resolve(relative: String): RemoteFile {
        if (relative.isEmpty()) return this

        val fixedPath = when {
            relative[0] == '/' -> relative
            else -> "/$relative"
        }.let { ist ->
            var end = ist.length
            while (end > 1 && ist[end - 1] == '/') {
                end--
            }
            ist.substring(0, end)
        }

        if (fixedPath == "/" || fixedPath == ".") return this

        val fixedName = fixedPath.substringAfterLast('/')

        return MockRemoteFile(
            root = this,
            parent = resolve(fixedPath.substring(0, fixedPath.lastIndexOf('/'))),
            path = fixedPath,
            fileId = null,
            name = fixedName,
        )
    }

    override fun resolve(relative: RemoteFile): RemoteFile = resolve(relative.path)

    override suspend fun resolveById(id: String, deep: Boolean): RemoteFile? {
        if (id == "/") return this
        val resolved = fileSystem.resolveById(id) ?: return null
        return convert(resolved)
    }

    internal fun convert(src: MockServerRemoteFile): RemoteFile {
        if (src == fileSystem.root) return this
        return MockRemoteFile(
            name = src.name,
            root = this,
            path = src.path,
            fileId = src.id,
            parent = convert(src.parent)
        )
    }

    override fun resolveSibling(relative: String): RemoteFile = resolve(relative)
    override fun resolveSibling(relative: RemoteFile): RemoteFile = resolveSibling(relative.path)

    override suspend fun delete(): Boolean = false
    override suspend fun renameTo(name: String): Boolean = false
    override suspend fun moveTo(target: RemoteFile): Boolean = false
    override suspend fun mkdir(): Boolean = true

    private fun listFilesSeq(): Sequence<RemoteFile> {
        return fileSystem.root.listFiles()!!.map { convert(it) }
    }

    override suspend fun listFiles(): Flow<RemoteFile> = listFilesSeq().asFlow()

    @JavaFriendlyAPI
    override suspend fun listFilesIterator(lazy: Boolean): Iterator<RemoteFile> {
        return listFilesSeq().iterator()
    }

    override suspend fun toMessage(): FileMessage? = null

    @Deprecated(
        "Use uploadAndSend instead.",
        replaceWith = ReplaceWith("this.uploadAndSend(resource, callback)"),
        level = DeprecationLevel.ERROR
    )
    override suspend fun upload(resource: ExternalResource, callback: RemoteFile.ProgressionCallback?): FileMessage {
        error("Uploading as root directory")
    }

    @MiraiExperimentalApi
    override suspend fun uploadAndSend(resource: ExternalResource): MessageReceipt<Contact> {
        error("Uploading as root directory")
    }

    override suspend fun getDownloadInfo(): RemoteFile.DownloadInfo? = null

    fun resolveTx(f: RemoteFile?): MockServerRemoteFile? {
        if (f === this) return fileSystem.root
        return f.cast<MockRemoteFile>().resolveFile()
    }
}

@Suppress("DuplicatedCode")
internal class MockRemoteFile(
    val root: RootRemoteFile,
    override val parent: RemoteFile,
    override val path: String,
    val fileId: String?,
    override val name: String,
) : RemoteFile {
    override val id: String? get() = fileId
    override val contact: FileSupported get() = root.contact
    private val fileSystem get() = root.fileSystem
    internal fun resolveFile(): MockServerRemoteFile? {
        fileId?.let { fid ->
            fileSystem.resolveById(fid)?.let { return it }
        }
        return fileSystem.findByPath(path).firstOrNull()
    }

    private fun convert(src: MockServerRemoteFile): RemoteFile = root.convert(src)

    override suspend fun isFile(): Boolean {
        return resolveFile()?.isFile ?: false
    }

    override suspend fun length(): Long {
        val file = resolveFile() ?: return 0
        return file.size
    }

    override suspend fun getInfo(): RemoteFile.FileInfo? {
        val resolved = resolveFile() ?: return null
        val fileInf = resolved.fileInfo
        return RemoteFile.FileInfo(
            name = resolved.name,
            id = resolved.id,
            path = resolved.path,
            length = resolved.size,
            downloadTimes = if (resolved.isFile) 1 else 0,
            uploaderId = fileInf.creator,
            uploadTime = fileInf.createTime,
            lastModifyTime = fileInf.lastUpdateTime,
            sha1 = if (resolved.isDirectory) {
                byteArrayOf()
            } else {
                resolved.resolveNativePath().inputStream().use { it.sha1() }
            },
            md5 = if (resolved.isDirectory) {
                byteArrayOf()
            } else {
                resolved.resolveNativePath().inputStream().use { it.md5() }
            },
        )
    }

    override suspend fun exists(): Boolean = resolveFile() != null

    override fun toString(): String {
        val resolved = resolveFile()
        return "MockFile[c=$contact, resolved=$resolved]"
    }

    override fun resolve(relative: String): RemoteFile {
        if (relative == "/" || relative == "" || relative[0] == '/') {
            return root.resolve(relative)
        }
        return root.resolve("$path/$relative")
    }

    override fun resolve(relative: RemoteFile): RemoteFile = resolve(relative.path)

    override suspend fun resolveById(id: String, deep: Boolean): RemoteFile? {
        val resolved = fileSystem.resolveById(id) ?: return null
        if (deep) return convert(resolved)
        val thiz = resolveFile()
        if (resolved.parent == thiz) return convert(resolved)
        return null
    }

    override fun resolveSibling(relative: String): RemoteFile {
        return parent.resolve(relative)
    }

    override fun resolveSibling(relative: RemoteFile): RemoteFile {
        return parent.resolve(relative)
    }

    override suspend fun delete(): Boolean {
        val resolved = resolveFile() ?: return false
        if (!canModify(resolved, contact)) return false
        return resolved.delete()
    }

    override suspend fun renameTo(name: String): Boolean {
        val resolved = resolveFile() ?: return false

        if (!canModify(resolved, contact)) return false

        return resolved.rename(name)
    }

    override suspend fun moveTo(target: RemoteFile): Boolean {
        val resolved = resolveFile() ?: return false

        if (!canModify(resolved, contact)) return false

        val targetF = root.resolveTx(target.parent) ?: return false
        resolved.moveTo(targetF)
        resolved.rename(target.name)
        return true
    }

    override suspend fun mkdir(): Boolean {
        contact.safeCast<MockGroup>()?.let check@{ group ->
            if (group.botPermission.isOperator()) return@check
            return false
        }
        if (resolveFile() != null) return false
        val dirx = root.resolveTx(parent) ?: return false
        return kotlin.runCatching {
            dirx.mksubdir(name, contact.bot.id)
        }.isSuccess
    }

    private fun listFilesSeq(): Sequence<RemoteFile> {
        val resolved = resolveFile()?.listFiles() ?: return emptySequence()
        return resolved.map { convert(it) }
    }

    override suspend fun listFiles(): Flow<RemoteFile> {
        return listFilesSeq().asFlow()
    }

    @JavaFriendlyAPI
    override suspend fun listFilesIterator(lazy: Boolean): Iterator<RemoteFile> {
        return listFilesSeq().iterator()
    }

    override suspend fun toMessage(): FileMessage? {
        val resolved = resolveFile() ?: return null
        return FileMessageImpl(
            name = resolved.name,
            id = resolved.id,
            size = resolved.size,
            busId = 1544241
        )
    }

    @Suppress("DEPRECATION", "DEPRECATION_ERROR", "OVERRIDE_DEPRECATION", "OverridingDeprecatedMember")
    override suspend fun upload(resource: ExternalResource, callback: RemoteFile.ProgressionCallback?): FileMessage {
        callback?.onBegin(this, resource)
        try {
            contact.safeCast<MockGroup>()?.let check@{ group ->
                if (group.botPermission.isOperator()) return@check
                if (group.controlPane.isAllowMemberFileUploading) return@check
                throw PermissionDeniedException("Group $group disabled member file uploading...")
            }


            val parent = root.resolveTx(this.parent) ?: throw IllegalStateException("Parent ${this.parent} not found.")
            val rsSize = resource.size
            val rsp = parent.uploadFile(this.name, resource, contact.bot.id)
            callback?.onProgression(this, resource, rsSize)
            callback?.onSuccess(this, resource)
            return FileMessageImpl(
                name = rsp.name,
                id = rsp.id,
                size = rsp.size,
                busId = 1544241
            )
        } catch (errx: Throwable) {
            callback?.onFailure(this, resource, errx)
            throw errx
        }
    }

    @MiraiExperimentalApi
    @Suppress("DEPRECATION_ERROR", "OverridingDeprecatedMember")
    override suspend fun uploadAndSend(resource: ExternalResource): MessageReceipt<Contact> {
        return contact.sendMessage(upload(resource))
    }

    override suspend fun getDownloadInfo(): RemoteFile.DownloadInfo? {
        val resolved = resolveFile() ?: return null
        if (!resolved.isFile) return null
        val ntp = resolved.resolveNativePath()
        return RemoteFile.DownloadInfo(
            filename = resolved.name,
            id = resolved.id,
            path = resolved.path,
            sha1 = ntp.inputStream().use { it.sha1() },
            md5 = ntp.inputStream().use { it.md5() },
            url = contact.bot.mock().tmpResourceServer.resolveHttpUrlByPath(ntp).toString()
        )
    }

    companion object {
        internal fun canModify(resolved: MockServerRemoteFile, contact: FileSupported): Boolean {
            contact.safeCast<MockGroup>()?.let check@{ group ->
                if (group.botPermission.isOperator()) return true
                if (resolved.isDirectory) return false

                val finf = resolved.fileInfo
                if (finf.creator == group.bot.id) return true

                return false
            }

            return true
        }
    }
}
