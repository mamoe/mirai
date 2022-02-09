/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.terminal

import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.utils.TestOnly
import java.io.File
import java.io.RandomAccessFile
import java.nio.file.Files
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.withLock
import kotlin.concurrent.write

@OptIn(ConsoleTerminalExperimentalApi::class)
internal object LoggingService {
    @TestOnly
    internal lateinit var switchLogFileNow: () -> Unit

    private val threads = Executors.newScheduledThreadPool(3, object : ThreadFactory {
        private val group = ThreadGroup("mirai console terminal logging")
        private val counter = AtomicInteger(0)
        override fun newThread(r: Runnable): Thread {
            return Thread(
                group,
                r,
                "Mirai Console Terminal Logging Thread#" + counter.getAndIncrement()
            ).also { thread ->
                thread.isDaemon = true
            }
        }
    })
    val lines = ConcurrentLinkedQueue<String>()
    var isLogServiceRunning: () -> Boolean = { false }

    val notice = ReentrantLock()
    val condition = notice.newCondition()

    fun setup(logFiles: File) {
        if (ConsoleTerminalSettings.noLogging) return
        logFiles.mkdirs()
        // read  lock: write logs
        // write lock: change logging file

        val rwFile = AtomicReference<RandomAccessFile>()
        val rwLock = ReentrantReadWriteLock()

        val latestFile = logFiles.resolve("latest.log")

        isLogServiceRunning = { true }

        fun switchLogFile() {
            if (!isLogServiceRunning()) return
            try {
                rwLock.write {
                    rwFile.get()?.close()
                    if (latestFile.isFile) {
                        var targetFile: File
                        var counter = 0
                        do {
                            targetFile = logFiles.resolve("log-$counter.log")
                            counter++
                        } while (targetFile.exists())
                        Files.move(latestFile.toPath(), targetFile.toPath())
                    }
                    rwFile.set(RandomAccessFile(latestFile, "rw").also { it.seek(it.length()) })
                }
            } catch (e: Throwable) {
                MiraiConsole.mainLogger.warning(e)
            }
        }

        @OptIn(TestOnly::class)
        switchLogFileNow = ::switchLogFile

        switchLogFile()

        val task = threads.submit {
            while (!Thread.interrupted()) {
                val nextLine = lines.poll()
                if (nextLine == null) {
                    notice.withLock { condition.await() }
                    continue
                }
                rwLock.readLock().withLock {
                    rwFile.get().let { rw ->
                        rw.write(nextLine.toByteArray())
                        rw.write('\n'.code)
                    }
                }
            }
        }

        isLogServiceRunning = { !task.isDone && !task.isCancelled }

        val nextDayTimeSec = Instant.now()
            .atZone(ZoneId.systemDefault())
            .plus(1, ChronoUnit.DAYS)
            .withHour(0)
            .withMinute(0)
            .withSecond(0)
            .toEpochSecond()

        threads.scheduleAtFixedRate(
            ::switchLogFile,
            nextDayTimeSec * 1000 - System.currentTimeMillis(),
            TimeUnit.DAYS.toMillis(1), TimeUnit.MILLISECONDS
        )
    }

    fun pushLine(line: String) {
        if (ConsoleTerminalSettings.noLogging) return
        if (!isLogServiceRunning()) return
        lines.add(line)
        notice.withLock { condition.signal() }
    }
}