/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.spi

import net.mamoe.mirai.utils.MiraiLogger
import java.util.*
import kotlin.reflect.KClass


internal actual class SPIServiceLoader<T : BaseService> actual constructor(
    private val defaultService: T,
    private val serviceType: KClass<T>
) {
    actual var service: T = defaultService

    actual fun reload() {
        val loader = ServiceLoader.load(serviceType.java)
        service = loader.minByOrNull { it.priority } ?: defaultService
    }


    init {
        reload()
    }

    actual companion object {
        actual val SPI_SERVICE_LOADER_LOGGER: MiraiLogger by lazy {
            MiraiLogger.Factory.create(SPIServiceLoader::class, "spi-service-loader")
        }
    }

}