/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.auth

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.mamoe.mirai.auth.BotAuthInfo
import net.mamoe.mirai.auth.BotAuthResult
import net.mamoe.mirai.auth.BotAuthorization
import net.mamoe.mirai.internal.network.components.SsoProcessorImpl
import net.mamoe.mirai.utils.*
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.jvm.Volatile

internal class AuthControl(
    private val botAuthInfo: BotAuthInfo,
    private val authorization: BotAuthorization,
    private val logger: MiraiLogger,
    scope: CoroutineScope,
) {
    internal val exceptionCollector = ExceptionCollector()

    @Volatile
    private var authorizationContinuation: Continuation<Unit>? = null

    @Volatile
    private var authRspFuture = initCompletableDeferred()

    @Volatile
    private var isCompleted = false

    private val rsp = object : BotAuthResult {}

    @Suppress("RemoveExplicitTypeArguments")
    @OptIn(TestOnly::class)
    private val authComponent = object : SsoProcessorImpl.SsoProcessorAuthComponent() {
        override val botAuthResult: BotAuthResult get() = rsp

        override suspend fun emit(method: SsoProcessorImpl.AuthMethod) {
            logger.verbose { "[AuthControl/emit] Trying emit $method" }

            if (isCompleted) {
                val msg = "[AuthControl/emit] Failed to emit $method because control completed"

                error(msg.also { logger.verbose(it) })
            }
            suspendCoroutine<Unit> { next ->
                val rspTarget = authRspFuture
                if (!rspTarget.complete(method)) {
                    val msg = "[AuthControl/emit] Failed to emit $method because auth response completed"

                    error(msg.also { logger.verbose(it) })
                }
                authorizationContinuation = next
                logger.verbose { "[AuthControl/emit] Emitted $method to $rspTarget" }
            }
            logger.verbose { "[AuthControl/emit] Authorization resumed after $method" }
        }

        override suspend fun authByPassword(passwordMd5: SecretsProtection.EscapedByteBuffer): BotAuthResult {
            emit(SsoProcessorImpl.AuthMethod.Pwd(passwordMd5))
            return rsp
        }

        override suspend fun authByPassword(password: String): BotAuthResult {
            return authByPassword(password.md5())
        }

        override suspend fun authByPassword(passwordMd5: ByteArray): BotAuthResult {
            return authByPassword(SecretsProtection.EscapedByteBuffer(passwordMd5))
        }

        override suspend fun authByQRCode(): BotAuthResult {
            emit(SsoProcessorImpl.AuthMethod.QRCode)
            return rsp
        }
    }

    init {
        // start users' BotAuthorization.authorize
        scope.launch {
            try {
                logger.verbose { "[AuthControl/auth] Authorization started" }

                authorization.authorize(authComponent, botAuthInfo)

                logger.verbose { "[AuthControl/auth] Authorization exited" }

                isCompleted = true
                authRspFuture.complete(SsoProcessorImpl.AuthMethod.NotAvailable)

            } catch (e: Throwable) {
                logger.verbose({ "[AuthControl/auth] Authorization failed" }, e)

                isCompleted = true
                authRspFuture.complete(SsoProcessorImpl.AuthMethod.Error(e))
            }
        }
    }

    private fun onSpinWait() {}
    suspend fun acquireAuth(): SsoProcessorImpl.AuthMethod {
        val authTarget = authRspFuture
        logger.verbose { "[AuthControl/acquire] Acquiring auth method with $authTarget" }
        val rsp = authTarget.await()
        logger.debug { "[AuthControl/acquire] Authorization responded: $authTarget, $rsp" }

        while (authorizationContinuation == null && !isCompleted) {
            onSpinWait()
        }
        logger.verbose { "[AuthControl/acquire] authorizationContinuation setup: $authorizationContinuation, $isCompleted" }

        return rsp
    }

    fun actFailed(cause: Throwable) {
        logger.verbose { "[AuthControl/resume] Fire auth failed with cause: $cause" }

        authRspFuture = initCompletableDeferred()
        authorizationContinuation!!.let { cont ->
            authorizationContinuation = null
            cont.resumeWith(Result.failure(cause))
        }
    }

    @TestOnly // same as act failed
    fun actResume() {
        logger.verbose { "[AuthControl/resume] Fire auth resume" }

        authRspFuture = initCompletableDeferred()
        authorizationContinuation!!.let { cont ->
            authorizationContinuation = null
            cont.resume(Unit)
        }
    }

    fun actComplete() {
        logger.verbose { "[AuthControl/resume] Fire auth completed" }

        isCompleted = true
        authRspFuture = CompletableDeferred(SsoProcessorImpl.AuthMethod.NotAvailable)
        authorizationContinuation!!.let { cont ->
            authorizationContinuation = null
            cont.resume(Unit)
        }
    }

    private fun initCompletableDeferred(): CompletableDeferred<SsoProcessorImpl.AuthMethod> {
        return CompletableDeferred<SsoProcessorImpl.AuthMethod>().also { df ->
            df.invokeOnCompletion {
                logger.debug { "[AuthControl/cd] $df completed with $it" }
            }
        }
    }
}
