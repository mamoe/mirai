/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.mock.internal.contactbase

import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.event.events.BotAvatarChangedEvent
import net.mamoe.mirai.event.events.BotNickChangedEvent
import net.mamoe.mirai.event.events.FriendAvatarChangedEvent
import net.mamoe.mirai.event.events.FriendNickChangedEvent
import net.mamoe.mirai.mock.internal.MockBotImpl
import net.mamoe.mirai.mock.internal.contact.MockImage
import net.mamoe.mirai.mock.utils.broadcastBlocking
import net.mamoe.mirai.utils.lateinitMutableProperty

internal class ContactInfo(
    private val declaredBot: MockBotImpl,
    @JvmField val id: Long,
    @JvmField var nick: String,
) {
    var avatarUrl: String by lateinitMutableProperty {
        runBlocking { MockImage.randomForPerson(declaredBot, id).getUrl(declaredBot) }
    }

    fun changeAvatarUrl(newAvatar: String) {
        avatarUrl = newAvatar
        if (declaredBot.id == id) {
            BotAvatarChangedEvent(declaredBot).broadcastBlocking()
            return
        }
        declaredBot.getFriend(id)?.let {
            FriendAvatarChangedEvent(it).broadcastBlocking()
        }
    }

    fun changeNick(newNick: String) {
        if (id == declaredBot.id) {
            val o = nick
            nick = newNick
            BotNickChangedEvent(declaredBot, o, newNick).broadcastBlocking()
            return
        }
        val friend = declaredBot.getFriend(id)
        if (friend == null) {
            nick = newNick
            return
        }
        val o = nick
        nick = newNick
        FriendNickChangedEvent(friend, o, newNick).broadcastBlocking()
    }
}
