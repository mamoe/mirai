/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmName("CommonUtils")

package net.mamoe.mirai.console.internal.util

internal inline fun <reified E : Throwable, R> runIgnoreException(block: () -> R): R? {
    try {
        return block()
    } catch (e: Throwable) {
        if (e is E) return null
        throw e
    }
}

internal inline fun <reified E : Throwable> runIgnoreException(block: () -> Unit): Unit? {
    try {
        return block()
    } catch (e: Throwable) {
        if (e is E) return null
        throw e
    }
}