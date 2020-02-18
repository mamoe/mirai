/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.utils

@MiraiInternalAPI
expect fun Throwable.addSuppressed(e: Throwable)

@MiraiInternalAPI
@Suppress("DuplicatedCode")
inline fun <R> tryNTimes(repeat: Int, block: () -> R): R {
    var lastException: Throwable? = null

    repeat(repeat) {
        try {
            return block()
        } catch (e: Throwable) {
            if (lastException == null) {
                lastException = e
            }
            lastException!!.addSuppressed(e)
        }
    }

    throw lastException!!
}

@MiraiInternalAPI
@Suppress("DuplicatedCode")
inline fun <R> tryNTimesOrNull(repeat: Int, block: () -> R): R? {
    var lastException: Throwable? = null

    repeat(repeat) {
        try {
            return block()
        } catch (e: Throwable) {
            if (lastException == null) {
                lastException = e
            }
            lastException!!.addSuppressed(e)
        }
    }

    return null
}

@MiraiInternalAPI
@Suppress("DuplicatedCode")
inline fun <R> tryNTimesOrException(repeat: Int, block: () -> R): Throwable? {
    var lastException: Throwable? = null

    repeat(repeat) {
        try {
            block()
            return null
        } catch (e: Throwable) {
            if (lastException == null) {
                lastException = e
            }
            lastException!!.addSuppressed(e)
        }
    }

    return lastException!!
}