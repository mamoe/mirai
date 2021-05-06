/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.utils

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.sample
import kotlin.coroutines.CoroutineContext

@OptIn(FlowPreview::class)
internal class ScheduledJob(
    coroutineContext: CoroutineContext,
    private val intervalMillis: Long,
    private val task: suspend () -> Unit
) : CoroutineScope by CoroutineScope(coroutineContext + SupervisorJob(coroutineContext[Job])) {
    private val coroutineExceptionHandler =
        coroutineContext[CoroutineExceptionHandler].also {
            requireNotNull(it) {
                "Could not init ScheduledJob, coroutineExceptionHandler == null"
            }
        }

    private val channel = Channel<Unit>(Channel.CONFLATED)

    fun notice() {
        if (intervalMillis == 0L) {
            launch { task() }
        } else channel.trySend(Unit)
    }

    private suspend fun doTask() {
        runCatching {
            task()
        }.onFailure {
            coroutineExceptionHandler!!.handleException(currentCoroutineContext(), it)
        }
    }

    init {
        if (intervalMillis != 0L) {
            launch {
                channel.receiveAsFlow()
                    .runCatching {
                        sample(intervalMillis)
                    }
                    .fold(
                        onSuccess = { flow ->
                            flow.collect { doTask() }
                        },
                        onFailure = {
                            // binary change
                            while (isActive) {
                                delay(intervalMillis)
                                task()
                            }
                        }
                    )
            }
        }
    }
}