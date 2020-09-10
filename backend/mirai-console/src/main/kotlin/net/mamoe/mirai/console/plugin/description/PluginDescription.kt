/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.plugin.description

import com.vdurmont.semver4j.Semver
import net.mamoe.mirai.console.plugin.Plugin
import net.mamoe.mirai.console.plugin.PluginLoadException


/**
 * 插件描述.
 *
 * @see Plugin
 */
public interface PluginDescription {
    /**
     * 插件类型. 将会决定加载顺序
     *
     * @see PluginKind
     */
    public val kind: PluginKind

    /**
     * 插件 ID, 必须全英文, 仅允许英文字母, '-', '_', '.'.
     *
     * - 类似于 Java 包名, 插件 ID 需要 '域名.名称' 格式, 如 `net.mamoe.mirai.example-plugin`
     * - 域名和名称都是必须的
     * - '.' 不允许位于首位或末尾
     * - '-' 和 '_' 仅允许存在于两个英文字母之间
     *
     * ID 在插件发布后就应该保持不变, 以便其他插件添加依赖.
     *
     * 插件 ID 的域名和名称都不能完全是以下其中一个 ([FORBIDDEN_ID_WORDS]).
     * - "console"
     * - "main"
     * - "plugin"
     * - "config"
     * - "data"
     *
     *
     * ID 用于指令权限等一些内部处理
     *
     * @see FORBIDDEN_ID_LETTERS
     * @see FORBIDDEN_ID_WORDS
     */
    public val id: String

    /**
     * 插件名称. 允许中文, 允许各类符号.
     *
     * 插件名称不能完全是以下其中一种 ([FORBIDDEN_ID_WORDS]).
     * - console
     * - main
     * - plugin
     * - config
     * - data
     *
     * 插件名称用于显示给用户.
     *
     * @see FORBIDDEN_ID_LETTERS
     * @see FORBIDDEN_ID_WORDS
     */
    public val name: String

    /**
     * 插件作者, 允许为空
     */
    public val author: String

    /**
     * 插件版本.
     *
     * 语法参考: ([语义化版本 2.0.0](https://semver.org/lang/zh-CN/)).
     *
     * 合法的版本号示例:
     * - `1.0.0`
     * - `1.0`
     * - `1.0-M1`
     * - `1.0.0-M1`
     * - `1.0.0-M2-1`
     * - `1`  (尽管非常不建议这么做)
     *
     * 非法版本号实例:
     * - `DEBUG-1`
     * - `-1.0`
     * - `v1.0` (不允许 "v")
     * - `V1.0` (不允许 "V")
     *
     * @see Semver 语义化版本. 允许 [宽松][Semver.SemverType.LOOSE] 类型版本.
     */
    public val version: Semver

    /**
     * 插件信息, 允许为空
     */
    public val info: String

    /**
     * 此插件依赖的其他插件, 将会在这些插件加载之后加载此插件
     *
     * @see PluginDependency
     */
    public val dependencies: Set<PluginDependency>

    public companion object {
        public val FORBIDDEN_ID_LETTERS: Array<String> = "~!@#$%^&*()+/*<>{}|[]\\?".map(Char::toString).toTypedArray()
        public val FORBIDDEN_ID_WORDS: Array<String> = arrayOf("main", "console", "plugin", "config", "data")

        /**
         * 依次检查 [PluginDescription] 的 [PluginDescription.id], [PluginDescription.name], [PluginDescription.dependencies] 的合法性
         *
         * @throws IllegalPluginDescriptionException 当不合法时抛出.
         */
        @Throws(IllegalPluginDescriptionException::class)
        public fun checkPluginDescription(instance: PluginDescription) {
            kotlin.runCatching {
                checkPluginId(instance.id)
                checkPluginName(instance.name)
                checkDependencies(instance.id, instance.dependencies)
            }.getOrElse {
                throw IllegalPluginDescriptionException(
                    "Illegal description. Plugin ${instance.name} (${instance.id})",
                    it
                )
            }
        }

        /**
         * 检查 [PluginDescription.id] 的合法性.
         *
         * @throws IllegalPluginDescriptionException 当不合法时抛出.
         */
        @Throws(IllegalPluginDescriptionException::class)
        public fun checkPluginId(id: String) {
            if (id.isBlank()) throw IllegalPluginDescriptionException("Plugin id cannot be blank")
            if (id.count { it == '.' } < 2) throw IllegalPluginDescriptionException("'$id' is illegal. Plugin id must consist of both domain and name. ")

            FORBIDDEN_ID_LETTERS.firstOrNull { it in id }?.let { illegal ->
                throw IllegalPluginDescriptionException("Plugin id contains illegal char: $illegal.")
            }

            val idSections = id.split('.')
            FORBIDDEN_ID_WORDS.firstOrNull { it in idSections }?.let { illegal ->
                throw IllegalPluginDescriptionException("Plugin id contains illegal word: '$illegal'.")
            }
        }

        /**
         * 检查 [PluginDescription.name] 的合法性.
         *
         * @throws IllegalPluginDescriptionException 当不合法时抛出.
         */
        @Throws(IllegalPluginDescriptionException::class)
        public fun checkPluginName(name: String) {
            if (name.isBlank()) throw IllegalPluginDescriptionException("Plugin name cannot be blank")
            FORBIDDEN_ID_WORDS.firstOrNull { it in name }?.let { illegal ->
                throw IllegalPluginDescriptionException("Plugin name is illegal: '$illegal'.")
            }
        }

        /**
         * 检查 [PluginDescription.dependencies] 的合法性.
         *
         * @throws IllegalPluginDescriptionException 当不合法时抛出.
         */
        @Throws(IllegalPluginDescriptionException::class)
        public fun checkDependencies(pluginId: String, dependencies: Set<PluginDependency>) {
            if (dependencies.distinctBy { it.id }.size != dependencies.size)
                throw PluginLoadException("Duplicated dependency detected: A plugin cannot depend on different versions of dependencies of the same id")

            if (dependencies.any { it.id == pluginId })
                throw PluginLoadException("Recursive dependency detected: A plugin cannot depend on itself")
        }
    }
}

