/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.internal.shutdown

import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.internal.pluginManagerImpl
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.description
import java.io.File
import java.io.FileDescriptor
import java.io.FileOutputStream
import java.io.PrintStream
import java.lang.management.ManagementFactory
import java.lang.reflect.Method
import java.nio.file.Paths
import java.time.Instant
import java.time.ZoneOffset
import java.util.*
import java.util.concurrent.ConcurrentLinkedDeque
import kotlin.concurrent.thread
import kotlin.io.path.writeText

internal object ShutdownDaemon {
    private object ThreadInfoJava9Access {
        private val isDaemonM: Method?
        private val getPriorityM: Method?

        init {
            var idm: Method? = null
            var gpm: Method? = null
            kotlin.runCatching {
                val klass = Class.forName("java.lang.management.ThreadInfo")
                val mts = klass.methods.asSequence()
                idm = mts.firstOrNull { it.name == "isDaemon" }
                gpm = mts.firstOrNull { it.name == "getPriority" }
            }
            isDaemonM = idm
            getPriorityM = gpm
        }

        fun isDaemon(inf: Any): Boolean {
            isDaemonM?.invoke(inf)?.let { return it as Boolean }
            return false
        }

        fun getPriority(inf: Any): Int {
            getPriorityM?.invoke(inf)?.let { return it as Int }
            return -1
        }

        val canGetPri: Boolean get() = getPriorityM != null
    }

    val pluginDisablingThreads = ConcurrentLinkedDeque<Thread>()


    private val Thread.State.isWaiting: Boolean
        get() = this == Thread.State.WAITING || this == Thread.State.TIMED_WAITING

