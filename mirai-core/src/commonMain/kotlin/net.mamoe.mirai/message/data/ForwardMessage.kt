/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("MemberVisibilityCanBePrivate")

package net.mamoe.mirai.message.data

import net.mamoe.mirai.utils.MiraiExperimentalAPI
import net.mamoe.mirai.utils.SinceMirai


/**
 * 合并转发
 */
@SinceMirai("0.39.0")
class ForwardMessage(
    val messageList: Collection<MessageChain>
) : MessageContent {
    companion object Key : Message.Key<ForwardMessage> {
        override val typeName: String get() = "ForwardMessage"
    }

    override fun toString(): String = "[mirai:forward:$messageList]"


    private val contentToString: String by lazy {
        messageList.joinToString("\n")
    }

    @MiraiExperimentalAPI
    override fun contentToString(): String = contentToString

    override val length: Int
        get() = contentToString.length

    override fun get(index: Int): Char = contentToString[length]

    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence =
        contentToString.subSequence(startIndex, endIndex)

    override fun compareTo(other: String): Int = contentToString.compareTo(other)
}