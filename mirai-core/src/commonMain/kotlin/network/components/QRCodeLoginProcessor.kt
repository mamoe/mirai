/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.components

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.delay
import net.mamoe.mirai.internal.network.QQAndroidClient
import net.mamoe.mirai.internal.network.QRCodeLoginData
import net.mamoe.mirai.internal.network.component.ComponentKey
import net.mamoe.mirai.internal.network.handler.NetworkHandler
import net.mamoe.mirai.internal.network.protocol.packet.login.WtLogin
import net.mamoe.mirai.internal.utils.MiraiProtocolInternal.Companion.asInternal
import net.mamoe.mirai.utils.LoginSolver
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
            if (!ssoContext.bot.configuration.doQRCodeLogin) return NOOP
            check(ssoContext.bot.configuration.protocol.asInternal.canDoQRCodeLogin) {
                "The login protocol must be ANDROID_WATCH or MACOS while enabling qrcode login." +
                        "Set it by `bot.configuration.protocol = BotConfiguration.MiraiProtocol.ANDROID_WATCH`."
            }
            return QRCodeLoginProcessorPreLoaded(ssoContext, logger)
        }
    }
}

internal class QRCodeLoginProcessorPreLoaded(
    private val ssoContext: SsoProcessorContext,
    private val logger: MiraiLogger,
) : QRCodeLoginProcessor {
    override fun prepareProcess(handler: NetworkHandler, client: QQAndroidClient): QRCodeLoginProcessor {
        val loginSolver = ssoContext.bot.configuration.loginSolver
            ?: throw IllegalStateException(
                "No LoginSolver found while enabling qrcode login. " +
                        "Please provide by BotConfiguration.loginSolver. " +
                        "For example use `BotFactory.newBot(...) { loginSolver = yourLoginSolver}` in Kotlin, " +
                        "use `BotFactory.newBot(..., new BotConfiguration() {{ setLoginSolver(yourLoginSolver) }})` in Java."
            )

        val qrCodeLoginListener = loginSolver.createQRCodeLoginListener(client.bot)

        return QRCodeLoginProcessorImpl(qrCodeLoginListener, logger)
    }
}

internal class QRCodeLoginProcessorImpl(
    private val qrCodeLoginListener: LoginSolver.QRCodeLoginListener,
    private val logger: MiraiLogger,
) : QRCodeLoginProcessor {

    private var state = atomic(LoginSolver.QRCodeLoginListener.State.DEFAULT)

    private suspend fun requestQRCode(
        handler: NetworkHandler,
        client: QQAndroidClient
    ): WtLogin.TransEmp.Response.FetchQRCode {
        logger.debug { "requesting qrcode." }
        val resp = handler.sendAndExpect(WtLogin.TransEmp.FetchQRCode(client), attempts = 1)
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
        val resp = handler.sendAndExpect(WtLogin.TransEmp.QueryQRCodeStatus(client, sig), attempts = 1, timeout = 500)
        check(
            resp is WtLogin.TransEmp.Response.QRCodeStatus || resp is WtLogin.TransEmp.Response.QRCodeConfirmed
        ) { "Cannot query qrcode status, resp=$resp" }

        val currentState = state.value
        val newState = resp.mapProtocolState()
        if (currentState != newState && state.compareAndSet(currentState, newState)) {
            logger.debug { "qrcode state changed: $state" }
            qrCodeLoginListener.onStatusChanged(handler.context.bot, newState)
        }
        return resp
    }

    override suspend fun process(handler: NetworkHandler, client: QQAndroidClient): QRCodeLoginData {
        main@ while (true) { // TODO: add new bot config property to set times of fetching qrcode
            val qrCodeData = requestQRCode(handler, client)
            state@ while (true) {
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
                delay(5000)
            }
        }
    }

    private fun WtLogin.TransEmp.Response.mapProtocolState(): LoginSolver.QRCodeLoginListener.State {
        return when (this) {
            is WtLogin.TransEmp.Response.QRCodeStatus -> when (this.state) {
                WtLogin.TransEmp.Response.QRCodeStatus.State.WAITING_FOR_SCAN ->
                    LoginSolver.QRCodeLoginListener.State.WAITING_FOR_SCAN

                WtLogin.TransEmp.Response.QRCodeStatus.State.WAITING_FOR_CONFIRM ->
                    LoginSolver.QRCodeLoginListener.State.WAITING_FOR_CONFIRM

                WtLogin.TransEmp.Response.QRCodeStatus.State.CANCELLED ->
                    LoginSolver.QRCodeLoginListener.State.CANCELLED

                WtLogin.TransEmp.Response.QRCodeStatus.State.TIMEOUT ->
                    LoginSolver.QRCodeLoginListener.State.TIMEOUT
            }

            is WtLogin.TransEmp.Response.QRCodeConfirmed ->
                LoginSolver.QRCodeLoginListener.State.CONFIRMED

            is WtLogin.TransEmp.Response.FetchQRCode ->
                error("$this cannot be mapped to listener state.")
        }
    }
}