/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */
@file:Suppress("EXPERIMENTAL_API_USAGE", "DEPRECATION_ERROR")

package net.mamoe.mirai.internal


internal expect class BotAccount {
    internal val id: Long
    /**
     * 登录之后发送SyncFirstView才能获取 因此考虑一下var
     */
    internal var tinyId : Long

    val phoneNumber: String

    constructor(id: Long, passwordMd5: ByteArray, phoneNumber: String = "")
    constructor(id: Long, passwordPlainText: String, phoneNumber: String = "")

    val passwordMd5: ByteArray

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
}