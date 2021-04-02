/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("MessageUtils")
@file:Suppress("MemberVisibilityCanBePrivate")

package net.mamoe.mirai.message.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.mamoe.mirai.message.code.CodableMessage
import net.mamoe.mirai.utils.MiraiExperimentalApi

/**
 * "@全体成员".
 *
 * 非会员每天只能发送 10 次 [AtAll]. 超出部分会被以普通文字看待.
 *
 * ## 使用 [AtAll]
 *
 * [AtAll] 是单例, 将 [AtAll] 实例[添加][Message.plus]到消息链中即可.
 * ```
 * // Kotlin
 * contact.sendMessage(AtAll + "test")
 *
 * // Java
 * contact.sendMessage(MessageUtils.newChain(AtAll.INSTANCE, new PlainText("test")));
 * ```
 *
 * ## mirai 码支持
 * 格式: &#91;mirai:atall&#93;
 *
 * @see At at 单个群成员
 */
@SerialName(AtAll.SERIAL_NAME)
@Serializable
public object AtAll :
    MessageContent, CodableMessage {
    public const val display: String = "@全体成员"
    public const val SERIAL_NAME: String = "AtAll"

    @Suppress("SpellCheckingInspection")
    override fun contentToString(): String = display
    override fun toString(): String = "[mirai:atall]"
    override fun serializeToMiraiCode(): String = toString()

    override fun hashCode(): Int = display.hashCode()
    override fun equals(other: Any?): Boolean = other === this

    @MiraiExperimentalApi
    override fun appendMiraiCodeTo(builder: StringBuilder) {
        builder.append(toString())
    }


    // 自动为消息补充 " "
    @JvmSynthetic
    public override fun followedBy(tail: Message): MessageChain {
        if (tail is PlainText && tail.content.startsWith(' ')) {
            return super<MessageContent>.followedBy(tail)
        }
        return super<MessageContent>.followedBy(PlainText(" ")) + tail
    }
}