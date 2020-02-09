/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.message.data

/**
 * 消息源, 用于被引用. 它将由协议模块实现为 `MessageSourceImpl`
 */
interface MessageSource : Message {
    companion object : Message.Key<MessageSource>

    /* 以下属性均无 backing field, 即都是以 `get() =` 实现 */

    /**
     * 原消息序列号
     */
    val originalSeq: Int

    /**
     * 发送人 id
     */
    val senderId: Long

    /**
     * 群 id
     */
    val groupId: Long

    /**
     * in seconds
     */
    val time: Int

    /**
     * 固定返回空字符串 ("")
     */
    override fun toString(): String
}