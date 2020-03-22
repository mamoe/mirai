package net.mamoe.mirai.console.scheduler

import net.mamoe.mirai.console.plugins.PluginBase

interface SchedulerTask<T : PluginBase> {
    abstract fun onTick(i: Long)
    abstract fun onRun()
}

abstract class RepeatTask<T : PluginBase>(
    val intervalInMs: Int
) : SchedulerTask<T> {

    override fun onTick(i: Long) {
        if (i % intervalInMs == 0L) {
            onRun()
        }
    }

    companion object {
        fun <T : PluginBase> of(
            intervalInMs: Int, runnable: () -> Unit
        ): RepeatTask<T> {
            return AnonymousRepeatTask<T>(
                intervalInMs, runnable
            )
        }
    }
}

internal class AnonymousRepeatTask<T : PluginBase>(
    intervalInMs: Int, private val runnable: () -> Unit
) : RepeatTask<T>(intervalInMs) {
    override fun onRun() {
        runnable.invoke()
    }
}

fun <T : PluginBase> T.repeatTask(
    intervalInMs: Int, runnable: () -> Unit
): RepeatTask<T> {
    return AnonymousRepeatTask<T>(
        intervalInMs, runnable
    )
}
