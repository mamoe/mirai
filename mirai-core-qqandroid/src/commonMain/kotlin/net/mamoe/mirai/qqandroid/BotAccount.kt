/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("EXPERIMENTAL_API_USAGE", "DEPRECATION_ERROR")

package net.mamoe.mirai.qqandroid

import kotlinx.io.core.toByteArray
import net.mamoe.mirai.qqandroid.utils.MiraiPlatformUtils
import net.mamoe.mirai.utils.MiraiExperimentalAPI
import kotlin.jvm.JvmSynthetic

internal data class BotAccount(
    @JvmSynthetic
    internal val id: Long,
    @JvmSynthetic
    @MiraiExperimentalAPI
    val passwordMd5: ByteArray // md5
) {
    constructor(id: Long, passwordPlainText: String) : this(id, MiraiPlatformUtils.md5(passwordPlainText.toByteArray()))
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as BotAccount

        if (id != other.id) return false
        if (!passwordMd5.contentEquals(other.passwordMd5)) return false

        return true
    }


    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + passwordMd5.contentHashCode()
        return result
    }
}