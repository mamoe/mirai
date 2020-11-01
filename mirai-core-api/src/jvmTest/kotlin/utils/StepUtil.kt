/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

/*
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

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