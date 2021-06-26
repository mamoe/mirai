package net.mamoe.mirai.internal.network.impl.netty

import net.mamoe.mirai.internal.network.handler.NetworkHandler
import net.mamoe.mirai.internal.test.runBlockingUnit
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

internal class NettyAddressChangedTest : AbstractNettyNHTest() {
    @Test
    fun `test login ip changes`() = runBlockingUnit {
        networkLogger.debug("before login, Assuming both ip is empty")
        val lastConnectedIpOld = bot.client.lastConnectedIp
        val lastDisconnectedIpOld = bot.client.lastDisconnectedIp
        assert(lastConnectedIpOld.isEmpty()) { "Assuming lastConnectedIp is empty" }
        assert(lastDisconnectedIpOld.isEmpty()) { "Assuming lastDisconnectedIp is empty" }

        networkLogger.debug("Do login, Assuming lastConnectedIp is NOT empty")
        bot.login()
        assertState(NetworkHandler.State.OK)
        assertNotEquals(lastConnectedIpOld, bot.client.lastConnectedIp, "Assuming lastConnectedIp is NOT empty")

        networkLogger.debug("Offline the bot, Assuming lastConnectedIp is equals lastDisconnectedIp")
        (bot.network as TestNettyNH).setStateClosed()
        assertState(NetworkHandler.State.CLOSED)
        assertEquals(
            bot.client.lastConnectedIp,
            bot.client.lastDisconnectedIp,
            "Assuming lastConnectedIp is equals lastDisconnectedIp"
        )
    }
}
