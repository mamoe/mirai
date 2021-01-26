/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.message

import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.safeCast

/**
 * 内部 flag, 放入 chain 强制作为 long 发送
 */
internal object ForceAsLongMessage : MessageMetadata, ConstrainSingle, InternalFlagOnlyMessage,
    AbstractMessageKey<ForceAsLongMessage>({ it.safeCast() }) {
    override val key: MessageKey<ForceAsLongMessage> get() = this

    override fun toString(): String = "ForceLongMessage"
}

/**
 * Ignore on transformation
 */
internal interface InternalFlagOnlyMessage : SingleMessage