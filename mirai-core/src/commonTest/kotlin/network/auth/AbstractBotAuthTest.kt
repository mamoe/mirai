/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.auth

import net.mamoe.mirai.auth.BotAuthInfo
import net.mamoe.mirai.auth.BotAuthResult
import net.mamoe.mirai.auth.BotAuthSession
import net.mamoe.mirai.auth.BotAuthorization
import net.mamoe.mirai.internal.network.components.SsoProcessor
import net.mamoe.mirai.internal.network.components.SsoProcessorContext
import net.mamoe.mirai.internal.network.components.SsoProcessorImpl
import net.mamoe.mirai.internal.network.framework.AbstractCommonNHTestWithSelector
import net.mamoe.mirai.internal.network.framework.PacketReplierDslBuilder
import net.mamoe.mirai.internal.network.framework.buildPacketReplier

internal abstract class AbstractBotAuthTest : AbstractCommonNHTestWithSelector() {
    init {
        // Use real processor to test login
        overrideComponents[SsoProcessor] = SsoProcessorImpl(overrideComponents[SsoProcessorContext])
    }

    protected fun setAuthorization(authorize: (session: BotAuthSession, info: BotAuthInfo) -> BotAuthResult) {
        // Run a real SsoProcessor, just without sending packets
        bot.account.authorization = object : BotAuthorization {
            override suspend fun authorize(session: BotAuthSession, info: BotAuthInfo): BotAuthResult {
                return authorize(session, info)
            }
        }
    }

    // Use the same replier even after reconnection
    protected fun usePacketReplierThroughout(builderAction: PacketReplierDslBuilder.() -> Unit) {
        val replier = buildPacketReplier {
            builderAction()
        }
        onEachNetworkInstance {
            addPacketReplier(replier) // share the decisions
        }
    }
}