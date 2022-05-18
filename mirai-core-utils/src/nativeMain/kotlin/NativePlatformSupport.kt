/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

import kotlinx.cinterop.*

public inline infix fun UShort.flag(flag: UShort): Boolean = this and flag != 0u.toUShort()
public inline infix fun UInt.flag(flag: UInt): Boolean = this and flag != 0u
public inline infix fun UInt.flag(flag: Int): Boolean = this and flag.toUInt() != 0u
public inline infix fun Int.flag(flag: UInt): Boolean = this.toUInt() and flag != 0u
public inline infix fun ULong.flag(flag: ULong): Boolean = this and flag != 0uL

public val NULL_PTR: COpaquePointerVar = nativeHeap.alloc()
public inline fun <reified T : NativePointed> nullPtr(): T = NULL_PTR.reinterpret()