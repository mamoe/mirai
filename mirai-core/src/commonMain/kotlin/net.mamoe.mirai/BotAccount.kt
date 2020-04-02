/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai

import kotlinx.io.core.toByteArray
import net.mamoe.mirai.utils.MiraiExperimentalAPI
import net.mamoe.mirai.utils.MiraiInternalAPI
import net.mamoe.mirai.utils.internal.md5
import kotlin.annotation.AnnotationTarget.*

@MiraiInternalAPI
data class BotAccount(
    /**
     * **注意**: 在 Android 协议, 总是使用 `QQAndroidClient.uin` 或 [Bot.uin], 而不要使用 [BotAccount.id]. 将来 [BotAccount.id] 可能会变为 [String]
     */
    @RawAccountIdUse
    val id: Long,
    @MiraiExperimentalAPI
    @MiraiInternalAPI
    val passwordMd5: ByteArray // md5
) {
    constructor(id: Long, passwordPlainText: String) : this(id, passwordPlainText.toByteArray().md5())

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

/**
 * 标记直接访问 [BotAccount.id], 而不是访问 [Bot.uin]. 这可能会不兼容未来的 API 修改.
 */
@MiraiInternalAPI
@Retention(AnnotationRetention.SOURCE)
@Target(CLASS, TYPEALIAS, FUNCTION, PROPERTY, FIELD, CONSTRUCTOR)
@RequiresOptIn(level = RequiresOptIn.Level.WARNING)
annotation class RawAccountIdUse