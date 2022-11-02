/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.terminal

import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import net.mamoe.mirai.console.fontend.ProcessProgress
import net.mamoe.mirai.console.terminal.noconsole.NoConsole
import net.mamoe.mirai.utils.ConcurrentLinkedQueue
import net.mamoe.mirai.utils.cast
import net.mamoe.mirai.utils.currentTimeMillis
import org.jline.reader.MaskingCallback
import org.jline.reader.impl.LineReaderImpl
import org.jline.utils.AttributedString
import org.jline.utils.AttributedStringBuilder
import org.jline.utils.AttributedStyle
import org.jline.utils.Display
import java.lang.reflect.Field
import kotlin.concurrent.withLock
import kotlin.coroutines.Continuation
import kotlin.reflect.KProperty

internal class TerminalProcessProgress(
    private val reader: org.jline.reader.LineReader,
) : ProcessProgress {
    private var totalSize: Long = 1
    private var processed: Long = 0
    private val txt: StringBuilder = StringBuilder()
    private val renderedTxt: StringBuilder = StringBuilder()
    private var failed: Boolean = false
    private var disposed: Boolean = false

    @JvmField
    var pendingErase: Boolean = false

    @JvmField
    var eraseTimestamp: Long = 0

    @JvmField
    var ansiMsg: AttributedString = AttributedString.EMPTY

    private var lastTerminalWidth = 0
    private var needRerender: Boolean = true
    private var needUpdateTxt: Boolean = true

    override fun updateText(txt: CharSequence) {
        this.txt.setLength(0)
        this.txt.append(txt)
        needUpdateTxt = true
        needRerender = true
    }

    override fun updateText(txt: String) {
        updateText(txt as CharSequence)
    }

    override fun setTotalSize(totalSize: Long) {
        this.totalSize = totalSize
        needRerender = true
    }

    override fun update(processed: Long) {
        this.processed = processed
        needRerender = true
    }

    override fun update(processed: Long, totalSize: Long) {
        this.processed = processed
        this.totalSize = totalSize
        needRerender = true
    }

    override fun markFailed() {
        failed = true
        needRerender = true
    }

    internal fun updateTxt(terminalWidth: Int) {

        // region check need to update
        if (needUpdateTxt || lastTerminalWidth != terminalWidth) {
            // <text changed / screen width changed>
            lastTerminalWidth = terminalWidth
            synchronized(renderedTxt) {
                renderedTxt.setLength(0)
                renderedTxt.append(txt)
                // paddings
                if (renderedTxt.length < terminalWidth) {
                    repeat(terminalWidth - renderedTxt.length) {
                        renderedTxt.append(' ')
                    }
                }
            }
        } else if (!needRerender) {
            // nothing changed
            return
        } /* else { <api require rerender> } */

        lastTerminalWidth = terminalWidth
        // endregion
        synchronized(renderedTxt) {

            val renderedTextWidth = when (terminalWidth) {
                0 -> renderedTxt.length
                else -> terminalWidth
            }

            val finalAnsiLineBuilder = AttributedStringBuilder()

            if (failed) {
                finalAnsiLineBuilder.style(
                    AttributedStyle.DEFAULT
                        .background(AttributedStyle.RED)
                        .foreground(AttributedStyle.BLACK)
                )
                finalAnsiLineBuilder.append(renderedTxt, 0, renderedTextWidth)
            } else {
                val downpcent = (renderedTextWidth * processed / totalSize).toInt()
                if (downpcent > 0) {
                    finalAnsiLineBuilder.style(
                        AttributedStyle.DEFAULT
                            .background(AttributedStyle.GREEN)
                            .foreground(AttributedStyle.BLACK)
                    )
                    finalAnsiLineBuilder.append(renderedTxt, 0, downpcent)
                }
                if (downpcent < renderedTextWidth) {
                    finalAnsiLineBuilder.style(
                        AttributedStyle.DEFAULT
                            .background(AttributedStyle.WHITE)
                            .foreground(AttributedStyle.BLACK)
                    )
                    finalAnsiLineBuilder.append(renderedTxt, downpcent, renderedTextWidth)
                }
            }
            ansiMsg = finalAnsiLineBuilder.toAttributedString()
            needUpdateTxt = false
            needRerender = false
        }
    }

    override fun rerender() {
        updateTerminalDownloadingProgresses()
    }

    override fun close() {
        if (disposed) return
        disposed = true

        totalSize = 1
        processed = 1
        needUpdateTxt = true
        updateTxt(reader.terminal.width)
        if (failed) {
            terminalDownloadingProgresses.remove(this)
            printToScreen(ansiMsg)
            ansiMsg = AttributedString.EMPTY
            return
        }
        // terminalDownloadingProgresses.remove(this)
        pendingErase = true
        eraseTimestamp = System.currentTimeMillis() + 1500L

        updateTerminalDownloadingProgresses()

        // prePrintNewLog()
        // reader.printAbove(ansiMsg)
        // ansiMsg = AttributedString.EMPTY
    }
}


internal val terminalDisplay: Display by object : kotlin.properties.ReadOnlyProperty<Any?, Display> {
    val delegate: () -> Display by lazy {
        val terminal = terminal
        if (terminal is NoConsole) {
            val display = Display(terminal, false)
            return@lazy { display }
        }

        val lr = lineReader
        val field = LineReaderImpl::class.java.declaredFields.first { it.type == Display::class.java }
        field.isAccessible = true
        return@lazy { field[lr] as Display }
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): Display {
        return delegate()
    }
}
internal val lineReaderMaskingCallback: Field by lazy {
    val field = LineReaderImpl::class.java.declaredFields.first {
        MaskingCallback::class.java.isAssignableFrom(it.type)
    }
    field.isAccessible = true
    field
}

