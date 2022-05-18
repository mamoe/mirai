/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.impl.common

import net.mamoe.mirai.internal.network.components.ServerList
import net.mamoe.mirai.internal.network.framework.AbstractCommonNHTest
import net.mamoe.mirai.internal.network.framework.TestCommonNetworkHandler
import net.mamoe.mirai.internal.network.handler.NetworkHandler
import net.mamoe.mirai.internal.test.runBlockingUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

internal class CommonNHAddressChangedTest : AbstractCommonNHTest() {
    @Test
    fun `test login ip changes`() = runBlockingUnit {
        networkLogger.debug("before login, Assuming both ip is empty")
        val lastConnectedIpOld = bot.components[ServerList].lastConnectedIP
        val lastDisconnectedIpOld = bot.components[ServerList].lastDisconnectedIP
        assertTrue(lastConnectedIpOld.isEmpty(), "Assuming lastConnectedIp is empty")
        assertTrue(lastDisconnectedIpOld.isEmpty(), "Assuming lastDisconnectedIp is empty")

        networkLogger.debug("Do login, Assuming lastConnectedIp is NOT empty")
        bot.login()
        assertState(NetworkHandler.State.OK)
        assertNotEquals(
            lastConnectedIpOld,
            bot.components[ServerList].lastConnectedIP,
            "Assuming lastConnectedIp is NOT empty"
        )

        networkLogger.debug("Offline the bot, Assuming lastConnectedIp is equals lastDisconnectedIp")
        (bot.network as TestCommonNetworkHandler).setStateClosed()
        assertState(NetworkHandler.State.CLOSED)
        assertEquals(
            bot.components[ServerList].lastConnectedIP,
            bot.components[ServerList].lastDisconnectedIP,
            "Assuming lastConnectedIp is equals lastDisconnectedIp"
        )
    }
}
