/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("MessageUtils")
@file:Suppress("NOTHING_TO_INLINE")

package net.mamoe.mirai.message.data

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmStatic
import kotlin.jvm.JvmSynthetic

/**
 * 纯文本. 可含 emoji 表情.
 *
 * 一般不需要主动构造 [PlainText], [Message] 可直接与 [String] 相加. Java 用户请使用 [MessageChain.plus]
 */
inline class PlainText(val stringValue: String) : Message, MessageContent {
    constructor(charSequence: CharSequence) : this(charSequence.toString())

    override operator fun contains(sub: String): Boolean = sub in stringValue
    override fun toString(): String = stringValue

    companion object Key : Message.Key<PlainText> {
        @JvmStatic
        val Empty = PlainText("")

        @JvmStatic
        val Null = PlainText("null")

        inline fun of(value: String): PlainText {
            return PlainText(value)
        }

        inline fun of(value: CharSequence): PlainText {
            return PlainText(value)
        }
    }
}

/**
 * 构造 [PlainText]
 */
@JvmSynthetic
@Suppress("NOTHING_TO_INLINE")
inline fun String.toMessage(): PlainText = PlainText(this)