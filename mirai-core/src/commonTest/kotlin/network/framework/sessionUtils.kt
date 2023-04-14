/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.framework

import net.mamoe.mirai.event.events.BotOnlineEvent
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.QQAndroidClient
import net.mamoe.mirai.internal.network.WLoginSigInfo
import net.mamoe.mirai.internal.network.components.AccountSecrets
import net.mamoe.mirai.internal.network.components.AccountSecretsImpl
import net.mamoe.mirai.internal.network.components.SsoSession
import net.mamoe.mirai.internal.utils.io.serialization.toByteArray
import net.mamoe.mirai.utils.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.utils.MiraiFile
import net.mamoe.mirai.utils.debug
import net.mamoe.mirai.utils.writeBytes


internal class TestSsoSession(
    private val accountSecrets: AccountSecrets,
    override var outgoingPacketSessionId: ByteArray = byteArrayOf(1, 2, 3, 4),
    override var loginState: Int = 0,
) : SsoSession {
    override var wLoginSigInfo: WLoginSigInfo by accountSecrets::wLoginSigInfo
    override val randomKey: ByteArray by accountSecrets::randomKey
}

//internal fun loadSession(
//    resourceName: String,
//): AccountSecretsImpl {
//    val bytes = ClassLoader.getSystemResourceAsStream(resourceName)?.withUse { readBytes() }
//        ?: error("AccountSecrets resource '$resourceName' not found.")
//    return bytes.loadAs(AccountSecretsImpl.serializer())
//}

/**
 * Secure to share with others. Designed to save real data for tests.
 */
internal fun QQAndroidClient.dumpSessionSafe(): ByteArray {
    val secrets =
        AccountSecretsImpl(device).copy(
            wLoginSigInfoField = wLoginSigInfo.copy(
                tgt = EMPTY_BYTE_ARRAY,
                encryptA1 = EMPTY_BYTE_ARRAY,
            )
        )
    return secrets.toByteArray(AccountSecretsImpl.serializer())
}

internal fun QQAndroidBot.scheduleSafeSessionDump(outputFile: MiraiFile) {
    this.eventChannel.subscribeAlways<BotOnlineEvent> {
        outputFile.writeBytes(client.dumpSessionSafe())
        bot.logger.debug { "Dumped safe session to " }
    }
}