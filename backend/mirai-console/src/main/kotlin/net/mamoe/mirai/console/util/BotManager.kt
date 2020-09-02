/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("NOTHING_TO_INLINE")
@file:JvmMultifileClass
@file:JvmName("ConsoleUtils")

package net.mamoe.mirai.console.util

import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.internal.data.builtins.BotManagerImpl
import net.mamoe.mirai.contact.User

public interface BotManager {
    /**
     * 判断此用户是否为 console 管理员
     */
    public val User.isManager: Boolean
    public val Bot.managers: List<Long>

    public fun Bot.removeManager(id: Long): Boolean
    public fun Bot.addManager(id: Long): Boolean

    public companion object INSTANCE : BotManager { // kotlin import handler doesn't recognize delegation.
        override fun Bot.addManager(id: Long): Boolean = BotManagerImpl.run { addManager(id) }
        override fun Bot.removeManager(id: Long): Boolean = BotManagerImpl.run { removeManager(id) }
        override val User.isManager: Boolean get() = BotManagerImpl.run { isManager }
        override val Bot.managers: List<Long> get() = BotManagerImpl.run { managers }
    }
}