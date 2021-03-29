/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.intellij.assets

import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

object Icons {
    val CommandDeclaration: Icon = IconLoader.getIcon("/icons/commandDeclaration.svg", Icons::class.java)
    val PluginMainDeclaration: Icon = IconLoader.getIcon("/icons/pluginMainDeclaration.png", Icons::class.java)

    val MainIcon: Icon = PluginMainDeclaration
}

object FT { // file template
    const val BuildGradleKts = "Plugin build.gradle.kts"
    const val BuildGradle = "Plugin build.gradle"

    const val SettingsGradleKts = "Plugin settings.gradle.kts"
    const val SettingsGradle = "Plugin settings.gradle"

    const val GradleProperties = "Gradle gradle.properties"

    const val PluginMainKt = "Plugin main class Kotlin.kt"
    const val PluginMainJava = "Plugin main class Java.java"

    const val Gitignore = ".gitignore"
}