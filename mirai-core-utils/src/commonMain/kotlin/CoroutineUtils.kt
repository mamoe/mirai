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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext

@Suppress("unused", "INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "DeprecatedCallableAddReplaceWith")
@Deprecated(
    message = "Use runBIO which delegates to `runInterruptible`. " +
            "Technically remove suspend call in `block` and remove CoroutineScope parameter usages.",
    level = DeprecationLevel.WARNING
)
@kotlin.internal.LowPriorityInOverloadResolution
public suspend inline fun <R> runBIO(
    noinline block: suspend CoroutineScope.() -> R
): R = withContext(Dispatchers.IO, block)

public suspend inline fun <R> runBIO(
    noinline block: () -> R
): R = runInterruptible(context = Dispatchers.IO, block = block)