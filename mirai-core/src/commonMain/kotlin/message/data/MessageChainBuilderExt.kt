/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message.data

import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageChainBuilder
import net.mamoe.mirai.message.data.SingleMessage
import net.mamoe.mirai.message.data.toMessageChain
import net.mamoe.mirai.message.data.visitor.MessageVisitor
import net.mamoe.mirai.message.data.visitor.MessageVisitorUnit
import net.mamoe.mirai.message.data.visitor.accept
import net.mamoe.mirai.utils.replaceAllKotlin


internal fun MessageChainBuilder.acceptChildren(visitor: MessageVisitorUnit) {
    forEach { it.accept(visitor) }
}

internal fun MessageChainBuilder.transformChildren(visitor: MessageVisitor<MessageChainBuilder, SingleMessage>) {
    replaceAllKotlin { it.accept(visitor, this) }
}

internal inline fun MessageChain.transform(trans: (SingleMessage) -> SingleMessage?): MessageChain {
    return this.mapNotNull(trans).toMessageChain()
}