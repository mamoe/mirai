package net.mamoe.mirai.console.scheduler

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.mamoe.mirai.console.plugins.PluginBase
import java.lang.Runnable
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.function.Supplier
import kotlin.coroutines.CoroutineContext



internal object SchedulerTaskManagerInstance{
    private val schedulerTaskManagerInstance = mutableMapOf<PluginBase,PluginScheduler>()

    private val mutex = Mutex()

    fun getPluginScheduler(pluginBase: PluginBase):PluginScheduler{
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


/**
 * 作为Java插件开发者, 你应该使用PluginScheduler
 * 他们使用kotlin更高效的协程实现，并在API上对java有很高的亲和度
 * 且可以保证在PluginBase关闭的时候结束所有任务
 *
 * 你应该使用SchedulerTaskManager获取PluginScheduler, 或直接通过PluginBase获取
 */

class PluginScheduler(_coroutineContext: CoroutineContext) :CoroutineScope{
    override val coroutineContext: CoroutineContext = SupervisorJob() + _coroutineContext


    class RepeatTaskReceipt(@Volatile var cancelled:Boolean = false)
    /**
     * 新增一个Repeat Task(定时任务)
     *
     * 这个Runnable会被每intervalMs调用一次(不包含runnable执行时间)
     *
     * 作为Java使用者, 你要注意可见行, 原子性
     *
     * 在Runnable中使用Thread.sleep()不是一个明智的行为, 这会导致IO线程池的一个线程被锁死
     *
     * 使用返回的RepeatTaskReceipt, 你可以取消这个定时任务
     */
    fun repeat(runnable: Runnable, intervalMs: Long):RepeatTaskReceipt{
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
     * 新增一个Delay Task(倒计时任务)
     *
     * 这个Runnable会被再intervalMs调用一次, 之后结束
     *
     * 作为Java使用者, 你要注意可见行, 原子性
     *
     * 在Runnable中使用Thread.sleep()不是一个明智的行为, 这会导致IO线程池的一个线程被锁死
     */
    fun delay(runnable: Runnable, delayMs: Long){
        this.launch {
            delay(delayMs)
            withContext(Dispatchers.IO) {
                runnable.run()
            }
        }
    }

    /**
     * 异步执行一个任务, 最终返回Future<T>, 与java使用方法无异, 但效率更高且可以在插件关闭时停止
     */
    fun <T> async(supplier: Supplier<T>):Future<T>{
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
    fun async(runnable: Runnable){
        this.launch {
            withContext(Dispatchers.IO){
                runnable.run()
            }
        }
    }

}



/**
 * 这个类作为java与kotlin的桥接
 * 用java的interface进行了kotlin的实现
 * 使得java开发者可以使用kotlin的协程async
 * 具体使用方法与java的@link Future没有区别
 */

class AsyncResult<T>(
    private val deferred: Deferred<T>
): Future<T> {

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
            try{
                withTimeout(p1.toMillis(p0)){
                    deferred.await()
                }
            }catch (e:TimeoutCancellationException){
                throw TimeoutException()
            }
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

