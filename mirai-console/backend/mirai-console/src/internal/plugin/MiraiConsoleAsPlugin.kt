/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.internal.plugin

import net.mamoe.mirai.console.command.ConsoleCommandOwner
import net.mamoe.mirai.console.internal.MiraiConsoleBuildConstants
import net.mamoe.mirai.console.permission.Permission
import net.mamoe.mirai.console.permission.PermissionId
import net.mamoe.mirai.console.plugin.Plugin
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.description
import net.mamoe.mirai.console.plugin.description.PluginDependency
import net.mamoe.mirai.console.plugin.description.PluginDescription
import net.mamoe.mirai.console.plugin.loader.PluginLoader
import net.mamoe.mirai.console.util.SemVersion

internal object MiraiConsoleAsPlugin : Plugin {
    // MiraiConsole always enabled
    override val isEnabled: Boolean get() = true

    override val loader: PluginLoader<*, *> get() = TheLoader

    override val parentPermission: Permission
        get() = ConsoleCommandOwner.parentPermission

    override fun permissionId(name: String): PermissionId {
        return ConsoleCommandOwner.permissionId(name)
    }

    internal object TheLoader : PluginLoader<Plugin, PluginDescription> {
        override fun listPlugins(): List<Plugin> = listOf(MiraiConsoleAsPlugin)

        override fun disable(plugin: Plugin) {
            // noop
        }

        override fun enable(plugin: Plugin) {
            // noop
        }

        override fun load(plugin: Plugin) {
            // noop
        }

        override fun getPluginDescription(plugin: Plugin): PluginDescription {
            if (plugin !== MiraiConsoleAsPlugin) {
                error("loader not match with " + plugin.description.id)
            }
            return TheDescription
        }
    }

    internal object TheDescription : PluginDescription {
        override val id: String get() = "net.mamoe.mirai-console"
        override val name: String get() = "Console"
        override val author: String get() = "Mamoe Technologies"
        override val version: SemVersion get() = MiraiConsoleBuildConstants.version
        override val info: String get() = ""
        override val dependencies: Set<PluginDependency> get() = setOf()


        override fun toString(): String {
            return "PluginDescription[ mirai-console ]"
        }
    }
}