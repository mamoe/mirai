/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.message

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.SingleMessage

/**
 * 在接收解析消息后会经过一层转换的消息.
 * @see MessageChain.refine
 */
internal interface RefinableMessage : SingleMessage {

    /**
     * Refine if possible (without suspension), returns self otherwise.
     * @since 2.6
     */ // see #1157
    fun tryRefine(
        contact: Contact,
        context: MessageChain,
    ): Message? = this

    /**
     * This message [RefinableMessage] will be replaced by return value of [refine]
     */
    suspend fun refine(
        contact: Contact,
        context: MessageChain,
    ): Message? = tryRefine(contact, context)
}