/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.frontendbase

import kotlinx.coroutines.CoroutineScope
import net.mamoe.mirai.console.MiraiConsoleImplementation
import net.mamoe.mirai.console.frontendbase.logging.AsyncLogRecorderForwarded
import net.mamoe.mirai.console.frontendbase.logging.DailySplitLogRecorder
import net.mamoe.mirai.console.frontendbase.logging.LogRecorder
import net.mamoe.mirai.utils.MiraiLogger
import java.io.PrintStream
import java.nio.file.Path
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger


/**
 * 前端的基本实现
 *
 * @see AbstractMiraiConsoleFrontendImplementation
 */
public abstract class FrontendBase {
    /**
     * 所属前端的 [CoroutineScope]
     *
     * Implementation note: 直接返回前端实例
     */
    public abstract val scope: CoroutineScope

    public open val threadGroup: ThreadGroup by lazy {
        ThreadGroup("Mirai Console FrontEnd Threads")
    }
    public open val daemonThreadGroup: ThreadGroup by lazy {
        ThreadGroup(threadGroup, "Mirai Console FrontEnd Daemon Threads")
    }
    public open val loggingRecorder: LogRecorder by lazy { initLogRecorder() }

    /**
     * Console 的运行目录
     *
     * Implementation note: 返回 [MiraiConsoleImplementation.rootPath]
     */
    public abstract val workingDirectory: Path

    /**
     * 日志存放目录
     */
    public open val loggingDirectory: Path by lazy { workingDirectory.resolve("logs") }

    /**
     * 存储的日志是否需要去除 ansi 标志
     */
    public open val logDropAnsi: Boolean get() = true

    /**
     * 创建一个新的非守护线程, 此线程不会预先启动
     */
    public open fun newThread(name: String, task: Runnable): Thread {
        return Thread(threadGroup, task, name)
    }

    /**
     * 创建一个新的守护线程, 此线程不会预先启动
     */
    public open fun newDaemon(name: String, task: Runnable): Thread {
        return Thread(daemonThreadGroup, task, name).apply {
            isDaemon = true
        }
    }

    /**
     * 创建一个新的 [ThreadFactory], 创建的线程的名字为 `$name#{counter}`
     */
    public open fun newThreadFactory(name: String, isDemon: Boolean, postSetup: (Thread) -> Unit = {}): ThreadFactory {
        return object : ThreadFactory {
            private val group = ThreadGroup(if (isDemon) daemonThreadGroup else threadGroup, name)
            private val counter = AtomicInteger()

            override fun newThread(r: Runnable): Thread {
                return Thread(group, r, "$name#${counter.getAndIncrement()}").also {
                    it.isDaemon = isDemon
                }.also(postSetup)
            }
        }
    }

    /**
     * 将一条信息直接打印到前端的屏幕上
     *
     * Implementation note: 打印时不需要添加任何修饰, [msg] 已经是格式化好的信息
     */
    public abstract fun printToScreenDirectly(msg: String)

    @Suppress("FunctionName")
    protected open fun initScreen_forwardStdToScreen() {
        val forwarder = RepipedMessageForward { msg ->
            printToScreenDirectly(msg)
            recordToLogging(msg)
        }
        val printer = PrintStream(forwarder.pipedOutputStream, true, "UTF-8")
        System.setOut(printer)

        // stderr is reserved for printing fatal errors when something crashed in logger factory initialization
    }

    @Suppress("FunctionName")
    protected open fun initScreen_forwardStdToMiraiLogger() {
        val logStdout = MiraiLogger.Factory.create(javaClass, "stdout")
        val logStderr = MiraiLogger.Factory.create(javaClass, "stderr")

        val forwarderStdout = RepipedMessageForward(logStdout::info)
        val forwarderStderr = RepipedMessageForward(logStderr::warning)


        System.setOut(PrintStream(forwarderStdout.pipedOutputStream, true, "UTF-8"))
        System.setErr(PrintStream(forwarderStderr.pipedOutputStream, true, "UTF-8"))
    }

    /**
     * 将一条消息记录至日志 (不会显示至屏幕)
     */
    public open fun recordToLogging(msg: String) {
        loggingRecorder.record(msg)
    }

    protected open fun initLogRecorder(): LogRecorder {
        return AsyncLogRecorderForwarded(
            DailySplitLogRecorder(
                loggingDirectory,
                this
            ),
            this
        )
    }
}
