/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.plugin

import kotlinx.serialization.*
import kotlinx.serialization.builtins.serializer
import net.mamoe.mirai.console.setting.internal.map
import net.mamoe.yamlkt.Yaml
import net.mamoe.yamlkt.YamlDynamicSerializer
import java.io.File


/** 插件类型 */
@Serializable(with = PluginKind.AsStringSerializer::class)
enum class PluginKind {
    /** 表示此插件提供一个 [PluginLoader], 应在加载其他 [NORMAL] 类型插件前加载 */
    LOADER,

    /** 表示此插件为一个通常的插件, 按照正常的依赖关系加载. */
    NORMAL;

    object AsStringSerializer : KSerializer<PluginKind> by String.serializer().map(
        serializer = { it.name },
        deserializer = { str ->
            values().firstOrNull {
                it.name.equals(str, ignoreCase = true)
            } ?: NORMAL
        }
    )
}

/**
 * 插件描述
 */
interface PluginDescription {
    val kind: PluginKind

    val name: String
    val author: String
    val version: String
    val info: String

    /** 此插件依赖的其他插件, 将会在这些插件加载之后加载此插件 */
    val dependencies: List<@Serializable(with = PluginDependency.SmartSerializer::class) PluginDependency>
}

/** 插件的一个依赖的信息 */
@Serializable
data class PluginDependency(
    /** 依赖插件名 */
    val name: String,
    /**
     * 依赖版本号. 为 null 时则为不限制版本.
     * @see versionKind 版本号类型
     */
    val version: String? = null,
    /** 版本号类型 */
    val versionKind: VersionKind = VersionKind.AT_LEAST,
    /**
     * 若为 `false`, 插件在找不到此依赖时也能正常加载.
     */
    val isOptional: Boolean = false
) {
    /** 版本号类型 */
    @Serializable(with = VersionKind.AsStringSerializer::class)
    enum class VersionKind(
        private vararg val serialNames: String
    ) {
        /** 要求依赖精确的版本 */
        EXACT("exact"),

        /** 要求依赖最低版本 */
        AT_LEAST("at_least", "AtLeast", "least", "lowest", "+"),

        /** 要求依赖最高版本 */
        AT_MOST("at_most", "AtMost", "most", "highest", "-");

        object AsStringSerializer : KSerializer<VersionKind> by String.serializer().map(
            serializer = { it.serialNames.first() },
            deserializer = { str ->
                values().firstOrNull {
                    it.serialNames.any { name -> name.equals(str, ignoreCase = true) }
                } ?: AT_LEAST
            }
        )
    }

    override fun toString(): String {
        return "$name ${versionKind.toEnglishString()}v$version"
    }


    /**
     * 可支持解析 [String] 作为 [PluginDependency.version] 或单个 [PluginDependency]
     */
    object SmartSerializer : KSerializer<PluginDependency> by YamlDynamicSerializer.map(
        serializer = { it },
        deserializer = { any ->
            when (any) {
                is Map<*, *> -> Yaml.nonStrict.parse(serializer(), Yaml.nonStrict.stringify(any))
                else -> PluginDependency(any.toString())
            }
        }
    )
}

/**
 * 基于文件的插件的描述
 */
interface FilePluginDescription : PluginDescription {
    val file: File
}

internal fun PluginDependency.VersionKind.toEnglishString(): String = when (this) {
    PluginDependency.VersionKind.EXACT -> ""
    PluginDependency.VersionKind.AT_LEAST -> "at least "
    PluginDependency.VersionKind.AT_MOST -> "at most "
}
