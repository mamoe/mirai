/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.components

import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.QQAndroidClient
import net.mamoe.mirai.internal.network.WLoginSigInfo
import net.mamoe.mirai.internal.network.component.ComponentKey
import net.mamoe.mirai.internal.network.handler.NetworkHandler
import net.mamoe.mirai.internal.network.handler.selector.NetworkException
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacketWithRespType
import net.mamoe.mirai.internal.network.protocol.packet.login.DeviceVerificationResultImpl
import net.mamoe.mirai.internal.network.protocol.packet.login.SmsDeviceVerificationResult
import net.mamoe.mirai.internal.network.protocol.packet.login.StatSvc
import net.mamoe.mirai.internal.network.protocol.packet.login.UrlDeviceVerificationResult
import net.mamoe.mirai.internal.network.protocol.packet.login.WtLogin.Login.LoginPacketResponse
import net.mamoe.mirai.internal.network.protocol.packet.login.WtLogin.Login.LoginPacketResponse.Captcha
import net.mamoe.mirai.internal.network.protocol.packet.login.wtlogin.*
import net.mamoe.mirai.network.LoginFailedException
import net.mamoe.mirai.network.RetryLaterException
import net.mamoe.mirai.network.UnsupportedSliderCaptchaException
import net.mamoe.mirai.network.WrongPasswordException
import net.mamoe.mirai.utils.BotConfiguration.MiraiProtocol
import net.mamoe.mirai.utils.LoginSolver
import net.mamoe.mirai.utils.info
import net.mamoe.mirai.utils.withExceptionCollector
import kotlin.coroutines.cancellation.CancellationException
import kotlin.jvm.Volatile

/**
 * Handles login, and acts also as a mediator of [BotInitProcessor]
 */
internal interface SsoProcessor {
    val client: QQAndroidClient
    val ssoSession: SsoSession

    val firstLoginResult: AtomicRef<FirstLoginResult?> // null means just initialized
    val firstLoginSucceed: Boolean get() = firstLoginResult.value?.success ?: false
    val registerResp: StatSvc.Register.Response?

    /**
     * Do login. Throws [LoginFailedException] if failed
     */
    @Throws(LoginFailedException::class, CancellationException::class)
    suspend fun login(handler: NetworkHandler)

    suspend fun logout(handler: NetworkHandler)

    suspend fun sendRegister(handler: NetworkHandler): StatSvc.Register.Response

    companion object : ComponentKey<SsoProcessor>
}

/**
 * Wraps [LoginFailedException] into [NetworkException]
 */
internal class LoginFailedExceptionAsNetworkException(
    private val underlying: LoginFailedException
) : NetworkException(underlying.message ?: "Login failed", underlying, !underlying.killBot) {
    override fun unwrapForPublicApi(): Throwable {
        return underlying
    }
}

internal enum class FirstLoginResult(
    val success: Boolean,
    val canRecoverOnFirstLogin: Boolean,
) {
    PASSED(true, true),
    CHANGE_SERVER(false, true), // by ConfigPush
    OTHER_FAILURE(false, false),
}

/**
 * Contains secrets for encryption and decryption during a session created by [SsoProcessor] and [PacketCodec].
 *
 * @see AccountSecrets
 */
internal interface SsoSession {
    var outgoingPacketSessionId: ByteArray

    /**
     * always 0 for now.
     */
    var loginState: Int

    // also present in AccountSecrets
    var wLoginSigInfo: WLoginSigInfo
    val randomKey: ByteArray
}

/**
 * Strategy that performs the process of single sing-on (SSO). (login)
 *
 * And allows to retire the [session][ssoSession] after success.
 *
 * Used by `NettyNetworkHandler.StateConnecting`.
 */
