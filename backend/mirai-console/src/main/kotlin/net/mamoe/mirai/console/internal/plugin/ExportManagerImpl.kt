/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 *
 * LuckPerms-Mirai/mirai-console.mirai-console.main/ExportManagerImpl.kt
 *
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/Karlatemp/LuckPerms-Mirai/blob/master/LICENSE
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
            val rules = ArrayList<(String) -> Boolean?>()
            lines.asSequence().map { it.trim() }.filter { it.isNotBlank() }.filterNot {
                it[0] == '#'
            }.forEach { line ->
                val command = line.substringBefore(' ')
                val argument = line.substringAfter(' ', missingDelimiterValue = "").trim()

                when (command) {
                    "export" -> {
                        when {
                            // export-all
                            argument.isBlank() -> rules.add { true }
                            // export package
                            argument.endsWith(".") -> rules.add {
                                if (it.startsWith(argument)) true else null
                            }
                            // export class
                            else -> rules.add {
                                if (it == argument) true else null
                            }
                        }
                    }
                    "deny", "internal", "hidden" -> {
                        when {
                            // deny-all
                            argument.isBlank() -> rules.add { false }
                            // deny package
                            argument.endsWith(".") -> rules.add {
                                if (it.startsWith(argument)) false else null
                            }
                            // deny class
                            else -> rules.add {
                                if (it == argument) false else null
                            }
                        }
                    }
                    "export-all" -> rules.add { true }
                    "deny-all", "hidden-all" -> rules.add { false }
                }
            }
            return ExportManagerImpl(rules)
        }
    }
}