/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.contact.roaming

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.roaming.RoamingMessage
import net.mamoe.mirai.contact.roaming.RoamingMessages
import net.mamoe.mirai.internal.contact.AbstractContact
import net.mamoe.mirai.internal.message.source.decodeRandom
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm
import net.mamoe.mirai.utils.mapToIntArray
import net.mamoe.mirai.utils.toLongUnsigned

internal abstract class AbstractRoamingMessages : RoamingMessages {
    abstract val contact: AbstractContact

    protected fun createRoamingMessage(
        message: MsgComm.Msg,
        messages: List<MsgComm.Msg>
    ) = object : RoamingMessage {
        override val contact: Contact get() = this@AbstractRoamingMessages.contact
        override val sender: Long get() = message.msgHead.fromUin
        override val target: Long
            get() = message.msgHead.groupInfo?.groupCode ?: message.msgHead.toUin
        override val time: Long get() = message.msgHead.msgTime.toLongUnsigned()
        override val ids: IntArray by lazy { messages.mapToIntArray { it.msgHead.msgSeq } }
        override val internalIds: IntArray by lazy {
            messages.mapToIntArray { it.decodeRandom() }
        }
    }
}