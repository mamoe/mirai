/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.protocol.packet.login

import net.mamoe.mirai.internal.test.runBlockingUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IpConversionTest {
    @Test
    fun `test bad ipAddress`() = runBlockingUnit {
        assertEquals(-2, "some^ting%bad".toIpV4Long())
        assertEquals(-2, "another_bad".toIpV4Long())
        assertEquals(-2, " ".toIpV4Long())
        assertEquals(-2, "w.a.c.d".toIpV4Long())
        assertEquals(-2, "the..anotherbad......".toIpV4Long())
        assertEquals(-2, "错误的IP地址".toIpV4Long())
    }

    @Test
    fun `test empty ipAddress`() = runBlockingUnit {
        assertTrue("".toIpV4Long() == 0L)
    }

    @Test
    fun `test good ipAddress`() = runBlockingUnit {
        assertTrue("www.baidu.com".toIpV4Long() > 0)
        assertTrue("www.qq.com".toIpV4Long() > 0)
        assertTrue("www.sohu.com".toIpV4Long() > 0)
        assertTrue("www.weibo.com".toIpV4Long() > 0)
    }

    @Test
    fun `test plain ipAddress`() = runBlockingUnit {
        assertEquals(16885952L, "192.168.1.1".toIpV4Long())
        assertEquals(4294967295L, "255.255.255.255".toIpV4Long())
        assertEquals(0L, "0.0.0.0".toIpV4Long())
        assertEquals(1869573999L, "111.111.111.111".toIpV4Long())
    }
}