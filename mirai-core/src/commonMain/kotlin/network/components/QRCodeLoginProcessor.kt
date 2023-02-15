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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.mamoe.mirai.internal.network.QQAndroidClient
import net.mamoe.mirai.internal.network.QRCodeLoginData
import net.mamoe.mirai.internal.network.component.ComponentKey
import net.mamoe.mirai.internal.network.handler.NetworkHandler
import net.mamoe.mirai.internal.network.protocol.packet.login.WtLogin
import net.mamoe.mirai.utils.*

internal interface QRCodeLoginProcessor {
    suspend fun process(handler: NetworkHandler, client: QQAndroidClient): QRCodeLoginData

    companion object : ComponentKey<QRCodeLoginProcessor> {
        internal val NOOP = object : QRCodeLoginProcessor {

            override suspend fun process(handler: NetworkHandler, client: QQAndroidClient): QRCodeLoginData {
                error("NOOP")
            }
        }

        fun parse(ssoContext: SsoProcessorContext, logger: MiraiLogger): QRCodeLoginProcessor {
            val loginSolver = ssoContext.bot.configuration.loginSolver ?: return NOOP
            if (ssoContext.bot.configuration.protocol != BotConfiguration.MiraiProtocol.ANDROID_WATCH) return NOOP
            val qrCodeLoginListener = loginSolver.qrCodeLoginListener ?: return NOOP
            return QRCodeLoginProcessorImpl(qrCodeLoginListener, logger)
        }
    }
}

internal class QRCodeLoginProcessorImpl(
    private val listener: LoginSolver.QRCodeLoginListener,
    private val logger: MiraiLogger,
) : QRCodeLoginProcessor {

    private val lock = Mutex(false)
    private var state by atomic(LoginSolver.QRCodeLoginListener.State.DEFAULT)

    private suspend fun requestQRCode(handler: NetworkHandler, client: QQAndroidClient) : WtLogin.TransEmp.TransEmpResponse.FetchQRCode {
        logger.debug { "requesting qrcode." }
        val resp = handler.sendAndExpect(WtLogin.TransEmp.FetchQRCode(client), attempts = 1)
        check(resp is WtLogin.TransEmp.TransEmpResponse.FetchQRCode) { "Cannot fetch qrcode, resp=$resp" }
        listener.onFetchQRCode(resp.imageData)
        return resp
    }

    private suspend fun queryQRCodeStatus(
        handler: NetworkHandler,
        client: QQAndroidClient,
        sig: ByteArray
    ) : WtLogin.TransEmp.TransEmpResponse {
        logger.debug { "querying qrcode state. sig=${sig.toUHexString()}" }
        val resp = handler.sendAndExpect(WtLogin.TransEmp.QueryQRCodeStatus(client, sig), attempts = 1, timeout = 500)
        check(
            resp is WtLogin.TransEmp.TransEmpResponse.QRCodeStatus ||
            resp is WtLogin.TransEmp.TransEmpResponse.QRCodeConfirmed
        ) { "Cannot query qrcode status, resp=$resp" }
        lock.withLock {
            val currState = resp.mapProtocolState()
            if (currState != state) {
                state = currState
                logger.debug { "qrcode state changed: $state" }
                listener.onStatusChanged(state)
            }
        }
        return resp
    }

    override suspend fun process(handler: NetworkHandler, client: QQAndroidClient): QRCodeLoginData {
        main@ while (true) { // TODO: add new bot config property to set times of fetching qrcode
            val qrCodeData = try {
                requestQRCode(handler, client)
            } catch (e: IllegalStateException) {
                logger.warning(e)
                continue@main
            }
            state@ while (true) {
                val status = try {
                    queryQRCodeStatus(handler, client, qrCodeData.sig)
                } catch (e: IllegalStateException) {
                    logger.warning(e)
                    delay(5000) // TODO: add new bot config property to set interval of querying qrcode state
                    continue@state
                }

                when(status) {
                    is WtLogin.TransEmp.TransEmpResponse.QRCodeConfirmed -> {
                        return status.data
                    }
                    is WtLogin.TransEmp.TransEmpResponse.QRCodeStatus -> when(status.state) {
                        WtLogin.TransEmp.TransEmpResponse.QRCodeStatus.State.TIMEOUT,
                        WtLogin.TransEmp.TransEmpResponse.QRCodeStatus.State.CANCELLED -> {
                            val e = IllegalStateException("QRCode cancelled. sig=${qrCodeData.sig.toUHexString()}")
                            logger.warning(e)
                            break@state
                        }
                        else -> { delay(5000) } // WAITING_FOR_SCAN or WAITING_FOR_CONFIRM
                    }
                    // status is FetchQRCode, which is unreachable.
                    else -> { break@state }
                }
            }
        }
    }

    private fun WtLogin.TransEmp.TransEmpResponse.mapProtocolState(): LoginSolver.QRCodeLoginListener.State {
        return when (this) {
            is WtLogin.TransEmp.TransEmpResponse.QRCodeStatus -> when(this.state) {
                WtLogin.TransEmp.TransEmpResponse.QRCodeStatus.State.WAITING_FOR_SCAN ->
                    LoginSolver.QRCodeLoginListener.State.WAITING_FOR_SCAN

                WtLogin.TransEmp.TransEmpResponse.QRCodeStatus.State.WAITING_FOR_CONFIRM ->
                    LoginSolver.QRCodeLoginListener.State.WAITING_FOR_CONFIRM

                WtLogin.TransEmp.TransEmpResponse.QRCodeStatus.State.CANCELLED ->
                    LoginSolver.QRCodeLoginListener.State.CANCELLED

                WtLogin.TransEmp.TransEmpResponse.QRCodeStatus.State.TIMEOUT ->
                    LoginSolver.QRCodeLoginListener.State.TIMEOUT
            }
            is WtLogin.TransEmp.TransEmpResponse.QRCodeConfirmed ->
                LoginSolver.QRCodeLoginListener.State.CONFIRMED
            is WtLogin.TransEmp.TransEmpResponse.FetchQRCode ->
                error("TransEmpResponse is not QRCodeStatus or QRCodeConfirmed.")
        }
    }
}