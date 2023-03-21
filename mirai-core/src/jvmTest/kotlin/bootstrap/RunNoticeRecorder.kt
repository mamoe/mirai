/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.bootstrap

import kotlinx.serialization.Serializable
import net.mamoe.mirai.Bot
import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.internal.asQQAndroidBot
import net.mamoe.mirai.internal.network.components.NoticeProcessorPipeline
import net.mamoe.mirai.internal.testFramework.desensitizer.Desensitizer
import net.mamoe.mirai.internal.testFramework.notice.RecordingNoticeProcessor
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.readResource
import net.mamoe.yamlkt.Yaml
import kotlin.concurrent.thread

@Serializable
internal data class LocalAccount(
    val id: Long,
    val password: String
)

internal suspend fun main() {
    Runtime.getRuntime().addShutdownHook(thread(start = false) {
        Bot.instances.forEach {
            it.close()
        }
    })


    Desensitizer.local.desensitize("") // verify rules

    val account = Yaml.decodeFromString(LocalAccount.serializer(), readResource("local.account.yml"))
    val bot = BotFactory.newBot(account.id, account.password) {
        enableContactCache()
        fileBasedDeviceInfo("local.device.json")
        protocol = BotConfiguration.MiraiProtocol.ANDROID_PAD
    }.asQQAndroidBot()

    bot.components[NoticeProcessorPipeline].registerProcessor(RecordingNoticeProcessor())

    bot.login()

    bot.join()
}