/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.message

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.event.BroadcastControllable
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.utils.getValue
import net.mamoe.mirai.utils.unsafeWeakRef

class FriendMessage(
    sender: QQ,
    override val message: MessageChain
) : MessagePacket<QQ, QQ>(), BroadcastControllable {
    override val sender: QQ by sender.unsafeWeakRef()
    override val bot: Bot get() = sender.bot
    override val subject: QQ get() = sender

    override fun toString(): String = "FriendMessage(sender=${sender.id}, message=$message)"
}
