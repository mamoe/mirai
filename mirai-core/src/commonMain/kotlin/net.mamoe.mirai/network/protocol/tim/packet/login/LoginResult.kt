package net.mamoe.mirai.network.protocol.tim.packet.login

import net.mamoe.mirai.network.protocol.tim.packet.login.LoginResult.SUCCESS
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * 登录结果. 除 [SUCCESS] 外均为失败.
 * @see LoginResult.requireSuccess 要求成功
 */
enum class LoginResult {
    /**
     * 登录成功
     */
    SUCCESS,

    /**
     * 密码错误
     */
    WRONG_PASSWORD,

    /**
     * 被冻结
     */
    BLOCKED,

    /**
     * QQ 号码输入有误
     */
    UNKNOWN_QQ_NUMBER,

    /**
     * 账号开启了设备锁. 暂不支持设备锁登录
     */
    DEVICE_LOCK,

    /**
     * 账号被回收
     */
    TAKEN_BACK,

    /**
     * 未知. 更换服务器或等几分钟再登录可能解决.
     */
    UNKNOWN,

    /**
     * 包数据错误
     */
    INTERNAL_ERROR,

    /**
     * 超时
     */
    TIMEOUT,
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
fun LoginResult.requireSuccess() {
    if (requireSuccessOrNull() === null)
        error("Login failed: $this")
}

/**
 * 检查 [this] 为 [LoginResult.SUCCESS].
 * 失败则返回 `null`
 *
 * @return 成功时 [Unit], 失败时 `null`
 */
fun LoginResult.requireSuccessOrNull(): Unit? {
    return if (this == SUCCESS) Unit else null
}


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
