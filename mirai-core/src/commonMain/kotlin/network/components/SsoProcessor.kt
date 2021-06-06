/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.components

import kotlinx.coroutines.launch
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.QQAndroidClient
import net.mamoe.mirai.internal.network.component.ComponentKey
import net.mamoe.mirai.internal.network.context.AccountSecretsImpl
import net.mamoe.mirai.internal.network.context.SsoProcessorContext
import net.mamoe.mirai.internal.network.context.SsoSession
import net.mamoe.mirai.internal.network.handler.NetworkHandler
import net.mamoe.mirai.internal.network.handler.NetworkHandler.State
import net.mamoe.mirai.internal.network.handler.NetworkHandlerSupport
import net.mamoe.mirai.internal.network.handler.state.StateChangedObserver
import net.mamoe.mirai.internal.network.handler.state.StateObserver
import net.mamoe.mirai.internal.network.impl.netty.NettyNetworkHandler
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacketWithRespType
import net.mamoe.mirai.internal.network.protocol.packet.login.StatSvc
import net.mamoe.mirai.internal.network.protocol.packet.login.WtLogin.Login.LoginPacketResponse
import net.mamoe.mirai.internal.network.protocol.packet.login.WtLogin.Login.LoginPacketResponse.Captcha
import net.mamoe.mirai.internal.network.protocol.packet.login.wtlogin.WtLogin10
import net.mamoe.mirai.internal.network.protocol.packet.login.wtlogin.WtLogin2
import net.mamoe.mirai.internal.network.protocol.packet.login.wtlogin.WtLogin20
import net.mamoe.mirai.internal.network.protocol.packet.login.wtlogin.WtLogin9
import net.mamoe.mirai.internal.network.protocol.packet.sendAndExpect
import net.mamoe.mirai.network.*
import net.mamoe.mirai.utils.BotConfiguration.MiraiProtocol
import net.mamoe.mirai.utils.LoginSolver
import net.mamoe.mirai.utils.info
import net.mamoe.mirai.utils.withExceptionCollector

/**
 * Handles login, and acts also as a mediator of [BotInitProcessor]
 */
internal interface SsoProcessor {
    val client: QQAndroidClient
    val ssoSession: SsoSession

    var firstLoginSucceed: Boolean
    val registerResp: StatSvc.Register.Response?

    /**
     * The observers to launch jobs for states.
     *
     * E.g. start heartbeat job for [NetworkHandler.State.OK].
     */
    fun createObserverChain(): StateObserver // todo not used

    /**
     * Do login. Throws [LoginFailedException] if failed
     */
    @Throws(LoginFailedException::class)
    suspend fun login(handler: NetworkHandler)

    suspend fun logout(handler: NetworkHandler)

    companion object : ComponentKey<SsoProcessor>
}

/**
 * Strategy that performs the process of single sing-on (SSO). (login)
 *
 * And allows to retire the [session][ssoSession] after success.
 *
 * Used by [NettyNetworkHandler.StateConnecting].
 */
