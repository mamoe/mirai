/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.component

import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import net.mamoe.mirai.auth.BotAuthComponent
import net.mamoe.mirai.auth.BotAuthInfo
import net.mamoe.mirai.auth.BotAuthResult
import net.mamoe.mirai.auth.BotAuthorization
import net.mamoe.mirai.internal.network.components.SsoProcessorContext
import net.mamoe.mirai.internal.network.components.SsoProcessorImpl
import net.mamoe.mirai.internal.network.framework.AbstractCommonNHTest
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.DeviceInfo
import net.mamoe.mirai.utils.EMPTY_BYTE_ARRAY
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertFails
import kotlin.test.fail

internal class BotAuthControlTest : AbstractCommonNHTest() {
    val botAuthInfo = object : BotAuthInfo {
        override val id: Long
            get() = bot.id
        override val deviceInfo: DeviceInfo
            get() = bot.components[SsoProcessorContext].device
        override val configuration: BotConfiguration
            get() = bot.configuration
    }

    private suspend fun SsoProcessorImpl.AuthControl.assertRequire(exceptedType: KClass<*>) {
        println("Requiring auth method")
        val nextAuth = acquireAuth()
        println("Got $nextAuth")
        yield()

        if (nextAuth is SsoProcessorImpl.AuthMethod.Error) {
            fail(cause = nextAuth.exception)
        }
        if (exceptedType.isInstance(nextAuth)) return
        fail("Type not match, excepted $exceptedType but got ${nextAuth::class}")
    }

    @Test
    fun `auth test`() = runTest {

        val control = SsoProcessorImpl.AuthControl(botAuthInfo, object : BotAuthorization {
            override suspend fun authorize(authComponent: BotAuthComponent, bot: BotAuthInfo): BotAuthResult {
                return authComponent.authByPassword(EMPTY_BYTE_ARRAY)
            }
        }, bot.logger, backgroundScope)

        control.assertRequire(SsoProcessorImpl.AuthMethod.Pwd::class)
        control.actComplete()
        control.assertRequire(SsoProcessorImpl.AuthMethod.NotAvailable::class)

    }

    @Test
    fun `test auth failed and reselect`() = runTest {

        val control = SsoProcessorImpl.AuthControl(botAuthInfo, object : BotAuthorization {
            override suspend fun authorize(authComponent: BotAuthComponent, bot: BotAuthInfo): BotAuthResult {
                assertFails { authComponent.authByPassword(EMPTY_BYTE_ARRAY); println("!") }
                println("114514")
                return authComponent.authByPassword(EMPTY_BYTE_ARRAY)
            }
        }, bot.logger, backgroundScope)

        control.assertRequire(SsoProcessorImpl.AuthMethod.Pwd::class)
        control.actFailed(Throwable())
        control.assertRequire(SsoProcessorImpl.AuthMethod.Pwd::class)
        control.actComplete()
        control.assertRequire(SsoProcessorImpl.AuthMethod.NotAvailable::class)

    }

    @Test
    fun `failed when login complete`() = runTest {

        val control = SsoProcessorImpl.AuthControl(botAuthInfo, object : BotAuthorization {
            override suspend fun authorize(authComponent: BotAuthComponent, bot: BotAuthInfo): BotAuthResult {
                val rsp = authComponent.authByPassword(EMPTY_BYTE_ARRAY)
                assertFails { authComponent.authByPassword(EMPTY_BYTE_ARRAY) }
                assertFails { authComponent.authByPassword(EMPTY_BYTE_ARRAY) }
                assertFails { authComponent.authByPassword(EMPTY_BYTE_ARRAY) }
                return rsp
            }
        }, bot.logger, backgroundScope)

        control.assertRequire(SsoProcessorImpl.AuthMethod.Pwd::class)
        control.actComplete()
        control.assertRequire(SsoProcessorImpl.AuthMethod.NotAvailable::class)

    }
}