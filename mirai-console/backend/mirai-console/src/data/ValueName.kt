/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.data

/**
 * 序列化之后的名称.
 *
 * 例:
 * ```
 * object AccountPluginData : AutoSavePluginData() {
 *    @ValueName("info")
 *    val map: Map<String, String> by value("a" to "b")
 * }
 * ```
 *
 * 将被保存为配置 (YAML 作为示例):
 * ```yaml
 * AccountPluginData:
 *   info:
 *     a: b
 * ```
 *
 * @see PluginData
 * @see Value
 */
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
public annotation class ValueName(val value: String)
