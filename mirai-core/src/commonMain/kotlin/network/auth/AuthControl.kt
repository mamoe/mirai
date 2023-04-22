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
import net.mamoe.mirai.auth.BotAuthorization
import net.mamoe.mirai.internal.network.components.SsoProcessorImpl
import net.mamoe.mirai.internal.utils.asUtilsLogger
import net.mamoe.mirai.internal.utils.subLogger
import net.mamoe.mirai.utils.ExceptionCollector
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.channels.OnDemandReceiveChannel
import net.mamoe.mirai.utils.channels.ProducerFailureException
import net.mamoe.mirai.utils.debug
import net.mamoe.mirai.utils.verbose
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

    private val userDecisions: OnDemandReceiveChannel<Throwable?, SsoProcessorImpl.AuthMethod> =
        OnDemandReceiveChannel(
            parentCoroutineContext,
            logger.subLogger("AuthControl/UserDecisions").asUtilsLogger()
        ) { _ ->
            val sessionImpl = SafeBotAuthSession(this)

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
