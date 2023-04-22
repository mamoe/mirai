/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.auth

import net.mamoe.mirai.auth.BotAuthInfo
import net.mamoe.mirai.auth.BotAuthResult
import net.mamoe.mirai.auth.BotAuthorization
import net.mamoe.mirai.internal.network.components.SsoProcessorImpl
import net.mamoe.mirai.internal.utils.subLogger
import net.mamoe.mirai.utils.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.cancellation.CancellationException


/**
 * Event sequence:
 *
 * 1. Starts a user coroutine [BotAuthorization.authorize].
 * 2. User coroutine
 */
internal class AuthControl(
    private val botAuthInfo: BotAuthInfo,
    private val authorization: BotAuthorization,
    private val logger: MiraiLogger,
    parentCoroutineContext: CoroutineContext,
) {
    internal val exceptionCollector = ExceptionCollector()

    private val userDecisions: OnDemandConsumer<Throwable?, SsoProcessorImpl.AuthMethod> =
        CoroutineOnDemandValueScope(parentCoroutineContext, logger.subLogger("AuthControl/UserDecisions")) { _ ->
            /**
             * Implements [BotAuthSessionInternal] from API, to be called by the user, to receive user's decisions.
             */
            val sessionImpl = object : BotAuthSessionInternal() {
                private val authResultImpl = object : BotAuthResult {}

                override suspend fun authByPassword(passwordMd5: SecretsProtection.EscapedByteBuffer): BotAuthResult {
                    runWrapInternalException {
                        emit(SsoProcessorImpl.AuthMethod.Pwd(passwordMd5))
                    }?.let { throw it }
                    return authResultImpl
                }

                override suspend fun authByQRCode(): BotAuthResult {
                    runWrapInternalException {
                        emit(SsoProcessorImpl.AuthMethod.QRCode)
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

            try {
                logger.verbose { "[AuthControl/auth] Authorization started" }

                authorization.authorize(sessionImpl, botAuthInfo)

                logger.verbose { "[AuthControl/auth] Authorization exited" }
                finish()
            } catch (e: CancellationException) {
                logger.verbose { "[AuthControl/auth] Authorization cancelled" }
            } catch (e: Throwable) {
                logger.verbose { "[AuthControl/auth] Authorization failed: $e" }
                finishExceptionally(e)
            }
        }

    fun start() {
        userDecisions.expectMore(null)
    }

    // Does not throw
    suspend fun acquireAuth(): SsoProcessorImpl.AuthMethod {
        logger.verbose { "[AuthControl/acquire] Acquiring auth method" }

        val rsp = try {
            userDecisions.receiveOrNull() ?: SsoProcessorImpl.AuthMethod.NotAvailable
        } catch (e: ProducerFailureException) {
            SsoProcessorImpl.AuthMethod.Error(e)
        }

        logger.debug { "[AuthControl/acquire] Authorization responded: $rsp" }
        return rsp
    }

    fun actMethodFailed(cause: Throwable) {
        logger.verbose { "[AuthControl/resume] Fire auth failed with cause: $cause" }
        userDecisions.expectMore(cause)
    }

    fun actComplete() {
        logger.verbose { "[AuthControl/resume] Fire auth completed" }
        userDecisions.finish()
    }
}
