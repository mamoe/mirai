/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */


package net.mamoe.mirai.console.intellij.wizard

import net.mamoe.mirai.console.intellij.diagnostics.adjustToClassName
import kotlin.contracts.contract

data class ProjectCoordinates(
    val groupId: String, // already checked by pattern
    val artifactId: String,
    val version: String,
    val moduleName: String
) {
    val packageName: String get() = groupId
}

data class PluginCoordinates(
    val id: String,
    val name: String,
    val author: String,
    val info: String,
    val dependsOn: String,
)

class MiraiProjectModel(
    val projectCoordinates: ProjectCoordinates,
    val pluginCoordinates: PluginCoordinates,
    val miraiVersion: String,
    val kotlinVersion: String,

    val buildSystemType: BuildSystemType,
    val languageType: LanguageType,
    val useProxyRepo: Boolean,

    val mainClassSimpleName: String = pluginCoordinates.run {
        name.adjustToClassName() ?: id.substringAfterLast('.').adjustToClassName()
    } ?: "PluginMain",
    val packageName: String = projectCoordinates.checkNotNull("projectCoordinates").groupId,
)

fun <T : Any> T?.checkNotNull(name: String): T {
    contract {
        returns() implies (this@checkNotNull != null)
    }
    checkNotNull(this) {
        "$name is not yet initialized."
    }
    return this
}