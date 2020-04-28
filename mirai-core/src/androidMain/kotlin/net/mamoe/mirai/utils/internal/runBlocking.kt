package net.mamoe.mirai.utils.internal

import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

/**
 * Mirror of `runBlocking` from `kotlinx.serialization` on JVM
 */
internal actual fun <T> runBlocking(
    context: CoroutineContext,
    block: suspend CoroutineScope.() -> T
): T = kotlinx.coroutines.runBlocking(context, block)