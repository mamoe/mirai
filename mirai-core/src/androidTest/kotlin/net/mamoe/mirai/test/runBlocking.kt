package net.mamoe.mirai.test

import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

internal actual fun <R> runBlocking(
    context: CoroutineContext,
    block: suspend CoroutineScope.() -> R
): R = kotlinx.coroutines.runBlocking(context, block)