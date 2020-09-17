/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused")

package net.mamoe.mirai.console.compiler.common

import net.mamoe.mirai.console.util.ConsoleExperimentalApi

/**
 * 标记一个参数的语境类型, 用于帮助编译器和 IntelliJ 插件进行语境推断.
 */
@ConsoleExperimentalApi
@Target(AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FIELD,
    AnnotationTarget.EXPRESSION)
@Retention(AnnotationRetention.SOURCE)
public annotation class ResolveContext(
    val kind: Kind,
) {
    /**
     * 元素数量可能在任意时间被改动
     */
    public enum class Kind {
        PLUGIN_ID,
        PLUGIN_NAME,
        PLUGIN_VERSION,
    }
}