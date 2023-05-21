/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.spi

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.lateinitMutableProperty
import net.mamoe.mirai.utils.loadServices
import kotlin.reflect.KClass

/**
 * 基本 SPI 接口
 * @since 2.8.0
 */ // stable since 2.15
public interface BaseService {
    /** 使用优先级, 值越小越先使用 */
    public val priority: Int get() = 0
}

internal fun <T : BaseService> SpiServiceLoader(
    serviceType: KClass<T>,
    defaultImplementation: () -> T
): SpiServiceLoader<T> {
    return SpiServiceLoaderImpl(serviceType, defaultImplementation)
}


internal fun <T : BaseService> SpiServiceLoader(
    serviceType: KClass<T>
): SpiServiceLoader<T?> {
    return SpiServiceLoaderImpl(serviceType, null)
}

internal interface SpiServiceLoader<T : BaseService?> {
    val service: T
}

internal class SpiServiceLoaderImpl<T : BaseService?>(
    private val serviceType: KClass<T & Any>,
    defaultService: (() -> T)?
) : SpiServiceLoader<T> {
    private val defaultInstance: T? by lazy {
        defaultService?.invoke()
    }
    private val lock = SynchronizedObject()

    override val service: T get() = _service

    private var _service: T by lateinitMutableProperty {
        synchronized(lock) {
            reloadAndSelect()
        }
    }

    fun reload() {
        synchronized(lock) {
            _service = reloadAndSelect()
        }
    }

    private fun reloadAndSelect(): T {
        @Suppress("UNCHECKED_CAST")
        return (loadServices(serviceType).minByOrNull { it.priority } ?: defaultInstance) as T
    }

    companion object {
        val SPI_SERVICE_LOADER_LOGGER: MiraiLogger by lazy {
            MiraiLogger.Factory.create(SpiServiceLoader::class, "spi-service-loader")
        }
    }
}
