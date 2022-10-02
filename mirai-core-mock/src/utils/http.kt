/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("MiraiUtils")
@file:Suppress("NOTHING_TO_INLINE")

package net.mamoe.mirai.mock.utils

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName

public fun String.plusHttpSubpath(subpath: String): String {

    if (this[this.lastIndex] == '/') return this + subpath

    return "$this/$subpath"
}