internal class SsoProcessorImpl(
    val ssoContext: SsoProcessorContext,
) : SsoProcessor {

    ///////////////////////////////////////////////////////////////////////////
    // public
    ///////////////////////////////////////////////////////////////////////////

    override val firstLoginResult: AtomicRef<FirstLoginResult?> = atomic(null)

    @Volatile
    override var registerResp: StatSvc.Register.Response? = null

    override var client
        get() = ssoContext.bot.components[BotClientHolder].client
        set(value) {
            ssoContext.bot.components[BotClientHolder].client = value
        }

    override val ssoSession: SsoSession get() = client
    private val components get() = ssoContext.bot.components

    /**
     * Do login. Throws [LoginFailedException] if failed
     */
    override suspend fun login(handler: NetworkHandler) = withExceptionCollector {
        components[BdhSessionSyncer].loadServerListFromCache()
        try {
            if (client.wLoginSigInfoInitialized) {
                ssoContext.bot.components[EcdhInitialPublicKeyUpdater].refreshInitialPublicKeyAndApplyEcdh()
                kotlin.runCatching {
                    FastLoginImpl(handler).doLogin()
                }.onFailure { e ->
                    collectException(e)
                    SlowLoginImpl(handler).doLogin()
                }
            } else {
                client = createClient(ssoContext.bot)
                ssoContext.bot.components[EcdhInitialPublicKeyUpdater].refreshInitialPublicKeyAndApplyEcdh()
                SlowLoginImpl(handler).doLogin()
            }
        } catch (e: Exception) {
            // Failed to log in, invalidate secrets.
            ssoContext.bot.components[AccountSecretsManager].invalidate()
            throw e
        }
        components[AccountSecretsManager].saveSecrets(ssoContext.account, AccountSecretsImpl(client))
        registerClientOnline(handler)
        ssoContext.bot.logger.info { "Login successful." }
    }

    override suspend fun sendRegister(handler: NetworkHandler): StatSvc.Register.Response {
        return registerClientOnline(handler).also { registerResp = it }
    }

    private suspend fun registerClientOnline(handler: NetworkHandler): StatSvc.Register.Response {
        return handler.sendAndExpect(StatSvc.Register.online(client)).also {
            registerResp = it
        }
    }

    override suspend fun logout(handler: NetworkHandler) {
        handler.sendWithoutExpect(StatSvc.Register.offline(client))
    }

    private fun createClient(bot: QQAndroidBot): QQAndroidClient {
        val device = ssoContext.device
        return QQAndroidClient(
            ssoContext.account,
            device = device,
            accountSecrets = bot.components[AccountSecretsManager].getSecretsOrCreate(ssoContext.account, device)
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

        protected suspend fun <R : Packet?> OutgoingPacketWithRespType<R>.sendAndExpect(): R =
            handler.sendAndExpect(this)

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

                    is LoginPacketResponse.VerificationNeeded -> {
                        val result = loginSolverNotNull().onSolveDeviceVerification(
                            bot, response.requests
                        )
                        check(result is DeviceVerificationResultImpl)
                        response = when (result) {
                            is UrlDeviceVerificationResult -> {
                                WtLogin9(client, allowSlider).sendAndExpect()
                            }
                            is SmsDeviceVerificationResult -> {
                                WtLogin7(client, result.token, result.code).sendAndExpect()
                            }
                        }
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
                            collectThrow(RetryLaterException("Login failed: $response"))
                        }
                        val msg = response.toString()
                        collectThrow(WrongPasswordException(buildString(capacity = msg.length) {
                            append(msg)
                            if (msg.contains("当前上网环境异常")) { // Error(title=禁止登录, message=当前上网环境异常，请更换网络环境或在常用设备上登录或稍后再试。, errorInfo=)
                                append(", mirai 提示: 若频繁出现, 请尝试开启设备锁")
                            }
                            if (msg.contains("当前登录存在安全风险")) { // Error(title=禁止登录, message=当前上网环境异常，请更换网络环境或在常用设备上登录或稍后再试。, errorInfo=)
                                append(", mirai 提示: 这可能是尝试登录次数过多导致的, 请等待一段时间后再试")
                            }
                            if (!sliderSupported) {
                                append(", extra={ sliderSupported=false, login-solver=").append(bot.configuration.loginSolver)
                                append(" <").append(
                                    bot.configuration.loginSolver?.let { it::class }
                                )
                                append("> }")
                                if (msg.contains("版本过低")) {
                                    append(", mirai 提示: 提供给 mirai 的验证码处理器不支持滑块验证, 请报告至此验证器的作者")
                                }
                            }
                        }))
                    }

                    is LoginPacketResponse.SmsRequestSuccess -> {
                        error("Unexpected response: $response")
                    }
                }
            }

        }
    }

    private inner class FastLoginImpl(handler: NetworkHandler) : LoginStrategy(handler) {
        override suspend fun doLogin() {
            val login10 = handler.sendAndExpect(WtLogin10(client))
            check(login10 is LoginPacketResponse.Success) { "Fast login failed: $login10" }
        }
    }
}