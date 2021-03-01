/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.utils

import kotlinx.coroutines.withTimeoutOrNull
import net.mamoe.mirai.utils.cast
import kotlin.math.roundToInt


internal suspend inline fun <R, reified IP> Collection<Pair<IP, Int>>.retryWithServers(
    timeoutMillis: Long,
    onFail: (exception: Throwable?) -> Nothing,
    crossinline block: suspend (ip: String, port: Int) -> R
): R {
    require(this.isNotEmpty()) { "receiver of retryWithServers must not be empty" }

    var exception: Throwable? = null
    for (pair in this) {
        return kotlin.runCatching {
            withTimeoutOrNull(timeoutMillis) {
                if (IP::class == Int::class) {
                    block(pair.first.cast<Int>().toIpV4AddressString(), pair.second)
                } else {
                    block(pair.first.toString(), pair.second)
                }
            }
        }.recover {
            if (exception != null) {
                it.addSuppressed(exception!!)
            }
            exception = it // so as to show last exception followed by suppressed others
            null
        }.getOrNull() ?: continue
    }

    onFail(exception)
}

internal fun Int.sizeToString() = this.toLong().sizeToString()
internal fun Long.sizeToString(): String {
    return if (this < 1024) {
        "$this B"
    } else ((this * 100.0 / 1024).roundToInt() / 100.0).toString() + " KiB"
}