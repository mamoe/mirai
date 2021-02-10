/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package keys

import org.gradle.api.Project
import java.io.BufferedReader

open class SecretKeys(
    val type: String,
    val user: String,
    val password: String
) {
    class Invalid(
        type: String,
        override val isDisabled: Boolean = false
    ) : SecretKeys(type, "", "") {
        override val isValid: Boolean get() = false
        override fun requireNotInvalid(): Nothing {
            error(
                """
                Key $type not found.
                Please lease specify by creating a file $type.key in projectDir/build-secret-keys
                or by providing JVM parameter '$type.user', `$type.password`
            """.trimIndent()
            )
        }
    }

    companion object {
        val keyCaches = mutableMapOf<Project, ProjectKeysCache>()

        @JvmStatic
        fun getCache(project: Project): ProjectKeysCache =
            keyCaches.computeIfAbsent(project, SecretKeys::ProjectKeysCache)
    }

    class ProjectKeysCache(val project: Project) {
        val keys = mutableMapOf<String, SecretKeys>()
        fun loadKey(type: String) = keys.computeIfAbsent(type, this::loadKey0)

        private fun loadKey0(type: String): SecretKeys {

            project.parent?.let { parent ->
                getCache(parent).loadKey(type).takeIf {
                    it.isValid || it.isDisabled
                }?.let { return it }
            }

            val secretKeys = project.projectDir.resolve("build-secret-keys")

            kotlin.run {
                val secretKeyFile = secretKeys.resolve("$type.disable").takeIf { it.isFile }
                    ?: secretKeys.resolve("$type.disable.txt")
                if (secretKeyFile.isFile) return Invalid(type, true) // Disabled
            }

            // Load from secretKeys/$type.key
            kotlin.run {
                val secretKeyFile = secretKeys.resolve("$type.key").takeIf { it.isFile }
                    ?: secretKeys.resolve("$type.key.txt")
                if (secretKeyFile.isFile) {
                    secretKeyFile.bufferedReader().use {
                        fun BufferedReader.readLineNonEmpty(): String {
                            while (true) {
                                val nextLine = readLine() ?: return ""
                                if (nextLine.isNotBlank()) {
                                    return nextLine.trim()
                                }
                            }
                        }
                        return SecretKeys(type, it.readLineNonEmpty(), it.readLineNonEmpty())
                    }
                }
            }
            // Load from project/%type.key, user
            kotlin.run {
                val userFile = project.projectDir.resolve("$type.user.txt")
                val keyFile = project.projectDir.resolve("$type.key.txt")
                if (userFile.isFile && keyFile.isFile) {
                    return SecretKeys(type, userFile.readText().trim(), keyFile.readText().trim())
                }
            }


            // Load from property $type.user, $type.password

            fun findProperty(type: String): String? {
                val p = project.findProperty(type)
                    ?: System.getProperty(type)
                    ?: System.getenv(type)

                return p?.toString()
            }

            val tUser = findProperty("$type.user")
                ?: findProperty("${type}_user")

            val tPassword = findProperty("$type.password")
                ?: findProperty("$type.passwd")
                ?: findProperty("$type.key")
                ?: findProperty("${type}_password")
                ?: findProperty("${type}_passwd")
                ?: findProperty("${type}_key")

            if (tUser != null && tPassword != null) {
                return SecretKeys(type, tUser, tPassword)
            }

            return Invalid(type)
        }
    }

    open val isValid: Boolean get() = true
    open val isDisabled: Boolean get() = false
    open fun requireNotInvalid(): SecretKeys = this
}