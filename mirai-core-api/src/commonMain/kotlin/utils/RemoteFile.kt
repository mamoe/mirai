/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import net.mamoe.kjbb.JvmBlockingBridge
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.io.RandomAccessFile
import java.util.stream.Stream

/**
 * @since 2.5
 */
@MiraiExperimentalApi
@JvmBlockingBridge
public interface RemoteFile {
    public val name: String

    public val path: String

    public fun parent(): RemoteFile?

    public suspend fun isFile(): Boolean

    public suspend fun isDirectory(): Boolean = !isFile()

    public suspend fun length(): Long

    public suspend fun exists(): Boolean

    public suspend fun listFiles(): Flow<RemoteFile>

    @JavaFriendlyAPI
    public suspend fun listFilesStream(): Stream<RemoteFile>

    public suspend fun resolve(relativePath: String): RemoteFile

    public suspend fun resolve(relative: RemoteFile): RemoteFile = resolve(relative.path)

    public suspend fun resolveSibling(other: String): RemoteFile

    public suspend fun resolveSibling(relative: RemoteFile): RemoteFile = resolve(relative.path)

    public suspend fun delete(recursively: Boolean): Boolean

    public suspend fun moveTo(target: RemoteFile): Boolean

    @MiraiExperimentalApi
    public suspend fun copyTo(target: RemoteFile): Boolean

    public suspend fun write(resource: ExternalResource)

    public suspend fun open(): FileDownloadSession
}

/**
 * @since 2.5
 */
@MiraiExperimentalApi
@JvmBlockingBridge
public interface FileDownloadSession {
    /**
     * 当有进度更新时更新
     */
    public val onProgression: SharedFlow<Long>

    public suspend fun downloadTo(out: OutputStream)

    public suspend fun downloadTo(file: RandomAccessFile)

    public suspend fun downloadTo(file: File) {
        val raf = runBIO { RandomAccessFile(file, "w") }
        try {
            return downloadTo(raf)
        } finally {
            runBIO { raf.close() }
        }
    }

    // block all
    public fun inputStream(): InputStream
}