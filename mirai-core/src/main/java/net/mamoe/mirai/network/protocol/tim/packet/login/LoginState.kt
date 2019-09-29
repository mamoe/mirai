package net.mamoe.mirai.network.protocol.tim.packet.login

/**
 * @author Him188moe
 */
enum class LoginState {
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
     * 超时
     */
    TIMEOUT,
}