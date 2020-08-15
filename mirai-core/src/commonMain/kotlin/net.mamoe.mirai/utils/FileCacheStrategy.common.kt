/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.utils

import kotlinx.io.core.Closeable
import kotlinx.io.core.Input
import kotlinx.io.core.use
import kotlinx.io.errors.IOException
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

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
        override fun newImageCache(input: ByteArray): ExternalImage
    }
}

internal inline fun <I : Closeable, O : Closeable, R> I.withOut(output: O, block: I.(output: O) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return use { output.use { block(this, output) } }
}
