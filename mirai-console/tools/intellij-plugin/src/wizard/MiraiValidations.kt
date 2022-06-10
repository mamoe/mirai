/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.intellij.wizard

import com.intellij.ide.starters.shared.TextValidationFunction
import net.mamoe.mirai.console.compiler.common.CheckerConstants
import net.mamoe.mirai.console.intellij.util.RequirementHelper
import net.mamoe.mirai.console.intellij.util.RequirementParser
import net.mamoe.mirai.console.intellij.wizard.MiraiProjectWizardBundle.message

object MiraiValidations {
    val CHECK_FORBIDDEN_PLUGIN_NAME = TextValidationFunction { text ->
        val lowercaseName = text.lowercase().trim()
        val illegal =
            CheckerConstants.PLUGIN_FORBIDDEN_NAMES.firstOrNull { it == lowercaseName }
        if (illegal != null) {
            message("validation.plugin.name.forbidden.character", illegal)
        } else null
    }

    val CHECK_PLUGIN_ID = TextValidationFunction { text ->
        if (!CheckerConstants.PLUGIN_ID_REGEX.matches(text.trim())) {
            message("validation.illegal.plugin.id", text)
        } else null
    }

    val CHECK_ILLEGAL_VERSION_LINE = TextValidationFunction { text ->
        checkVersionLine(text)?.let {
            message("validation.illegal.version", text, it)
        }
    }

    val CHECK_PLUGIN_DEPENDENCIES_LINE = TextValidationFunction { text ->
        try {
            val trim = text.trim()
            val dep = PluginDependency.parseFromString(trim)
            if (!CheckerConstants.PLUGIN_ID_REGEX.matches(dep.id)) {
                return@TextValidationFunction message("validation.illegal.plugin.id", dep.id)
            }

            dep.versionRequirement?.let { checkVersionRequirementLine(it) }?.let {
                return@TextValidationFunction message("validation.illegal.version", it)
            }

            null // no error
        } catch (e: IllegalArgumentException) {
            message("validation.illegal.version", text, e.message ?: message("no.error.message"))
        }
    }

    val CHECK_PLUGIN_DEPENDENCIES_SEGMENT = TextValidationFunction { text ->
        text.lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { CHECK_PLUGIN_DEPENDENCIES_LINE.checkText(it) }
            .firstOrNull()
    }

    /**
     * Check multiple lines and returns information about the first illegal one.
     */
    val CHECK_ILLEGAL_VERSION_SEGMENT = TextValidationFunction { text ->
        text.lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .mapNotNull { CHECK_ILLEGAL_VERSION_LINE.checkText(text) }
            .firstOrNull()
    }

    private class PluginDependency @JvmOverloads constructor(
        val id: String,
        val versionRequirement: String? = null,
    ) {
        companion object {

            /**
             * Frozen version of [net.mamoe.mirai.console.plugin.description.PluginDescription.Companion.parseFromString] from `2.11.0-M2.2`.
             */
            @JvmStatic
            fun parseFromString(string: String): PluginDependency {
//                val optional = string.endsWith('?')
                val (id, version) = string.removeSuffix("?").let { rule ->
                    if (rule.contains(':')) {
                        rule.substringBeforeLast(':') to rule.substringAfterLast(':')
                    } else {
                        rule to null
                    }
                }
                return PluginDependency(id, version)
            }
        }
    }

    private fun checkVersionLine(version: String): String? {
        return checkVersionRequirementLine(version) // for simplicity
    }

    private fun checkVersionRequirementLine(versionRequirement: String): String? {
        kotlin.runCatching {
            RequirementHelper.RequirementChecker.processLine(
                RequirementParser.TokenReader(
                    versionRequirement
                )
            )
        }.onFailure { return it.message ?: message("no.error.message") }
        return null
    }
}