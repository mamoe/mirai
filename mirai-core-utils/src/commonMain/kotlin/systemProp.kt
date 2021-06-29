/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */


@file:JvmMultifileClass
@file:JvmName("MiraiUtils")

package net.mamoe.mirai.utils

import java.util.concurrent.ConcurrentHashMap


public fun systemProp(name: String, default: String): String =
    System.getProperty(name, default) ?: default

public fun systemProp(name: String, default: Boolean): Boolean =
    System.getProperty(name, default.toString())?.toBoolean() ?: default


public fun systemProp(name: String, default: Long): Long =
    System.getProperty(name, default.toString())?.toLongOrNull() ?: default


private val debugProps = ConcurrentHashMap<String, Boolean>()
public fun Any?.toDebugString(prop: String, default: Boolean = false): String {
    if (this == null) return "null"
    val debug = debugProps.getOrPut(prop) { systemProp(prop, default) }
    return if (debug) {
        "${this::class.simpleName}($this)"
    } else {
        "${this::class.simpleName}"
    }
}