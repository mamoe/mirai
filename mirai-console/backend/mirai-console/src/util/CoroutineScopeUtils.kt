/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.util

import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.loop
import kotlinx.coroutines.*
import net.mamoe.mirai.console.internal.util.runIgnoreException
import net.mamoe.mirai.utils.currentTimeMillis
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@ConsoleExperimentalApi
public object CoroutineScopeUtils {
    @JvmStatic
    @ConsoleExperimentalApi
    public fun CoroutineContext.overrideWithSupervisorJob(name: String? = null): CoroutineContext =
        this + NamedSupervisorJob(name ?: "<unnamed>", this[Job])

    @JvmStatic
    @ConsoleExperimentalApi
    public fun CoroutineScope.childScope(
        name: String? = null,
        context: CoroutineContext = EmptyCoroutineContext
    ): CoroutineScope =
        CoroutineScope(this.childScopeContext(name, context))

    @JvmStatic
    @ConsoleExperimentalApi
    public fun CoroutineScope.childScopeContext(
        name: String? = null,
        context: CoroutineContext = EmptyCoroutineContext
    ): CoroutineContext =
        this.coroutineContext.overrideWithSupervisorJob(name) + context.let {
            if (name != null) it + CoroutineName(name)
            else it
        }
}

/**
 * Runs `action` every `intervalMillis` since each time [setChanged] is called, ignoring subsequent calls during the interval.
 */
internal class TimedTask(
    scope: CoroutineScope,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    intervalMillis: Long,
    action: suspend CoroutineScope.() -> Unit,
) {
    companion object {
        private const val UNCHANGED = 0L
    }

    private val lastChangedTime = atomic(UNCHANGED)

    fun setChanged() {
        lastChangedTime.value = currentTimeMillis()
    }

    val job: Job = scope.launch(coroutineContext) {
        // `delay` always checks for cancellation
        lastChangedTime.loop { last ->
            val current = currentTimeMillis()
            if (last == UNCHANGED) {
                runIgnoreException<CancellationException> {
                    delay(3000) // accuracy not necessary
                } ?: return@launch
            } else {
                if (current - last > intervalMillis) {
                    if (!lastChangedTime.compareAndSet(last, UNCHANGED)) return@loop
                    action()
                }
                runIgnoreException<CancellationException> {
                    delay(3000) // accuracy not necessary
                } ?: return@launch
            }
        }
    }
}

internal fun CoroutineScope.launchTimedTask(
    intervalMillis: Long,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    action: suspend CoroutineScope.() -> Unit,
) = TimedTask(this, coroutineContext, intervalMillis, action)

@ConsoleExperimentalApi
public class NamedSupervisorJob @JvmOverloads constructor(
    private val name: String,
    parent: Job? = null
) : CompletableJob by SupervisorJob(parent) {
    override fun toString(): String {
        return "NamedSupervisorJob($name)"
    }
}
