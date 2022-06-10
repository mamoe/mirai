/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.message

import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

internal data class ContextualBugReportException(
    override val message: String,
    override val cause: Throwable?,
) : IllegalStateException()

internal fun contextualBugReportException(
    context: String,
    forDebug: String?,
    e: Throwable? = null,
    additional: String = "",
): ContextualBugReportException {
    return ContextualBugReportException(
        "在 $context 时遇到了意料之中的问题. 请完整复制此日志提交给 mirai: https://github.com/mamoe/mirai/issues/new/choose   $additional 调试信息: $forDebug",
        e,
    )
}

internal inline fun <R> runWithBugReport(context: String, forDebug: () -> String, block: () -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        callsInPlace(forDebug, InvocationKind.AT_MOST_ONCE)
    }

    return runCatching(block).getOrElse {
        throw contextualBugReportException(context, forDebug(), it)
    }
}
