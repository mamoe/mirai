/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.mock.internal.db

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.roaming.RoamingMessage
import net.mamoe.mirai.contact.roaming.RoamingMessageFilter
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageSourceKind
import net.mamoe.mirai.mock.database.MessageDatabase
import net.mamoe.mirai.mock.database.MessageInfo
import net.mamoe.mirai.mock.database.mockMsgDatabaseId
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random

internal class MsgDatabaseImpl : MessageDatabase {
    override fun disconnect() {}
    override fun connect() {}

    val db = ConcurrentLinkedDeque<MessageInfo>()
    val idCounter1 = AtomicInteger(Random.nextInt())
    val idCounter2 = AtomicInteger(Random.nextInt())

    override fun newMessageInfo(
        sender: Long, subject: Long,
        kind: MessageSourceKind,
        time: Long,
        message: MessageChain,
    ): MessageInfo {
        val dbid = mockMsgDatabaseId(idCounter1.getAndIncrement(), idCounter2.getAndDecrement())
        val info = MessageInfo(
            mixinedMsgId = dbid,
            sender = sender,
            subject = subject,
            kind = kind,
            time = time,
            message = message,
        )
        db.add(info)
        return info
    }

    override fun queryMessageInfo(msgId: Long): MessageInfo? {
        return db.firstOrNull { it.mixinedMsgId == msgId }
    }

    override fun removeMessageInfo(msgId: Long) {
        db.removeIf { it.mixinedMsgId == msgId }
    }

    override fun queryMessageInfosBy(
        subject: Long, kind: MessageSourceKind,
        contact: Contact,
        timeStart: Long,
        timeEnd: Long,
        filter: RoamingMessageFilter
    ): Sequence<MessageInfo> {
        if (timeEnd < timeStart) return emptySequence()
        return sequence<MessageInfo> {
            val rm = object : RoamingMessage {
                override val contact: Contact get() = contact
                override var sender: Long = -1
                override var target: Long = -1
                override var time: Long = -1
                override val ids: IntArray = intArrayOf(-1)
                override val internalIds: IntArray = intArrayOf(-1)
            }
            for (msgInfo in db) {
                if (msgInfo.kind != kind) continue
                if (msgInfo.time < timeStart) continue
                if (msgInfo.time > timeEnd) continue
                if (msgInfo.subject != subject) continue

                rm.sender = msgInfo.sender
                if (kind != MessageSourceKind.GROUP) {
                    if (msgInfo.sender == contact.id) {
                        rm.target = contact.bot.id
                    } else {
                        rm.target = msgInfo.subject
                    }
                } else {
                    rm.target = msgInfo.subject
                }
                rm.time = msgInfo.time
                rm.ids[0] = msgInfo.id
                rm.internalIds[0] = msgInfo.internal

                if (filter.invoke(rm)) {
                    yield(msgInfo)
                }
            }
        }
    }
}