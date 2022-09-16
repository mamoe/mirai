/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package net.mamoe.mirai.console.command

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.internal.command.builtin.LoginCommandImpl
import net.mamoe.mirai.console.internal.data.builtins.AutoLoginConfig
import net.mamoe.mirai.console.internal.data.builtins.AutoLoginConfig.Account
import net.mamoe.mirai.console.internal.data.builtins.AutoLoginConfig.Account.PasswordKind
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.md5
import net.mamoe.mirai.utils.toUHexString
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@OptIn(ExperimentalCommandDescriptors::class)
internal class LoginCommandTest : AbstractCommandTest() {

    @Test
    fun `login with provided password`() = runTest {
        val myId = 123L
        val myPwd = "password001"

        val bot = awaitDeferred { cont ->
            val command = object : LoginCommandImpl() {
                override suspend fun doLogin(bot: Bot) {
                    cont.complete(bot as QQAndroidBot)
                }
            }
            command.register(true)
            command.execute(consoleSender, "$myId $myPwd")
        }

        val account = bot.account
        assertContentEquals(myPwd.md5(), account.passwordMd5)
        assertEquals(myId, account.id)
    }

    @Test
    fun `login with saved plain password`() = runTest {
        val myId = 123L
        val myPwd = "password001"

        dataScope.set(AutoLoginConfig().apply {
            accounts.add(
                Account(
                    account = myId.toString(),
                    password = Account.Password(PasswordKind.PLAIN, myPwd)
                )
            )
        })

        val bot = awaitDeferred { cont ->
            val command = object : LoginCommandImpl() {
                override suspend fun doLogin(bot: Bot) {
                    cont.complete(bot as QQAndroidBot)
                }
            }
            command.register(true)
            command.execute(consoleSender, "$myId")
        }

        val account = bot.account
        assertContentEquals(myPwd.md5(), account.passwordMd5)
        assertEquals(myId, account.id)
    }

    @Test
    fun `login with saved md5 password`() = runTest {
        val myId = 123L
        val myPwd = "password001"

        dataScope.set(AutoLoginConfig().apply {
            accounts.add(
                Account(
                    account = myId.toString(),
                    password = Account.Password(PasswordKind.MD5, myPwd.md5().toUHexString(""))
                )
            )
        })

        val bot = awaitDeferred<QQAndroidBot> { cont ->
            val command = object : LoginCommandImpl() {
                override suspend fun doLogin(bot: Bot) {
                    cont.complete(bot as QQAndroidBot)
                }
            }
            command.register(true)
            command.execute(consoleSender, "$myId")
        }

        val account = bot.account
        assertContentEquals(myPwd.md5(), account.passwordMd5)
        assertEquals(myId, account.id)
    }

    @Test
    fun `login with saved configuration`() = runTest {
        val myId = 123L
        val myPwd = "password001"

        dataScope.set(AutoLoginConfig().apply {
            accounts.add(
                Account(
                    account = myId.toString(),
                    password = Account.Password(PasswordKind.MD5, myPwd.md5().toUHexString("")),
                    configuration = mapOf(
                        Account.ConfigurationKey.protocol to BotConfiguration.MiraiProtocol.ANDROID_PAD.name,
                        Account.ConfigurationKey.device to "device.new.json",
                        Account.ConfigurationKey.heartbeatStrategy to BotConfiguration.HeartbeatStrategy.REGISTER.name
                    )
                )
            )
        })

        val bot = awaitDeferred<QQAndroidBot> { cont ->
            val command = object : LoginCommandImpl() {
                override suspend fun doLogin(bot: Bot) {
                    cont.complete(bot as QQAndroidBot)
                }
            }
            command.register(true)
            command.execute(consoleSender, "$myId")
        }

        val configuration = bot.configuration
        assertEquals(BotConfiguration.MiraiProtocol.ANDROID_PAD, configuration.protocol)
        assertEquals(BotConfiguration.HeartbeatStrategy.REGISTER, configuration.heartbeatStrategy)
        assertNotNull(configuration.deviceInfo)
    }
}

@BuilderInference
internal suspend inline fun <T> awaitDeferred(
    @BuilderInference
    crossinline block: suspend (CompletableDeferred<T>) -> Unit
): T {
    val deferred = CompletableDeferred<T>()
    block(deferred)
    return deferred.await()
}