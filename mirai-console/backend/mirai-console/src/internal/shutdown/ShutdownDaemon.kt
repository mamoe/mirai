/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.internal.shutdown

import java.util.concurrent.ConcurrentLinkedDeque
import kotlin.concurrent.thread

internal object ShutdownDaemon {
    val pluginDisablingThreads = ConcurrentLinkedDeque<Thread>()

    private val Thread.State.isWaiting: Boolean
        get() = this == Thread.State.WAITING || this == Thread.State.TIMED_WAITING

    fun start() {
        val crtThread = Thread.currentThread()
        thread(name = "Mirai Console Shutdown Daemon", isDaemon = true) { listen(crtThread) }
    }

    private fun listen(thread: Thread) {
        val startTime = System.currentTimeMillis()
        val timeout = 1000L * 60
        while (thread.isAlive) {
            val crtTime = System.currentTimeMillis()
            if (crtTime - startTime >= timeout) {
                pluginDisablingThreads.forEach { threadKill(it) }
                threadKill(thread)
                return
            }
            Thread.sleep(1000)

            // Force intercept known death codes
            pluginDisablingThreads.forEach { pluginCrtThread ->
                val stackTraces by lazy { pluginCrtThread.stackTrace }

                if (pluginCrtThread.state.isWaiting) {
                    /// java.desktop:
                    //      WToolkit 关闭后执行 java.awt.Window.dispose() 会堵死在 native code
                    for (i in 0 until stackTraces.size.coerceAtMost(10)) {
                        val stack = stackTraces[i]
                        if (stack.className.startsWith("java.awt.") && stack.methodName.contains(
                                "dispose",
                                ignoreCase = true
                            )
                        ) {
                            pluginCrtThread.interrupt()
                            break
                        }
                    }
                }
            }
        }
    }

    private fun threadKill(thread: Thread) {
        thread.interrupt()
        Thread.sleep(10)
        if (!thread.isAlive) return
        Thread.sleep(100)
        if (!thread.isAlive) return
        Thread.sleep(500)

        @Suppress("DEPRECATION")
        if (thread.isAlive) thread.stop()
    }
}