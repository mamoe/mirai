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

internal class MessageChainImmutableTestJdk8 : MessageChainImmutableTest() {
    @Test
    fun `access with JDK8 lambda`() {
        val chain = messageChainOf(AtAll, PlainText("Hello!"), At(114514)) as java.util.List<SingleMessage>
        assertFails { chain.removeIf { true } }
        assertFails { chain.replaceAll { AtAll } }
        assertFails { chain.sort { o1, o2 -> o1.javaClass.name.compareTo(o2.javaClass.name) } }
        assertFails { chain.clear() }
    }
}