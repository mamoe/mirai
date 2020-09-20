/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused")

package net.mamoe.mirai.console.plugin.description

import net.mamoe.mirai.console.compiler.common.ResolveContext
import net.mamoe.mirai.console.compiler.common.ResolveContext.Kind.PLUGIN_ID
import net.mamoe.mirai.console.util.SemVersion

/**
 * 插件的一个依赖的信息.
 *
 * @see PluginDescription.dependencies
 */
public data class PluginDependency @JvmOverloads constructor(
    /**
     * 依赖插件 ID, [PluginDescription.id]
     */
    @ResolveContext(PLUGIN_ID) public val id: String,
    /**
     * 依赖版本号. 为 null 时则为不限制版本.
     *
     * 版本遵循 [语义化版本 2.0 规范](https://semver.org/lang/zh-CN/),
     *
     * @see SemVersion.Requirement
     */
    public val versionRequirement: SemVersion.Requirement? = null,
    /**
     * 若为 `false`, 插件在找不到此依赖时也能正常加载.
     */
    public val isOptional: Boolean = false,
) {
    init {
        kotlin.runCatching {
            PluginDescription.checkPluginId(id)
        }.getOrElse {
            throw IllegalArgumentException(it)
        }
    }

    /**
     * @see PluginDependency
     */
    public constructor(
        @ResolveContext(PLUGIN_ID) id: String,
        isOptional: Boolean = false,
    ) : this(
        id, null, isOptional
    )
}