package net.mamoe.mirai.qqandroid.utils

/**
 * 连接类型
 */
inline class NetworkType(val value: Int) {
    companion object {
        /**
         * 移动网络
         */
        val MOBILE = NetworkType(1)
        /**
         * Wifi
         */
        val WIFI = NetworkType(2)

        /**
         * 其他任何类型
         */
        val OTHER = NetworkType(0)
    }
}