/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */


package net.mamoe.mirai.internal.message.visitor

import net.mamoe.mirai.internal.message.data.ForwardMessageInternal
import net.mamoe.mirai.internal.message.data.LongMessageInternal
import net.mamoe.mirai.internal.message.data.MarketFaceImpl
import net.mamoe.mirai.internal.message.flags.*
import net.mamoe.mirai.internal.message.source.MessageSourceInternal
import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.message.data.visitor.MessageVisitor
import net.mamoe.mirai.utils.castOrNull

internal fun <D, R> MessageVisitor<D, R>?.ex(): MessageVisitorEx<D, R>? = castOrNull()

/**
 * For mirai-core-specific types
 */
internal interface MessageVisitorEx<in D, out R> : MessageVisitor<D, R> {
    fun <T> visitMessageSourceInternal(message: T, data: D): R where T : MessageSourceInternal, T : MessageSource {
        return visitMessageSource(message, data)
    }

    fun visitForwardMessageInternal(message: ForwardMessageInternal, data: D): R {
        return visitAbstractServiceMessage(message, data)
    }

    fun visitLongMessageInternal(message: LongMessageInternal, data: D): R {
        return visitAbstractServiceMessage(message, data)
    }

    fun visitMarketFaceImpl(message: MarketFaceImpl, data: D): R {
        return visitMarketFace(message, data)
    }

    fun visitInternalFlagOnlyMessage(message: InternalFlagOnlyMessage, data: D): R {
        return visitMessageMetadata(message, data)
    }

    fun visitForceAsLongMessage(message: ForceAsLongMessage, data: D): R {
        return visitInternalFlagOnlyMessage(message, data)
    }

    fun visitForceAsFragmentedMessage(message: ForceAsFragmentedMessage, data: D): R {
        return visitInternalFlagOnlyMessage(message, data)
    }

    fun visitDontAsLongMessage(message: DontAsLongMessage, data: D): R {
        return visitInternalFlagOnlyMessage(message, data)
    }

    fun visitIgnoreLengthCheck(message: IgnoreLengthCheck, data: D): R {
        return visitInternalFlagOnlyMessage(message, data)
    }

    fun visitSkipEventBroadcast(message: SkipEventBroadcast, data: D): R {
        return visitInternalFlagOnlyMessage(message, data)
    }

    fun visitAllowSendFileMessage(message: AllowSendFileMessage, data: D): R {
        return visitSkipEventBroadcast(message, data)
    }
}