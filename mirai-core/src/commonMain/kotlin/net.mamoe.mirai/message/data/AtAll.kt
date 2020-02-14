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
 * "@全体成员"
 *
 * @see At at 单个群成员
 */
object AtAll : Message, Message.Key<AtAll> {
    override fun toString(): String = "@全体成员"

    // 自动为消息补充 " "

    override fun followedBy(tail: Message): MessageChain {
        if(tail is PlainText && tail.stringValue.startsWith(' ')){
            return super.followedBy(tail)
        }
        return super.followedBy(PlainText(" ")) + tail
    }
}