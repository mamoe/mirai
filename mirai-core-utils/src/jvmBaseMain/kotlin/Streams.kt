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

package net.mamoe.mirai.utils

import java.util.stream.Stream
import kotlin.streams.asStream

@JvmSynthetic
public inline fun <T> stream(@BuilderInference noinline block: suspend SequenceScope<T>.() -> Unit): Stream<T> =
    sequence(block).asStream()