/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.auth

import net.mamoe.mirai.auth.BotAuthResult
import net.mamoe.mirai.internal.network.components.SsoProcessorImpl
import net.mamoe.mirai.utils.SecretsProtection
import net.mamoe.mirai.utils.channels.IllegalProducerStateException
import net.mamoe.mirai.utils.channels.OnDemandSendChannel

/**
 * Implements [BotAuthSessionInternal] from API, to be called by the user, to receive user's decisions.
 */
internal class SafeBotAuthSession(
    private val producer: OnDemandSendChannel<Throwable?, SsoProcessorImpl.AuthMethod>
) : BotAuthSessionInternal() {
    private val authResultImpl = object : BotAuthResult {}

    override suspend fun authByPassword(passwordMd5: SecretsProtection.EscapedByteBuffer): BotAuthResult {
        runWrapInternalException {
            producer.emit(SsoProcessorImpl.AuthMethod.Pwd(passwordMd5))
        }?.let { throw it }
        return authResultImpl
    }

    override suspend fun authByQRCode(): BotAuthResult {
        runWrapInternalException {
            producer.emit(SsoProcessorImpl.AuthMethod.QRCode)
        }?.let { throw it }
        return authResultImpl
    }

    private inline fun <R> runWrapInternalException(block: () -> R): R {
        try {
            return block()
        } catch (e: IllegalProducerStateException) {
            if (e.lastStateWasSucceed) {
                throw IllegalStateException(
                    "This login session has already completed. Please return the BotAuthResult you get from 'authBy*()' immediately",
                    e
                )
            } else {
                throw e // internal bug
            }
        }
    }
}
