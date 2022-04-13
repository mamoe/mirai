/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.terminal

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.onFailure
import net.mamoe.mirai.utils.TestOnly
import net.mamoe.mirai.utils.systemProp
import java.io.File
import java.io.RandomAccessFile
import java.nio.file.Files
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

// Used by https://github.com/iTXTech/soyuz, change with care.
internal sealed class LoggingService {
    @TestOnly
    internal lateinit var switchLogFileNow: () -> Unit

    internal abstract fun pushLine(line: String)
}

internal class LoggingServiceNoop : LoggingService() {
    override fun pushLine(line: String) {
    }

    init {
        @OptIn(TestOnly::class)
        switchLogFileNow = {}
    }
}

@OptIn(ConsoleTerminalExperimentalApi::class)
internal class LoggingServiceI(
    private val scope: CoroutineScope,
) : LoggingService() {

    private val threadPool = Executors.newScheduledThreadPool(3, object : ThreadFactory {
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
    private val threadDispatcher = threadPool.asCoroutineDispatcher()
    internal val pipelineSize = systemProp("mirai.console.terminal.log.buffer", 2048).toInt()
    private val pipeline = Channel<String>(capacity = pipelineSize)
    internal lateinit var autoSplitTask: Future<*>

    @Suppress("BlockingMethodInNonBlockingContext")
    fun startup(logDir: File) {
        logDir.mkdirs()

        val outputLock = Any()
        val output = AtomicReference<RandomAccessFile>()

        fun switchLogFile() {
            val latestLogFile = logDir.resolve("latest.log")
            var targetFile: File
            if (latestLogFile.isFile) {
                var counter = 0
                do {
                    targetFile = logDir.resolve("log-$counter.log")
                    counter++
                } while (targetFile.exists())

            } else {
                targetFile = latestLogFile
            }

            synchronized(outputLock) {
                output.get()?.close()
                if (latestLogFile !== targetFile) {
                    Files.move(latestLogFile.toPath(), targetFile.toPath())
                }
                output.set(RandomAccessFile(latestLogFile, "rw").also { it.seek(it.length()) })
            }
        }
        switchLogFile()

        @OptIn(TestOnly::class)
        switchLogFileNow = ::switchLogFile

        scope.launch(threadDispatcher) {
            while (isActive) {
                val nextLine = pipeline.receive()
                synchronized(outputLock) {
                    output.get().let { out ->
                        out.write(nextLine.toByteArray())
                        out.write('\n'.code)
                    }
                }
            }
        }

        // Daily split log files
        val nextDayTimeSec = Instant.now()
            .atZone(ZoneId.systemDefault())
            .plus(1, ChronoUnit.DAYS)
            .withHour(0)
            .withMinute(0)
            .withSecond(0)
            .toEpochSecond()

        autoSplitTask = threadPool.scheduleAtFixedRate(
            ::switchLogFile,
            nextDayTimeSec * 1000 - System.currentTimeMillis(),
            TimeUnit.DAYS.toMillis(1), TimeUnit.MILLISECONDS
        )

        scope.coroutineContext.job.invokeOnCompletion {
            threadPool.shutdown()
            synchronized(outputLock) {
                output.get()?.close()
            }
        }
    }

    private fun pushInPool(line: String) {
        scope.launch(threadDispatcher, start = CoroutineStart.UNDISPATCHED) {
            pipeline.send(line)
        }
    }

    override fun pushLine(line: String) {
        pipeline.trySend(line).onFailure {
            pushInPool(line)
        }
    }

}