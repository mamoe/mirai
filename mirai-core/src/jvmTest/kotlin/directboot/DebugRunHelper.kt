/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.directboot

import kotlinx.coroutines.Dispatchers
import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.auth.BotAuthorization
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.utils.BotConfiguration
import java.io.File

internal object DebugRunHelper {
    fun newBot(id: Long, pwd: String, conf: BotConfiguration.(botid: Long) -> Unit): QQAndroidBot {
        return newBot(id, BotAuthorization.byPassword(pwd), conf)
    }

    fun newBot(id: Long, authorization: BotAuthorization, conf: BotConfiguration.(botid: Long) -> Unit): QQAndroidBot {
        val bot = BotFactory.newBot(id, authorization) {
            parentCoroutineContext = Dispatchers.IO

            workingDir = File("test/session/$id").also { it.mkdirs() }.absoluteFile
            cacheDir = workingDir.resolve("cache").absoluteFile
            this.fileBasedDeviceInfo(File("test/session/$id/device.json").absolutePath)

            conf(id)
        }
        return bot as QQAndroidBot
    }
}