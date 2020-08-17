/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("EXPERIMENTAL_API_USAGE", "unused")

package net.mamoe.mirai.utils
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import kotlinx.io.core.Input
import kotlinx.io.core.IoBuffer
import kotlinx.io.core.copyTo
import kotlinx.io.errors.IOException
import kotlinx.io.streams.asInput
import kotlinx.io.streams.asOutput
import net.mamoe.mirai.utils.internal.md5
import java.io.File
import java.io.InputStream
import java.net.URL

/*
 * 将各类型图片容器转为 [ExternalImage]
 */

/**
 * 读取 [Bitmap] 的属性, 然后构造 [ExternalImage]
 */
@Suppress("UNUSED_PARAMETER")
@Throws(IOException::class)
fun Bitmap.toExternalImage(formatName: String = "gif"): ExternalImage {
    TODO()
}

// suspend inline fun BufferedImage.suspendToExternalImage(): ExternalImage = withContext(IO) { toExternalImage() }

/**
 * 读取文件头识别图片属性, 然后构造 [ExternalImage]
 */
@Throws(IOException::class)
fun File.toExternalImage(): ExternalImage {
    val input = BitmapFactory.decodeFile(this.absolutePath)
    checkNotNull(input) { "Unable to read file(path=${this.path}), BitmapFactory.decodeFile returned null" }

    return ExternalImage(
        width = input.width,
        height = input.height,
        md5 = this.inputStream().use { it.md5() },
        imageFormat = this.nameWithoutExtension,
        input = this.inputStream().asInput(IoBuffer.Pool),
        inputSize = this.length(),
        filename = this.name
    )
}

/**
 * 在 [IO] 中进行 [File.toExternalImage]
 */
suspend inline fun File.suspendToExternalImage(): ExternalImage = withContext(IO) { toExternalImage() }

/**
 * 下载文件到临时目录然后调用 [File.toExternalImage]
 */
@Throws(IOException::class)
fun URL.toExternalImage(): ExternalImage {
    val file = createTempFile().apply { deleteOnExit() }
    file.outputStream().asOutput().use { output ->
        openStream().asInput().use { input ->
            input.copyTo(output)
        }
        output.flush()
    }
    return file.toExternalImage()
}

/**
 * 在 [IO] 中进行 [URL.toExternalImage]
 */
suspend inline fun URL.suspendToExternalImage(): ExternalImage = withContext(IO) { toExternalImage() }

/**
 * 保存为临时文件然后调用 [File.toExternalImage]
 */
@Throws(IOException::class)
fun InputStream.toExternalImage(): ExternalImage {
    val file = createTempFile().apply { deleteOnExit() }
    file.outputStream().use {
        this.copyTo(it)
        it.flush()
    }
    this.close()
    return file.toExternalImage()
}

/**
 * 在 [IO] 中进行 [InputStream.toExternalImage]
 */
suspend inline fun InputStream.suspendToExternalImage(): ExternalImage = withContext(IO) { toExternalImage() }

/**
 * 保存为临时文件然后调用 [File.toExternalImage].
 *
 * 需要函数调用者 close [this]
 */
@Throws(IOException::class)
fun Input.toExternalImage(): ExternalImage {
    val file = createTempFile().apply { deleteOnExit() }
    file.outputStream().asOutput().use {
        this.copyTo(it)
        it.flush()
    }
    return file.toExternalImage()
}

/**
 * 在 [IO] 中进行 [Input.toExternalImage]
 */
suspend inline fun Input.suspendToExternalImage(): ExternalImage = withContext(IO) { toExternalImage() }

/*
/**
 * 保存为临时文件然后调用 [File.toExternalImage].
 */
suspend fun ByteReadChannel.toExternalImage(): ExternalImage {
    val file = createTempFile().apply { deleteOnExit() }
    file.outputStream().use {
        withContext(IO) { copyTo(it) }
        it.flush()
    }

    return file.suspendToExternalImage()
}*/