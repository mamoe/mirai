package net.mamoe.mirai.qqandroid.network.protocol

inline class LoginType(
    val value: Int
) {
    companion object {
        /**
         * 短信验证登录
         */
        val SMS = LoginType(3)
        /**
         * 密码登录
         */
        val PASSWORD = LoginType(1)
        /**
         * 微信一键登录
         */
        val WE_CHAT = LoginType(4)
    }
}