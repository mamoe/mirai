/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.plugin.dsecription

import com.vdurmont.semver4j.Semver
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import net.mamoe.mirai.console.internal.data.map
import net.mamoe.yamlkt.Yaml
import net.mamoe.yamlkt.YamlDynamicSerializer

/**
 * 插件的一个依赖的信息.
 *
 * @see PluginDescription.dependencies
 */
@Serializable(with = PluginDependency.SmartSerializer::class)
public data class PluginDependency(
    /** 依赖插件名 */
    public val name: String,
    /**
     * 依赖版本号. 为 null 时则为不限制版本.
     *
     * 版本遵循 [语义化版本 2.0 规范](https://semver.org/lang/zh-CN/),
     *
     * 允许 [Apache Ivy 风格版本号表示](http://ant.apache.org/ivy/history/latest-milestone/settings/version-matchers.html)
     */
    public val version: @Serializable(net.mamoe.mirai.console.internal.data.SemverAsStringSerializerIvy::class) Semver? = null,
    /**
     * 若为 `false`, 插件在找不到此依赖时也能正常加载.
     */
    public val isOptional: Boolean = false
) {
    public override fun toString(): String {
        return "$name v$version${if (isOptional) "?" else ""}"
    }


    /**
     * 可支持解析 [String] 作为 [PluginDependency.version] 或单个 [PluginDependency]
     */
    public object SmartSerializer : KSerializer<PluginDependency> by YamlDynamicSerializer.map(
        serializer = { it },
        deserializer = { any ->
            when (any) {
                is Map<*, *> -> Yaml.nonStrict.decodeFromString(
                    serializer(),
                    Yaml.nonStrict.encodeToString<Map<*, *>>(any)
                )
                else -> {
                    var value = any.toString()
                    val isOptional = value.endsWith('?')
                    if (isOptional) {
                        value = value.removeSuffix("?")
                    }

                    val components = value.split(':')
                    when (components.size) {
                        1 -> PluginDependency(value, isOptional = isOptional)
                        2 -> PluginDependency(
                            components[0],
                            Semver(components[1], Semver.SemverType.IVY),
                            isOptional = isOptional
                        )
                        else -> error("Illegal plugin dependency statement: $value")
                    }
                }
            }
        }
    )
}