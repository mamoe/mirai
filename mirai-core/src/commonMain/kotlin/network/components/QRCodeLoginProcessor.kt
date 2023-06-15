/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.components

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.delay
import net.mamoe.mirai.auth.QRCodeLoginListener
import net.mamoe.mirai.internal.network.QQAndroidClient
import net.mamoe.mirai.internal.network.QRCodeLoginData
import net.mamoe.mirai.internal.network.component.ComponentKey
import net.mamoe.mirai.internal.network.handler.NetworkHandler
import net.mamoe.mirai.internal.network.protocol.packet.login.WtLogin
import net.mamoe.mirai.internal.utils.MiraiProtocolInternal.Companion.asInternal
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.debug

internal interface QRCodeLoginProcessor {
    suspend fun process(handler: NetworkHandler, client: QQAndroidClient): QRCodeLoginData = error("Not implemented")

    /**
     * Allocate a special processor for once login request
     */
    fun prepareProcess(handler: NetworkHandler, client: QQAndroidClient): QRCodeLoginProcessor =
        error("Not implemented")

    companion object : ComponentKey<QRCodeLoginProcessor> {
        internal val NOOP = object : QRCodeLoginProcessor {}

        fun parse(ssoContext: SsoProcessorContext, logger: MiraiLogger): QRCodeLoginProcessor {
            return QRCodeLoginProcessorPreLoaded(ssoContext, logger)
        }
    }
}

internal class QRCodeLoginProcessorPreLoaded(
    private val ssoContext: SsoProcessorContext,
    private val logger: MiraiLogger,
) : QRCodeLoginProcessor {
    override fun prepareProcess(handler: NetworkHandler, client: QQAndroidClient): QRCodeLoginProcessor {
        check(ssoContext.bot.configuration.protocol.asInternal.supportsQRLogin) {
            "The login protocol must be ANDROID_WATCH or MACOS while enabling qrcode login." +
                    "Set it by `bot.configuration.protocol = BotConfiguration.MiraiProtocol.ANDROID_WATCH`."
        }

        val loginSolver = ssoContext.bot.configuration.loginSolver
            ?: throw IllegalStateException(
                "No LoginSolver found while enabling qrcode login. " +
                        "Please provide by BotConfiguration.loginSolver. " +
                        "For example use `BotFactory.newBot(...) { loginSolver = yourLoginSolver}` in Kotlin, " +
                        "use `BotFactory.newBot(..., new BotConfiguration() {{ setLoginSolver(yourLoginSolver) }})` in Java."
            )

        val qrCodeLoginListener = loginSolver.createQRCodeLoginListener(client.bot)

        return loginSolver.run {
            QRCodeLoginProcessorImpl(qrCodeLoginListener, logger)
        }
    }
}

internal class QRCodeLoginProcessorImpl(
    private val qrCodeLoginListener: QRCodeLoginListener,
    private val logger: MiraiLogger,
) : QRCodeLoginProcessor {

    private var state = atomic(QRCodeLoginListener.State.DEFAULT)

    private suspend fun requestQRCode(
        handler: NetworkHandler,
        client: QQAndroidClient
    ): WtLogin.TransEmp.Response.FetchQRCode {
        logger.debug { "requesting qrcode." }
        val resp = handler.sendAndExpect(
            WtLogin.TransEmp.FetchQRCode(
                client,
                size = qrCodeLoginListener.qrCodeSize,
                margin = qrCodeLoginListener.qrCodeMargin,
                ecLevel = qrCodeLoginListener.qrCodeEcLevel,
            ),
        )
        check(resp is WtLogin.TransEmp.Response.FetchQRCode) { "Cannot fetch qrcode, resp=$resp" }
        qrCodeLoginListener.onFetchQRCode(handler.context.bot, resp.imageData)
        return resp
    }

    private suspend fun queryQRCodeStatus(
        handler: NetworkHandler,
        client: QQAndroidClient,
        sig: ByteArray
    ): WtLogin.TransEmp.Response {
        logger.debug { "querying qrcode state." }
        val resp = handler.sendAndExpect(WtLogin.TransEmp.QueryQRCodeStatus(client, sig))
        check(
            resp is WtLogin.TransEmp.Response.QRCodeStatus || resp is WtLogin.TransEmp.Response.QRCodeConfirmed
        ) { "Cannot query qrcode status, resp=$resp" }

        val currentState = state.value
        val newState = resp.mapProtocolState()
        if (currentState != newState && state.compareAndSet(currentState, newState)) {
            logger.debug { "qrcode state changed: $state" }
            qrCodeLoginListener.onStateChanged(handler.context.bot, newState)
        }
        return resp
    }

    override suspend fun process(handler: NetworkHandler, client: QQAndroidClient): QRCodeLoginData {
        return try {
            process0(handler, client)
        } finally {
            qrCodeLoginListener.onCompleted()
        }
    }

    private suspend fun process0(handler: NetworkHandler, client: QQAndroidClient): QRCodeLoginData {
        main@ while (true) {
            val qrCodeData = requestQRCode(handler, client)
            state@ while (true) {
                qrCodeLoginListener.onIntervalLoop()

                when (val status = queryQRCodeStatus(handler, client, qrCodeData.sig)) {
                    is WtLogin.TransEmp.Response.QRCodeConfirmed -> {
                        return status.data
                    }

                    is WtLogin.TransEmp.Response.QRCodeStatus -> when (status.state) {
                        WtLogin.TransEmp.Response.QRCodeStatus.State.TIMEOUT,
                        WtLogin.TransEmp.Response.QRCodeStatus.State.CANCELLED -> {
                            break@state
                        }

                        else -> {} // WAITING_FOR_SCAN or WAITING_FOR_CONFIRM
                    }
                    // status is FetchQRCode, which is unreachable.
                    else -> {
                        error("query qrcode status should not be FetchQRCode.")
                    }
                }

                delay(qrCodeLoginListener.qrCodeStateUpdateInterval.coerceAtLeast(200L))
            }
        }
    }

    private fun WtLogin.TransEmp.Response.mapProtocolState(): QRCodeLoginListener.State {
        return when (this) {
            is WtLogin.TransEmp.Response.QRCodeStatus -> when (this.state) {
                WtLogin.TransEmp.Response.QRCodeStatus.State.WAITING_FOR_SCAN ->
                    QRCodeLoginListener.State.WAITING_FOR_SCAN

                WtLogin.TransEmp.Response.QRCodeStatus.State.WAITING_FOR_CONFIRM ->
                    QRCodeLoginListener.State.WAITING_FOR_CONFIRM

                WtLogin.TransEmp.Response.QRCodeStatus.State.CANCELLED ->
                    QRCodeLoginListener.State.CANCELLED

                WtLogin.TransEmp.Response.QRCodeStatus.State.TIMEOUT ->
                    QRCodeLoginListener.State.TIMEOUT
            }

            is WtLogin.TransEmp.Response.QRCodeConfirmed ->
                QRCodeLoginListener.State.CONFIRMED

            is WtLogin.TransEmp.Response.FetchQRCode ->
                error("$this cannot be mapped to listener state.")
        }
    }
}