/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.utils

import kotlinx.coroutines.*
import net.mamoe.mirai.console.MiraiConsole
import java.util.concurrent.Executors

@Suppress("unused")
object ConsoleInput {
    private val inputDispatcher = Executors.newFixedThreadPool(1).asCoroutineDispatcher()

    /**
     * 向用户索要一个Input
     * 你需要提供一个hint（提示）并等待获取一个结果
     * 具体索要方式将根据frontend不同而不同
     * 如弹出框，或一行字
     */
    suspend fun requestInput(
        hint: String
    ): String {
        return withContext(inputDispatcher) {
            MiraiConsole.frontEnd.requestInput(hint)
        }
    }

    fun requestInputBlocking(hint: String): String = runBlocking { requestInput(hint) }

    /**
     * asnyc获取
     */
    fun requestInputAsync(
        scope: CoroutineScope,
        hint: String
    ): Deferred<String> {
        return scope.async {
            requestInput(hint)
        }
    }

    suspend fun MiraiConsole.requestInput(hint: String): String = requestInput(hint)
}




