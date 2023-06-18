/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.spi

import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.runAutoClose
import net.mamoe.mirai.utils.useAutoClose
import net.mamoe.mirai.utils.withAutoClose
import kotlin.jvm.JvmStatic

/**
 * 将源音频文件转换为 silk v3 with tencent 格式
 *
 * @since 2.8.0
 */ // stable since 2.15
public interface AudioToSilkService : BaseService {
    /**
     * implementation note:
     *
     * 如果返回值为转换后的资源文件:
     *
     * 如果 [ExternalResource.isAutoClose], 需要关闭 [source],
     * 返回的 [ExternalResource] 的 [ExternalResource.isAutoClose] 必须为 `true`
     *
     * 特别的, 如果该方法体抛出了一个错误, 如果 [ExternalResource.isAutoClose], 需要关闭 [source]
     *
     * @see [withAutoClose]
     * @see [runAutoClose]
     * @see [useAutoClose]
     */
    public suspend fun convert(source: ExternalResource): ExternalResource

    public companion object {
        private val loader = SpiServiceLoader(AudioToSilkService::class) {
            object : AudioToSilkService {
                override suspend fun convert(source: ExternalResource): ExternalResource = source
            }
        }

        /**
         * 获取当前实例
         */
        @JvmStatic
        public val instance: AudioToSilkService get() = loader.service
    }
}