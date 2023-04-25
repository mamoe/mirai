/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.components

import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import net.mamoe.mirai.auth.BotAuthInfo
import net.mamoe.mirai.auth.BotAuthorization
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.QQAndroidClient
import net.mamoe.mirai.internal.network.QRCodeLoginData
import net.mamoe.mirai.internal.network.WLoginSigInfo
import net.mamoe.mirai.internal.network.auth.AuthControl
import net.mamoe.mirai.internal.network.auth.BotAuthorizationWithSecretsProtection
import net.mamoe.mirai.internal.network.component.ComponentKey
import net.mamoe.mirai.internal.network.handler.NetworkHandler
import net.mamoe.mirai.internal.network.handler.logger
import net.mamoe.mirai.internal.network.handler.selector.NetworkException
import net.mamoe.mirai.internal.network.handler.selector.SelectorRequireReconnectException
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
import net.mamoe.mirai.utils.*
import net.mamoe.mirai.utils.BotConfiguration.MiraiProtocol
import kotlin.coroutines.cancellation.CancellationException
import kotlin.jvm.Volatile

/**
 * Handles login, and acts also as a mediator of [BotInitProcessor]
 */
internal interface SsoProcessor {
    val client: QQAndroidClient
    val ssoSession: SsoSession

    val firstLoginResult: FirstLoginResult? // null means just initialized
    fun casFirstLoginResult(
        expect: FirstLoginResult?,
        update: FirstLoginResult?
    ): Boolean // enable compiler optimization

    fun setFirstLoginResult(value: FirstLoginResult?)

    val firstLoginSucceed: Boolean get() = firstLoginResult?.success ?: false
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

    private val _firstLoginResult: AtomicRef<FirstLoginResult?> = atomic(null)
    override val firstLoginResult get() = _firstLoginResult.value
    override fun casFirstLoginResult(expect: FirstLoginResult?, update: FirstLoginResult?): Boolean =
        _firstLoginResult.compareAndSet(expect, update)

    override fun setFirstLoginResult(value: FirstLoginResult?) {
        _firstLoginResult.value = value
    }

    @Volatile
    override var registerResp: StatSvc.Register.Response? = null

    override var client
        get() = ssoContext.bot.components[BotClientHolder].client
        set(value) {
            ssoContext.bot.components[BotClientHolder].client = value
        }

    override val ssoSession: SsoSession get() = client
    private val components get() = ssoContext.bot.components

    private val botAuthInfo = object : BotAuthInfo {
        override val id: Long
            get() = ssoContext.bot.id
        override val deviceInfo: DeviceInfo
            get() = ssoContext.device
        override val configuration: BotConfiguration
            get() = ssoContext.bot.configuration
    }

    /**
     * Do login. Throws [LoginFailedException] if failed
     */
    override suspend fun login(handler: NetworkHandler) {

        fun initAndStartAuthControl() {
            authControl = AuthControl(
                botAuthInfo,
                ssoContext.bot.account.authorization,
                ssoContext.bot.network.logger,
                ssoContext.bot.coroutineContext, // do not use network context because network may restart whilst auth control should keep alive
            ).also { it.start() }
        }

        suspend fun loginSuccess() {
            components[AccountSecretsManager].saveSecrets(ssoContext.account, AccountSecretsImpl(client))
            sendRegister(handler)
            ssoContext.bot.logger.info { "Login successful." }
        }

        if (authControl == null) {
            ssoContext.bot.account.let { account ->
                if (account.accountSecretsKeyBuffer == null) {

                    account.accountSecretsKeyBuffer = when (val authorization = account.authorization) {
                        is BotAuthorizationWithSecretsProtection -> authorization.calculateSecretsKeyImpl(botAuthInfo)
                        else -> SecretsProtection.EscapedByteBuffer(authorization.calculateSecretsKey(botAuthInfo))
                    }
                }
            }

            components[CacheValidator].validate()

            components[BdhSessionSyncer].loadServerListFromCache()

            // try fast login
            if (client.wLoginSigInfoInitialized) {
                ssoContext.bot.components[EcdhInitialPublicKeyUpdater].refreshInitialPublicKeyAndApplyEcdh()
                kotlin.runCatching {
                    FastLoginImpl(handler).doLogin()
                }.onFailure { e ->
                    initAndStartAuthControl()
                    authControl!!.exceptionCollector.collect(e)

                    throw SelectorRequireReconnectException()
                }

                loginSuccess()

                return
            }
        }

        if (authControl == null) initAndStartAuthControl()
        val authControl0 = authControl!!


        var nextAuthMethod: AuthMethod? = null
        try {
            ssoContext.bot.components[BotClientHolder].refreshClient()
            ssoContext.bot.components[EcdhInitialPublicKeyUpdater].refreshInitialPublicKeyAndApplyEcdh()

            when (val authw = authControl0.acquireAuth().also { nextAuthMethod = it }) {
                is AuthMethod.Error -> {
                    authControl = null
                    throw authw.exception
                }

                AuthMethod.NotAvailable -> {
                    authControl = null
                    error("No more auth method available")
                }

                is AuthMethod.Pwd -> {
                    SlowLoginImpl(handler, LoginType.Password(authw.passwordMd5)).doLogin()
                }

                AuthMethod.QRCode -> {
                    val rsp = ssoContext.bot.components[QRCodeLoginProcessor].prepareProcess(
                        handler, client
                    ).process(handler, client)

                    SlowLoginImpl(handler, LoginType.QRCode(rsp)).doLogin()
                }
            }

            authControl!!.actComplete()
            authControl = null
        } catch (exception: Throwable) {
            if (exception is SelectorRequireReconnectException) {
                throw exception
            }

            ssoContext.bot.network.logger.warning({ "Failed with auth method: $nextAuthMethod" }, exception)
            authControl0.exceptionCollector.collectException(exception)

            if (nextAuthMethod !is AuthMethod.Error && nextAuthMethod != null) {
                authControl0.actMethodFailed(exception)
            }

            if (exception is NetworkException) {
                if (exception.recoverable) throw exception
            }

            if (nextAuthMethod == null || nextAuthMethod is AuthMethod.NotAvailable || nextAuthMethod is AuthMethod.Error) {
                authControl = null
                authControl0.exceptionCollector.throwLast()
            }

            throw SelectorRequireReconnectException()
        }

        loginSuccess()

    }


