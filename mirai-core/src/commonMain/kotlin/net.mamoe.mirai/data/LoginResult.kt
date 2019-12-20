@file:Suppress("unused")

package net.mamoe.mirai.data

import net.mamoe.mirai.utils.BotConfiguration

/**
 * 登录失败的原因
 */
enum class LoginResult(val id: Byte) {
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