/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.plugin

import com.vdurmont.semver4j.Semver
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encodeToString
import net.mamoe.mirai.console.internal.data.map
import net.mamoe.yamlkt.Yaml
import net.mamoe.yamlkt.YamlDynamicSerializer
import java.io.File


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
     * 插件名称.
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
    public val dependencies: List<@Serializable(with = PluginDependency.SmartSerializer::class) PluginDependency>
}

/**
 * 插件类型
 */
@Serializable(with = PluginKind.AsStringSerializer::class)
public enum class PluginKind {
    /** 表示此插件提供一个 [PluginLoader], 应在加载其他 [NORMAL] 类型插件前加载 */
    LOADER,

    /** 表示此插件为一个通常的插件, 按照正常的依赖关系加载. */
    NORMAL;

    public object AsStringSerializer : KSerializer<PluginKind> by String.serializer().map(
        serializer = { it.name },
        deserializer = { str ->
            values().firstOrNull {
                it.name.equals(str, ignoreCase = true)
            } ?: NORMAL
        }
    )
}

/**
 * 插件的一个依赖的信息.
 *
 * 在 YAML 格式下, 典型的插件依赖示例:
 * ```yaml
 * dependencies:
 *   - name: "依赖的插件名"  # 依赖的插件名
 *     version: "" # 依赖的版本号, 支持 Apache Ivy 格式. 为 null 或不指定时不限制版本
 *     isOptional: true # `true` 表示插件在找不到此依赖时也能正常加载
 *   - "SamplePlugin" # 名称为 SamplePlugin 的插件, 不限制版本, isOptional=false
 *   - "TestPlugin:1.0.0+" # 名称为 ExamplePlugin 的插件, 版本至少为 1.0.0, isOptional=false
 *   - "ExamplePlugin:1.5.0+?" # 名称为 ExamplePlugin 的插件, 版本至少为 1.5.0, 末尾 `?` 表示 isOptional=true
 *   - "Another test plugin:[1.0.0, 2.0.0)" # 名称为 Another test plugin 的插件, 版本要求大于等于 1.0.0, 小于 2.0.0, isOptional=false
 * ```
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
    public val version: @Serializable(SemverAsStringSerializerIvy::class) Semver? = null,
    /**
     * 若为 `false`, 插件在找不到此依赖时也能正常加载.
     */
    public val isOptional: Boolean = false
) {
    public override fun toString(): String {
        return "$name v$version"
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

/**
 * 基于文件的插件 的描述
 */
public interface FilePluginDescription : PluginDescription {
    public val file: File
}
