package net.mamoe.mirai.console.scheduler

import net.mamoe.mirai.console.plugins.PluginBase

interface SchedulerTask<T:PluginBase>{
    abstract fun onTick(i:Long)
    abstract fun onRun()
}

abstract class RepeatTask<T:PluginBase>(
    val intervalInMs: Int
):SchedulerTask<T>{

    override fun onTick(i: Long) {
        if(i%intervalInMs == 0L){
            onRun()
        }
    }

    companion object{
        fun <T:PluginBase> of(
            intervalInMs: Int, runnable: () -> Unit
        ):RepeatTask<T>{
            return AnonymousRepeatTask<T>(
                intervalInMs, runnable
            )
        }
    }
}

internal class AnonymousRepeatTask<T: PluginBase>(
    intervalInMs: Int, private val runnable: () -> Unit
): RepeatTask<T>(intervalInMs){
    override fun onRun() {
        runnable.invoke()
    }
}

fun <T:PluginBase> T.repeatTask(
    intervalInMs: Int, runnable: () -> Unit
):RepeatTask<T>{
    return AnonymousRepeatTask<T>(
        intervalInMs, runnable
    )
}


fun <T> repeatTask(){

}

class X: PluginBase() {
    override fun onLoad() {
        //kotlin
        this.repeatTask(5){

        }
        //java1
        RepeatTask.of<X>(5){

        }
        //java2
        class Xtask:RepeatTask<X>(5){
            override fun onRun() {

            }
        }
    }
}