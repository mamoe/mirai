/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.pure

import org.jline.reader.LineReader
import org.jline.reader.LineReaderBuilder
import org.jline.reader.impl.completer.NullCompleter
import org.jline.terminal.Terminal
import org.jline.terminal.TerminalBuilder

internal object ConsoleUtils {

    val lineReader: LineReader
    val terminal: Terminal
    lateinit var miraiLineReader: suspend (String) -> String

    init {

        val dumb = System.getProperty("java.class.path")
            .contains("idea_rt.jar") || System.getProperty("mirai.idea") !== null || System.getenv("mirai.idea") !== null

        terminal = kotlin.runCatching {
            TerminalBuilder.builder()
                .dumb(dumb)
                .build()
        }.recoverCatching {
            TerminalBuilder.builder()
                .jansi(true)
                .build()
        }.recoverCatching {
            TerminalBuilder.builder()
                .system(true)
                .build()
        }.getOrThrow()

        lineReader = LineReaderBuilder.builder()
            .terminal(terminal)
            .completer(NullCompleter())
            .build()
    }
}