/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.utils.internal

import kotlin.reflect.KClass

internal expect fun Throwable.addSuppressedMirai(e: Throwable)


// Currently we can't share internal code between modules.
@Suppress("DuplicatedCode", "INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "RESULT_CLASS_IN_RETURN_TYPE")
@kotlin.internal.InlineOnly
internal inline fun <R> retryCatching(
    n: Int,
    except: KClass<out Throwable>? = null,
    block: (count: Int, lastException: Throwable?) -> R
): Result<R> {
    require(n >= 0) {
        "param n for retryCatching must not be negative"
    }
    var exception: Throwable? = null
    repeat(n) {
        try {
            return Result.success(block(it, exception))
        } catch (e: Throwable) {
            if (except?.isInstance(e) == true) {
                return Result.failure(e)
            }
            exception?.addSuppressedMirai(e)
            exception = e
        }
    }
    return Result.failure(exception!!)
}