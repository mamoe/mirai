/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.contact.todo

import me.him188.kotlin.jvm.blocking.bridge.JvmBlockingBridge
import net.mamoe.mirai.event.events.MemberCompleteTodoEvent
import net.mamoe.mirai.event.events.MemberRecallTodoEvent
import net.mamoe.mirai.event.events.MemberSetTodoEvent
import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.utils.NotStableForInheritance

/**
 * 表示一个群待办管理.
 *
 * ## 获取 [GroupTodo] 实例
 *
 * 只可以通过 [Group.todo] 获取一个群的精华消息管理, 即 [GroupTodo] 实例.
 *
 * ### 获取当前群待办内容
 *
 * 通过 [current] 可以获得当前群待办内容
 *
 * ### 获取当前群待办状态
 *
 * 通过 [status] 可以获得当前群待办内容
 *
 * ### 操作群待办
 *
 * 通过 [set] 可以设置新的群待办
 *
 * 通过 [close] 可以关闭群待办
 *
 * 通过 [complete] 可以完成群待办
 *
 * 通过 [recall] 可以撤销群待办
 *
 * @since 2.16
 */
@NotStableForInheritance
@JvmBlockingBridge
public interface GroupTodo {

    public suspend fun status(): GroupTodoStatus

    public suspend fun current(): GroupTodoRecord?

    /**
     * @see MemberSetTodoEvent
     */
    public suspend fun set(source: MessageSource): GroupTodoRecord

    /**
     * @see MemberRecallTodoEvent
     */
    public suspend fun recall(source: MessageSource)

    /**
     * @see MemberRecallTodoEvent
     */
    public suspend fun recall(record: GroupTodoRecord)

    /**
     * @see MemberCompleteTodoEvent
     */
    public suspend fun complete(source: MessageSource)

    /**
     * @see MemberCompleteTodoEvent
     */
    public suspend fun complete(record: GroupTodoRecord)

    /**
     * @see MemberSetTodoEvent
     */
    public suspend fun close(source: MessageSource)

    /**
     * @see MemberSetTodoEvent
     */
    public suspend fun close(record: GroupTodoRecord)
}