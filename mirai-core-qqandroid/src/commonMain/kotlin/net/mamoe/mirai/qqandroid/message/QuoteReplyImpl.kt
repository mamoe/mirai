/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid.message

import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.message.data.QuoteReply

class QuoteReplyImpl(override val source: MessageSource) : QuoteReply {
    override fun toString(): kotlin.String = ""
}