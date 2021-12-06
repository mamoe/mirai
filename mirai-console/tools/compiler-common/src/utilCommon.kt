/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("unused")

package net.mamoe.mirai.console.compiler.common

import org.jetbrains.kotlin.name.FqName
import kotlin.contracts.contract

val SERIALIZABLE_FQ_NAME = FqName("kotlinx.serialization.Serializable")

fun <K, V> Map<K, V>.firstValue(): V = this.entries.first().value
fun <K, V> Map<K, V>.firstKey(): K = this.entries.first().key


inline fun <reified T : Any> Any?.castOrNull(): T? {
    contract {
        returnsNotNull() implies (this@castOrNull is T)
    }
    return this as? T
}

inline fun <reified T : Any> Any?.cast(): T {
    contract {
        returns() implies (this@cast is T)
    }
    return this as T
}