/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.terminal

import net.mamoe.mirai.console.fontend.DownloadingProgress
import org.jline.utils.AttributedString
import org.jline.utils.AttributedStringBuilder
import org.jline.utils.AttributedStyle

internal class TerminalDownloadingProgress(
    private val reader: org.jline.reader.LineReader,
) : DownloadingProgress {
    private var totalSize: Long = 1
    private var processed: Long = 0
    private val txt: StringBuilder = StringBuilder()
    private var needUpdateTxt: Boolean = true
    private var failed: Boolean = false
    private var disposed: Boolean = false

    @JvmField
    var pendingErase: Boolean = false

    @JvmField
    var eraseTimestamp: Long = 0

    @JvmField
    var ansiMsg: AttributedString = AttributedString.EMPTY

    override fun updateText(txt: String) {
        this.txt.setLength(0)
        this.txt.append(txt)
        needUpdateTxt = true
    }

    override fun initProgress(totalSize: Long) {
        this.totalSize = totalSize
        needUpdateTxt = true
    }

    override fun updateProgress(processed: Long) {
        this.processed = processed
        needUpdateTxt = true
    }

    override fun updateProgress(processed: Long, totalSize: Long) {
        this.processed = processed
        this.totalSize = totalSize
        needUpdateTxt = true
    }

    override fun markFailed() {
        failed = true
        needUpdateTxt = true
    }

    internal fun updateTxt(terminalWidth: Int) {
        // paddings
        val txtlen = txt.length
        if (txtlen < terminalWidth || needUpdateTxt) {
            repeat(terminalWidth - txtlen) {
                txt.append(' ')
            }
        } else {
            // txt.setLength(terminalWidth)
            return
        }

        val finalAnsiLineBuilder = AttributedStringBuilder()

        if (failed) {
            finalAnsiLineBuilder.style(
                AttributedStyle.DEFAULT
                    .background(AttributedStyle.RED)
                    .foreground(AttributedStyle.BLACK)
            )
            finalAnsiLineBuilder.append(txt)
        } else {

            val downpcent = (txt.length * processed / totalSize).toInt()
            if (downpcent > 0) {
                finalAnsiLineBuilder.style(
                    AttributedStyle.DEFAULT
                        .background(AttributedStyle.GREEN)
                        .foreground(AttributedStyle.BLACK)
                )
                finalAnsiLineBuilder.append(txt, 0, downpcent)
            }
            if (downpcent < txt.length) {
                finalAnsiLineBuilder.style(
                    AttributedStyle.DEFAULT
                        .background(AttributedStyle.WHITE)
                        .foreground(AttributedStyle.BLACK)
                )
                finalAnsiLineBuilder.append(txt, downpcent, txt.length)
            }
        }
        ansiMsg = finalAnsiLineBuilder.toAttributedString()
        needUpdateTxt = false
    }

    override fun rerender() {
        updateTerminalDownloadingProgresses()
    }

    override fun dispose() {
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