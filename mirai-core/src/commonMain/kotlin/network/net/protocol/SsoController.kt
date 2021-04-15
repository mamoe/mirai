/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.net.protocol

import net.mamoe.mirai.internal.network.AccountSecrets
import net.mamoe.mirai.internal.network.AccountSecretsImpl
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.QQAndroidClient
import net.mamoe.mirai.internal.network.handler.NetworkHandler
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacketWithRespType
import net.mamoe.mirai.internal.network.protocol.packet.login.WtLogin.Login.LoginPacketResponse
import net.mamoe.mirai.internal.network.protocol.packet.login.WtLogin.Login.LoginPacketResponse.Captcha
import net.mamoe.mirai.internal.network.protocol.packet.login.wtlogin.WtLogin10
import net.mamoe.mirai.internal.network.protocol.packet.login.wtlogin.WtLogin2
import net.mamoe.mirai.internal.network.protocol.packet.login.wtlogin.WtLogin20
import net.mamoe.mirai.internal.network.protocol.packet.login.wtlogin.WtLogin9
import net.mamoe.mirai.internal.network.protocol.packet.sendAndExpect
import net.mamoe.mirai.internal.utils.crypto.TEA
import net.mamoe.mirai.internal.utils.io.serialization.loadAs
import net.mamoe.mirai.network.*
import net.mamoe.mirai.utils.BotConfiguration.MiraiProtocol
import net.mamoe.mirai.utils.DeviceInfo
import net.mamoe.mirai.utils.LoginSolver
import net.mamoe.mirai.utils.info
import net.mamoe.mirai.utils.withExceptionCollector
import java.io.File

internal interface SsoContext {
    var client: QQAndroidClient
}

internal class SsoController(
    private val ssoContext: SsoContext,
    private val handler: NetworkHandler,
) {
    @Throws(LoginFailedException::class)
    suspend fun login() = withExceptionCollector {
        if (bot.client.wLoginSigInfoInitialized) {
            kotlin.runCatching {
                fastLogin()
            }.onFailure { e ->
                collectException(e)
                slowLogin()
            }
        } else {
            slowLogin()
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // impl
    ///////////////////////////////////////////////////////////////////////////

    private val configuration get() = handler.context.configuration
    private val context get() = handler.context
    private val bot get() = context.bot
    private val logger get() = bot.logger
    private val account get() = bot.account


    private suspend fun fastLogin() {
        val login10 = WtLogin10(bot.client).sendAndExpect(bot)
        check(login10 is LoginPacketResponse.Success) { "Fast login failed: $login10" }
    }

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
                    append(configuration.protocol)
                    append(" 强制要求滑块验证, 请更换协议后重试.")
                }
                append(" 另请参阅: https://github.com/project-mirai/mirai-login-solver-selenium")
            }
        )
    }

    private suspend fun slowLogin() = withExceptionCollector {

        var allowSlider = sliderSupported || bot.configuration.protocol == MiraiProtocol.ANDROID_PHONE

        var response: LoginPacketResponse = WtLogin9(bot.client, allowSlider).sendAndExpect()

        mainloop@ while (true) {
            when (response) {
                is LoginPacketResponse.Success -> {
                    logger.info { "Login successful" }
                    break@mainloop
                }
                is LoginPacketResponse.DeviceLockLogin -> {
                    response = WtLogin20(bot.client).sendAndExpect(bot)
                }

                is LoginPacketResponse.UnsafeLogin -> {
                    loginSolverNotNull().onSolveUnsafeDeviceLoginVerify(bot, response.url)
                    response = WtLogin9(bot.client, allowSlider).sendAndExpect()
                }

                is Captcha.Picture -> {
                    var result = loginSolverNotNull().onSolvePicCaptcha(bot, response.data)
                    if (result == null || result.length != 4) {
                        //refresh captcha
                        result = "ABCD"
                    }
                    response = WtLogin2.SubmitPictureCaptcha(bot.client, response.sign, result).sendAndExpect()
                }

                is Captcha.Slider -> {
                    if (sliderSupported) {
                        // use solver
                        val ticket = try {
                            loginSolverNotNull().onSolveSliderCaptcha(bot, response.url)?.takeIf { it.isNotEmpty() }
                        } catch (e: LoginFailedException) {
                            throw e
                        } catch (error: Throwable) {
                            if (allowSlider) {
                                collectException(error)
                                allowSlider = false
                                response = WtLogin9(bot.client, allowSlider).sendAndExpect()
                                continue@mainloop
                            }
                            throw error
                        }
                        response = if (ticket == null) {
                            WtLogin9(bot.client, allowSlider).sendAndExpect()
                        } else {
                            WtLogin2.SubmitSliderCaptcha(bot.client, ticket).sendAndExpect()
                        }
                    } else {
                        // retry once
                        if (!allowSlider) throw createUnsupportedSliderCaptchaException(allowSlider)
                        allowSlider = false
                        response = WtLogin9(bot.client, allowSlider).sendAndExpect()
                    }
                }

                is LoginPacketResponse.Error -> {
                    if (response.message.contains("0x9a")) { //Error(title=登录失败, message=请你稍后重试。(0x9a), errorInfo=)
                        throw RetryLaterException().initCause(IllegalStateException("Login failed: $response"))
                    }
                    val msg = response.toString()
                    throw WrongPasswordException(buildString(capacity = msg.length) {
                        append(msg)
                        if (msg.contains("当前上网环境异常")) { // Error(title=禁止登录, message=当前上网环境异常，请更换网络环境或在常用设备上登录或稍后再试。, errorInfo=)
                            append(", tips=若频繁出现, 请尝试开启设备锁")
                        }
                    })
                }

                is LoginPacketResponse.SMSVerifyCodeNeeded -> {
                    val message = "SMS required: $response, which isn't yet supported"
                    logger.error(message)
                    throw UnsupportedSMSLoginException(message)
                }
            }
        }

    }

    internal fun initClient() {
        val device = configuration.deviceInfo?.invoke(bot) ?: DeviceInfo.random()
        ssoContext.client = QQAndroidClient(
            bot.account,
            device = device,
            accountSecrets = loadSecretsFromCacheOrCreate(device)
        ).apply {
            _bot = bot
        }
    }

    private suspend inline fun <R : Packet?> OutgoingPacketWithRespType<R>.sendAndExpect(): R = sendAndExpect(bot)

    ///////////////////////////////////////////////////////////////////////////
    // cache
    ///////////////////////////////////////////////////////////////////////////

    // TODO: 2021/4/14 extract a cache service

    private val cacheDir: File by lazy {
        configuration.workingDir.resolve(bot.configuration.cacheDir).apply { mkdirs() }
    }
    private val accountSecretsFile: File by lazy {
        cacheDir.resolve("account.secrets")
    }

    private fun loadSecretsFromCacheOrCreate(deviceInfo: DeviceInfo): AccountSecrets {
        val loaded = if (configuration.loginCacheEnabled && accountSecretsFile.exists()) {
            kotlin.runCatching {
                TEA.decrypt(accountSecretsFile.readBytes(), account.passwordMd5).loadAs(AccountSecretsImpl.serializer())
            }.getOrElse { e ->
                logger.error("Failed to load account secrets from local cache. Invalidating cache...", e)
                accountSecretsFile.delete()
                null
            }
        } else null
        if (loaded != null) {
            logger.info { "Loaded account secrets from local cache." }
            return loaded
        }

        return AccountSecretsImpl(deviceInfo, account) // wLoginSigInfoField is null, no need to save.
    }

}