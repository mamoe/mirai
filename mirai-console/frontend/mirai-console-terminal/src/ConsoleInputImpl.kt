/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.terminal

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import net.mamoe.mirai.console.util.ConsoleInput
import org.fusesource.jansi.Ansi
import org.jline.reader.EndOfFileException
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.Executors
import kotlin.coroutines.resumeWithException


internal object ConsoleInputImpl : ConsoleInput {
    private val format = DateTimeFormatter.ofPattern("HH:mm:ss")
    internal val thread = Executors.newSingleThreadExecutor { task ->
        Thread(task, "Mirai Console Input Thread").also {
            it.isDaemon = false
        }
    }
    internal var executingCoroutine: CancellableContinuation<String>? = null


    override suspend fun requestInput(hint: String): String {
        return suspendCancellableCoroutine { coroutine ->
            if (thread.isShutdown || thread.isTerminated) {
                coroutine.resumeWithException(EndOfFileException())
                return@suspendCancellableCoroutine
            }
            executingCoroutine = coroutine
            kotlin.runCatching {
                thread.submit {
                    kotlin.runCatching {
                        lineReader.readLine(
                            if (hint.isNotEmpty()) {
                                lineReader.printAbove(
                                    Ansi.ansi()
                                        .fgCyan()
                                        .a(
                                            LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault())
                                                .format(format)
                                        )
                                        .a(" ")
                                        .fgMagenta().a(hint)
                                        .reset()
                                        .toString()
                                )
                                "$hint > "
                            } else "> "
                        )
                    }.let { result ->
                        executingCoroutine = null
                        coroutine.resumeWith(result)
                    }
                }
            }.onFailure { error ->
                executingCoroutine = null
                kotlin.runCatching { coroutine.resumeWithException(EndOfFileException(error)) }
            }
        }
    }
}