internal val lineReaderReadingField: Field by lazy {
    val field = LineReaderImpl::class.java.getDeclaredField("reading")
    field.isAccessible = true
    field
}

internal val terminalExecuteLock: java.util.concurrent.locks.Lock by lazy {
    val terminal = terminal
    if (terminal is NoConsole) return@lazy java.util.concurrent.locks.ReentrantLock()
    val lr = lineReader
    val field = LineReaderImpl::class.java.declaredFields.first {
        java.util.concurrent.locks.Lock::class.java.isAssignableFrom(it.type)
    }
    field.isAccessible = true
    field[lr].cast()
}

private val terminalDownloadingProgressesNoticer = Object()
internal var containDownloadingProgress: Boolean = false
    get() = field || terminalDownloadingProgresses.isNotEmpty()

internal val terminalDownloadingProgresses = ConcurrentLinkedQueue<TerminalProcessProgress>()


internal var downloadingProgressCoroutine: Continuation<Unit>? = null
internal suspend fun downloadingProgressDaemonStub() {
    delay(500L)
    if (containDownloadingProgress) {
        updateTerminalDownloadingProgresses()
    } else {
        suspendCancellableCoroutine<Unit> { cp ->
            downloadingProgressCoroutine = cp
        }
        downloadingProgressCoroutine = null
    }
}

internal fun updateTerminalDownloadingProgresses() {
    if (!containDownloadingProgress) return

    runCatching { downloadingProgressCoroutine?.resumeWith(Result.success(Unit)) }

    terminalExecuteLock.withLock {
        JLineInputDaemon.suspendReader(false)

        if (terminalDownloadingProgresses.isNotEmpty()) {
            val wid = terminal.width
            if (wid == 0) { // Run in idea
                if (terminalDownloadingProgresses.removeIf { it.pendingErase }) {
                    updateTerminalDownloadingProgresses()
                    return
                }
                terminalDisplay.update(listOf(AttributedString.EMPTY), 0, false)
                // Error in idea when more than one bar displaying
                terminalDisplay.update(listOf(terminalDownloadingProgresses.peek()?.let {
                    it.updateTxt(0); it.ansiMsg
                } ?: AttributedString.EMPTY), 0)
            } else {
                if (terminalDownloadingProgresses.size > 4) {
                    // to mush. delete some completed status
                    var allowToDelete = terminalDownloadingProgresses.size - 4
                    terminalDownloadingProgresses.removeIf { pg ->
                        if (allowToDelete == 0) {
                            return@removeIf false
                        }
                        if (pg.pendingErase) {
                            allowToDelete--
                            return@removeIf true
                        }
                        return@removeIf false
                    }
                }
                terminalDisplay.update(terminalDownloadingProgresses.map {
                    it.updateTxt(wid); it.ansiMsg
                }, 0)
                cleanupErase()
            }
        } else {
            terminalDisplay.update(emptyList(), 0)
            (lineReader as LineReaderImpl).let { lr ->
                if (lr.isReading) {
                    lr.redisplay()
                }
            }
            noticeDownloadingProgressEmpty()
            terminal.writer().print("\u001B[?25h") // show cursor
        }
    }
}

internal fun printToScreen(msg: String) {
    if (!containDownloadingProgress) {
        if (msg.endsWith(ANSI_RESET)) {
            lineReader.printAbove(msg)
        } else {
            lineReader.printAbove(msg + ANSI_RESET)
        }
        return
    }
    terminalExecuteLock.withLock {
        terminalDisplay.update(emptyList(), 0)

        if (msg.endsWith(ANSI_RESET)) {
            lineReader.printAbove(msg)
        } else {
            lineReader.printAbove(msg + ANSI_RESET)
        }

        updateTerminalDownloadingProgresses()
        cleanupErase()
    }
}

internal fun printToScreen(msg: AttributedString) {
    if (!containDownloadingProgress) {
        return lineReader.printAbove(msg)
    }
    terminalExecuteLock.withLock {
        terminalDisplay.update(emptyList(), 0)

        lineReader.printAbove(msg)

        updateTerminalDownloadingProgresses()
        cleanupErase()
    }
}


internal fun cleanupErase() {
    val now = currentTimeMillis()
    terminalDownloadingProgresses.removeIf { pg ->
        if (!pg.pendingErase) return@removeIf false
        if (now > pg.eraseTimestamp) {
            pg.ansiMsg = AttributedString.EMPTY
            return@removeIf true
        }
        return@removeIf false
    }
}


private fun noticeDownloadingProgressEmpty() {
    synchronized(terminalDownloadingProgressesNoticer) {
        containDownloadingProgress = false
        if (terminalDownloadingProgresses.isEmpty()) {
            terminalDownloadingProgressesNoticer.notifyAll()
        }

        JLineInputDaemon.tryResumeReader(false)
    }
}

internal fun waitDownloadingProgressEmpty() {
    synchronized(terminalDownloadingProgressesNoticer) {
        if (containDownloadingProgress) {
            terminalDownloadingProgressesNoticer.wait()
        }
    }
}

