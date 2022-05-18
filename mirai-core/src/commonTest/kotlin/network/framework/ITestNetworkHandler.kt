/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.framework

import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.components.EventDispatcher
import net.mamoe.mirai.internal.network.components.SsoProcessor
import net.mamoe.mirai.internal.network.handler.NetworkHandler
import net.mamoe.mirai.internal.network.handler.NetworkHandlerSupport

internal interface ITestNetworkHandler<Conn> : NetworkHandler {
    val bot: QQAndroidBot


    fun setStateClosed(exception: Throwable? = null): NetworkHandlerSupport.BaseStateImpl?
    fun setStateConnecting(exception: Throwable? = null): NetworkHandlerSupport.BaseStateImpl?
    fun setStateOK(conn: Conn, exception: Throwable? = null): NetworkHandlerSupport.BaseStateImpl?
    fun setStateLoading(conn: Conn): NetworkHandlerSupport.BaseStateImpl?
}

internal val ITestNetworkHandler<*>.eventDispatcher get() = bot.components[EventDispatcher]
internal val ITestNetworkHandler<*>.ssoProcessor get() = bot.components[SsoProcessor]
