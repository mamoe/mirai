/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.internal.util

import kotlinx.coroutines.*
import kotlinx.coroutines.future.future
import net.mamoe.mirai.console.plugin.jvm.JavaPluginScheduler
import net.mamoe.mirai.console.util.CoroutineScopeUtils.overrideWithSupervisorJob
import java.util.concurrent.Callable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import kotlin.coroutines.CoroutineContext

internal class JavaPluginSchedulerImpl internal constructor(parentCoroutineContext: CoroutineContext) : CoroutineScope,
    JavaPluginScheduler {
    override val coroutineContext: CoroutineContext =
        parentCoroutineContext.overrideWithSupervisorJob(this.toString())

    override fun repeating(intervalMs: Long, runnable: Runnable): Future<Void?> {
        return this.future {
            while (isActive) {
                withContext(Dispatchers.IO) { runnable.run() }
                delay(intervalMs)
            }
            null
        }
    }

    override fun delayed(delayMillis: Long, runnable: Runnable): CompletableFuture<Void?> {
        return future {
            delay(delayMillis)
            withContext(Dispatchers.IO) {
                runnable.run()
            }
            null
        }
    }

    override fun <R> delayed(delayMillis: Long, runnable: Callable<R>): CompletableFuture<Void?> {
        return future {
            delay(delayMillis)
            withContext(Dispatchers.IO) { runnable.call() }
            null
        }
    }

    override fun <R> async(supplier: Callable<R>): Future<R> {
        return future {
            withContext(Dispatchers.IO) { supplier.call() }
        }
    }

    override fun async(runnable: Runnable): Future<Void?> {
        return future {
            withContext(Dispatchers.IO) { runnable.run() }
            null
        }
    }
}