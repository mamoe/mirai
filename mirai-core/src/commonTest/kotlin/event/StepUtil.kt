/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.event

import kotlinx.atomicfu.atomic
import net.mamoe.mirai.utils.ConcurrentLinkedDeque

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