/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.mock.internal.contactbase

import net.mamoe.mirai.mock.internal.MockBotImpl
import net.mamoe.mirai.utils.ConcurrentHashMap

internal class ContactDatabase(
    private val bot: MockBotImpl,
) {
    val contacts = ConcurrentHashMap<Long, ContactInfo>()

    fun acquireCI(id: Long): ContactInfo {
        return contacts.computeIfAbsent(id) {
            ContactInfo(bot, id, bot.nameGenerator.nextFriendName())
        }
    }

    fun acquireCI(id: Long, name: String): ContactInfo {
        return contacts.computeIfAbsent(id) {
            ContactInfo(bot, id, name)
        }.also { rsp ->
            if (rsp.nick != name) rsp.changeNick(name)
        }
    }
}
