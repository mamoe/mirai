package net.mamoe.mirai.utils

import kotlinx.atomicfu.atomic

class StepUtil {
    val step = atomic(0)
    fun step(step: Int, message: String = "Wrong step") {
        if (step != this.step.getAndIncrement()) {
            throw IllegalStateException(message)
        }
        println("Invoking step $step")
    }
}