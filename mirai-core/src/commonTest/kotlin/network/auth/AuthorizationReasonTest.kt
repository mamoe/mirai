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
import kotlinx.coroutines.yield
import net.mamoe.mirai.auth.AuthReason
import net.mamoe.mirai.auth.BotAuthResult
import net.mamoe.mirai.internal.MockAccount
import net.mamoe.mirai.internal.contact.uin
import net.mamoe.mirai.internal.network.components.AccountSecretsManager
import net.mamoe.mirai.internal.network.framework.createWLoginSigInfo
import net.mamoe.mirai.internal.network.protocol.data.jce.RequestPushForceOffline
import net.mamoe.mirai.internal.network.protocol.data.jce.SvcRespRegister
import net.mamoe.mirai.internal.network.protocol.packet.IncomingPacket
import net.mamoe.mirai.internal.network.protocol.packet.chat.receive.MessageSvcPushForceOffline
import net.mamoe.mirai.internal.network.protocol.packet.login.StatSvc
import net.mamoe.mirai.internal.network.protocol.packet.login.WtLogin
import org.junit.jupiter.api.Disabled
import java.util.concurrent.atomic.AtomicReference
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

internal class AuthorizationReasonTest : AbstractBotAuthTest() {
    @Test
    fun `first login without fast login`() = runTest {
        var isFirstLogin: Boolean = false
        var authReason: AuthReason? = AuthReason.Unknown(bot, null)

        setAuthorization { auth, info ->
            isFirstLogin = info.isFirstLogin
            authReason = info.reason

            auth.authByPassword("")
            return@setAuthorization object : BotAuthResult {}
        }

        usePacketReplierThroughout {
            expect(WtLogin.Login) reply {
                bot.components[AccountSecretsManager].getSecrets(MockAccount)
                    ?.wLoginSigInfo = createWLoginSigInfo(bot.id)

                WtLogin.Login.LoginPacketResponse.Success(bot)
            }
            expect(StatSvc.Register) reply { StatSvc.Register.Response(SvcRespRegister()) }
            expect(StatSvc.Register) reply { StatSvc.Register.Response(SvcRespRegister()) }
        }

        bot.components[AccountSecretsManager].getSecrets(MockAccount)?.wLoginSigInfoField = null
        bot.login()

        assertTrue(isFirstLogin)
        assertIs<AuthReason.FreshLogin>(authReason)
    }

    @Test
    fun `first login with fast login success`() = runTest {
        var authorizationCalled = false
        setAuthorization { auth, _ ->
            authorizationCalled = true

            auth.authByPassword("")
            return@setAuthorization object : BotAuthResult {}
        }

        usePacketReplierThroughout {
            expect(WtLogin.ExchangeEmp) reply { WtLogin.Login.LoginPacketResponse.Success(bot) }
            expect(StatSvc.Register) reply { StatSvc.Register.Response(SvcRespRegister()) }
            expect(StatSvc.Register) reply { StatSvc.Register.Response(SvcRespRegister()) }
        }

        bot.login()

        assertFalse(authorizationCalled)
    }

    @Test
    fun `first login with fast login fail`() = runTest {
        var isFirstLogin: Boolean = false
        var authReason: AuthReason? = null

        setAuthorization { auth, info ->
            isFirstLogin = info.isFirstLogin
            authReason = info.reason

            auth.authByPassword("")
            return@setAuthorization object : BotAuthResult {}
        }

        usePacketReplierThroughout {
            expect(WtLogin.ExchangeEmp) reply { WtLogin.Login.LoginPacketResponse.Error(bot, 1, "", "", ", ") }
            expect(WtLogin.Login) reply { WtLogin.Login.LoginPacketResponse.Success(bot) }
            expect(StatSvc.Register) reply { StatSvc.Register.Response(SvcRespRegister()) }
            expect(StatSvc.Register) reply { StatSvc.Register.Response(SvcRespRegister()) }
        }

        bot.login()

        assertTrue(isFirstLogin)
        assertIs<AuthReason.FastLoginError>(authReason)
    }

    @Test
    @Disabled // FIXME - bad github ci, but it is ok on local computer, reason unknown
    fun `force offline`() = runTest {
        var isFirstLogin: Boolean = true

        // volatile
        val authReason = AtomicReference<AuthReason?>(null)

        setAuthorization { auth, info ->
            isFirstLogin = info.isFirstLogin
            authReason.set(info.reason)

            auth.authByPassword("")
            return@setAuthorization object : BotAuthResult {}
        }

        usePacketReplierThroughout {
            expect(WtLogin.ExchangeEmp) reply { WtLogin.Login.LoginPacketResponse.Success(bot) }
            expect(StatSvc.Register) reply { StatSvc.Register.Response(SvcRespRegister()) }
            expect(WtLogin.Login) reply {
                bot.components[AccountSecretsManager].getSecrets(MockAccount)
                    ?.wLoginSigInfo = createWLoginSigInfo(bot.id)
                WtLogin.Login.LoginPacketResponse.Success(bot)
            }
            expect(StatSvc.Register) reply { StatSvc.Register.Response(SvcRespRegister()) }
            expect(StatSvc.Register) reply { StatSvc.Register.Response(SvcRespRegister()) }
        }

        bot.configuration.autoReconnectOnForceOffline = true
        bot.login()

        network.currentInstance().collectReceived(
            IncomingPacket(
                MessageSvcPushForceOffline.commandName,
                RequestPushForceOffline(bot.uin, tips = "force offline manually in test")
            )
        )

        eventDispatcher.joinBroadcast() // why test finished before code reaches end??
        yield()

        assertFalse(isFirstLogin)
        assertIs<AuthReason.ForceOffline>(authReason.get(), message = authReason.toString())
    }
}