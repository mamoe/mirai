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
            for (msgInfo in db) {
                if (msgInfo.kind != kind) continue
                if (msgInfo.time < timeStart) continue
                if (msgInfo.time > timeEnd) continue
                if (msgInfo.subject != subject) continue

                val rm = msgInfo.toRoamingMessage(contact)

                if (filter.invoke(rm)) {
                    yield(msgInfo)
                }
            }
        }
    }

    override fun queryMessageInfosBy(
        subject: Long, kind: MessageSourceKind,
        contact: Contact,
        sequence: Long,
        filter: RoamingMessageFilter
    ): Sequence<MessageInfo> {
        return sequence<MessageInfo> {
            var emitted = 0
            for (msgInfo in db) {
                if (msgInfo.kind != kind) continue
                if (msgInfo.subject != subject) continue

                val rm = msgInfo.toRoamingMessage(contact)

                if (filter.invoke(rm)) {
                    yield(msgInfo)
                }
            }
        }
    }

    private fun MessageInfo.toRoamingMessage(contact: Contact): RoamingMessage {
        val info = this

        return object : RoamingMessage {
            override val contact: Contact = contact
            override val sender: Long = info.sender
            override val target: Long =
                if (info.kind != MessageSourceKind.GROUP) {
                    if (info.sender == contact.id) {
                        contact.bot.id
                    } else {
                        info.subject
                    }
                } else {
                    info.subject
                }
            override val time: Long = info.time
            override val ids: IntArray = IntArray(1) { info.id }
            override val internalIds: IntArray = IntArray(1) { info.internal }

        }
    }
}