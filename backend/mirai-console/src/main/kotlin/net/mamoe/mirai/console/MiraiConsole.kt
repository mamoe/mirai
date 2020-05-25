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
import net.mamoe.mirai.console.plugin.PluginLoader
import net.mamoe.mirai.console.plugin.jvm.JarPluginLoader
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin
import net.mamoe.mirai.console.setting.SettingStorage
import net.mamoe.mirai.utils.DefaultLogger
import net.mamoe.mirai.utils.MiraiExperimentalAPI
import net.mamoe.mirai.utils.MiraiLogger
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import kotlin.coroutines.CoroutineContext

/**
 * mirai 控制台实例.
 */
object MiraiConsole : CoroutineScope, IMiraiConsole {
    private lateinit var instance: IMiraiConsole

    /** 由前端调用 */
    internal fun init(instance: IMiraiConsole) {
        this.instance = instance
    }

    override val build: String get() = instance.build
    override val version: String get() = instance.version
    override val rootDir: File get() = instance.rootDir
    override val frontEnd: MiraiConsoleFrontEnd get() = instance.frontEnd
    override val mainLogger: MiraiLogger get() = instance.mainLogger
    override val coroutineContext: CoroutineContext get() = instance.coroutineContext

    override val builtInPluginLoaders: List<PluginLoader<*, *>> get() = instance.builtInPluginLoaders

    @Suppress("CANNOT_WEAKEN_ACCESS_PRIVILEGE")
    internal override val jvmSettingStorage: SettingStorage
        get() = instance.jvmSettingStorage

    init {
        DefaultLogger = { identity -> this.newLogger(identity) }
        this.coroutineContext[Job]!!.invokeOnCompletion {
            Bot.botInstances.forEach { kotlin.runCatching { it.close() }.exceptionOrNull()?.let(mainLogger::error) }
        }
    }

    @MiraiExperimentalAPI
    fun newLogger(identity: String?): MiraiLogger = frontEnd.loggerFor(identity)
}


// 前端使用
internal interface IMiraiConsole : CoroutineScope {
    val build: String
    val version: String

    /**
     * Console 运行路径
     */
    val rootDir: File

    /**
     * Console 前端接口
     */
    val frontEnd: MiraiConsoleFrontEnd

    /**
     * 与前端交互所使用的 Logger
     */
    val mainLogger: MiraiLogger

    /**
     * 内建加载器列表, 一般需要包含 [JarPluginLoader]
     */
    val builtInPluginLoaders: List<PluginLoader<*, *>>

    /**
     * 内建的供 [JvmPlugin] 使用的 [SettingStorage]
     */
    val jvmSettingStorage: SettingStorage
}

/**
 * Included in kotlin stdlib 1.4
 */
internal val Throwable.stacktraceString: String
    get() =
        ByteArrayOutputStream().apply {
            printStackTrace(PrintStream(this))
        }.use { it.toByteArray().encodeToString() }


@Suppress("NOTHING_TO_INLINE")
internal inline fun ByteArray.encodeToString(charset: Charset = Charsets.UTF_8): String =
    kotlinx.io.core.String(this, charset = charset)
