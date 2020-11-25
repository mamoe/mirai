/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.internal.util.report

import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.internal.MiraiConsoleBuildConstants
import net.mamoe.mirai.console.internal.MiraiConsoleImplementationBridge
import net.mamoe.mirai.console.internal.data.isDirectory
import net.mamoe.mirai.console.internal.data.isFile
import net.mamoe.mirai.console.internal.data.mkdir
import net.mamoe.mirai.console.internal.plugin.PluginManagerImpl
import net.mamoe.mirai.console.permission.PermissionService
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.description
import java.io.*
import java.lang.management.LockInfo
import java.lang.management.ManagementFactory
import java.lang.management.MonitorInfo
import java.lang.management.ThreadInfo
import java.nio.file.Files
import java.nio.file.Path
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Suppress("unused")
internal class ReportGenerator(
    val pw: PrintWriter
) : Closeable {
    companion object {
        internal val threadMXBean = ManagementFactory.getThreadMXBean()
        internal val directory by lazy {
            MiraiConsole.rootPath.resolve("error-reports")
        }

        fun ThreadInfo.dumpTo(sb: Appendable) {
            sb.run {
                append('\"')
                append(threadName)
                append("\" Id=")
                append(threadId.toString())
                append(" ")
                append(threadState.toString())

                lockName?.let { append(" on ").append(it) }
                lockOwnerName?.let { append(" owned by \"").append(it).append("\" Id=").append(lockOwnerId.toString()) }
                if (isSuspended) {
                    sb.append(" (suspended)")
                }
                if (isInNative) {
                    sb.append(" (in native)")
                }
                sb.append('\n')
                var i = 0
                while (i < stackTrace.size) {
                    val ste: StackTraceElement = stackTrace[i]
                    sb.append("\tat $ste")
                    sb.append('\n')
                    if (i == 0 && lockInfo != null) {
                        when (threadState) {
                            Thread.State.BLOCKED -> {
                                sb.append("\t-  blocked on $lockInfo")
                                sb.append('\n')
                            }
                            Thread.State.WAITING -> {
                                sb.append("\t-  waiting on $lockInfo")
                                sb.append('\n')
                            }
                            Thread.State.TIMED_WAITING -> {
                                sb.append("\t-  waiting on $lockInfo")
                                sb.append('\n')
                            }
                            else -> {
                            }
                        }
                    }
                    for (mi: MonitorInfo in lockedMonitors) {
                        if (mi.lockedStackDepth == i) {
                            sb.append("\t-  locked $mi")
                            sb.append('\n')
                        }
                    }
                    i++
                }
                val locks: Array<LockInfo> = lockedSynchronizers
                if (locks.isNotEmpty()) {
                    sb.append("\n\tNumber of locked synchronizers = " + locks.size)
                    sb.append('\n')
                    for (li: LockInfo in locks) {
                        sb.append("\t- $li")
                        sb.append('\n')
                    }
                }
            }
        }

        fun generateToString(action: ReportGenerator.() -> Unit): String {
            return StringWriter().apply {
                ReportGenerator(PrintWriter(this)).use(action)
            }.toString()
        }

        fun generateReport(
            prefix: String = "",
            action: ReportGenerator.() -> Unit
        ): Path {
            val now = System.currentTimeMillis()
            var counter = 0
            var outputName = "$prefix$now.log"
            directory.mkdir()
            var path: Path
            do {
                path = directory.resolve(outputName)
                if (!path.isFile && !path.isDirectory) {
                    break
                }
                outputName = "$prefix$now-$counter.log"
                counter++
            } while (true)
            ReportGenerator(PrintWriter(BufferedWriter(OutputStreamWriter(Files.newOutputStream(path)))))
                .use(action)
            return path
        }
    }

    fun renderCurrentThread() {
        title("Current Thread")
        renderThread(Thread.currentThread())
    }

    fun renderThread(thread: Thread) {
        threadMXBean.getThreadInfo(
            longArrayOf(thread.id),
            true,
            true
        )[0].dumpTo(pw)
    }

    fun title(title: String) {
        pw.append("=============== [ ").append(title).append(" ] ===============")
        pw.println()
    }

    fun dumpSystemEnv() {
        title("System Env")

        pw.println("SysEnv")
        pw.println()
        pw.println("```")
        System.getenv().forEach { (key, value) ->
            pw.println("$key\t=\t$value")
        }
        pw.println("```")
        pw.println()
        pw.println("JavaProp")
        pw.println()
        pw.println("```")
        System.getProperties().store(pw, null)
        pw.println("```")
        pw.println()
    }

    fun dumpConsoleEnv() {
        title("Mirai Console Env")
        val buildDateFormatted =
            MiraiConsoleBuildConstants.buildDate.atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        pw.append("MiraiConsole v${MiraiConsoleBuildConstants.versionConst}, built on ")
            .append(buildDateFormatted)
            .append(".\n")
        pw.println("FrontEnd:")
        pw.append("\t").println(MiraiConsoleImplementationBridge.instance.javaClass.name)
        pw.append("\t").println(MiraiConsoleImplementationBridge.frontEndDescription.render())
        pw.println()
        pw.println("Plugins:")
        PluginManagerImpl.resolvedPlugins.forEach { plugin ->
            val desc = plugin.description
            pw.append("\t").append(desc.name).append(" v").append(desc.version.toString()).append(" by ").append(desc.author).println()
            pw.append("\t\t `-- ").println(plugin.javaClass.name)
        }
        pw.println()
        pw.println("PermissionService: ")
        pw.append("\t").println(PermissionService.INSTANCE)
        pw.append("\t\t`- ").println(PermissionService.INSTANCE.javaClass)
    }

    fun renderException(throwable: Throwable) {
        throwable.printStackTrace(pw)
        pw.println()
    }

    fun hr() {
        pw.println("====================================================")
    }

    override fun close() {
        pw.close()
    }
}