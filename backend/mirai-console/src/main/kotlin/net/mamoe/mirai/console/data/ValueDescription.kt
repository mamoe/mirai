/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.data

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
 */
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
public annotation class ValueDescription(val value: String)