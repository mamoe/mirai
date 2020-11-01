/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.protocol

internal inline class LoginType(
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