/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.mock.internal.contact.todo

import kotlinx.coroutines.launch
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.contact.checkBotPermission
import net.mamoe.mirai.contact.todo.GroupTodoRecord
import net.mamoe.mirai.contact.todo.GroupTodoStatus
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.MemberCompleteTodoEvent
import net.mamoe.mirai.event.events.MemberRecallTodoEvent
import net.mamoe.mirai.event.events.MemberSetTodoEvent
import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.mock.contact.todo.MockGroupTodo
import net.mamoe.mirai.mock.internal.contact.MockGroupImpl
import net.mamoe.mirai.utils.currentTimeSeconds
import net.mamoe.mirai.utils.toLongUnsigned
import java.util.concurrent.atomic.AtomicReference

internal class MockGroupTodoImpl(
    private val group: MockGroupImpl
) : MockGroupTodo {

    private val status: AtomicReference<GroupTodoStatus> = AtomicReference()

    private val current: AtomicReference<GroupTodoRecord> = AtomicReference()

    override suspend fun status(): GroupTodoStatus {
        return status.get() ?: GroupTodoStatus.NONE
    }

    override suspend fun current(): GroupTodoRecord? {
        return current.get()
    }

    override suspend fun set(source: MessageSource): GroupTodoRecord {
        group.checkBotPermission(MemberPermission.ADMINISTRATOR)
        val record = GroupTodoRecord(
            group = group,
            title = source.originalMessage.contentToString(),
            operator = group.botAsMember,
            operatorId = group.bot.id,
            operatorNick = group.botAsMember.nick,
            operatorTime = currentTimeSeconds().toInt(),
            msgSeq = source.ids.first().toLongUnsigned(),
            msgRandom = source.internalIds.first().toLongUnsigned()
        )
        current.set(record)
        status.set(GroupTodoStatus.NONE)
        group.launch {
            MemberSetTodoEvent(member = group.botAsMember).broadcast()
        }
        return record
    }

    override suspend fun recall(source: MessageSource) {
        val current = checkNotNull(current.get()) { "current no todo" }
        val id = source.ids.first().toLongUnsigned()
        val internalId = source.internalIds.first().toLongUnsigned()
        check(id == current.msgSeq && internalId == current.msgRandom) { "todo no match" }
        status.set(GroupTodoStatus.NONE)
        group.launch {
            MemberRecallTodoEvent(member = group.botAsMember).broadcast()
        }
    }

    override suspend fun recall(record: GroupTodoRecord) {
        val current = checkNotNull(current.get()) { "current no todo" }
        check(record.msgSeq == current.msgSeq && record.msgRandom == current.msgRandom) { "todo no match" }
        status.set(GroupTodoStatus.NONE)
        group.launch {
            MemberRecallTodoEvent(member = group.botAsMember).broadcast()
        }
    }

    override suspend fun complete(source: MessageSource) {
        val current = checkNotNull(current.get()) { "current no todo" }
        val id = source.ids.first().toLongUnsigned()
        val internalId = source.internalIds.first().toLongUnsigned()
        check(id == current.msgSeq && internalId == current.msgRandom) { "todo no match" }
        status.set(GroupTodoStatus.COMPLETED)
        group.launch {
            MemberCompleteTodoEvent(member = group.botAsMember).broadcast()
        }
    }

    override suspend fun complete(record: GroupTodoRecord) {
        val current = checkNotNull(current.get()) { "current no todo" }
        check(record.msgSeq == current.msgSeq && record.msgRandom == current.msgRandom) { "todo no match" }
        status.set(GroupTodoStatus.COMPLETED)
        group.launch {
            MemberCompleteTodoEvent(member = group.botAsMember).broadcast()
        }
    }

    override suspend fun close(source: MessageSource) {
        group.checkBotPermission(MemberPermission.ADMINISTRATOR)
        val current = checkNotNull(current.get()) { "current no todo" }
        val id = source.ids.first().toLongUnsigned()
        val internalId = source.internalIds.first().toLongUnsigned()
        check(id == current.msgSeq && internalId == current.msgRandom) { "todo no match" }
        status.set(GroupTodoStatus.CLOSED)
        group.launch {
            MemberSetTodoEvent(member = group.botAsMember).broadcast()
        }
    }

    override suspend fun close(record: GroupTodoRecord) {
        group.checkBotPermission(MemberPermission.ADMINISTRATOR)
        val current = checkNotNull(current.get()) { "current no todo" }
        check(record.msgSeq == current.msgSeq && record.msgRandom == current.msgRandom) { "todo no match" }
        status.set(GroupTodoStatus.CLOSED)
        group.launch {
            MemberSetTodoEvent(member = group.botAsMember).broadcast()
        }
    }
}