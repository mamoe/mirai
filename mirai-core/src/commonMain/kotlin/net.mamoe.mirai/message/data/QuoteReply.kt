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
 * 群内的引用回复. 它将由协议模块实现为 `QuoteReplyImpl`
 */
interface QuoteReply : Message {
    val source: MessageSource

    companion object Key : Message.Key<QuoteReply>
}