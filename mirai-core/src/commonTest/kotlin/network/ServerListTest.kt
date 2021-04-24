/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network

import net.mamoe.mirai.internal.network.components.ServerAddress
import net.mamoe.mirai.internal.network.components.ServerList
import net.mamoe.mirai.internal.network.components.ServerListImpl
import net.mamoe.mirai.internal.test.AbstractTest
import kotlin.test.*

internal class ServerListTest : AbstractTest() {

    @Test
    fun canInitializeDefaults() {
        assertNotEquals(0, ServerList.DEFAULT_SERVER_LIST.size)
    }

    @Test
    fun `can poll current for initial`() {
        assertNotNull(ServerListImpl().pollCurrent())
    }

    @Test
    fun `not empty for initial`() {
        assertNotNull(ServerListImpl().pollAny())
    }

    @Test
    fun `poll current will end with null`() {
        val instance = ServerListImpl()
        repeat(100) {
            instance.pollCurrent()
        }
        assertNull(instance.pollCurrent())
    }

    @Test
    fun `poll any is always not null`() {
        val instance = ServerListImpl()
        repeat(100) {
            instance.pollAny()
        }
        assertNotNull(instance.pollAny())
    }

    @Test
    fun `preferred cannot be empty`() {
        assertFailsWith<IllegalArgumentException> {
            ServerListImpl().setPreferred(emptyList())
        }
    }

    @Test
    fun `use preferred`() {
        val instance = ServerListImpl()
        val addr = ServerAddress("test", 1)
        instance.setPreferred(listOf(addr))
        repeat(100) {
            instance.pollAny()
        }
        assertSame(addr, instance.pollAny())
    }
}