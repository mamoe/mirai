/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.internal.plugin

import net.mamoe.mirai.console.data.runCatchingLog
import net.mamoe.mirai.console.permission.Permission
import net.mamoe.mirai.console.permission.PermissionId
import net.mamoe.mirai.console.permission.PermissionService
import net.mamoe.mirai.console.plugin.NotYetLoadedPlugin
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.utils.MiraiLogger
import java.io.File
import java.io.InputStream
import java.nio.file.Path
import kotlin.coroutines.CoroutineContext

internal abstract class NotYetLoadedJvmPlugin(
    override val description: JvmPluginDescription,
    val classLoaderN: JvmPluginClassLoaderN,
) : JvmPlugin, NotYetLoadedPlugin<JvmPlugin> {
    abstract override fun resolve(): JvmPlugin

    override val logger: MiraiLogger by lazy {
        BuiltInJvmPluginLoaderImpl.logger.runCatchingLog {
            MiraiLogger.Factory.create(NotYetLoadedJvmPlugin::class, this.description.name)
        }.getOrThrow()
    }

    override val isEnabled: Boolean get() = false
    override val parentPermission: Permission
        get() = error("Not yet loaded")

    override fun permissionId(name: String): PermissionId {
        return PermissionService.INSTANCE.allocatePermissionIdForPlugin(this, name)
    }

    override val coroutineContext: CoroutineContext
        get() = error("Not yet loaded")
    override val dataFolderPath: Path
        get() = error("Not yet loaded")
    override val dataFolder: File
        get() = error("Not yet loaded")
    override val configFolderPath: Path
        get() = error("Not yet loaded")
    override val configFolder: File
        get() = error("Not yet loaded")

    override fun getResourceAsStream(path: String): InputStream? {
        return classLoaderN.getResourceAsStream(path)
    }
}