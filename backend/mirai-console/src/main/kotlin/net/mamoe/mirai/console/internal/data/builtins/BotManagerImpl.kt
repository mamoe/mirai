/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("MemberVisibilityCanBePrivate")

package net.mamoe.mirai.console.internal.data.builtins

import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.PluginDataExtensions.mapKeys
import net.mamoe.mirai.console.data.PluginDataExtensions.withEmptyDefault
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.console.util.BotManager
import net.mamoe.mirai.contact.User

internal object BotManagerImpl : BotManager {
    override val User.isManager: Boolean get() = this.id in ManagersConfig[this.bot]

    override fun Bot.removeManager(id: Long): Boolean {
        return ManagersConfig[this].remove(id)
    }

    override val Bot.managers: List<Long>
        get() = ManagersConfig[this].toList()

    override fun Bot.addManager(id: Long): Boolean {
        return ManagersConfig[this].add(id)
    }
}

internal object ManagersConfig : AutoSavePluginConfig() {
    override val saveName: String
        get() = "Managers"

    @ValueDescription(
        """
        管理员列表
    """
    )
    private val managers by value<MutableMap<Long, MutableSet<Long>>>().withEmptyDefault()
        .mapKeys(Bot::getInstance, Bot::id)

    internal operator fun get(bot: Bot): MutableSet<Long> = managers[bot]!!
}