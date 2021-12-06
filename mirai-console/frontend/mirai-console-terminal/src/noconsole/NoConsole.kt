/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

/*
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 */
@file:OptIn(ConsoleTerminalExperimentalApi::class)

package net.mamoe.mirai.console.terminal.noconsole

import net.mamoe.mirai.console.terminal.ConsoleTerminalExperimentalApi
import net.mamoe.mirai.console.terminal.ConsoleTerminalSettings
import org.jline.keymap.KeyMap
import org.jline.reader.*
import org.jline.terminal.Attributes
import org.jline.terminal.MouseEvent
import org.jline.terminal.Size
import org.jline.terminal.Terminal
import org.jline.terminal.impl.AbstractTerminal
import org.jline.utils.AttributedString
import org.jline.utils.NonBlockingReader
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.io.PrintWriter

private const val LN_INT = '\n'.code
private const val LN_BYTE = '\n'.code.toByte()

internal object NoConsoleNonBlockingReader : NonBlockingReader() {
    override fun read(timeout: Long, isPeek: Boolean): Int {
        return LN_INT
    }

    override fun close() {}

    override fun readBuffered(b: CharArray?): Int {
        return 0
    }
}

internal object AllNextLineInputStream : InputStream() {
    override fun read(): Int = LN_INT

    override fun available(): Int = 1

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        for (i in off until (off + len)) {
            b[i] = LN_BYTE
        }
        return len
    }

    override fun close() {}
}

internal object AllIgnoredOutputStream : OutputStream() {
    override fun close() {}
    override fun write(b: ByteArray, off: Int, len: Int) {}
    override fun write(b: ByteArray) {}
    override fun write(b: Int) {}
    override fun flush() {}
}

internal val SystemOutputPrintStream by lazy {
    @OptIn(ConsoleTerminalExperimentalApi::class)
    if (ConsoleTerminalSettings.setupAnsi) {
        org.fusesource.jansi.AnsiConsole.systemInstall()
    }
    System.out
}
private val ANSI_REGEX = """\u001b\[[0-9a-zA-Z;]*?m""".toRegex()

internal object AllEmptyLineReader : LineReader {

    override fun printAbove(str: String?) {
        if (str == null) return
        @OptIn(ConsoleTerminalExperimentalApi::class)
        if (ConsoleTerminalSettings.noAnsi) {
            SystemOutputPrintStream.println(ANSI_REGEX.replace(str, ""))
        } else SystemOutputPrintStream.println(str)
    }

    @OptIn(ConsoleTerminalExperimentalApi::class)
    override fun readLine(): String =
        if (ConsoleTerminalSettings.noConsoleSafeReading) ConsoleTerminalSettings.noConsoleReadingReplacement
        else throw EndOfFileException("Unsupported Reading line when console front-end closed.")

    // region
    private fun <T> ignored(): T = error("Ignored")
    override fun readLine(mask: Char?): String = readLine()
    override fun readLine(prompt: String?): String = readLine()
    override fun readLine(prompt: String?, mask: Char?): String = readLine()
    override fun readLine(prompt: String?, mask: Char?, buffer: String?): String = readLine()
    override fun readLine(prompt: String?, rightPrompt: String?, mask: Char?, buffer: String?): String = readLine()
    override fun readLine(
        prompt: String?,
        rightPrompt: String?,
        maskingCallback: MaskingCallback?,
        buffer: String?
    ): String = readLine()

    override fun printAbove(str: AttributedString?) {
        str?.let { printAbove(it.toAnsi()) }
    }

    override fun defaultKeyMaps(): MutableMap<String, KeyMap<Binding>> = ignored()
    override fun isReading(): Boolean = false
    override fun variable(name: String?, value: Any?) = this
    override fun option(option: LineReader.Option?, value: Boolean) = this
    override fun callWidget(name: String?) {}
    override fun getVariables(): MutableMap<String, Any> = ignored()
    override fun getVariable(name: String?): Any = ignored()
    override fun setVariable(name: String?, value: Any?) {}
    override fun isSet(option: LineReader.Option?): Boolean = ignored()
    override fun setOpt(option: LineReader.Option?) {}
    override fun unsetOpt(option: LineReader.Option?) {}
    override fun getTerminal(): Terminal = NoConsole
    override fun getWidgets(): MutableMap<String, Widget> = ignored()
    override fun getBuiltinWidgets(): MutableMap<String, Widget> = ignored()
    override fun getBuffer(): Buffer = ignored()
    override fun getAppName(): String = "Mirai Console"
    override fun runMacro(macro: String?) {}
    override fun readMouseEvent(): MouseEvent = ignored()
    override fun getHistory(): History = ignored()
    override fun getParser(): Parser = ignored()
    override fun getHighlighter(): Highlighter = ignored()
    override fun getExpander(): Expander = ignored()
    override fun getKeyMaps(): MutableMap<String, KeyMap<Binding>> = ignored()
    override fun getKeyMap(): String = ignored()
    override fun setKeyMap(name: String?): Boolean = ignored()
    override fun getKeys(): KeyMap<Binding> = ignored()
    override fun getParsedLine(): ParsedLine = ignored()
    override fun getSearchTerm(): String = ignored()
    override fun getRegionActive(): LineReader.RegionType = ignored()
    override fun getRegionMark(): Int = ignored()
    override fun addCommandsInBuffer(commands: MutableCollection<String>?) {}
    override fun editAndAddInBuffer(file: File?) {}
    override fun getLastBinding(): String = ignored()
    override fun getTailTip(): String = ignored()
    override fun setTailTip(tailTip: String?) {}
    override fun setAutosuggestion(type: LineReader.SuggestionType?) {}
    override fun getAutosuggestion(): LineReader.SuggestionType = ignored()
    // endregion
}

internal object NoConsole : AbstractTerminal(
    "No Console", "No Console"
) {
    override fun reader(): NonBlockingReader = NoConsoleNonBlockingReader

    private val AllIgnoredPrintWriter = object : PrintWriter(AllIgnoredOutputStream) {
        override fun close() {}
        override fun flush() {}
    }

    // We don't need it. Mirai-Console using LineReader to print messages.
    override fun writer(): PrintWriter = AllIgnoredPrintWriter

    override fun input(): InputStream = AllNextLineInputStream

    override fun output(): OutputStream = AllIgnoredOutputStream

    private val attributes0 = Attributes()
    override fun getAttributes(): Attributes {
        return Attributes(attributes0)
    }

    override fun setAttributes(attr: Attributes?) {
        attr?.let { attributes0.copy(it) }
    }

    private val size0 = Size(189, 53)
    override fun getSize(): Size {
        return Size().also { it.copy(size0) }
    }

    override fun setSize(size: Size?) {
        size?.let { size0.copy(it) }
    }
}