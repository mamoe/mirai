/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.util

import kotlin.contracts.contract

/**
 * Perform `this as? T`.
 */
@JvmSynthetic
public inline fun <reified T : Any> Any?.safeCast(): T? {
    contract {
        returnsNotNull() implies (this@safeCast is T)
    }
    return this as? T
}

/**
 * Perform `this as T`.
 */
@JvmSynthetic
public inline fun <reified T : Any> Any?.cast(): T {
    contract {
        returns() implies (this@cast is T)
    }
    return this as T
}