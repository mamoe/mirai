@file:Suppress("NOTHING_TO_INLINE")

package net.mamoe.mirai.console.util

import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.internal.utils.BotManagerImpl
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