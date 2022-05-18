/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("MessageUtils")

package net.mamoe.mirai.message.data

import java.util.stream.Stream
import kotlin.streams.asSequence


/**
 * 扁平化 [this] 并创建一个 [MessageChain].
 */
@JvmName("newChain")
public fun Stream<Message>.toMessageChain(): MessageChain = this.asSequence().toMessageChain()


