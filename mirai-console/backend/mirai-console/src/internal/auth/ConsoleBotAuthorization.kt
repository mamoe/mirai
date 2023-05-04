/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.internal.auth

import net.mamoe.mirai.auth.BotAuthInfo
import net.mamoe.mirai.auth.BotAuthResult
import net.mamoe.mirai.auth.BotAuthSession
import net.mamoe.mirai.auth.BotAuthorization
import net.mamoe.mirai.console.ConsoleFrontEndImplementation
import net.mamoe.mirai.console.MiraiConsoleImplementation
import java.io.ByteArrayOutputStream

internal class ConsoleBotAuthorization(
    private val delegate: suspend (BotAuthSession, BotAuthInfo) -> BotAuthResult,
) : BotAuthorization {

    override suspend fun authorize(session: BotAuthSession, info: BotAuthInfo): BotAuthResult {
        return delegate.invoke(session, info)
    }

    @OptIn(ConsoleFrontEndImplementation::class)
    override fun calculateSecretsKey(bot: BotAuthInfo): ByteArray {
        val calc = MiraiConsoleImplementation.getBridge().consoleSecretsCalculator

        val writer = ByteArrayOutputStream()

        writer += calc.consoleKey.asByteArray

        writer += bot.deviceInfo.apn
        writer += bot.deviceInfo.device
        writer += bot.deviceInfo.bootId
        writer += bot.deviceInfo.imsiMd5

        return writer.toByteArray()
    }


    private operator fun ByteArrayOutputStream.plusAssign(data: ByteArray) {
        write(data)
    }

    companion object {
        fun byQRCode(): ConsoleBotAuthorization = ConsoleBotAuthorization { session, _ ->
            session.authByQRCode()
        }
    }
}

