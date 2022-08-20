/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.terminal

import net.mamoe.mirai.console.fontend.ProcessProgress
import org.jline.utils.AttributedString
import org.jline.utils.AttributedStringBuilder
import org.jline.utils.AttributedStyle

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
            prePrintNewLog()
            reader.printAbove(ansiMsg)
            ansiMsg = AttributedString.EMPTY
            postPrintNewLog()
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