    fun start() {
        val crtThread = Thread.currentThread()
        thread(name = "Mirai Console Shutdown Daemon", isDaemon = true) { listen(crtThread) }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun dumpCrashReport(saveError: Boolean) {
        val isAndroidSystem = kotlin.runCatching { Class.forName("android.util.Log") }.isSuccess
        val sb = StringBuilder(1024).append("\n\n")
        val now = System.currentTimeMillis()
        sb.append("=============================================================\n")
        sb.append("MIRAI CONSOLE CRASH REPORT.\n")
        sb.append("Console has take too long to shutdown.\n\n")
        sb.append("TIME: ").append(now).append(" <")

        fun msgAfterTimeDump() {
            sb.append(">\nSYSTEM: ").append(System.getProperty("os.name")).append(" ")
                .append(System.getProperty("os.arch")).append(" ").append(System.getProperty("os.version"))

            sb.append("\nJRT:\n  ")
            sb.append(System.getProperty("java.runtime.name"))
            sb.append("  ").append(System.getProperty("java.version"))
            sb.append("\n    by ").append(System.getProperty("java.vendor"))
            sb.append("\nSPEC:\n  ").append(System.getProperty("java.specification.name")).append(" ")
                .append(System.getProperty("java.specification.version"))
            sb.append("\n    by ").append(System.getProperty("java.specification.vendor"))
            sb.append("\nVM:\n  ").append(System.getProperty("java.vm.name")).append(" ")
                .append(System.getProperty("java.vm.version"))
            sb.append("\n    by ").append(System.getProperty("java.vm.vendor"))

            sb.append("\n\n")
            kotlin.runCatching {
                sb.append("\nPROCESS Working dir: ").append(File("a").absoluteFile.parent ?: File(".").absoluteFile)
                sb.append("\nConsole Working Dir: ").append(MiraiConsole.rootPath.toAbsolutePath())
            }
            sb.append("\nLoaded plugins:\n")
            kotlin.runCatching {
                MiraiConsole.pluginManagerImpl.resolvedPlugins.forEach { plugin ->
                    val desc = plugin.description
                    sb.append("|- ").append(desc.name).append(" v").append(desc.version).append('\n')
                    sb.append("|   `- ID: ").append(desc.id).append('\n')
                    desc.author.takeUnless { it.isBlank() }?.let {
                        sb.append("|   `- AUTHOR: ").append(it).append('\n')
                    }
                    sb.append("|   `- MAIN: ").append(plugin.javaClass).append('\n')
                    plugin.javaClass.protectionDomain?.codeSource?.location?.let { from ->
                        @Suppress("IntroduceWhenSubject")
                        val f: Any = when {
                            from.protocol == "file" -> Paths.get(from.toURI())
                            else -> from
                        }
                        sb.append("|   `- FROM: ").append(f).append('\n')
                    }
                }
            }
            sb.append("\n\n\n")
        }

        if (isAndroidSystem) {
            sb.append(Date(now))
            msgAfterTimeDump()
            sb.append("\n\nTHREADS:\n\n")
            val threads = Thread.getAllStackTraces()
            threads.forEach { (thread, stackTrace) ->
                sb.append("\n\n\n").append(thread).append('\n')
                stackTrace.forEach { stack ->
                    sb.append('\t').append(stack).append('\n')
                }
            }
        } else {
            object { // Android doesn't contain management system & classing boxing
                fun a() {
                    sb.append(Instant.ofEpochMilli(now).atOffset(ZoneOffset.UTC))
                    msgAfterTimeDump()

                    val rtMxBean = ManagementFactory.getRuntimeMXBean()
                    sb.append("PROCESS: ").append(rtMxBean.name)
                    sb.append("\nVM OPTIONS:\n")
                    rtMxBean.inputArguments.forEach { cmd ->
                        sb.append("  ").append(cmd).append("\n")
                    }
                    sb.append("\n\nTHREADS:\n\n")

                    val threadMxBean = ManagementFactory.getThreadMXBean()
                    val infs = threadMxBean.dumpAllThreads(true, true)
                    infs.forEach { inf ->
                        sb.append("\n\n").append('"')
                        sb.append(inf.threadName)
                        sb.append('"')
                        if (ThreadInfoJava9Access.isDaemon(inf)) {
                            sb.append(" daemon")
                        }
                        if (ThreadInfoJava9Access.canGetPri) {
                            sb.append(" prio=").append(ThreadInfoJava9Access.getPriority(inf))
                        }
                        sb.append(" Id=").append(inf.threadId)
                        inf.lockName?.let { sb.append(" on ").append(it) }
                        inf.lockOwnerName?.let { lon ->
                            sb.append(" owned by \"").append(lon)
                            sb.append("\" Id=").append(inf.lockOwnerId)
                        }
                        if (inf.isSuspended) sb.append(" (suspended)")
                        if (inf.isInNative) sb.append(" (in native)")
                        sb.append('\n')
                        val lockInf = inf.lockInfo
                        val lockedMonitors = inf.lockedMonitors

                        inf.stackTrace.forEachIndexed { index, stackTraceElement ->
                            sb.append("\tat ").append(stackTraceElement).append('\n')
                            if (index == 0 && lockInf != null) {
                                when (inf.threadState!!) {
                                    Thread.State.BLOCKED -> {
                                        sb.append("\t-  blocked on ").append(lockInf).append('\n')
                                    }
                                    Thread.State.WAITING,
                                    Thread.State.TIMED_WAITING -> {
                                        sb.append("\t-  waiting on ").append(lockInf).append('\n')
                                    }
                                    else -> {}
                                }
                            }
                            lockedMonitors.forEach { mi ->
                                if (mi.lockedStackDepth == index) {
                                    sb.append("\t-  locked ").append(mi).append('\n')
                                }
                            }
                        }
                        sb.append("\n\n")
                    }
                }
            }.a()
        }
        sb.append("\n\n")
        val report = sb.toString()
        if (!isAndroidSystem && saveError) {
            kotlin.runCatching {
                PrintStream(FileOutputStream(FileDescriptor.err)).println(report)
            }
        }
        if (saveError) {
            val fileName = "CONSOLE_CRASH_REPORT_${now}.log"
            kotlin.runCatching {
                MiraiConsole.rootPath.resolve(fileName).writeText(report)
            }.recoverCatching {
                if (!isAndroidSystem) {
                    File("CONSOLE_CRASH_REPORT_${now}.log").writeText(report)
                }
            }
        }
        kotlin.runCatching {
            MiraiConsole.mainLogger.error(report)
        }
    }

    private fun listen(thread: Thread) {
        val startTime = System.currentTimeMillis()
        val timeout = 1000L * 60
        while (thread.isAlive) {
            val crtTime = System.currentTimeMillis()
            if (crtTime - startTime >= timeout) {
                kotlin.runCatching {
                    dumpCrashReport(saveError = true)
                }
                pluginDisablingThreads.forEach { threadKill(it) }
                threadKill(thread)
                pluginDisablingThreads.clear()
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