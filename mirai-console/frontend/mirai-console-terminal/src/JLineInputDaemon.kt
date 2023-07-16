/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:OptIn(ConsoleExperimentalApi::class)

package net.mamoe.mirai.console.terminal

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.isActive
import kotlinx.coroutines.suspendCancellableCoroutine
import net.mamoe.mirai.console.terminal.noconsole.NoConsole
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.utils.ConcurrentLinkedDeque
import net.mamoe.mirai.utils.cast
import org.jline.reader.MaskingCallback
import org.jline.reader.impl.LineReaderImpl
import org.jline.utils.AttributedStringBuilder
import org.jline.utils.AttributedStyle
import java.util.*
import kotlin.concurrent.withLock

internal object JLineInputDaemon : Runnable {
    lateinit var terminal0: MiraiConsoleImplementationTerminal
    private val readerImpl: LineReaderImpl get() = lineReader.cast()

    private var pausedByDaemon: Boolean = false
    private var canResumeByNewRequest: Boolean = false

    class Request(
        val masked: Boolean = false,
        val delayable: Boolean = false,
        val coroutine: CancellableContinuation<String>,
        val prompt: String? = null,
    )

    private val pwdMasker = object : MaskingCallback {
        override fun display(line: String): String {
            return buildString(line.length) { repeat(line.length) { append('*') } }
        }

        override fun history(line: String?): String? {
            return null
        }
    }

    val queue = ConcurrentLinkedDeque<Request>()
    val queueDelayable = ConcurrentLinkedDeque<Request>()

    val queueStateChangeNoticer = Object()
    var processing: Request? = null

    override fun run() {

        while (terminal0.isActive) {
            val nextTask = queue.poll() ?: queueDelayable.poll()

            if (nextTask == null) {
                synchronized(queueStateChangeNoticer) {
                    if (queue.isEmpty() && queueDelayable.isEmpty()) {
                        queueStateChangeNoticer.wait()
                    }
                }
                continue
            }
            if (nextTask.coroutine.isCompleted) continue


            synchronized(queueStateChangeNoticer) {
                processing = nextTask
                updateFlags(nextTask)
            }

            val rsp = kotlin.runCatching {
                lineReader.readLine(
                    nextTask.prompt ?: "> ",
                    null,
                    if (nextTask.masked) pwdMasker else null,
                    null
                )
            }

            val crtProcessing: Request
            synchronized(queueStateChangeNoticer) {
                crtProcessing = processing ?: error("!processing lost")
                processing = null
            }
            crtProcessing.coroutine.resumeWith(rsp)
        }
    }

    internal fun sendRequest(req: Request) {
        if (terminal is NoConsole) {
            req.coroutine.resumeWith(kotlin.runCatching {
                lineReader.readLine()
            })
            return
        }

        req.coroutine.invokeOnCancellation {
            if (req.delayable) {
                queueDelayable
            } else {
                queue
            }.remove(req)

            synchronized(queueStateChangeNoticer) {
                if (processing !== req) return@invokeOnCancellation

                val nnextTask: Request
                while (true) {
                    val nnextTask2 = queue.poll() ?: queueDelayable.poll()
                    if (nnextTask2 == null) {
                        suspendReader(true)
                        return@invokeOnCancellation
                    }
                    if (nnextTask2.coroutine.isCompleted) continue

                    nnextTask = nnextTask2
                    break
                }

                processing = nnextTask
                updateFlags(nnextTask)
                if (lineReader.isReading) {
                    readerImpl.redisplay()
                }
            }
        }

        synchronized(queueStateChangeNoticer) {
            val crtProcessing = processing
            if (crtProcessing != null) {
                if (crtProcessing.delayable) {
                    processing = req
                    queueDelayable.addLast(crtProcessing)

                    updateFlags(req)
                    if (lineReader.isReading) {
                        readerImpl.redisplay()
                    }
                    return@synchronized
                }
            }

            if (req.delayable) {
                queueDelayable
            } else {
                queue
            }.addLast(req)

            queueStateChangeNoticer.notify()

            if (crtProcessing != null && crtProcessing.coroutine.isCompleted) {
                val nnextTask: Request
                while (true) {
                    val nnextTask2 = queue.poll() ?: queueDelayable.poll()
                    if (nnextTask2 == null) {
                        nnextTask = req
                        break
                    }
                    if (nnextTask2.coroutine.isCompleted) continue

                    nnextTask = nnextTask2
                    break
                }
                processing = nnextTask
                updateFlags(nnextTask)
                if (lineReader.isReading) {
                    readerImpl.redisplay()
                }
            }
        }
        tryResumeReader(true)
    }

    private fun updateFlags(req: Request) {
        if (req.masked) {
            lineReaderMaskingCallback[lineReader] = pwdMasker
        } else {
            lineReaderMaskingCallback[lineReader] = null
        }
        readerImpl.setPrompt(req.prompt ?: "> ")
    }


    internal fun suspendReader(canResumeByNewRequest: Boolean): Unit = terminalExecuteLock.withLock {
        if (!lineReader.isReading) return

        terminal.pause()
        pausedByDaemon = true
        this.canResumeByNewRequest = canResumeByNewRequest
        lineReaderReadingField.setBoolean(lineReader, false)
        terminalDisplay.update(Collections.emptyList(), 0)
    }

    internal fun tryResumeReader(byNewReq: Boolean): Unit = terminalExecuteLock.withLock {
        if (!pausedByDaemon) return
        if (byNewReq && !canResumeByNewRequest) return

        pausedByDaemon = false
        terminal.resume()
        lineReaderReadingField.setBoolean(lineReader, true)
        readerImpl.redisplay()
    }

    suspend fun nextInput(hint: String): String = suspendCancellableCoroutine { cort ->
        sendRequest(
            Request(
                masked = false,
                delayable = false,
                coroutine = cort,
                prompt = "$hint> "
            )
        )
    }

    suspend fun nextCmd(): String = suspendCancellableCoroutine { cort ->
        sendRequest(Request(masked = false, delayable = true, coroutine = cort))
    }

    suspend fun nextPwd(): String = suspendCancellableCoroutine { cort ->
        sendRequest(
            Request(
                masked = true,
                delayable = false,
                coroutine = cort,
                prompt = AttributedStringBuilder()
                    .style(AttributedStyle.DEFAULT.background(AttributedStyle.CYAN).foreground(AttributedStyle.WHITE))
                    .append("PASSWORD")
                    .style(AttributedStyle.DEFAULT)
                    .append("> ")
                    .toAnsi()
            )
        )
    }
}
