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
     * 插件名称. 不允许存在 ":"
     */
    public val name: String

    /**
     * 插件作者, 允许为空
     */
    public val author: String

    /**
     * 插件版本.
     *
     * 语法参考: ([语义化版本 2.0.0](https://semver.org/lang/zh-CN/))
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
    public val dependencies: List<PluginDependency>
}

