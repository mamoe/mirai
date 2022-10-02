/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

package net.mamoe.mirai.internal.netinternalkit

import kotlinx.atomicfu.locks.withLock
import net.mamoe.mirai.utils.*
import java.io.File

internal object LogCapture {
    lateinit var logCache: SizedCache<String>
    private val output: (String) -> Unit = { log ->
        println(log)
        logCache.emit(log.dropAnsi())
    }
    var outputDir = File("test/capture")

    fun setupCapture(maxLine: Int = 200) {
        logCache = SizedCache(maxLine)

        @Suppress("INVISIBLE_MEMBER")
        MiraiLoggerFactoryImplementationBridge.wrapCurrent {
            object : MiraiLogger.Factory {
                override fun create(requester: Class<*>, identity: String?): MiraiLogger {
                    return PlatformLogger(
                        identity ?: requester.kotlin.simpleName ?: requester.simpleName,
                        output
                    )
                }
            }
        }

        NetReplayHelperSettings.logger_console = PlatformLogger(
            identity = "NetReplayHelper",
            output = ::println
        )
        NetReplayHelperSettings.logger_file = PlatformLogger(
            identity = "NetReplayHelper",
            output = { log -> logCache.emit(log.dropAnsi()) }
        )
    }

    fun saveCapture(type: String = "capture") {
        val output = outputDir.resolve("$type-${currentTimeMillis()}.txt")
        logCache.lock.withLock {
            output.also { it.parentFile.mkdirs() }.bufferedWriter().use { writer ->
                logCache.forEach { line ->
                    writer.write(line)
                    writer.write(10)
                }
            }
        }
    }
}
