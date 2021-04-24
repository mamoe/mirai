/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.handler

import net.mamoe.mirai.internal.network.framework.AbstractRealNetworkHandlerTest
import net.mamoe.mirai.internal.network.impl.netty.NettyNetworkHandler
import net.mamoe.mirai.internal.network.impl.netty.NettyNetworkHandlerFactory
import net.mamoe.mirai.internal.test.runBlockingUnit
import kotlin.test.Test

internal class HandlerEventTest : AbstractRealNetworkHandlerTest<NettyNetworkHandler>(NettyNetworkHandlerFactory) {

    @Test
    fun `BotOnlineEvent after successful logon`() = runBlockingUnit {
        bot.login()
    }
}