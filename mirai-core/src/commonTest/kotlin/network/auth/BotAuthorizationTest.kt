/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.auth

import kotlinx.coroutines.test.runTest
import net.mamoe.mirai.internal.network.protocol.data.jce.SvcRespRegister
import net.mamoe.mirai.internal.network.protocol.packet.login.StatSvc
import net.mamoe.mirai.internal.network.protocol.packet.login.WtLogin
import net.mamoe.mirai.network.BotAuthorizationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

internal class BotAuthorizationTest : AbstractBotAuthTest() {
    @Test
    fun `authorization failure throws BotAuthorizationException`() = runTest {
        // Run a real SsoProcessor, just without sending packets
        setAuthorization { _, _ ->
            throw IllegalStateException("Oops")
        }

        usePacketReplierThroughout {
            expect(WtLogin.ExchangeEmp) reply { WtLogin.Login.LoginPacketResponse.Error(bot, 1, "", "", "") }
            expect(WtLogin.Login) reply { WtLogin.Login.LoginPacketResponse.Success(bot) }
            expect(StatSvc.Register) reply { StatSvc.Register.Response(SvcRespRegister()) }
        }

        assertFailsWith<BotAuthorizationException> {
            bot.login()
        }.run {
            val cause = cause
            assertIs<IllegalStateException>(cause)
            assertEquals("Oops", cause.message)
        }

        // Stacktrace like this:

        //net.mamoe.mirai.network.BotAuthorizationException: BotAuthorization(net.mamoe.mirai.internal.network.auth.AbstractBotAuthTest$authorization failure throws BotAuthorizationException$1$2@157c6b0f) threw an exception during authorization process. See cause below.
        //	at net.mamoe.mirai.internal.network.components.SsoProcessorImpl.login(SsoProcessor.kt:232)
        //Caused by: java.lang.IllegalStateException: Oops
        //	at net.mamoe.mirai.internal.network.auth.AbstractBotAuthTest$authorization failure throws BotAuthorizationException$1$2.authorize(AbstractBotAuthTest.kt:44)
        //	(Coroutine boundary)
        //	at net.mamoe.mirai.utils.channels.CoroutineOnDemandReceiveChannel.receiveOrNull(OnDemandChannelImpl.kt:237)
        //	(Coroutine creation stacktrace)
        //	at net.mamoe.mirai.internal.network.handler.CommonNetworkHandler$StateConnecting.startState(CommonNetworkHandler.kt:244)
        //Caused by: java.lang.IllegalStateException: Oops
        //	at net.mamoe.mirai.internal.network.auth.AbstractBotAuthTest$authorization failure throws BotAuthorizationException$1$2.authorize(AbstractBotAuthTest.kt:44)
    }

}