    sealed class AuthMethod {
        object NotAvailable : AuthMethod() {
            override fun toString(): String = "NotAvailable"
        }

        object QRCode : AuthMethod() {
            override fun toString(): String = "QRCode"
        }

        class Pwd(val passwordMd5: SecretsProtection.EscapedByteBuffer) : AuthMethod() {
            override fun toString(): String = "Password@${hashCode()}"
        }

        /**
         * Exception in [BotAuthorization]
         */
        class Error(val exception: Throwable) : AuthMethod() {
            override fun toString(): String = "Error[$exception]@${hashCode()}"
        }
    }

    private var authControl: AuthControl? = null

    override suspend fun sendRegister(handler: NetworkHandler): StatSvc.Register.Response {
        return registerClientOnline(handler).also { registerResp = it }
    }

    private suspend fun registerClientOnline(handler: NetworkHandler): StatSvc.Register.Response {
        return handler.sendAndExpect(StatSvc.Register.online(client)).also {
            registerResp = it
        }
    }

    override suspend fun logout(handler: NetworkHandler) {
        if (firstLoginSucceed) {
            handler.sendWithoutExpect(StatSvc.Register.offline(client))
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

    private inner class SlowLoginImpl(
        handler: NetworkHandler,
        private val loginType: LoginType
    ) : LoginStrategy(handler) {

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
                    append(", extra={ login-solver=")
                    bot.configuration.loginSolver.let { ls ->
                        append(ls)
                        append(" <")
                        append(ls?.let { it::class })
                        append(">")
                    }
                    append(" 另请参阅: https://github.com/project-mirai/mirai-login-solver-selenium")
                }
            )
        }

        override suspend fun doLogin() = withExceptionCollector {

            @Suppress("FunctionName")
            fun SSOWtLogin9(allowSlider: Boolean) = when (loginType) {
                is LoginType.Password -> WtLogin9.Password(client, loginType.passwordMd5.asByteArray, allowSlider)
                is LoginType.QRCode -> WtLogin9.QRCode(client, loginType.qrCodeLoginData)
            }

            var allowSlider = sliderSupported || bot.configuration.protocol == MiraiProtocol.ANDROID_PHONE

            var response: LoginPacketResponse = SSOWtLogin9(allowSlider).sendAndExpect()

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
                                SSOWtLogin9(allowSlider).sendAndExpect()
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
                            } catch (error: Throwable) {
                                collectThrow(error)
                            }
                            response = if (ticket == null) {
                                SSOWtLogin9(allowSlider).sendAndExpect()
                            } else {
                                WtLogin2.SubmitSliderCaptcha(client, ticket).sendAndExpect()
                            }
                        } else {
                            // retry once
                            if (!allowSlider) collectThrow(createUnsupportedSliderCaptchaException(allowSlider))
                            // allowSlider = false
                            // TODO Reconnect without slider request
                            //      Need to create new connection NOT send it in current connection
                            // response = WtLogin9(client, allowSlider).sendAndExpect()
                            collectThrow(createUnsupportedSliderCaptchaException(false))
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

    private sealed class LoginType {
        class Password(val passwordMd5: SecretsProtection.EscapedByteBuffer) : LoginType()
        class QRCode(val qrCodeLoginData: QRCodeLoginData) : LoginType()
    }

    private inner class FastLoginImpl(handler: NetworkHandler) : LoginStrategy(handler) {
        override suspend fun doLogin() {
            val login10 = handler.sendAndExpect(WtLogin10(client))
            check(login10 is LoginPacketResponse.Success) { "Fast login failed: $login10" }
        }
    }
}