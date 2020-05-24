package net.mamoe.mirai.utils

import kotlinx.io.core.Input
import kotlinx.io.errors.IOException
import net.mamoe.mirai.utils.internal.InputStream

/**
 * 缓存策略.
 *
 * 图片上传时默认使用文件缓存.
 */
@MiraiExperimentalAPI
expect interface FileCacheStrategy {
    /**
     * 将 [input] 缓存为 [ExternalImage].
     * 此函数应 close 这个 [Input]
     */
    @MiraiExperimentalAPI
    @Throws(IOException::class)
    fun newImageCache(input: Input): ExternalImage

    /**
     * 将 [input] 缓存为 [ExternalImage].
     * 此函数应 close 这个 [InputStream]
     */
    @MiraiExperimentalAPI
    @Throws(IOException::class)
    fun newImageCache(input: InputStream): ExternalImage

    /**
     * 将 [input] 缓存为 [ExternalImage].
     * 此 [input] 的内容应是不变的.
     */
    @MiraiExperimentalAPI
    @Throws(IOException::class)
    fun newImageCache(input: ByteArray): ExternalImage

    /**
     * 默认的缓存方案. 在 JVM 平台使用系统临时文件.
     */
    @MiraiExperimentalAPI
    object PlatformDefault : FileCacheStrategy

    /**
     * 使用内存直接存储所有图片文件.
     */
    object MemoryCache : FileCacheStrategy {
        @MiraiExperimentalAPI
        @Throws(IOException::class)
        override fun newImageCache(input: Input): ExternalImage

        @MiraiExperimentalAPI
        @Throws(IOException::class)
        override fun newImageCache(input: InputStream): ExternalImage

        @MiraiExperimentalAPI
        @Throws(IOException::class)
        override fun newImageCache(input: ByteArray): ExternalImage
    }
}