internal class SsoProcessorImpl(
    val ssoContext: SsoProcessorContext,
) : SsoProcessor {

    ///////////////////////////////////////////////////////////////////////////
    // public
    ///////////////////////////////////////////////////////////////////////////

    @Volatile
    override var firstLoginSucceed: Boolean = false

    @Volatile
    override var registerResp: StatSvc.Register.Response? = null

    @Volatile
    override var client = createClient(ssoContext.bot)

    override val ssoSession: SsoSession get() = client
    override fun createObserverChain(): StateObserver = StateObserver.chainOfNotNull(
        object : StateChangedObserver(State.OK) {
            override fun stateChanged0(
                networkHandler: NetworkHandlerSupport,
                previous: NetworkHandlerSupport.BaseStateImpl,
                new: NetworkHandlerSupport.BaseStateImpl
            ) {
                new.launch { }
            }
        }
    )

    /**
     * Do login. Throws [LoginFailedException] if failed
     */
    @Throws(LoginFailedException::class)
    override suspend fun login(handler: NetworkHandler) = withExceptionCollector {
        ssoContext.bot.components[BdhSessionSyncer].loadServerListFromCache()
        if (client.wLoginSigInfoInitialized) {
            kotlin.runCatching {
                FastLoginImpl(handler).doLogin()
            }.onFailure { e ->
                collectException(e)
                SlowLoginImpl(handler).doLogin()
            }
        } else {
            client = createClient(ssoContext.bot)
            SlowLoginImpl(handler).doLogin()
        }
        ssoContext.accountSecretsManager.saveSecrets(ssoContext.account, AccountSecretsImpl(client))
        registerClientOnline(handler)
        ssoContext.bot.logger.info { "Login successful." }
    }

    private suspend fun registerClientOnline(handler: NetworkHandler): StatSvc.Register.Response {
        return StatSvc.Register.online(client).sendAndExpect(handler).also { registerResp = it }
    }

    override suspend fun logout(handler: NetworkHandler) {
        handler.sendWithoutExpect(StatSvc.Register.offline(client))
    }

    private fun createClient(bot: QQAndroidBot): QQAndroidClient {
        val device = ssoContext.device
        return QQAndroidClient(
            ssoContext.account,
            device = device,
            accountSecrets = ssoContext.accountSecretsManager.getSecretsOrCreate(ssoContext.account, device)
        ).apply {
            _bot = bot
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // login
    ///////////////////////////////////////////////////////////////////////////

    // we have exactly two methods----slow and fast.

    private abstract inner class LoginStrategy(
        val handler: NetworkHandler,
    ) {
        protected val context get() = handler.context
        protected val bot get() = context.bot
        protected val logger get() = bot.logger

        protected suspend fun <R : Packet?> OutgoingPacketWithRespType<R>.sendAndExpect(): R = sendAndExpect(handler)

        abstract suspend fun doLogin()
    }

    private inner class SlowLoginImpl(handler: NetworkHandler) : LoginStrategy(handler) {

        private fun loginSolverNotNull(): LoginSolver {
            fun LoginSolver?.notnull(): LoginSolver {
                checkNotNull(this) {
                    "No LoginSolver found. Please provide by BotConfiguration.loginSolver. " +
                            "For example use `BotFactory.newBot(...) { loginSolver = yourLoginSolver}` in Kotlin, " +
                            "use `BotFactory.newBot(..., new BotConfiguration() {{ setLoginSolver(yourLoginSolver) }})` in Java."
                }
                return this
            }

            return bot.configuration.loginSolver.notnull()
        }

        private val sliderSupported get() = bot.configuration.loginSolver?.isSliderCaptchaSupported ?: false

        private fun createUnsupportedSliderCaptchaException(allowSlider: Boolean): UnsupportedSliderCaptchaException {
            return UnsupportedSliderCaptchaException(
                buildString {
                    append("Mirai 无法完成滑块验证.")
                    if (allowSlider) {
                        append(" 使用协议 ")
                        append(ssoContext.protocol)
                        append(" 强制要求滑块验证, 请更换协议后重试.")
                    }
                    append(" 另请参阅: https://github.com/project-mirai/mirai-login-solver-selenium")
                }
            )
        }

        override suspend fun doLogin() = withExceptionCollector {

            var allowSlider = sliderSupported || bot.configuration.protocol == MiraiProtocol.ANDROID_PHONE

            var response: LoginPacketResponse = WtLogin9(client, allowSlider).sendAndExpect()

            mainloop@ while (true) {
                when (response) {
                    is LoginPacketResponse.Success -> {
                        logger.info { "Login successful" }
                        break@mainloop
                    }
                    is LoginPacketResponse.DeviceLockLogin -> {
                        response = WtLogin20(client).sendAndExpect()
                    }

                    is LoginPacketResponse.UnsafeLogin -> {
                        loginSolverNotNull().onSolveUnsafeDeviceLoginVerify(bot, response.url)
                        response = WtLogin9(client, allowSlider).sendAndExpect()
                    }

                    is Captcha.Picture -> {
                        var result = loginSolverNotNull().onSolvePicCaptcha(bot, response.data)
                        if (result == null || result.length != 4) {
                            //refresh captcha
                            result = "ABCD"
                        }
                        response = WtLogin2.SubmitPictureCaptcha(client, response.sign, result).sendAndExpect()
                    }

                    is Captcha.Slider -> {
                        if (sliderSupported) {
                            // use solver
                            val ticket = try {
                                loginSolverNotNull().onSolveSliderCaptcha(bot, response.url)?.takeIf { it.isNotEmpty() }
                            } catch (e: LoginFailedException) {
                                collectThrow(e)
                            } catch (error: Throwable) {
                                if (allowSlider) {
                                    collectException(error)
                                    allowSlider = false
                                    response = WtLogin9(client, allowSlider).sendAndExpect()
                                    continue@mainloop
                                }
                                collectThrow(error)
                            }
                            response = if (ticket == null) {
                                WtLogin9(client, allowSlider).sendAndExpect()
                            } else {
                                WtLogin2.SubmitSliderCaptcha(client, ticket).sendAndExpect()
                            }
                        } else {
                            // retry once
                            if (!allowSlider) collectThrow(createUnsupportedSliderCaptchaException(allowSlider))
                            allowSlider = false
                            response = WtLogin9(client, allowSlider).sendAndExpect()
                        }
                    }

                    is LoginPacketResponse.Error -> {
                        if (response.message.contains("0x9a")) { //Error(title=登录失败, message=请你稍后重试。(0x9a), errorInfo=)
                            collectThrow(RetryLaterException().initCause(IllegalStateException("Login failed: $response")))
                        }
                        val msg = response.toString()
                        collectThrow(WrongPasswordException(buildString(capacity = msg.length) {
                            append(msg)
                            if (msg.contains("当前上网环境异常")) { // Error(title=禁止登录, message=当前上网环境异常，请更换网络环境或在常用设备上登录或稍后再试。, errorInfo=)
                                append(", tips=若频繁出现, 请尝试开启设备锁")
                            }
                        }))
                    }

                    is LoginPacketResponse.SMSVerifyCodeNeeded -> {
                        val message = "SMS required: $response, which isn't yet supported"
                        logger.error(message)
                        collectThrow(UnsupportedSMSLoginException(message))
                    }
                }
            }

        }
    }

    private inner class FastLoginImpl(handler: NetworkHandler) : LoginStrategy(handler) {
        override suspend fun doLogin() {
            val login10 = WtLogin10(client).sendAndExpect(handler)
            check(login10 is LoginPacketResponse.Success) { "Fast login failed: $login10" }
        }
    }
}