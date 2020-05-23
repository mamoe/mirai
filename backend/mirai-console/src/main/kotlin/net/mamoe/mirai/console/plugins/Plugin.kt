/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.plugins

import kotlinx.serialization.Serializable
import java.io.File

/** 插件类型 */
enum class PluginKind {
    /** 表示此插件提供一个 [PluginLoader], 应在加载其他 [NORMAL] 类型插件前加载 */
    LOADER,

    /** 表示此插件为一个通常的插件, 按照正常的依赖关系加载. */
    NORMAL
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

    /** 指定此插件需要在这些插件之前加载 */
    val loadBefore: List<String>

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
    val versionKind: VersionKind
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


internal fun PluginDependency.VersionKind.toEnglishString(): String = when (this) {
    PluginDependency.VersionKind.EXACT -> ""
    PluginDependency.VersionKind.AT_LEAST -> "at least "
    PluginDependency.VersionKind.AT_MOST -> "at most "
}

/**
 * 基于文件的插件的描述
 */
interface FilePluginDescription : PluginDescription {
    val file: File
}

/**
 * 表示一个 mirai-console 插件.
 *
 * @see JvmPlugin
 */
interface Plugin