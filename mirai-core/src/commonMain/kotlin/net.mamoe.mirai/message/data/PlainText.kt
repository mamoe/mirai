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
import kotlin.jvm.JvmSynthetic

/**
 * 纯文本. 可含 emoji 表情.
 *
 * 一般不需要主动构造 [PlainText], [Message] 可直接与 [String] 相加. Java 用户请使用 [MessageChain.plus]
 */
class PlainText(val stringValue: String) :
    MessageContent,
    Comparable<String> by stringValue,
    CharSequence by stringValue {

    @Suppress("unused")
    constructor(charSequence: CharSequence) : this(charSequence.toString())

    override fun toString(): String = stringValue
    override fun contentToString(): String = stringValue

    override fun equals(other: Any?): Boolean {
        return other is PlainText && other.stringValue == this.stringValue
    }

    override fun hashCode(): Int {
        return stringValue.hashCode()
    }

    companion object Key : Message.Key<PlainText> {
        override val typeName: String
            get() = "PlainText"
    }
}

/**
 * 构造 [PlainText]
 */
@JvmSynthetic
@Suppress("NOTHING_TO_INLINE")
inline fun String.toMessage(): PlainText = PlainText(this)