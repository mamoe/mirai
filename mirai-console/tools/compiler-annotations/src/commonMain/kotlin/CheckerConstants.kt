/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.compiler.common

import org.intellij.lang.annotations.Language

/**
 * @suppress 这是内部 API. 可能在任意时刻变动
 */
public object CheckerConstants {
    @Language("RegExp")
    public const val PLUGIN_ID_PATTERN: String = """([a-zA-Z]\w*(?:\.[a-zA-Z]\w*)*)\.([a-zA-Z]\w*(?:-\w+)*)"""

    @JvmField
    public val PLUGIN_ID_REGEX: Regex = Regex(PLUGIN_ID_PATTERN)


    @JvmField
    public val PLUGIN_FORBIDDEN_NAMES: Array<String> = arrayOf("main", "console", "plugin", "config", "data")
}
