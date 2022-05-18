/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN", "UNCHECKED_CAST")

package net.mamoe.mirai.message.data

import kotlin.test.Test
import kotlin.test.assertFails
import java.util.List as JdkList

internal open class MessageChainImmutableTest {
    private fun msg0(): MessageChain = messageChainOf(
        AtAll, PlainText("Hello!"), At(114514),
    )

    fun msgAsJdk(): JdkList<SingleMessage> {
        return msg0() as java.util.List<SingleMessage>
    }

    @Test
    fun `direct access`() {
        val chain = msgAsJdk()

        assertFails { chain.set(0, AtAll) }
        assertFails { chain.remove(0) }
        assertFails { chain.clear() }
        assertFails { chain.add(PlainText("Hey Hey!")) }
    }

    @Test
    fun `iterator access`() {
        val chain = msgAsJdk()
        assertFails { chain.iterator().remove() }
        assertFails { chain.iterator().also { it.next() }.remove() }
        assertFails { chain.listIterator().remove() }
        assertFails { chain.listIterator().also { it.next() }.remove() }
    }
}
