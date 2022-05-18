/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.handler

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.component.ComponentStorage
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.SimpleLogger

/**
 * Immutable context for [NetworkHandler]
 * @see NetworkHandlerContextImpl
 */
internal interface NetworkHandlerContext : ComponentStorage {
    val bot: QQAndroidBot
    // however migration requires a major change.

    val logger: MiraiLogger
}

internal inline fun NetworkHandlerContext.mapComponents(action: (ComponentStorage) -> ComponentStorage): NetworkHandlerContext {
    return NetworkHandlerContextImpl(bot, logger, this.let(action))
}

internal class NetworkHandlerContextImpl(
    override val bot: QQAndroidBot,
    override val logger: MiraiLogger,
    private val storage: ComponentStorage, // should be the same as bot.components
) : NetworkHandlerContext, ComponentStorage by storage {
    override fun toString(): String {
        return "NetworkHandlerContextImpl(bot=${bot.id}, storage=$storage)"
    }
}

internal fun MiraiLogger.asCoroutineExceptionHandler(
    priority: SimpleLogger.LogPriority = SimpleLogger.LogPriority.ERROR,
): CoroutineExceptionHandler {
    return CoroutineExceptionHandler { context, e ->
        call(
            priority,
            context[CoroutineName]?.let { "Exception in coroutine '${it.name}'." } ?: "Exception in unnamed coroutine.",
            e
        )
    }
}
