/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal

import net.mamoe.mirai.utils.isSameClass
import net.mamoe.mirai.utils.md5

internal actual class BotAccount actual constructor(
    internal actual val id: Long,
    actual val passwordMd5: ByteArray,
    actual val phoneNumber: String,
) {
    actual constructor(id: Long, passwordPlainText: String, phoneNumber: String) : this(
        id,
        passwordPlainText.md5(),
        phoneNumber
    )

    actual override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BotAccount || !isSameClass(this, other)) return false

        if (id != other.id) return false
        if (!passwordMd5.contentEquals(other.passwordMd5)) return false

        return true
    }

    actual override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + passwordMd5.hashCode()
        return result
    }

    /**
     * 登录之后发送SyncFirstView才能获取 因此考虑一下var
     */
    internal actual var tinyId: Long = 0L
}