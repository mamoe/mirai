/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("MemberVisibilityCanBePrivate")

package net.mamoe.mirai.utils

import kotlinx.io.core.*
import kotlinx.io.streams.asInput
import kotlinx.io.streams.asOutput
import net.mamoe.mirai.Bot
import net.mamoe.mirai.utils.internal.asReusableInput
import java.awt.image.BufferedImage
import java.io.*
import java.net.URL
import java.security.MessageDigest
import java.util.*
import javax.imageio.ImageIO
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.io.use

/**
 * 缓存策略.
 *
 * 图片上传时默认使用文件缓存.
 *
 * @see BotConfiguration.fileCacheStrategy 为 [Bot] 指定缓存策略
 */
@MiraiExperimentalAPI
public actual interface FileCacheStrategy {
    /**
     * 将 [input] 缓存为 [ExternalImage].
     * 此函数应 close 这个 [Input]
     */
    @MiraiExperimentalAPI
    @Throws(IOException::class)
    public actual fun newImageCache(input: Input): ExternalImage

    /**
     * 将 [input] 缓存为 [ExternalImage].
     * 此函数应 close 这个 [InputStream]
     */
    @MiraiExperimentalAPI
    @Throws(IOException::class)
    public fun newImageCache(input: InputStream): ExternalImage

    /**
     * 将 [input] 缓存为 [ExternalImage].
     * 此 [input] 的内容应是不变的.
     */
    @MiraiExperimentalAPI
    @Throws(IOException::class)
    public actual fun newImageCache(input: ByteArray): ExternalImage

    /**
     * 将 [input] 缓存为 [ExternalImage].
     * 此 [input] 的内容应是不变的.
     */
    @MiraiExperimentalAPI
    @Throws(IOException::class)
    public fun newImageCache(input: BufferedImage, format: String = "png"): ExternalImage

    /**
     * 将 [input] 缓存为 [ExternalImage].
     */
    @MiraiExperimentalAPI
    @Throws(IOException::class)
    public fun newImageCache(input: URL): ExternalImage

    /**
     * 默认的缓存方案, 使用系统临时文件夹存储.
     */
    @MiraiExperimentalAPI
    public actual object PlatformDefault : FileCacheStrategy by TempCache(null)

    /**
     * 使用内存直接存储所有图片文件.
     */
    public actual object MemoryCache : FileCacheStrategy {
        @MiraiExperimentalAPI
        @Throws(IOException::class)
        actual override fun newImageCache(input: Input): ExternalImage {
            return newImageCache(input.readBytes())
        }

        @MiraiExperimentalAPI
        @Throws(IOException::class)
        override fun newImageCache(input: InputStream): ExternalImage {
            return newImageCache(input.readBytes())
        }

        @MiraiExperimentalAPI
        @Throws(IOException::class)
        actual override fun newImageCache(input: ByteArray): ExternalImage {
            return ExternalImage(input.asReusableInput())
        }

        @MiraiExperimentalAPI
        override fun newImageCache(input: BufferedImage, format: String): ExternalImage {
            val out = ByteArrayOutputStream()
            ImageIO.write(input, format, out)
            return newImageCache(out.toByteArray())
        }

        @MiraiExperimentalAPI
        override fun newImageCache(input: URL): ExternalImage {
            val out = ByteArrayOutputStream()
            input.openConnection().getInputStream().use { it.copyTo(out) }
            return newImageCache(out.toByteArray())
        }
    }

    /**
     * 使用系统临时文件夹缓存图片文件. 在图片使用完毕后删除临时文件.
     */
    @MiraiExperimentalAPI
    public class TempCache @JvmOverloads constructor(
        /**
         * 缓存图片存放位置. 为 `null` 时使用主机系统的临时文件夹
         */
        public val directory: File? = null
    ) : FileCacheStrategy {
        @MiraiExperimentalAPI
        @Throws(IOException::class)
        override fun newImageCache(input: Input): ExternalImage {
            return ExternalImage(createTempFile(directory = directory).apply {
                deleteOnExit()
                input.withOut(this.outputStream()) { copyTo(it) }
            }.asReusableInput(true))
        }

        @MiraiExperimentalAPI
        @Throws(IOException::class)
        override fun newImageCache(input: InputStream): ExternalImage {
            return ExternalImage(createTempFile(directory = directory).apply {
                deleteOnExit()
                input.withOut(this.outputStream()) { copyTo(it) }
            }.asReusableInput(true))
        }

        @MiraiExperimentalAPI
        @Throws(IOException::class)
        override fun newImageCache(input: ByteArray): ExternalImage {
            return ExternalImage(input.asReusableInput())
        }

        @MiraiExperimentalAPI
        override fun newImageCache(input: BufferedImage, format: String): ExternalImage {
            val file = createTempFile(directory = directory).apply { deleteOnExit() }

            val digest = MessageDigest.getInstance("md5")
            digest.reset()

            file.outputStream().use { out ->
                ImageIO.write(input, format, object : OutputStream() {
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
            return ExternalImage(file.asReusableInput(true, digest.digest()))
        }

        @MiraiExperimentalAPI
        override fun newImageCache(input: URL): ExternalImage {
            return ExternalImage(createTempFile(directory = directory).apply {
                deleteOnExit()
                input.openConnection().getInputStream().withOut(this.outputStream()) { copyTo(it) }
            }.asReusableInput(true))
        }
    }
}


@Throws(IOException::class)
internal fun Input.copyTo(out: OutputStream, bufferSize: Int = DEFAULT_BUFFER_SIZE): Long {
    var bytesCopied: Long = 0
    val buffer = ByteArray(bufferSize)
    var bytes = readAvailable(buffer)
    while (bytes >= 0) {
        out.write(buffer, 0, bytes)
        bytesCopied += bytes
        bytes = readAvailable(buffer)
    }
    return bytesCopied
}