/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.auth

import net.mamoe.mirai.Bot
import net.mamoe.mirai.network.LoginFailedException

/**
 * 二维码扫描登录监听器
 *
 * @since 2.15
 */
public interface QRCodeLoginListener {

    /**
     * 使用二维码登录时获取的二维码图片大小字节数.
     */
    public val qrCodeSize: Int get() = 3

    /**
     * 使用二维码登录时获取的二维码边框宽度像素.
     */
    public val qrCodeMargin: Int get() = 4

    /**
     * 使用二维码登录时获取的二维码校正等级，必须为 1-3 之间.
     */
    public val qrCodeEcLevel: Int get() = 2

    /**
     * 每隔 [qrCodeStateUpdateInterval] 毫秒更新一次[二维码状态][State]
     */
    public val qrCodeStateUpdateInterval: Long get() = 5000

    /**
     * 从服务器获取二维码时调用，在下级显示二维码并扫描.
     *
     * @param data 二维码图像数据 (文件)
     */
    public fun onFetchQRCode(bot: Bot, data: ByteArray)

    /**
     * 当二维码状态变化时调用.
     * @see State
     */
    public fun onStateChanged(bot: Bot, state: State)

    /**
     * 每隔一段时间会调用一次此函数
     *
     * 在此函数抛出 [LoginFailedException] 以中断登录
     */
    public fun onIntervalLoop() {
    }

    /**
     * 当二维码登录扫描完毕时执行, 在此执行资源释放
     */
    public fun onCompleted() {
    }

    public enum class State {
        /**
         * 等待扫描中，请在此阶段请扫描二维码.
         * @see QRCodeLoginListener.onFetchQRCode
         */
        WAITING_FOR_SCAN,

        /**
         * 二维码已扫描，等待扫描端确认登录.
         */
        WAITING_FOR_CONFIRM,

        /**
         * 扫描后取消了确认.
         */
        CANCELLED,

        /**
         * 二维码超时，必须重新获取二维码.
         */
        TIMEOUT,

        /**
         * 二维码已确认，将会继续登录.
         */
        CONFIRMED,

        /**
         * 默认状态，在登录前通常为此状态.
         */
        DEFAULT,
    }
}