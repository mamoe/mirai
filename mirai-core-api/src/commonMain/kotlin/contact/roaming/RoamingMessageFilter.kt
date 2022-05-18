/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.contact.roaming

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.MessageSource
import kotlin.jvm.JvmField

/**
 * @since 2.8
 */
public fun interface RoamingMessageFilter {
    public operator fun invoke(roamingMessage: RoamingMessage): Boolean


    public infix fun and(other: RoamingMessageFilter): RoamingMessageFilter {
        return RoamingMessageFilter { this.invoke(it) && other.invoke(it) }
    }

    public infix fun or(other: RoamingMessageFilter): RoamingMessageFilter {
        return RoamingMessageFilter { this.invoke(it) || other.invoke(it) }
    }

    public fun not(): RoamingMessageFilter {
        return RoamingMessageFilter { !this.invoke(it) }
    }


    public companion object {
        /**
         * 筛选任何消息 (相当于不筛选)
         */
        @JvmField
        public val ANY: RoamingMessageFilter = RoamingMessageFilter { true }

        /**
         * 筛选 bot 接收的消息
         */
        @JvmField
        public val RECEIVED: RoamingMessageFilter = RoamingMessageFilter { it.sender != it.bot.id }

        /**
         * 筛选 bot 发送的消息
         */
        @JvmField
        public val SENT: RoamingMessageFilter = RoamingMessageFilter { it.sender == it.bot.id }
    }
}

/**
 * 还未解析的漫游消息.
 *
 * @since 2.8
 */
public interface RoamingMessage {
    public val contact: Contact
    public val bot: Bot get() = contact.bot

    /**
     * 发送人 id
     */
    public val sender: Long

    /**
     * 收信人或群的 id
     */
    public val target: Long

    /**
     * 时间戳, 单位为秒, 服务器时间.
     */
    public val time: Long

    /**
     * @see MessageSource.ids
     */
    public val ids: IntArray

    /**
     * @see MessageSource.internalIds
     */
    public val internalIds: IntArray
}