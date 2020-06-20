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
    "INVISIBLE_ABSTRACT_MEMBER_FROM_SUPE_WARNING"
)

package net.mamoe.mirai.console.pure


import kotlinx.coroutines.CoroutineScope
import net.mamoe.mirai.console.IMiraiConsole
import net.mamoe.mirai.console.MiraiConsoleFrontEnd
import net.mamoe.mirai.console.plugin.PluginLoader
import net.mamoe.mirai.utils.DefaultLogger
import net.mamoe.mirai.utils.MiraiLogger
import java.io.File
import java.util.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

private val delegateScope = CoroutineScope(EmptyCoroutineContext)

object MiraiConsolePure : IMiraiConsole {
    override val build: String = "UNKNOWN"
    override val builtInPluginLoaders: List<PluginLoader<*, *>> = LinkedList()
    override val frontEnd: MiraiConsoleFrontEnd = MiraiConsoleFrontEndPure
    override val mainLogger: MiraiLogger = DefaultLogger("Console")
    override val rootDir: File = File("./test/console").also {
        it.mkdirs()
    }
    override val version: String
        get() = "UNKNOWN"
    override val coroutineContext: CoroutineContext
        get() = delegateScope.coroutineContext
}