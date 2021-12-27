/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console

import kotlinx.coroutines.*
import net.mamoe.mirai.console.MiraiConsoleImplementation.Companion.start
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.data.MemoryPluginDataStorage
import net.mamoe.mirai.console.data.PluginDataStorage
import net.mamoe.mirai.console.plugin.jvm.JvmPluginLoader
import net.mamoe.mirai.console.plugin.loader.PluginLoader
import net.mamoe.mirai.console.util.ConsoleInput
import net.mamoe.mirai.console.util.ConsoleInternalApi
import net.mamoe.mirai.console.util.SemVersion
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.LoginSolver
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.PlatformLogger
import java.nio.file.Path
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.io.path.createTempDirectory
import kotlin.test.assertNotNull

internal object Testing {
    @Volatile
    internal var cont: Continuation<Any?>? = null

    @Suppress("UNCHECKED_CAST")
    suspend fun <R> withTesting(timeout: Long = 50000L, block: suspend () -> Unit): R {
        @Suppress("RemoveExplicitTypeArguments") // bug
        return if (timeout != -1L) {
            withTimeout<R>(timeout) {
                suspendCancellableCoroutine<R> { ct ->
                    this@Testing.cont = ct as Continuation<Any?>
                    runBlocking { block() }
                }
            }
        } else {
            suspendCancellableCoroutine<R> { ct ->
                this.cont = ct as Continuation<Any?>
                runBlocking { block() }
            }
        }
    }

    fun ok(result: Any? = Unit) {
        val cont = cont
        assertNotNull(cont)
        cont.resume(result)
    }
}
