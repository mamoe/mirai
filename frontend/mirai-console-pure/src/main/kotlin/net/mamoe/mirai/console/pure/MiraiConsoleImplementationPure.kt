/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
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
@file:OptIn(ConsoleInternalAPI::class, ConsoleFrontEndImplementation::class)

package net.mamoe.mirai.console.pure


import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import net.mamoe.mirai.console.ConsoleFrontEndImplementation
import net.mamoe.mirai.console.MiraiConsoleFrontEnd
import net.mamoe.mirai.console.MiraiConsoleImplementation
import net.mamoe.mirai.console.command.ConsoleCommandSender
import net.mamoe.mirai.console.plugin.DeferredPluginLoader
import net.mamoe.mirai.console.plugin.PluginLoader
import net.mamoe.mirai.console.plugin.jvm.JarPluginLoader
import net.mamoe.mirai.console.setting.MultiFileSettingStorage
import net.mamoe.mirai.console.setting.SettingStorage
import net.mamoe.mirai.console.util.ConsoleInternalAPI
import net.mamoe.mirai.utils.MiraiLogger
import java.io.File
import java.util.*

/**
 * mirai-console-pure 后端实现
 *
 * @see MiraiConsoleFrontEndPure 前端实现
 * @see MiraiConsolePureLoader CLI 入口点
 */
class MiraiConsoleImplementationPure
@JvmOverloads constructor(
    override val rootDir: File = File("."),
    override val builtInPluginLoaders: List<PluginLoader<*, *>> = Collections.unmodifiableList(
        listOf(
            DeferredPluginLoader { JarPluginLoader })
    ),
    override val frontEnd: MiraiConsoleFrontEnd = MiraiConsoleFrontEndPure,
    override val mainLogger: MiraiLogger = frontEnd.loggerFor("main"),
    override val consoleCommandSender: ConsoleCommandSender = ConsoleCommandSenderImpl,
    override val settingStorageForJarPluginLoader: SettingStorage = MultiFileSettingStorage(rootDir),
    override val settingStorageForBuiltIns: SettingStorage = MultiFileSettingStorage(rootDir)
) : MiraiConsoleImplementation, CoroutineScope by CoroutineScope(SupervisorJob()) {
    init {
        rootDir.mkdir()
        require(rootDir.isDirectory) { "rootDir ${rootDir.absolutePath} is not a directory" }
    }
}