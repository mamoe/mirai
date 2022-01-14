/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("unused")

package net.mamoe.mirai.internal.message.flags

import net.mamoe.mirai.internal.message.visitor.ex
import net.mamoe.mirai.message.data.AbstractMessageKey
import net.mamoe.mirai.message.data.ConstrainSingle
import net.mamoe.mirai.message.data.MessageKey
import net.mamoe.mirai.message.data.MessageMetadata
import net.mamoe.mirai.message.data.visitor.MessageVisitor
import net.mamoe.mirai.utils.safeCast

/**
 * Ignore on transformation
 */
internal sealed interface InternalFlagOnlyMessage : MessageMetadata

/**
 * 内部 flag, 放入 chain 强制作为 long 发送
 */
internal object ForceAsLongMessage : MessageMetadata, ConstrainSingle, InternalFlagOnlyMessage,
    AbstractMessageKey<ForceAsLongMessage>({ it.safeCast() }) {
    override val key: MessageKey<ForceAsLongMessage> get() = this

    override fun toString(): String = ""

    override fun <D, R> accept(visitor: MessageVisitor<D, R>, data: D): R {
        return visitor.ex()?.visitForceAsLongMessage(this, data) ?: super<InternalFlagOnlyMessage>.accept(visitor, data)
    }
}

/**
 * 强制不发 long
 */
internal object DontAsLongMessage : MessageMetadata, ConstrainSingle, InternalFlagOnlyMessage,
    AbstractMessageKey<DontAsLongMessage>({ it.safeCast() }) {
    override val key: MessageKey<DontAsLongMessage> get() = this

    override fun toString(): String = ""

    override fun <D, R> accept(visitor: MessageVisitor<D, R>, data: D): R {
        return visitor.ex()?.visitDontAsLongMessage(this, data) ?: super<InternalFlagOnlyMessage>.accept(visitor, data)
    }
}

internal object IgnoreLengthCheck : MessageMetadata, ConstrainSingle, InternalFlagOnlyMessage,
    AbstractMessageKey<IgnoreLengthCheck>({ it.safeCast() }) {
    override val key: MessageKey<IgnoreLengthCheck> get() = this

    override fun toString(): String = ""

    override fun <D, R> accept(visitor: MessageVisitor<D, R>, data: D): R {
        return visitor.ex()?.visitIgnoreLengthCheck(this, data) ?: super<InternalFlagOnlyMessage>.accept(visitor, data)
    }
}

/**
 * 代表来自 mirai 内部
 */
internal object MiraiInternalMessageFlag : MessageMetadata, ConstrainSingle, InternalFlagOnlyMessage,
    AbstractMessageKey<MiraiInternalMessageFlag>({ it.safeCast() }) {
    override val key: MessageKey<MiraiInternalMessageFlag> get() = this
    override fun toString(): String = ""

    override fun <D, R> accept(visitor: MessageVisitor<D, R>, data: D): R {
        return visitor.ex()?.visitMiraiInternalMessageFlag(this, data) ?: super<InternalFlagOnlyMessage>.accept(
            visitor,
            data
        )
    }
}