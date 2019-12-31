@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.utils

import android.graphics.BitmapFactory
import io.ktor.util.asStream
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import kotlinx.io.core.Input
import kotlinx.io.core.IoBuffer
import kotlinx.io.core.copyTo
import kotlinx.io.errors.IOException
import kotlinx.io.streams.asInput
import kotlinx.io.streams.asOutput
import java.io.File
import java.io.InputStream
import java.net.URL

/*
 * 将各类型图片容器转为 [ExternalImage]
 */

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
        inputSize = this.length()
    )
}

/**
 * 在 [IO] 中进行 [File.toExternalImage]
 */
@Suppress("unused")
suspend fun File.suspendToExternalImage(): ExternalImage = withContext(IO) { toExternalImage() }

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
    }
    return file.toExternalImage()
}

/**
 * 在 [IO] 中进行 [URL.toExternalImage]
 */
@Suppress("unused")
suspend fun URL.suspendToExternalImage(): ExternalImage = withContext(IO) { toExternalImage() }

/**
 * 保存为临时文件然后调用 [File.toExternalImage]
 */
@Throws(IOException::class)
fun InputStream.toExternalImage(): ExternalImage {
    val file = createTempFile().apply { deleteOnExit() }
    file.outputStream().asOutput().use {
        this.asInput().copyTo(it)
    }
    this.close()
    return file.toExternalImage()
}

/**
 * 在 [IO] 中进行 [InputStream.toExternalImage]
 */
@Suppress("unused")
suspend fun InputStream.suspendToExternalImage(): ExternalImage = withContext(IO) { toExternalImage() }

/**
 * 保存为临时文件然后调用 [File.toExternalImage]
 */
@Throws(IOException::class)
fun Input.toExternalImage(): ExternalImage {
    val file = createTempFile().apply { deleteOnExit() }
    file.outputStream().asOutput().use {
        this.asStream().asInput().copyTo(it)
    }
    return file.toExternalImage()
}

/**
 * 在 [IO] 中进行 [Input.toExternalImage]
 */
@Suppress("unused")
suspend fun Input.suspendToExternalImage(): ExternalImage = withContext(IO) { toExternalImage() }
