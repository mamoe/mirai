/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */
@file:JvmName("BotManagers")

package net.mamoe.mirai.console.utils

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.User


/**
 * 判断此用户是否为 console 管理员
 */
public val User.isManager: Boolean
    get() = this.bot.managers.contains(this.id)

internal fun Bot.addManager(long: Long): Boolean {
    TODO()
    return true
}

public fun Bot.removeManager(long: Long) {
    TODO()
}

public val Bot.managers: List<Long>
    get() = TODO()
