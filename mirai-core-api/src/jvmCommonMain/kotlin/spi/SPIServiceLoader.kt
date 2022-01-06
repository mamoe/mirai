/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.spi

import net.mamoe.mirai.utils.MiraiExperimentalApi
import net.mamoe.mirai.utils.MiraiLogger
import java.util.*

/**
 * 基本 SPI 接口
 * @since 2.8.0
 */
@MiraiExperimentalApi
public interface BaseService {
    /** 使用优先级, 值越小越先使用 */
    public val priority: Int get() = 5
}

internal class SPIServiceLoader<T : BaseService>(
    @JvmField val defaultService: T,
    @JvmField val serviceType: Class<T>,
) {
    @JvmField
    var service: T = defaultService

    fun reload() {
        val loader = ServiceLoader.load(serviceType)
        service = loader.minByOrNull { it.priority } ?: defaultService
    }

    init {
        reload()
    }

    companion object {
        val SPI_SERVICE_LOADER_LOGGER by lazy {
            MiraiLogger.Factory.create(SPIServiceLoader::class.java, "spi-service-loader")
        }
    }
}
