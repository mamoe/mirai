package net.mamoe.mirai.utils

import kotlinx.atomicfu.atomic
import java.util.concurrent.ConcurrentLinkedDeque

class StepUtil {
    val step = atomic(0)
    val exceptions = ConcurrentLinkedDeque<Throwable>()
    fun step(step: Int, message: String = "Wrong step") {
        println("Invoking step $step")
        if (step != this.step.getAndIncrement()) {
            throw IllegalStateException(message).also { exceptions.add(it) }
        }
    }

    fun throws() {
        if (exceptions.isEmpty()) return
        val root = exceptions.poll()!!
        while (true) {
            root.addSuppressed(exceptions.poll() ?: throw root)
        }
    }
}