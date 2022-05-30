/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.message.data

import kotlin.test.Test
import kotlin.test.assertIs

internal class MessageChainImplTest {
    @OptIn(MessageChainConstructor::class)
    @Test
    fun allInternalImplementationsOfMessageChainAreMessageChainImpl() {
        assertIs<AbstractMessageChain>(CombinedMessage(AtAll, AtAll, false))
        assertIs<AbstractMessageChain>(emptyMessageChain())
        val linear = LinearMessageChainImpl.create(listOf(AtAll), true)
        assertIs<LinearMessageChainImpl>(linear)
        assertIs<AbstractMessageChain>(linear)
    }
}