/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.configuration

import net.mamoe.mirai.console.MiraiConsoleImplementation.Companion.start
import net.mamoe.mirai.console.internal.data.builtins.AutoLoginConfig
import net.mamoe.mirai.console.testFramework.AbstractConsoleInstanceTest
import net.mamoe.mirai.console.testFramework.MockConsoleImplementation
import net.mamoe.mirai.event.events.BotOnlineEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.utils.BotConfiguration
import org.junit.jupiter.api.Disabled
import kotlin.test.Test
import kotlin.test.assertEquals

class AutoLoginTest : AbstractConsoleInstanceTest() {

    @Disabled // no mock login
    @Test
    fun testHeartbeatStrategy() {
        stopConsole() // stop previous console
        val console = MockConsoleImplementation()
        val config = AutoLoginConfig()
        config.accounts.clear()
        config.accounts.add(
            AutoLoginConfig.Account(
                "111",
                AutoLoginConfig.Account.Password(AutoLoginConfig.Account.PasswordKind.PLAIN, "pwd"),
                configuration = mapOf(AutoLoginConfig.Account.ConfigurationKey.heartbeatStrategy to "REGISTER")
            )
        )
        console.consoleDataScope.addAndReloadConfig(config)
        console.globalEventChannel().subscribeAlways<BotOnlineEvent> {
            assertEquals(BotConfiguration.HeartbeatStrategy.REGISTER, this.bot.configuration.heartbeatStrategy)
        }
        console.start()
    }
}