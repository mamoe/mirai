/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.internal.plugin

import net.mamoe.mirai.console.plugin.jvm.ExportManager

internal class ExportManagerImpl(
    private val rules: List<(String) -> Boolean?>
) : ExportManager {

    override fun isExported(className: String): Boolean {
        rules.forEach {
            val result = it(className)
            if (result != null) return@isExported result
        }
        return true
    }

    companion object {
        @JvmStatic
        fun parse(lines: Iterator<String>): ExportManagerImpl {
            fun Boolean.without(value: Boolean) = if (this == value) null else this

            val rules = ArrayList<(String) -> Boolean?>()
            lines.asSequence().map { it.trim() }.filter { it.isNotBlank() }.filterNot {
                it[0] == '#'
            }.forEach { line ->
                val command = line.substringBefore(' ')
                val argument = line.substringAfter(' ', missingDelimiterValue = "").trim()
                val argumentPackage = "$argument."

                when (command) {
                    "exports" -> rules.add {
                        (it == argument || it.startsWith(argumentPackage)).without(false)
                    }
                    "protects" -> rules.add {
                        if (it == argument || it.startsWith(argumentPackage))
                            false
                        else null
                    }
                    "export-all", "export-plugin", "export-system" -> rules.add { true }
                    "protect-all", "protect-plugin", "protect-system" -> rules.add { false }
                }
            }
            return ExportManagerImpl(rules)
        }
    }
}