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
@file:Suppress("NOTHING_TO_INLINE")

package net.mamoe.mirai.message.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.mamoe.mirai.message.code.CodableMessage
import net.mamoe.mirai.message.code.internal.appendStringAsMiraiCode
import net.mamoe.mirai.utils.MiraiExperimentalApi

/**
 * 纯文本.
 *
 * 使用时直接构造即可. [Message] 也可以直接与 [String] 相加, 详见 [Message.plus].
 *
 * ## mirai 码支持
 * 将 [content] 转义. 而没有 `[mirai:`.
 *
 * @see String.toPlainText
 */
@Serializable
@SerialName(PlainText.SERIAL_NAME)
public data class PlainText(
    /**
     * 消息内容
     */
    public val content: String
) : MessageContent, CodableMessage {
    @Suppress("unused")
    public constructor(charSequence: CharSequence) : this(charSequence.toString())

    public override fun toString(): String = content
    public override fun contentToString(): String = content

    @MiraiExperimentalApi
    override fun appendMiraiCodeTo(builder: StringBuilder) {
        builder.appendStringAsMiraiCode(content)
    }

    public companion object {
        public const val SERIAL_NAME: String = "PlainText"
    }
}

/**
 * 构造 [PlainText]
 */
@JvmSynthetic
@Suppress("NOTHING_TO_INLINE")
public inline fun String.toPlainText(): PlainText = PlainText(this)