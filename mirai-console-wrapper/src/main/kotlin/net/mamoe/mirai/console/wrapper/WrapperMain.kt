/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */
@file:Suppress("EXPERIMENTAL_API_USAGE")
package net.mamoe.mirai.console.wrapper

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.jvm.javaio.copyTo
import kotlinx.coroutines.*
import java.io.File
import kotlin.system.exitProcess


val contentPath by lazy {
    File(System.getProperty("user.dir") + "/content/").also {
        if (!it.exists()) {
            it.mkdirs()
        }
    }
}

object WrapperMain {
    @JvmStatic
    fun main(args: Array<String>) {
        println("You are running Mirai-Console-Wrapper under " + System.getProperty("user.dir"))
        println("Starting version check...")
        /**
         * ask for type
         */
        val type = CONSOLE_PURE

        runBlocking {
            launch {
                CoreUpdator.versionCheck()
            }
            launch {
                ConsoleUpdator.versionCheck(type)
            }
        }
        println("Version check complete, starting Mirai")
    }
}

class MiraiClassLoader(
    val core:File,
    val protocol: File,
    val console: File
){

}