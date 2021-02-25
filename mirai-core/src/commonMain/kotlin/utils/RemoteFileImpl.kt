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
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.utils.*
import java.io.InputStream
import java.io.OutputStream
import java.io.RandomAccessFile
import java.util.stream.Stream
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
        return path.trimStart().replace('\\', '/').removeSuffix("/") // tolerant leading white spaces
    }

    // TODO: 2021/2/25 add tests for FS
    // net.mamoe.mirai.internal.utils.internal.utils.FileSystemTest

    fun normalize(parent: String, name: String): String {
        var nParent = normalize(parent)
        if (!nParent.startsWith('/')) nParent = "/$nParent"

        var nName = normalize(name)
        nName = nName.removeSurrounding("/")

        val slash = nName.indexOf('/')
        if (slash != -1) {
            nParent += '/' + nName.substring(0, slash)
            nName = nName.substring(slash + 1)
        }

        return "$nParent/$nName"
    }
}

internal class RemoteFileImpl(
    contact: Group,
    override val path: String,
) : RemoteFile {
    private val contactRef by contact.weakRef()
    private val contact get() = contactRef ?: error("RemoteFile is closed due to Contact closed.")

    constructor(contact: Group, parent: String, name: String) : this(contact, fs.normalize(parent, name))

    override val name: String
        get() = path.substringAfterLast('/')

    override fun parent(): RemoteFile? {
        val s = path.substringBeforeLast('/', "")
        if (s.isEmpty()) return null
        return RemoteFileImpl(contact, s)
    }

    override suspend fun isFile(): Boolean {
        val parent = parent() ?: return false // path must == '/'

        // TODO: 2021/2/25
        return false
    }

    override suspend fun length(): Long {
        TODO("Not yet implemented")
    }

    override suspend fun exists(): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun listFiles(): Flow<RemoteFile> {
        TODO("Not yet implemented")
    }

    @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
    @OptIn(net.mamoe.mirai.utils.JavaFriendlyAPI::class)
    override suspend fun listFilesStream(): Stream<RemoteFile> {
        TODO("Not yet implemented")
    }

    override suspend fun resolve(relativePath: String): RemoteFile {
        TODO("Not yet implemented")
    }

    override suspend fun resolveSibling(other: String): RemoteFile {
        TODO("Not yet implemented")
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