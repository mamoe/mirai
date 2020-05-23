/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.plugins

import kotlinx.serialization.*
import kotlinx.serialization.builtins.serializer
import java.io.File


/** 插件类型 */
@Serializable(with = PluginKind.Serializer::class)
enum class PluginKind {
    /** 表示此插件提供一个 [PluginLoader], 应在加载其他 [NORMAL] 类型插件前加载 */
    LOADER,

    /** 表示此插件为一个通常的插件, 按照正常的依赖关系加载. */
    NORMAL;

    companion object Serializer : KSerializer<PluginKind> {
        override val descriptor: SerialDescriptor get() = String.serializer().descriptor

        override fun deserialize(decoder: Decoder): PluginKind {
            val name = String.serializer().deserialize(decoder)
            return values().firstOrNull { it.name.equals(name, ignoreCase = true) } ?: NORMAL
        }

        override fun serialize(encoder: Encoder, value: PluginKind) {
            return String.serializer().serialize(encoder, value.toString())
        }
    }
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
    val dependencies: List<PluginDependency>
}

/** 插件的一个依赖的信息 */
@Serializable
data class PluginDependency(
    /** 依赖插件名 */
    val name: String,
    /**
     * 依赖版本号
     * @see versionKind 版本号类型
     */
    val version: String,
    /** 版本号类型 */
    val versionKind: VersionKind,
    /**
     * 若为 `false`, 插件在找不到此依赖时也能正常加载.
     */
    val isOptional: Boolean
) {
    enum class VersionKind {
        /** 要求依赖精确的版本 */
        EXACT,

        /** 要求依赖最低版本 */
        AT_LEAST,

        /** 要求依赖最高版本 */
        AT_MOST
    }

    override fun toString(): String {
        return "$name ${versionKind.toEnglishString()}v$version"
    }
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
