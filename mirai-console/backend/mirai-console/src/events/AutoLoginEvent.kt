/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.events

import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.AbstractEvent
import net.mamoe.mirai.console.internal.*
import net.mamoe.mirai.event.events.BotEvent
import net.mamoe.mirai.utils.MiraiInternalApi

/**
 * 自动登录执行后广播的事件
 * @property bot 登录的BOT
 * @see MiraiConsoleImplementationBridge.doStart
 * @since 2.15
 */
public sealed class AutoLoginEvent : BotEvent, ConsoleEvent, AbstractEvent() {
    /**
     * 登录成功
     */
    public class Success @MiraiInternalApi constructor(
        override val bot: Bot
    ) : AutoLoginEvent() {
        override fun toString(): String {
            return "AutoLoginEvent.Success(bot=${bot.id}, protocol=${bot.configuration.protocol}, heartbeatStrategy=${bot.configuration.heartbeatStrategy})"
        }
    }

    /**
     * 登录失败
     */
    public class Failure @MiraiInternalApi constructor(
        override val bot: Bot,
        public val cause: Throwable
    ) : AutoLoginEvent() {
        override fun toString(): String {
            return "AutoLoginEvent.Failure(bot=${bot.id}, protocol=${bot.configuration.protocol}, cause=${cause})"
        }
    }
}