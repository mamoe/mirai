/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */


package net.mamoe.mirai.internal.test

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration

fun runBlockingUnit(
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScope.() -> Unit
) {
    return runBlocking(context) {
        withTimeout(Duration.seconds(60)) { // always checks for infinite runs.
            block()
        }
    }
}

fun runBlockingUnit(
    context: CoroutineContext = EmptyCoroutineContext,
    timeout: Duration,
    block: suspend CoroutineScope.() -> Unit
) {
    runBlockingUnit(context) {
        withTimeout(timeout) {
            block()
        }
    }
}