/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai

import io.ktor.utils.io.ByteReadChannel
import kotlinx.io.core.Input
import net.mamoe.mirai.utils.ExternalImage
import net.mamoe.mirai.utils.MiraiExperimentalAPI
import net.mamoe.mirai.utils.SinceMirai
import net.mamoe.mirai.utils.internal.InputStream
import kotlin.jvm.JvmStatic

/**
 * Mirai 全局环境.
 */
@SinceMirai("1.0.0")
expect object Mirai {

    @JvmStatic
    var fileCacheStrategy: FileCacheStrategy

    /**
     * 缓存策略.
     *
     * 图片上传时默认使用文件缓存.
     */
    interface FileCacheStrategy {
        @MiraiExperimentalAPI
        fun newImageCache(input: Input): ExternalImage

        @MiraiExperimentalAPI
        fun newImageCache(input: ByteReadChannel): ExternalImage

        @MiraiExperimentalAPI
        fun newImageCache(input: InputStream): ExternalImage

        companion object Default : FileCacheStrategy
    }
}