/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("MessageUtils")
@file:Suppress("NOTHING_TO_INLINE")

package net.mamoe.mirai.message.data

import net.mamoe.mirai.utils.PlannedRemoval
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic

/**
 * 纯文本. 可含 emoji 表情如 😊.
 *
 * 一般不需要主动构造 [PlainText], [Message] 可直接与 [String] 相加. Java 用户请使用 [Message.plus]
 */
public data class PlainText(
    public val content: String
) : MessageContent {

    @PlannedRemoval("1.2.0")
    @Deprecated(
        "use content instead for clearer semantics",
        level = DeprecationLevel.ERROR,
        replaceWith = ReplaceWith("content")
    )
    public val stringValue: String
        get() = content

    @Suppress("unused")
    public constructor(charSequence: CharSequence) : this(charSequence.toString())

    public override fun toString(): String = content
    public override fun contentToString(): String = content

    public companion object Key : Message.Key<PlainText> {
        public override val typeName: String get() = "PlainText"
    }
}

/**
 * 构造 [PlainText]
 */
@Deprecated(
    "为和 mirai code 区分, 请使用 PlainText(this)",
    ReplaceWith("PlainText(this)", "net.mamoe.mirai.message.data.PlainText"),
    level = DeprecationLevel.WARNING
)
@JvmSynthetic
@Suppress("NOTHING_TO_INLINE")
public inline fun String.toMessage(): PlainText = PlainText(this)