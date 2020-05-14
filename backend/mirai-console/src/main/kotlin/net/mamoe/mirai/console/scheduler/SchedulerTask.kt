package net.mamoe.mirai.console.scheduler

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.mamoe.mirai.console.plugins.PluginBase
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.function.Supplier
import kotlin.coroutines.CoroutineContext


/**
 * 作为Java插件开发者, 你应该使用PluginScheduler
 * 他们使用kotlin更高效的协程实现，并在API上对java有很高的亲和度
 * 且可以保证在PluginBase关闭的时候结束所有任务
 *
 * 你应该使用SchedulerTaskManager获取PluginScheduler, 或直接通过PluginBase获取
 */

class PluginScheduler(_coroutineContext: CoroutineContext) : CoroutineScope {
    override val coroutineContext: CoroutineContext = _coroutineContext + SupervisorJob(_coroutineContext[Job])


    class RepeatTaskReceipt(@Volatile var cancelled: Boolean = false)

    /**
     * 新增一个 Repeat Task (定时任务)
     *
     * 这个 Runnable 会被每 [intervalMs] 调用一次(不包含 [runnable] 执行时间)
     *
     * 使用返回的 [RepeatTaskReceipt], 可以取消这个定时任务
     */
    fun repeat(runnable: Runnable, intervalMs: Long): RepeatTaskReceipt {
        val receipt = RepeatTaskReceipt()

        this.launch {
            while (isActive && (!receipt.cancelled)) {
                withContext(Dispatchers.IO) {
                    runnable.run()
                }
                delay(intervalMs)
            }
        }

        return receipt
    }

    /**
     * 新增一个 Delay Task (延迟任务)
     *
     * 在延迟 [delayMs] 后执行 [runnable]
     *
     * 作为 Java 使用者, 你要注意可见性, 原子性
     */
    fun delay(runnable: Runnable, delayMs: Long) {
        this.launch {
            delay(delayMs)
            withContext(Dispatchers.IO) {
                runnable.run()
            }
        }
    }

    /**
     * 异步执行一个任务, 最终返回 [Future], 与 Java 使用方法无异, 但效率更高且可以在插件关闭时停止
     */
    fun <T> async(supplier: Supplier<T>): Future<T> {
        return AsyncResult(
            this.async {
                withContext(Dispatchers.IO) {
                    supplier.get()
                }
            }
        )
    }

    /**
     * 异步执行一个任务, 没有返回
     */
    fun async(runnable: Runnable) {
        this.launch {
            withContext(Dispatchers.IO) {
                runnable.run()
            }
        }
    }

}


/**
 * 这个类作为 Java 与 Kotlin 的桥接
 * 用 Java 的 interface 进行了 Kotlin 的实现
 * 使得 Java 开发者可以使用 Kotlin 的协程 [CoroutineScope.async]
 * 具体使用方法与 Java 的 [Future] 没有区别
 */
class AsyncResult<T>(
    private val deferred: Deferred<T>
) : Future<T> {

    override fun isDone(): Boolean {
        return deferred.isCompleted
    }

    override fun get(): T {
        return runBlocking {
            deferred.await()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun get(p0: Long, p1: TimeUnit): T {
        return runBlocking {
            withTimeoutOrNull(p1.toMillis(p0)) {
                deferred.await()
            } ?: throw TimeoutException()
        }
    }

    override fun cancel(p0: Boolean): Boolean {
        deferred.cancel()
        return true
    }

    override fun isCancelled(): Boolean {
        return deferred.isCancelled
    }
}


internal object SchedulerTaskManagerInstance {
    private val schedulerTaskManagerInstance = mutableMapOf<PluginBase, PluginScheduler>()

    private val mutex = Mutex()

    fun getPluginScheduler(pluginBase: PluginBase): PluginScheduler {
        runBlocking {
            mutex.withLock {
                if (!schedulerTaskManagerInstance.containsKey(pluginBase)) {
                    schedulerTaskManagerInstance[pluginBase] = PluginScheduler(pluginBase.coroutineContext)
                }
            }
        }
        return schedulerTaskManagerInstance[pluginBase]!!
    }
}

