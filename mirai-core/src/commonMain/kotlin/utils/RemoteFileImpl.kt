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
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.FileDownloadSession
import net.mamoe.mirai.utils.MiraiExperimentalApi
import net.mamoe.mirai.utils.RemoteFile
import java.io.InputStream
import java.io.OutputStream
import java.io.RandomAccessFile
import java.util.stream.Stream
import kotlin.coroutines.CoroutineContext

internal class RemoteFileImpl(folder: String, override val name: String, private val contact: Group) : RemoteFile {
    val folder: String = folder.replace('\\', '/')

    override val path: String
        get() = if (folder.endsWith('/')) "$folder$name" else "$folder/$name"

    override suspend fun parent(): RemoteFile {
        return RemoteFileImpl(
            folder = folder.substringBeforeLast('/', ""),
            name = folder.substringAfterLast('/'),
            contact = contact
        )
    }

    override suspend fun isFile(): Boolean {
        TODO("Not yet implemented")
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