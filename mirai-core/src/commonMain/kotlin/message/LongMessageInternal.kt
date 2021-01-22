/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.message

import net.mamoe.mirai.message.data.AbstractPolymorphicMessageKey
import net.mamoe.mirai.message.data.AbstractServiceMessage
import net.mamoe.mirai.message.data.ServiceMessage
import net.mamoe.mirai.utils.safeCast

// internal runtime value, not serializable
internal data class LongMessageInternal internal constructor(override val content: String, val resId: String) :
    AbstractServiceMessage() {
    override val serviceId: Int get() = 35

    companion object Key :
        AbstractPolymorphicMessageKey<ServiceMessage, LongMessageInternal>(ServiceMessage, { it.safeCast() })
}

// internal runtime value, not serializable
internal data class ForwardMessageInternal(override val content: String) : AbstractServiceMessage() {
    override val serviceId: Int get() = 35

    companion object Key :
        AbstractPolymorphicMessageKey<ServiceMessage, ForwardMessageInternal>(ServiceMessage, { it.safeCast() })
}