package net.mamoe.mirai.console.internal.util

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.util.ConsoleInput
import net.mamoe.mirai.console.util.requestInput

@Suppress("unused")
internal object ConsoleInputImpl : ConsoleInput {
    private val inputLock = Mutex()

    override suspend fun requestInput(hint: String): String =
        inputLock.withLock { MiraiConsole.requestInput(hint) }
}