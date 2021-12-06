/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.internal.util

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.mamoe.mirai.console.internal.MiraiConsoleImplementationBridge
import net.mamoe.mirai.console.util.ConsoleInput

@Suppress("unused")
internal object ConsoleInputImpl : ConsoleInput {
    private val inputLock = Mutex()

    override suspend fun requestInput(hint: String): String =
        inputLock.withLock { MiraiConsoleImplementationBridge.consoleInput.requestInput(hint) }
}