/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.contact.todo

import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.contact.checkBotPermission
import net.mamoe.mirai.contact.todo.GroupTodo
import net.mamoe.mirai.contact.todo.GroupTodoRecord
import net.mamoe.mirai.contact.todo.GroupTodoStatus
import net.mamoe.mirai.internal.contact.GroupImpl
import net.mamoe.mirai.internal.network.protocol.data.proto.Oidb0xf8e
import net.mamoe.mirai.internal.network.protocol.data.proto.Oidb0xf90
import net.mamoe.mirai.internal.network.protocol.packet.chat.TroopTodoManager
import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.toLongUnsigned

internal class GroupTodoImpl(
    internal val group: GroupImpl,
    internal val logger: MiraiLogger,
) : GroupTodo {

    private fun Oidb0xf8e.RspBody.toGroupTodoRecord(): GroupTodoRecord? {
        val info = info ?: return null
        return GroupTodoRecord(
            group = group,
            title = info.title,
            operator = group[info.uin],
            operatorId = info.uin,
            operatorNick = info.nickname,
            operatorTime = info.createTime,
            msgSeq = info.seq,
            msgRandom = info.random.toLongUnsigned()
        )
    }

    private fun Oidb0xf90.TodoInfo.toGroupTodoRecord(): GroupTodoRecord {
        return GroupTodoRecord(
            group = group,
            title = title,
            operator = group[uin],
            operatorId = uin,
            operatorNick = nickname,
            operatorTime = createTime,
            msgSeq = seq,
            msgRandom = random.toLongUnsigned()
        )
    }

    override suspend fun status(): GroupTodoStatus {
        val result = group.bot.network.sendAndExpect(
            TroopTodoManager.Status(
                group.bot.client,
                group.uin,
            ), 30000, 2
        )
        check(result.pkg.result == 0) { result.pkg.errorMsg }

        return when (val code = result.body.rptGroupList?.single()?.status) {
            null, 0 -> GroupTodoStatus.NONE
            1 -> GroupTodoStatus.COMPLETED
            2 -> GroupTodoStatus.CLOSED
            else -> throw IllegalStateException("status: $code")
        }
    }

    override suspend fun current(): GroupTodoRecord? {
        val result = group.bot.network.sendAndExpect(
            TroopTodoManager.Current(
                group.bot.client,
                group.uin,
            ), 30000, 2
        )

        return result.body.toGroupTodoRecord()
    }

    override suspend fun set(source: MessageSource): GroupTodoRecord {
        group.checkBotPermission(MemberPermission.ADMINISTRATOR)
        val result = group.bot.network.sendAndExpect(
            TroopTodoManager.SetTodo(
                group.bot.client,
                group.uin,
                source.internalIds.first().toLongUnsigned(),
                source.ids.first().toLongUnsigned()
            ), 30000, 2
        )
        check(result.pkg.result == 0) { result.pkg.errorMsg }
        val info = result.info!!

        return info.toGroupTodoRecord()
    }

    override suspend fun recall(source: MessageSource) {
        group.checkBotPermission(MemberPermission.ADMINISTRATOR)
        val result = group.bot.network.sendAndExpect(
            TroopTodoManager.RecallTodo(
                group.bot.client,
                group.uin,
                source.internalIds.first().toLongUnsigned(),
                source.ids.first().toLongUnsigned()
            ), 30000, 2
        )
        check(result.pkg.result == 0) { result.pkg.errorMsg }
    }

    override suspend fun recall(record: GroupTodoRecord) {
        group.checkBotPermission(MemberPermission.ADMINISTRATOR)
        val result = group.bot.network.sendAndExpect(
            TroopTodoManager.RecallTodo(
                group.bot.client,
                group.uin,
                record.msgRandom,
                record.msgSeq
            ), 30000, 2
        )
        check(result.pkg.result == 0) { result.pkg.errorMsg }
    }

    override suspend fun complete(source: MessageSource) {
        val result = group.bot.network.sendAndExpect(
            TroopTodoManager.CompleteTodo(
                group.bot.client,
                group.uin,
                source.internalIds.first().toLongUnsigned(),
                source.ids.first().toLongUnsigned()
            ), 30000, 2
        )
        check(result.pkg.result == 0) { result.pkg.errorMsg }
    }

    override suspend fun complete(record: GroupTodoRecord) {
        val result = group.bot.network.sendAndExpect(
            TroopTodoManager.CompleteTodo(
                group.bot.client,
                group.uin,
                record.msgRandom,
                record.msgSeq
            ), 30000, 2
        )
        check(result.pkg.result == 0) { result.pkg.errorMsg }
    }

    override suspend fun close(source: MessageSource) {
        group.checkBotPermission(MemberPermission.ADMINISTRATOR)
        val result = group.bot.network.sendAndExpect(
            TroopTodoManager.CloseTodo(
                group.bot.client,
                group.uin,
                source.internalIds.first().toLongUnsigned(),
                source.ids.first().toLongUnsigned()
            ), 30000, 2
        )
        check(result.pkg.result == 0) { result.pkg.errorMsg }
    }

    override suspend fun close(record: GroupTodoRecord) {
        group.checkBotPermission(MemberPermission.ADMINISTRATOR)
        val result = group.bot.network.sendAndExpect(
            TroopTodoManager.CloseTodo(
                group.bot.client,
                group.uin,
                record.msgRandom,
                record.msgSeq
            ), 30000, 2
        )
        check(result.pkg.result == 0) { result.pkg.errorMsg }
    }
}