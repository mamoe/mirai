/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("MemberVisibilityCanBePrivate")

package net.mamoe.mirai.console.frontendbase.logging

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.onFailure
import net.mamoe.mirai.console.frontendbase.FrontendBase
import net.mamoe.mirai.console.util.AnsiMessageBuilder.Companion.dropAnsi
import net.mamoe.mirai.utils.childScope
import java.io.Writer
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

public abstract class LogRecorder {
    public abstract fun record(msg: String)
}

public object AllDroppedLogRecorder : LogRecorder() {
    override fun record(msg: String) {
    }
}

public abstract class AsyncLogRecorder(
    private val base: FrontendBase,
    pipelineSize: Int = 2048,
) : LogRecorder() {
    protected val channel: Channel<String> = Channel(pipelineSize)
    protected val threadPool: ScheduledExecutorService = Executors.newScheduledThreadPool(
        3,
        base.newThreadFactory("Mirai Console Logging", true)
    )
    protected val dispatcher: CoroutineDispatcher = threadPool.asCoroutineDispatcher()

    protected val subscope: CoroutineScope = base.scope.childScope(name = "Mirai Console Async Logging", dispatcher)

    init {
        base.scope.coroutineContext.job.invokeOnCompletion {
            channel.close()
            threadPool.shutdown()
        }

        subscope.launch {
            while (isActive) {
                val nextLine = channel.receive()
                asyncRecord(nextLine)
            }
        }
    }

    override fun record(msg: String) {
        if (!subscope.isActive) return // Died

        channel.trySend(msg).onFailure {
            base.scope.launch(start = CoroutineStart.UNDISPATCHED) {
                channel.send(msg)
            }
        }
    }

    protected abstract fun asyncRecord(msg: String)
}

public open class AsyncLogRecorderForwarded(
    protected val delegate: LogRecorder,
    base: FrontendBase,
    pipelineSize: Int = 2048,
) : AsyncLogRecorder(base, pipelineSize) {
    override fun asyncRecord(msg: String) {
        delegate.record(msg)
    }
}

public open class WriterLogRecorder(
    protected val writer: Writer,
    protected val base: FrontendBase,
) : LogRecorder() {
    override fun record(msg: String) {
        try {
            writer.append(
                if (base.logDropAnsi) {
                    msg.dropAnsi()
                } else msg
            ).append('\n').flush()
        } catch (e: Throwable) {
            base.printToScreenDirectly(e.stackTraceToString())
        }
    }
}

public open class DailySplitLogRecorder(
    protected val directory: Path,
    protected val base: FrontendBase,
    protected val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(
        "yyyy-MM-dd'.log'"
    ),
) : LogRecorder() {
    @JvmField
    protected var writer: Writer? = null

    @JvmField
    protected var lastDate: Int = -1

    protected fun acquireFileWriter() {
        val instantNow = Instant.now()
            .atZone(ZoneId.systemDefault())

        val dayNow = instantNow.dayOfYear

        if (dayNow != lastDate) {
            lastDate = dayNow

            writer?.close()

            val logPath = directory.resolve(dateFormatter.format(instantNow))
            logPath.parent?.let { pt ->
                if (!Files.isDirectory(pt)) {
                    Files.createDirectories(pt)
                }
            }

            writer = Files.newBufferedWriter(
                logPath, Charsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND
            )
        }
    }

    override fun record(msg: String) {
        try {
            acquireFileWriter()

            (writer ?: error("Writer not setup")).append(
                if (base.logDropAnsi) {
                    msg.dropAnsi()
                } else msg
            ).append('\n').flush()
        } catch (e: Throwable) {
            base.printToScreenDirectly(e.stackTraceToString())
        }
    }
}
