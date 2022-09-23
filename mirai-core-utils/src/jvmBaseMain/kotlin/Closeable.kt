/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

import io.ktor.utils.io.core.use as ktorUse

public actual typealias Closeable = java.io.Closeable

@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
@kotlin.internal.LowPriorityInOverloadResolution
public actual inline fun <C : Closeable, R> C.use(block: (C) -> R): R = ktorUse(block)

public actual fun Closeable.asKtorCloseable(): io.ktor.utils.io.core.Closeable = this
public actual fun io.ktor.utils.io.core.Closeable.asMiraiCloseable(): Closeable = this