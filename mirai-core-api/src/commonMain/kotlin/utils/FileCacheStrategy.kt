/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.utils

import kotlinx.io.core.Input
import kotlinx.io.core.readBytes
import net.mamoe.mirai.Bot
import net.mamoe.mirai.IMirai
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import java.awt.image.BufferedImage
import java.io.*
import java.security.MessageDigest
import javax.imageio.ImageIO

/**
 * 缓存策略.
 *
 * 图片上传时默认使用文件缓存.
 *
 * @see BotConfiguration.fileCacheStrategy 为 [Bot] 指定缓存策略
 * @see IMirai.FileCacheStrategy
 */
@MiraiExperimentalApi
public interface FileCacheStrategy {
    /**
     * 将 [input] 缓存为 [ExternalResource].
     * 此函数应 close 这个 [Input]
     */
    @MiraiExperimentalApi
    @Throws(java.io.IOException::class)
    public fun newCache(input: Input, formatName: String?): ExternalResource

    /**
     * 将 [input] 缓存为 [ExternalResource].
     * 此函数应 close 这个 [InputStream]
     */
    @MiraiExperimentalApi
    @Throws(java.io.IOException::class)
    public fun newCache(input: InputStream, formatName: String?): ExternalResource

    @MiraiExperimentalApi
    @Throws(java.io.IOException::class)
    public fun newCache(input: BufferedImage, formatName: String): ExternalResource

    /**
     * 默认的缓存方案, 使用系统临时文件夹存储.
     */
    @MiraiExperimentalApi
    public object PlatformDefault : FileCacheStrategy by TempCache(null)

    /**
     * 使用内存直接存储所有图片文件.
     */
    public object MemoryCache : FileCacheStrategy {
        @MiraiExperimentalApi
        @Throws(java.io.IOException::class)
        override fun newCache(input: Input, formatName: String?): ExternalResource {
            return input.withUse { readBytes() }.toExternalResource(formatName)
        }

        @MiraiExperimentalApi
        @Throws(java.io.IOException::class)
        override fun newCache(input: InputStream, formatName: String?): ExternalResource {
            return input.withUse { readBytes() }.toExternalResource(formatName)
        }

        @MiraiExperimentalApi
        override fun newCache(input: BufferedImage, formatName: String): ExternalResource {
            val out = ByteArrayOutputStream()
            ImageIO.write(input, formatName, out)
            val array = out.toByteArray()
            return array.toExternalResource(formatName)
        }
    }

    /**
     * 使用系统临时文件夹缓存图片文件. 在图片使用完毕后删除临时文件.
     */
    @MiraiExperimentalApi
    public class TempCache @JvmOverloads constructor(
        /**
         * 缓存图片存放位置. 为 `null` 时使用主机系统的临时文件夹
         */
        public val directory: File? = null
    ) : FileCacheStrategy {
        private fun createTempFile(): File {
            return File.createTempFile("tmp", null, directory)
        }

        @MiraiExperimentalApi
        @Throws(java.io.IOException::class)
        override fun newCache(input: Input, formatName: String?): ExternalResource {
            return createTempFile().apply {
                deleteOnExit()
                input.withOut(this.outputStream()) { copyTo(it) }
            }.toExternalResource(formatName)
        }

        @MiraiExperimentalApi
        @Throws(java.io.IOException::class)
        override fun newCache(input: InputStream, formatName: String?): ExternalResource {
            return createTempFile().apply {
                deleteOnExit()
                input.withOut(this.outputStream()) { copyTo(it) }
            }.toExternalResource(formatName)
        }

        @MiraiExperimentalApi
        public override fun newCache(input: BufferedImage, formatName: String): ExternalResource {
            val file = File.createTempFile("tmp", null, directory).apply { deleteOnExit() }

            val digest = MessageDigest.getInstance("md5")
            digest.reset()

            file.outputStream().use { out ->
                ImageIO.write(input, formatName, object : OutputStream() {
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

            return ExternalResourceImplByFileWithMd5(RandomAccessFile(file, "r"), digest.digest(), null)
        }
    }
}
