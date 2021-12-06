/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.intellij.util

import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

internal val DEBUG_ENABLED = System.getenv("mirai.console.intellij.debug") == "true"

internal inline fun runIgnoringErrors(
    block: () -> Unit
) {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    try {
        block()
    } catch (e: Error) {
        // ignored
    }
}

@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
@kotlin.internal.LowPriorityInOverloadResolution
internal inline fun <R> R.runIgnoringErrors(
    block: R.() -> Unit
) {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    try {
        block()
    } catch (e: Error) {
        // ignored
    }
}