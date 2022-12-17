/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.mock.database

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.roaming.RoamingMessageFilter
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.mock.MockBot
import net.mamoe.mirai.mock.internal.db.MsgDatabaseImpl
import net.mamoe.mirai.utils.concatAsLong

/**
 * 一个消息数据库
 *
 * 该数据库用于存储发送者, 发送目标, 发送类型 等数据,
 * 用于支持 撤回/消息获取 等相关的功能的实现
 *
 * 一般在测试结束后销毁整个数据库
 */
public interface MessageDatabase {
    /**
     * implementation note: 该方法可能同时被多个线程同时调用
     *
     * @param time 单位秒
     */
    public fun newMessageInfo(
        sender: Long, subject: Long, kind: MessageSourceKind,
        time: Long,
        message: MessageChain,
    ): MessageInfo

    public fun queryMessageInfo(msgId: Long): MessageInfo?

    public fun queryMessageInfosBy(
        subject: Long, kind: MessageSourceKind,
        contact: Contact,
        timeStart: Long,
        timeEnd: Long,
        filter: RoamingMessageFilter
    ): Sequence<MessageInfo>

    /**
     * implementation note: 该方法可能同时被多个线程同时调用
     */
    public fun removeMessageInfo(msgId: Long)

    /**
     * 断开与数据库的连接, 在 [MockBot.close] 时会自动调用
     */
    public fun disconnect()

    /**
     * 建立与数据库的连接, 在 [MockBot] 构造后马上调用,
     * 抛出任何错误都会中断 [MockBot] 的初始化
     */
    public fun connect()

    public companion object {
        @JvmStatic
        public fun newDefaultDatabase(): MessageDatabase {
            return MsgDatabaseImpl()
        }
    }
}

public data class MessageInfo(
    public val mixinedMsgId: Long,
    public val sender: Long,
    public val subject: Long,
    public val kind: MessageSourceKind,
    public val time: Long, // seconds
    public val message: MessageChain,
) {
    public fun buildSource(bot: MockBot): MessageSource {
        return bot.buildMessageSource(kind = kind) {
            val info = this@MessageInfo
            sender(info.sender)
            time(info.time.toInt())

            if (kind == MessageSourceKind.GROUP) {
                target(subject)
            } else {
                if (info.sender == info.subject) {
                    target(bot.id)
                } else {
                    target(info.subject)
                }
            }

            ids = intArrayOf(info.id)
            internalIds = intArrayOf(info.internal)

            messages(info.message as Iterable<Message>)
        }
    }

    // ids
    public val id: Int get() = (mixinedMsgId shr 32).toInt()

    // internalIds
    public val internal: Int get() = mixinedMsgId.toInt()
}

public fun mockMsgDatabaseId(id: Int, internalId: Int): Long {
    return id.concatAsLong(internalId)
}

public fun MessageDatabase.removeMessageInfo(id: Int, internalId: Int) {
    removeMessageInfo(mockMsgDatabaseId(id, internalId))
}

public fun MessageDatabase.queryMessageInfo(ids: IntArray, internalIds: IntArray): MessageInfo? {
    return queryMessageInfo(mockMsgDatabaseId(ids[0], internalIds[0]))
}

public fun MessageDatabase.removeMessageInfo(source: MessageSource) {
    removeMessageInfo(source.ids[0], source.internalIds[0])
}
