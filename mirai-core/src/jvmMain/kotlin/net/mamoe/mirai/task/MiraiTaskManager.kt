package net.mamoe.mirai.task

import java.util.concurrent.Callable
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer
import java.util.function.Predicate

/**
 * @author NaturalHG
 */

/*
class MiraiTaskManager private constructor() {

    private val pool: MiraiThreadPool

    init {
        this.pool = MiraiThreadPool()
    }

    /**
     * 基础Future处理
     */

    fun execute(runnable: Runnable) {
        this.execute(runnable, MiraiTaskExceptionHandler.printing())
    }

    fun execute(runnable: Runnable, handler: MiraiTaskExceptionHandler) {
        this.pool.execute {
            try {
                runnable.run()
            } catch (e: Exception) {
                handler.onHandle(e)
            }
        }
    }


    fun <D> submit(callable: Callable<D>): Future<D> {
        return this.submit(callable, MiraiTaskExceptionHandler.printing())
    }

    fun <D> submit(callable: Callable<D>, handler: MiraiTaskExceptionHandler): Future<D> {
        return this.pool.submit<D> {
            try {
                callable.call()
            } catch (e: Throwable) {
                handler.onHandle(e)
                null
            }
        }
    }

    /**
     * 异步任务
     */
    fun <D> ansycTask(callable: Callable<D>, callback: Consumer<D>) {
        this.ansycTask(callable, callback, MiraiTaskExceptionHandler.printing())
    }

    fun <D> ansycTask(callable: Callable<D>, callback: Consumer<D>, handler: MiraiTaskExceptionHandler) {
        this.pool.execute {
            try {
                callback.accept(callable.call())
            } catch (e: Throwable) {
                handler.onHandle(e)
            }
        }
    }

    /**
     * 定时任务
     */

    fun repeatingTask(runnable: Runnable, intervalMillis: Long) {
        this.repeatingTask(runnable, intervalMillis, MiraiTaskExceptionHandler.printing())
    }

    fun repeatingTask(runnable: Runnable, intervalMillis: Long, handler: MiraiTaskExceptionHandler) {
        this.repeatingTask<Runnable>(runnable, intervalMillis, { a -> true }, handler)
    }

    fun repeatingTask(runnable: Runnable, intervalMillis: Long, times: Int) {
        this.repeatingTask(runnable, intervalMillis, times, MiraiTaskExceptionHandler.printing())
    }

    fun repeatingTask(runnable: Runnable, intervalMillis: Long, times: Int, handler: MiraiTaskExceptionHandler) {
        val integer = AtomicInteger(times - 1)
        this.repeatingTask<Runnable>(
                runnable, intervalMillis, { a -> integer.getAndDecrement() > 0 }, handler
        )
    }


    fun <D : Runnable> repeatingTask(runnable: D, intervalMillis: Long, shouldContinue: Predicate<D>, handler: MiraiTaskExceptionHandler) {
        Thread {
            do {
                this.pool.execute {
                    try {
                        runnable.run()
                    } catch (e: Exception) {
                        handler.onHandle(e)
                    }
                }
                try {
                    Thread.sleep(intervalMillis)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }

            } while (shouldContinue.test(runnable))
        }.start()
    }

    fun deletingTask(runnable: Runnable, intervalMillis: Long) {
        Thread {
            try {
                Thread.sleep(intervalMillis)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

            this.pool.execute(runnable)
        }.start()
    }

    companion object {

        val instance = MiraiTaskManager()
    }

}
*/