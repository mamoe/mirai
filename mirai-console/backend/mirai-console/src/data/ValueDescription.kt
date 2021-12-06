/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.data

import kotlinx.serialization.SerialInfo

/**
 * 序列化之后的注释.
 *
 * 例:
 * ```
 * object AccountPluginData : PluginData by ... {
 *    @ValueDescription("""
 *        一个 map
 *    """)
 *    val map: Map<String, String> by value("a" to "b")
 * }
 * ```
 *
 * 将被保存为配置 (YAML 作为示例):
 * ```yaml
 * AccountPluginData:
 *   # 一个 map
 *   map:
 *     a: b
 * ```
 *
 * @see net.mamoe.yamlkt.Comment
 */
@SerialInfo
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
public annotation class ValueDescription(
    /**
     * 将会被 [String.trimIndent] 处理
     */
    val value: String,
)