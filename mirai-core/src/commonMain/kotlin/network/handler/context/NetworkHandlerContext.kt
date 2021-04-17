/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.handler.context

import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.handler.NetworkHandler
import net.mamoe.mirai.internal.network.handler.components.SsoProcessor
import net.mamoe.mirai.internal.network.handler.state.StateObserver
import net.mamoe.mirai.utils.MiraiLogger

/**
 * Immutable context for [NetworkHandler]
 * @see NetworkHandlerContextImpl
 */
internal interface NetworkHandlerContext {
    val bot: QQAndroidBot
    // however migration requires a major change.

    val logger: MiraiLogger
    val ssoProcessor: SsoProcessor

    val stateObserver: StateObserver?
}

internal class NetworkHandlerContextImpl(
    override val bot: QQAndroidBot,
    override val ssoProcessor: SsoProcessor,
    override val logger: MiraiLogger,
    override val stateObserver: StateObserver?,
) : NetworkHandlerContext {
    override fun toString(): String {
        return "NetworkHandlerContextImpl(bot=${bot.id}, stateObserver=$stateObserver)"
    }
}
