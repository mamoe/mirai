/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.utils

import kotlinx.coroutines.CompletableDeferred
import net.mamoe.mirai.utils.COUNT_BYTES_USED_FOR_DETECTING_FILE_TYPE
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.getFileType
import net.mamoe.mirai.utils.md5
import java.io.InputStream
import java.io.RandomAccessFile


private fun InputStream.detectFileTypeAndClose(): String? {
    val buffer = ByteArray(COUNT_BYTES_USED_FOR_DETECTING_FILE_TYPE)
    return use {
        kotlin.runCatching { it.read(buffer) }.onFailure { return null }
        getFileType(buffer)
    }
}

internal class ExternalResourceImplByFileWithMd5(
    private val file: RandomAccessFile,
    override val md5: ByteArray,
    formatName: String?
) : ExternalResource {
    override val size: Long = file.length()
    override val formatName: String by lazy {
        formatName ?: inputStream().detectFileTypeAndClose().orEmpty()
    }

    override fun inputStream(): InputStream {
        check(file.filePointer == 0L) { "RandomAccessFile.inputStream cannot be opened simultaneously." }
        return file.inputStream()
    }

    override val closed: CompletableDeferred<Unit> = CompletableDeferred()

    override fun close() {
        try {
            file.close()
        } finally {
            kotlin.runCatching { closed.complete(Unit) }
        }
    }
}

internal class ExternalResourceImplByFile(
    private val file: RandomAccessFile,
    formatName: String?,
    private val closeOriginalFileOnClose: Boolean = true
) : ExternalResource {
    override val size: Long = file.length()
    override val md5: ByteArray by lazy { inputStream().md5() }
    override val formatName: String by lazy {
        formatName ?: inputStream().detectFileTypeAndClose().orEmpty()
    }

    override fun inputStream(): InputStream {
        check(file.filePointer == 0L) { "RandomAccessFile.inputStream cannot be opened simultaneously." }
        return file.inputStream()
    }

    override val closed: CompletableDeferred<Unit> = CompletableDeferred()
    override fun close() {
        try {
            if (closeOriginalFileOnClose) file.close()
        } finally {
            kotlin.runCatching { closed.complete(Unit) }
        }
    }
}

internal class ExternalResourceImplByByteArray(
    private val data: ByteArray,
    formatName: String?
) : ExternalResource {
    override val size: Long = data.size.toLong()
    override val md5: ByteArray by lazy { data.md5() }
    override val formatName: String by lazy {
        formatName ?: getFileType(data.copyOf(COUNT_BYTES_USED_FOR_DETECTING_FILE_TYPE)).orEmpty()
    }
    override val closed: CompletableDeferred<Unit> = CompletableDeferred()

    override fun inputStream(): InputStream = data.inputStream()
    override fun close() {
        kotlin.runCatching { closed.complete(Unit) }
    }
}

private fun RandomAccessFile.inputStream(): InputStream {
    val file = this
    return object : InputStream() {
        override fun read(): Int = file.read()
        override fun read(b: ByteArray, off: Int, len: Int): Int = file.read(b, off, len)
        override fun close() {
            file.seek(0)
        }
        // don't close file on stream.close. stream may be obtained at multiple times.
    }.buffered()
}


/*
 * ImgType:
 *  JPG:    1000
 *  PNG:    1001
 *  WEBP:   1002
 *  BMP:    1005
 *  GIG:    2000 // gig? gif?
 *  APNG:   2001
 *  SHARPP: 1004
 */
