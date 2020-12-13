/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("MessageUtils")
@file:Suppress("NOTHING_TO_INLINE")

package net.mamoe.mirai.message.data

import kotlinx.serialization.Serializable
import net.mamoe.mirai.message.code.CodableMessage
import net.mamoe.mirai.message.code.internal.appendAsMiraiCode

/**
 * 纯文本. 可含 emoji 表情如 😊.
 *
 * 一般不需要主动构造 [PlainText], [Message] 可直接与 [String] 相加. Java 用户请使用 [Message.plus]
 */
@Serializable
public data class PlainText(
    public val content: String
) : MessageContent, CodableMessage {
    @Suppress("unused")
    public constructor(charSequence: CharSequence) : this(charSequence.toString())

    public override fun toString(): String = content
    public override fun contentToString(): String = content

    override fun appendMiraiCode(builder: StringBuilder) {
        builder.appendAsMiraiCode(content)
    }

    public companion object
}

/**
 * 构造 [PlainText]
 */
@JvmSynthetic
@Suppress("NOTHING_TO_INLINE")
public inline fun String.toPlainText(): PlainText = PlainText(this)