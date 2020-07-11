/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 *
 */

@file:Suppress(
    "INVISIBLE_MEMBER",
    "INVISIBLE_REFERENCE",
    "CANNOT_OVERRIDE_INVISIBLE_MEMBER",
    "INVISIBLE_SETTER",
    "INVISIBLE_GETTER",
    "INVISIBLE_ABSTRACT_MEMBER_FROM_SUPER",
    "INVISIBLE_ABSTRACT_MEMBER_FROM_SUPER_WARNING",
    "EXPOSED_SUPER_CLASS"
)
@file:OptIn(ConsoleInternalAPI::class)

package net.mamoe.mirai.console.pure


import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import net.mamoe.mirai.console.IMiraiConsole
import net.mamoe.mirai.console.MiraiConsoleFrontEnd
import net.mamoe.mirai.console.MiraiConsoleInitializer
import net.mamoe.mirai.console.command.ConsoleCommandSender
import net.mamoe.mirai.console.plugin.DeferredPluginLoader
import net.mamoe.mirai.console.plugin.PluginLoader
import net.mamoe.mirai.console.plugin.jvm.JarPluginLoader
import net.mamoe.mirai.console.setting.MultiFileSettingStorage
import net.mamoe.mirai.console.setting.SettingStorage
import net.mamoe.mirai.console.utils.ConsoleInternalAPI
import net.mamoe.mirai.utils.MiraiLogger
import java.io.File

/**
 * mirai-console-pure 后端实现
 *
 * @see MiraiConsoleFrontEndPure 前端实现
 * @see MiraiConsolePureLoader CLI 入口点
 */
class MiraiConsolePure @JvmOverloads constructor(
    override val rootDir: File = File("."),
    override val builtInPluginLoaders: List<PluginLoader<*, *>> = listOf(DeferredPluginLoader { JarPluginLoader }),
    override val frontEnd: MiraiConsoleFrontEnd = MiraiConsoleFrontEndPure,
    override val mainLogger: MiraiLogger = frontEnd.loggerFor("main"),
    override val consoleCommandSender: ConsoleCommandSender = ConsoleCommandSenderImpl,
    override val settingStorageForJarPluginLoader: SettingStorage = MultiFileSettingStorage(rootDir),
    override val settingStorageForBuiltIns: SettingStorage = MultiFileSettingStorage(rootDir)
) : IMiraiConsole, CoroutineScope by CoroutineScope(SupervisorJob()) {
    init {
        rootDir.mkdir()
        require(rootDir.isDirectory) { "rootDir ${rootDir.absolutePath} is not a directory" }
    }

    @JvmField
    internal var started: Boolean = false

    companion object {
        @JvmStatic
        fun MiraiConsolePure.start() = synchronized(this) {
            check(!started) { "mirai-console is already started and can't be restarted." }
            MiraiConsoleInitializer.init(this)
            started = true
        }
    }
}