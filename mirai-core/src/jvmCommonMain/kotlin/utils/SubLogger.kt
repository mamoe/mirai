/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.utils

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.coroutineName


internal fun CoroutineExceptionHandler.Key.fromMiraiLogger(
    logger: MiraiLogger,
    ignoreCancellationException: Boolean = true, // kotlinx.coroutines default
): CoroutineExceptionHandler {
    return CoroutineExceptionHandler { coroutineContext, throwable ->
        if (!ignoreCancellationException || throwable !is CancellationException) {
            logger.error("Exception in coroutine '${coroutineContext.coroutineName}'", throwable)
        }
    }
}

@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
internal fun MiraiLogger.subLogger(name: String): MiraiLogger = subLoggerImpl(this, name)