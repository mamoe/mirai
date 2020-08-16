/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("INAPPLICABLE_JVM_NAME", "unused")

package net.mamoe.mirai.console.util

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.mamoe.mirai.console.MiraiConsole

/**
 * Console 输入. 由于 console 接管了 stdin, [readLine] 等操作需要在这里进行.
 */
public interface ConsoleInput {
    /**
     * 以 [提示][hint] 向用户索要一个输入
     */
    @JvmSynthetic
    public suspend fun requestInput(hint: String): String

    /**
     * 以 [提示][hint] 向用户索要一个输入. 仅供 Java 调用者使用
     */
    @JvmName("requestInput")
    @JavaFriendlyAPI
    public fun requestInputBlocking(hint: String): String

    public companion object INSTANCE : ConsoleInput by ConsoleInputImpl {
        public suspend inline fun MiraiConsole.requestInput(hint: String): String = ConsoleInput.requestInput(hint)
    }
}

@Suppress("unused")
internal object ConsoleInputImpl : ConsoleInput {
    private val inputLock = Mutex()

    override suspend fun requestInput(
        hint: String
    ): String = inputLock.withLock { MiraiConsole.frontEnd.requestInput(hint) }

    @JavaFriendlyAPI
    override fun requestInputBlocking(hint: String): String = runBlocking { requestInput(hint) }
}