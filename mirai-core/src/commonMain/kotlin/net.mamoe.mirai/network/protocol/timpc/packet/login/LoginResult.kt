@file:Suppress("unused")

package net.mamoe.mirai.network.protocol.timpc.packet.login

import net.mamoe.mirai.network.protocol.timpc.packet.login.LoginResult.SUCCESS
import net.mamoe.mirai.utils.BotConfiguration
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * 登录结果. 除 [SUCCESS] 外均为失败.
 * @see LoginResult.requireSuccess 要求成功
 */
enum class LoginResult(val id: Byte) {
    /**
     * 登录成功
     */
    SUCCESS(0),

    /**
     * 密码错误
     */
    WRONG_PASSWORD(1),

    /**
     * 被冻结
     */
    BLOCKED(2),

    /**
     * QQ 号码输入有误
     */
    UNKNOWN_QQ_NUMBER(3),

    /**
     * 账号开启了设备锁. 暂不支持设备锁登录
     */
    DEVICE_LOCK(4),

    /**
     * 账号被回收
     */
    TAKEN_BACK(5),

    /**
     * 未知. 更换服务器或等几分钟再登录可能解决.
     */
    UNKNOWN(6),

    /**
     * 包数据错误
     */
    INTERNAL_ERROR(7),

    /**
     * 超时
     */
    TIMEOUT(8),

    /**
     * 网络不可用
     */
    NETWORK_UNAVAILABLE(9),

    /**
     * 需要验证码且 [BotConfiguration.failOnCaptcha] 为 `true`
     */
    CAPTCHA(10),

    /**
     * 该号码长期未登录, 为了保证账号安全, 已被系统设置成保护状态, 请用手机 TIM 最新版本登录, 登录成功后即可自动解除保护模式
     */ // TIM的错误代码为 00020
    PROTECTED(11),
}

/**
 * 如果 [this] 不为 [LoginResult.SUCCESS] 就抛出消息为 [lazyMessage] 的 [IllegalStateException]
 */
@UseExperimental(ExperimentalContracts::class)
inline fun LoginResult.requireSuccess(lazyMessage: (LoginResult) -> String) {
    contract {
        callsInPlace(lazyMessage, InvocationKind.AT_MOST_ONCE)
    }
    if (this != SUCCESS) error(lazyMessage(this))
}


/**
 * 检查 [this] 为 [LoginResult.SUCCESS].
 * 失败则 [error]
 */
@Suppress("NOTHING_TO_INLINE")
inline fun LoginResult.requireSuccess() = requireSuccess { "Login failed: $this" }

/**
 * 检查 [this] 为 [LoginResult.SUCCESS].
 * 失败则返回 `null`
 *
 * @return 成功时 [Unit], 失败时 `null`
 */
@Suppress("NOTHING_TO_INLINE")
inline fun LoginResult.requireSuccessOrNull(): Unit? = if (this == SUCCESS) Unit else null

/**
 * 返回 [this] 是否为 [LoginResult.SUCCESS].
 */
@Suppress("NOTHING_TO_INLINE")
@UseExperimental(ExperimentalContracts::class)
inline fun LoginResult.isSuccess(): Boolean = this == SUCCESS

/**
 * 检查 [this] 为 [LoginResult.SUCCESS].
 * 失败则返回 `null`
 *
 * @return 成功时 [Unit], 失败时 `null`
 */
@UseExperimental(ExperimentalContracts::class)
inline fun <R> LoginResult.ifFail(block: (LoginResult) -> R): R? {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }
    return if (this != SUCCESS) block(this) else null
}
