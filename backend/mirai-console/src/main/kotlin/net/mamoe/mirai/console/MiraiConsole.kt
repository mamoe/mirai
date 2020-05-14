/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.io.charsets.Charset
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.utils.MiraiConsoleFrontEnd
import net.mamoe.mirai.utils.DefaultLogger
import net.mamoe.mirai.utils.MiraiExperimentalAPI
import net.mamoe.mirai.utils.MiraiLogger
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.coroutines.CoroutineContext

// 前端使用
interface IMiraiConsole : CoroutineScope {
    val build: String
    val version: String

    /**
     * Console运行路径
     */
    val path: String

    /**
     * Console前端接口
     */
    val frontEnd: MiraiConsoleFrontEnd

    /**
     * 与前端交互所使用的Logger
     */
    val mainLogger: MiraiLogger
}

object MiraiConsole : CoroutineScope, IMiraiConsole {
    private lateinit var instance: IMiraiConsole

    /** 由前端调用 */
    internal fun init(instance: IMiraiConsole) {
        this.instance = instance
    }

    override val build: String get() = instance.build
    override val version: String get() = instance.version
    override val path: String get() = instance.path
    override val frontEnd: MiraiConsoleFrontEnd get() = instance.frontEnd
    override val mainLogger: MiraiLogger get() = instance.mainLogger
    override val coroutineContext: CoroutineContext get() = instance.coroutineContext

    init {
        DefaultLogger = {
            this.newLogger(it)
        }
        this.coroutineContext[Job]!!.invokeOnCompletion {
            Bot.botInstances.forEach {
                it.close()
            }
        }
    }

    @MiraiExperimentalAPI
    fun newLogger(identity: String?): MiraiLogger = frontEnd.loggerFor(identity)
}

internal val Throwable.stacktraceString: String
    get() =
        ByteArrayOutputStream().apply {
            printStackTrace(PrintStream(this))
        }.use { it.toByteArray().encodeToString() }


@Suppress("NOTHING_TO_INLINE")
internal inline fun ByteArray.encodeToString(charset: Charset = Charsets.UTF_8): String =
    kotlinx.io.core.String(this, charset = charset)
