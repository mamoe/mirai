/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */


@file:Suppress("unused")

package net.mamoe.mirai.internal

import net.mamoe.mirai.utils.BotConfiguration

internal val MockAccount = BotAccount(1, "pwd")

internal val MockConfiguration = BotConfiguration {
}

internal class MockBotBuilder(
    val conf: BotConfiguration = BotConfiguration(),
    val debugConf: BotDebugConfiguration = BotDebugConfiguration()
) {
    fun conf(action: BotConfiguration.() -> Unit): MockBotBuilder {
        conf.apply(action)
        return this
    }

    fun debugConf(action: BotDebugConfiguration.() -> Unit): MockBotBuilder {
        debugConf.apply(action)
        return this
    }
}

@Suppress("TestFunctionName")
internal fun MockBot(conf: MockBotBuilder.() -> Unit) =
    MockBotBuilder(MockConfiguration.copy()).apply(conf).run {
        QQAndroidBot(MockAccount, this.conf, debugConf)
    }

@Suppress("TestFunctionName")
internal fun MockBot() =
    QQAndroidBot(MockAccount, MockConfiguration.copy())