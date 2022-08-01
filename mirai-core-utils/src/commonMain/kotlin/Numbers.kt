/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("MiraiUtils")

package net.mamoe.mirai.utils

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName

public fun Int.toLongUnsigned(): Long = this.toLong().and(0xFFFF_FFFF)
public fun Long.toLongUnsigned(): Long = this // for native unstable types
public fun Short.toIntUnsigned(): Int = this.toUShort().toInt()
public fun Byte.toIntUnsigned(): Int = toInt() and 0xFF
public fun Int.concatAsLong(i2: Int): Long = this.toLongUnsigned().shl(Int.SIZE_BITS) or i2.toLongUnsigned()
