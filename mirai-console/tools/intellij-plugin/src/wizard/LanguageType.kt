/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.intellij.wizard

import com.intellij.ide.fileTemplates.FileTemplate
import net.mamoe.mirai.console.intellij.assets.FT

data class NamedFile(
    val path: String,
    val template: FileTemplate
)

interface ILanguageType {
    val sourceSetDirName: String
    fun pluginMainClassFile(creator: ProjectCreator): NamedFile
}

sealed class LanguageType : ILanguageType {
    @Suppress("UNCHECKED_CAST")
    fun <T : String?> escapeString(string: T): T {
        string ?: return null as T
        return string
            .replace("\\", "\\\\")
            .replace("\n", "\\n")
            .replace("\"", "\\\"") as T
    }

    abstract fun <T : String?> escapeRawString(string: T): T

    companion object {
        fun values() = arrayOf(Kotlin, Java)
    }

    object Kotlin : LanguageType() {
        override fun toString(): String = "Kotlin" // display in UI
        override val sourceSetDirName: String get() = "kotlin"
        override fun pluginMainClassFile(creator: ProjectCreator): NamedFile = creator.model.run {
            return NamedFile(
                path = "src/main/kotlin/$mainClassSimpleName.kt",
                template = creator.getTemplate(FT.PluginMainKt)
            )
        }

        @Suppress("UNCHECKED_CAST")
        override fun <T : String?> escapeRawString(string: T): T {
            string ?: return null as T
            return string.replace("$", "\${'\$'}").replace("\n", "\\n") as T
        }
    }

    object Java : LanguageType() {
        override fun toString(): String = "Java" // display in UI
        override val sourceSetDirName: String get() = "java"
        override fun pluginMainClassFile(creator: ProjectCreator): NamedFile = creator.model.run {
            return NamedFile(
                path = "src/main/java/${packageName.replace('.', '/')}/$mainClassSimpleName.java",
                template = creator.getTemplate(FT.PluginMainJava)
            )
        }

        override fun <T : String?> escapeRawString(string: T): T = escapeString(string)
    }
}