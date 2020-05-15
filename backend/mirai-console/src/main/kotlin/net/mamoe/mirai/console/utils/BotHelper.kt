/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.utils

import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.plugins.*
import net.mamoe.mirai.contact.User
import java.io.File


/**
 * 判断此用户是否为 console 管理员
 */
val User.isManager: Boolean
    get() = this.bot.managers.contains(this.id)

@JvmName("addManager")
@JvmSynthetic
@Deprecated("for binary compatibility", level = DeprecationLevel.HIDDEN)
fun Bot.addManagerDeprecated(long: Long) {
    addManager(long)
}

internal fun Bot.addManager(long: Long): Boolean {
    TODO()
    return true
}

fun Bot.removeManager(long: Long) {
    TODO()
}

val Bot.managers: List<Long>
    get() {
       TODO()
    }

fun Bot.checkManager(long: Long): Boolean {
    return this.managers.contains(long)
}


fun getBotManagers(bot: Bot): List<Long> {
    return bot.managers
}
