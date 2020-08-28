/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused")

package net.mamoe.mirai.console.plugin.jvm

import com.vdurmont.semver4j.Semver
import net.mamoe.mirai.console.plugin.description.PluginDependency
import net.mamoe.mirai.console.plugin.description.PluginDescription
import net.mamoe.mirai.console.plugin.description.PluginKind

/**
 * JVM 插件的描述. 通常作为 `plugin.yml`
 * @see SimpleJvmPluginDescription
 */
public interface JvmPluginDescription : PluginDescription

/**
 * @see JvmPluginDescription
 */
public data class SimpleJvmPluginDescription
@JvmOverloads public constructor(
    public override val name: String,
    public override val version: Semver,
    public override val author: String = "",
    public override val info: String = "",
    public override val dependencies: List<PluginDependency> = listOf(),
    public override val kind: PluginKind = PluginKind.NORMAL,
) : JvmPluginDescription {

    @JvmOverloads
    public constructor(
        name: String,
        version: String,
        author: String = "",
        info: String = "",
        dependencies: List<PluginDependency> = listOf(),
        kind: PluginKind = PluginKind.NORMAL,
    ) : this(name, Semver(version, Semver.SemverType.LOOSE), author, info, dependencies, kind)

    init {
        require(!name.contains(':')) { "':' is forbidden in plugin name" }
    }
}