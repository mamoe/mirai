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

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.internal.message.visitor.ex
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.visitor.MessageVisitor
import net.mamoe.mirai.utils.safeCast

/**
 * Ignore on transformation
 */
internal sealed interface InternalFlagOnlyMessage : MessageMetadata

internal sealed interface ForceAs : InternalFlagOnlyMessage, ConstrainSingle {
    companion object Key : AbstractMessageKey<ForceAs>({ it.safeCast() })
}

/**
 * 内部 flag, 放入 chain 强制作为 long 发送
 */
internal object ForceAsLongMessage : ForceAs,
    AbstractPolymorphicMessageKey<ForceAs, ForceAsLongMessage>(ForceAs, { it.safeCast() }) {
    override val key: MessageKey<ForceAsLongMessage> get() = this

    override fun toString(): String = ""

    override fun <D, R> accept(visitor: MessageVisitor<D, R>, data: D): R {
        return visitor.ex()?.visitForceAsLongMessage(this, data) ?: super.accept(visitor, data)
    }
}

/**
 * 内部 flag, 放入 chain 强制作为 fragmented 发送
 */
internal object ForceAsFragmentedMessage : ForceAs,
    AbstractPolymorphicMessageKey<ForceAs, ForceAsFragmentedMessage>(ForceAs, { it.safeCast() }) {
    override val key: MessageKey<ForceAsFragmentedMessage> get() = this

    override fun toString(): String = ""

    override fun <D, R> accept(visitor: MessageVisitor<D, R>, data: D): R {
        return visitor.ex()?.visitForceAsFragmentedMessage(this, data) ?: super.accept(visitor, data)
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
 * Skip broadcasting events. Used for [Contact.sendMessage]
 */
internal sealed interface SkipEventBroadcast : MessageMetadata, ConstrainSingle, InternalFlagOnlyMessage {
    override val key: MessageKey<SkipEventBroadcast> get() = Companion

    override fun <D, R> accept(visitor: MessageVisitor<D, R>, data: D): R {
        return visitor.ex()?.visitSkipEventBroadcast(this, data) ?: super<InternalFlagOnlyMessage>.accept(
            visitor,
            data
        )
    }

    companion object : AbstractMessageKey<SkipEventBroadcast>({ it.safeCast() }), SkipEventBroadcast {
        // instance

        override fun toString(): String = ""
    }
}

internal sealed interface AllowSendFileMessage : MessageMetadata, ConstrainSingle, InternalFlagOnlyMessage,
    SkipEventBroadcast {
    override val key: MessageKey<AllowSendFileMessage> get() = Companion

    override fun <D, R> accept(visitor: MessageVisitor<D, R>, data: D): R {
        return visitor.ex()?.visitAllowSendFileMessage(this, data) ?: super<InternalFlagOnlyMessage>.accept(
            visitor,
            data
        )
    }

    companion object : AbstractMessageKey<AllowSendFileMessage>({ it.safeCast() }), AllowSendFileMessage {
        override fun toString(): String = ""
    }
}