/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("EXPERIMENTAL_API_USAGE", "unused")

package net.mamoe.mirai.utils

import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.io.ByteReadChannel
import kotlinx.coroutines.withContext
import kotlinx.io.core.Input
import kotlinx.io.core.copyTo
import kotlinx.io.errors.IOException
import kotlinx.io.streams.asOutput
import net.mamoe.mirai.utils.internal.md5
import java.awt.image.BufferedImage
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.net.URL
import java.security.MessageDigest
import javax.imageio.ImageIO

/*
 * 将各类型图片容器转为 [ExternalImage]
 */


/**
 * 将 [BufferedImage] 保存稳临时文件, 然后构造 [ExternalImage]
 */
@JvmOverloads
@Throws(IOException::class)
fun BufferedImage.toExternalImage(formatName: String = "png"): ExternalImage {
    val file = createTempFile().apply { deleteOnExit() }

    val digest = MessageDigest.getInstance("md5")
    digest.reset()

    file.outputStream().use { out ->
        ImageIO.write(this@toExternalImage, formatName, object : OutputStream() {
            override fun write(b: Int) {
                out.write(b)
                digest.update(b.toByte())
            }

            override fun write(b: ByteArray) {
                out.write(b)
                digest.update(b)
            }

            override fun write(b: ByteArray, off: Int, len: Int) {
                out.write(b, off, len)
                digest.update(b, off, len)
            }
        })
    }

    @Suppress("DEPRECATION_ERROR")
    return ExternalImage(digest.digest(), file.inputStream())
}

suspend inline fun BufferedImage.suspendToExternalImage(): ExternalImage = withContext(IO) { toExternalImage() }

/**
 * 直接使用文件 [inputStream] 构造 [ExternalImage]
 */
@OptIn(MiraiInternalAPI::class)
@Throws(IOException::class)
fun File.toExternalImage(): ExternalImage {
    @Suppress("DEPRECATION_ERROR")
    return ExternalImage(
        md5 = this.inputStream().md5(), // dont change
        input = this.inputStream()
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
    file.outputStream().use { output ->
        openStream().use { input ->
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
}