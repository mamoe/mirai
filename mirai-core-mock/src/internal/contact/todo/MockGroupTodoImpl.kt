/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.mock.internal.contact.todo

import net.mamoe.mirai.contact.todo.GroupTodoRecord
import net.mamoe.mirai.contact.todo.GroupTodoStatus
import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.mock.contact.todo.MockGroupTodo
import net.mamoe.mirai.mock.internal.contact.MockGroupImpl

internal class MockGroupTodoImpl(
    private val group: MockGroupImpl
) : MockGroupTodo {

    override suspend fun status(): GroupTodoStatus {
        TODO("Not yet implemented")
    }

    override suspend fun current(): GroupTodoRecord? {
        TODO("Not yet implemented")
    }

    override suspend fun set(source: MessageSource): GroupTodoRecord {
        TODO("Not yet implemented")
    }

    override suspend fun recall(source: MessageSource) {
        TODO("Not yet implemented")
    }

    override suspend fun recall(record: GroupTodoRecord) {
        TODO("Not yet implemented")
    }

    override suspend fun complete(source: MessageSource) {
        TODO("Not yet implemented")
    }

    override suspend fun complete(record: GroupTodoRecord) {
        TODO("Not yet implemented")
    }

    override suspend fun close(source: MessageSource) {
        TODO("Not yet implemented")
    }

    override suspend fun close(record: GroupTodoRecord) {
        TODO("Not yet implemented")
    }
}